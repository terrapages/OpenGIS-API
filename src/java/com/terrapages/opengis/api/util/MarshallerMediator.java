package com.terrapages.opengis.api.util;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.opengis.xls.ObjectFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This utily is to provide the most efficient use of the JAXB Marshaller.
 * Reuse of JAXB utilities and XSD schema's is provided within this wrapper
 * class. It will dramatically increase code resuse as much or the marshall 
 * operation is repeated by calling classes.  
 * @author ahughes
 */
public class MarshallerMediator {

	private static final Logger log = Logger.getLogger(MarshallerMediator.class);
	
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
	 * Default constructor for XmlMarshaller
	 */
	public MarshallerMediator() {

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
	 * Creates the JAXBMarshaller, with the provided xsdPath
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be null or empty to avoid loading
	 * @return the marshaller with validation loaded or ignored
	 * @throws JAXBException if there is a problem during the createMarshaller()
	 */
	private static Marshaller getMarshaller(Standard ogcSpec, String xsdPath) throws JAXBException{
		//Get the appropriate 
		Marshaller marshaller = null;
		if (ogcSpec == Standard.XLS){
			marshaller = xlsJAXBContext.createMarshaller();
		}
		//else if (ogcSpec == OpenGISSpecification.SLD){
		//marshaller = sldJAXBContext.createMarshaller();
		//}
		else {
			throw new JAXBException("Unhandled OpenGIS Specification");
		}
		//Add the XSD for validation if provided
		if (!isEmptyString(xsdPath)){
			marshaller.setSchema(getSchema(xsdPath));
		}
		//Set the XML output format.
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// This is to allow us to set the namespace to whatever prefixes we want
		marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperUtil());
		return marshaller;
	}	
		
	/**
	 * Marshal the JAXBElement and returns the output.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 * @return the output of the marshalling of the JAXBElement
	 */
	public static String marshall(Standard ogcSpec, String xsdPath, Object jaxbElement) {
		try{
			StringWriter stringWriter = new StringWriter();
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, stringWriter);
			return stringWriter.toString();
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	
	
	/**
	 * Marshal the JAXBElement to the provided output destination.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param output the destination of the marshal opearation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 */
	public static void marshall(Standard ogcSpec, String xsdPath, Object jaxbElement, Writer output ) {
		try{
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, output);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}

	/**
	 * Marshal the JAXBElement to the provided output destination.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param output the destination of the marshal opearation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 */
	public static void marshall(Standard ogcSpec, String xsdPath, Object jaxbElement, ContentHandler output) {
		try{
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, output);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	
	
	
	/**
	 * Marshal the JAXBElement to the provided output destination.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param output the destination of the marshal opearation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 */
	public static void marshall(Standard ogcSpec, String xsdPath, Object jaxbElement, Node output) {
		try{
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, output);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	
	


	
	/**
	 * Marshal the JAXBElement to the provided output destination.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param output the destination of the marshal opearation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 */
	public static void marshall(Standard ogcSpec, String xsdPath, Object jaxbElement, OutputStream output) {
		try{
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, output);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	
	
	
	/**
	 * Marshal the JAXBElement to the provided output destination.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param output the destination of the marshal opearation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 */
	public static void marshall(Standard ogcSpec, String xsdPath, Object jaxbElement, Result output) {
		try{
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, output);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	
	
	
	/**
	 * Marshal the JAXBElement to the provided output destination.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param output the destination of the marshal opearation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 */
	public static void marshall(Standard ogcSpec, String xsdPath, Object jaxbElement, XMLEventWriter output) {
		try{
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, output);
		} catch(PropertyException pe) {
			// if the JAXB provider doesn't recognize the prefix mapper, it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem, you can just continue marshalling without failing
			throw new RuntimeException("Couldnt set a property in the marshaller (in all likelihood the NamespacePrefixMapper", pe);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("Received a JAXBException while trying to marshal the JAXB classes to XML.", jaxbe);
		} 
	}	
	
	
	/**
	 * Marshal the JAXBElement to the provided output destination.
	 * @param ogcSpec the specification that contains the Type you are trying to marshal
	 * @param xsdPath this can be empty or null and hence ignore validation
	 * @param output the destination of the marshal opearation
	 * @param jaxbElement this is unchecked, by the marshaller, but will FAIL if it is not JAXBElement&lt;T&gt; These are created with the <code>ObjectFactory.createXXX(XXXType)</code> operation.
	 */
	public static void marshall(Standard ogcSpec, String xsdPath, Object jaxbElement, XMLStreamWriter output) {
		try{
			getMarshaller(ogcSpec,xsdPath).marshal(jaxbElement, output);
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
