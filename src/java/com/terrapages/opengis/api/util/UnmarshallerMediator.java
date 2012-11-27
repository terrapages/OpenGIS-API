package com.terrapages.opengis.api.util;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.opengis.xls.ObjectFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This utily is to provide the most efficient use of the JAXB Unmarshaller.
 * Reuse of JAXB utilities and XSD schema's is provided within this wrapper
 * class. It will dramatically increase code resuse as much or the unmarshall 
 * operation is repeated by calling classes.  
 * @author chartman - Chris Hartman - 30/03/2007 11:21:13 AM
 * @version $Id: UnmarshallerMediator.java,v 1.5 2007-06-25 06:29:14 chartman Exp $
 */
public class UnmarshallerMediator {

	private static final Logger log = Logger.getLogger(UnmarshallerMediator.class);

	public static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static SchemaFactory schemaFactory;    
    private static JAXBContext xlsJAXBContext;    
    private static final HashMap<String, Schema> schemas = new HashMap<String, Schema>();
	public static final String XLS_XSD_PATH = "http://terrapages.net/opengis-api/xsd/ols/1.1.0/Services.xsd"; 
        
    static {
		log.debug("Setting up JAXB to parse the request.");
		try {
			schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			//Create the JAXB Contexts for the various OGC Spec's
			xlsJAXBContext = JAXBContext.newInstance("net.opengis.xls");
			//sldJAXBContext = JAXBContext.newInstance("net.opengis.sld");
			//wcsJAXBContext = JAXBContext.newInstance("net.opengis.wcs");
			//wmsJAXBContext = JAXBContext.newInstance("net.opengis.wms");
			//wfsJAXBContext = JAXBContext.newInstance("net.opengis.wfs");
			//cswJAXBContext = JAXBContext.newInstance("net.opengis.csw");
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("JAXBException caught during marshal setup.", jaxbe);
		} catch (Exception e) {
			throw new RuntimeException("Exception caught during marshal setup", e);
		}
    }   

	/**
	 * Default constructor for UnmarshallerMediator
	 */
	public UnmarshallerMediator() {
	}


	/**
	 * Creates a new schema linked to the specified xsd, and puts it into the map of schemas.
	 * This method is synchronized because the SchemaFactory is not thread safe. 
	 * If 2 threads both try to load the same schema the first thread enters and locks the method and 
	 * checks if the schema has been loaded. As the schema has not been loaded, 
	 * the first thread loads the schema, places it into the schemas map and exits and unlocks the method. 
	 * The second thread then enters and locks the method and checks if the schema has been loaded, 
	 * as the schema has already been loaded the thread just exits and unlocks the method.
	 * @param xsdPath The location of the xsd file associated to the schema.
	 */
	private synchronized static void loadSchema(String xsdPath) {
		if (isEmptyString(xsdPath)){
			throw new RuntimeException("Cant load an XSD with xsdPath empty or null");
		}
		if(!schemas.containsKey(xsdPath)) {
			try{
				URL schemaURL = new URL(xsdPath);				
				Schema schema = schemaFactory.newSchema(schemaURL);
				schemas.put(xsdPath, schema);	
			} catch (MalformedURLException mue) {
				throw new RuntimeException("Received a JAXBException while trying to load a schema:"+xsdPath, mue);
			} catch (SAXException se) {
				throw new RuntimeException("Received a JAXBException while trying to load a schema:"+xsdPath, se);
			}
		}
	}
	
	/**
	 * This is used as a place holder to load schemas into a cache
	 * If you want to refresh this it will require a restart
	 * @param xsdPath the source of the XSD
	 * @return the Schema
	 */
	private static Schema getSchema(String xsdPath) {
		if(!schemas.containsKey(xsdPath))
			loadSchema(xsdPath);
		return schemas.get(xsdPath);		
	}
	
	/**
	 * Creates the JAXBUnmarshaller, with the provided xsdPath
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be null or empty to avoid loading
	 * @return the unmarshaller with validation loaded or ignored
	 * @throws JAXBException if there is a problem during the createMarshaller()
	 */
	private static Unmarshaller getUnmarshaller(Standard ogcSpec, String xsdPath) throws JAXBException{
		//Get the appropriate 
		Unmarshaller unmarshaller = null;
		if (ogcSpec == Standard.XLS){
			unmarshaller = xlsJAXBContext.createUnmarshaller();
		}
		//else if (ogcSpec == OpenGISSpecification.SLD){
		//unmarshaller = sldJAXBContext.createMarshaller();
		//}
		else {
			throw new JAXBException("Unhandled OpenGIS Specification");
		}
		//Add the XSD for validation if provided
		if (!isEmptyString(xsdPath)){
			unmarshaller.setSchema(getSchema(xsdPath));
		}
		return unmarshaller;
	}	
	
	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, String input) {
		try{

			StringReader stringReader = new StringReader(input);
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(stringReader);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	
	
	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, File input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, InputStream input) { 
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, Reader input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, URL input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, InputSource input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, Node input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input, mapped by the JAXB <code>declaredType</code>, into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @param declaredType appropriate JAXB mapped class to hold XML data.
	 * @return JAXB Element representation of the XML 
	 */
	public static <T> JAXBElement<T> unmarshall(Standard ogcSpec, String xsdPath, Node input, Class<T> declaredType) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input, declaredType);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, Source input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input, mapped by the JAXB <code>declaredType</code>, into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @param declaredType appropriate JAXB mapped class to hold XML data.
	 * @return JAXB Element representation of the XML 
	 */
	public static <T> JAXBElement<T> unmarshall(Standard ogcSpec, String xsdPath, Source input, Class<T> declaredType) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input, declaredType);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, XMLStreamReader input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input, mapped by the JAXB <code>declaredType</code>, into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @param declaredType appropriate JAXB mapped class to hold XML data.
	 * @return JAXB Element representation of the XML 
	 */
	public static <T> JAXBElement<T> unmarshall(Standard ogcSpec, String xsdPath, XMLStreamReader input, Class<T> declaredType) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input, declaredType);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @return the newly created root object of the java content tree.
	 */
	public static Object unmarshall(Standard ogcSpec, String xsdPath, XMLEventReader input) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Unmarshal XML data provided by the input, mapped by the JAXB <code>declaredType</code>, into a XML content tree.
	 * @param ogcSpec the specification that contains the Type you are trying to unmarshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param input the destination of the marshal opearation
	 * @param declaredType appropriate JAXB mapped class to hold XML data.
	 * @return JAXB Element representation of the XML 
	 */
	public static <T> JAXBElement<T> unmarshall(Standard ogcSpec, String xsdPath, XMLEventReader input, Class<T> declaredType) {
		try{
			return getUnmarshaller(ogcSpec, xsdPath).unmarshal(input, declaredType);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}
	
	/**
	 * Used to test of the provided string contains whitepace or is null
	 * @param input the value to test
	 * @return the test result
	 */	
	private static boolean isEmptyString(String input){
		return input == null ? true : "".equalsIgnoreCase(input.trim());
	}
	
}
