package com.terrapages.opengis.api;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;

import junit.framework.TestCase;
import net.opengis.xls.AddressType;
import net.opengis.xls.GeocodeRequestType;
import net.opengis.xls.RequestHeaderType;
import net.opengis.xls.RequestType;
import net.opengis.xls.XLSType;

import com.terrapages.opengis.api.util.MarshallerMediator;
import com.terrapages.opengis.api.util.Standard;
import com.terrapages.opengis.api.util.UnmarshallerMediator;

public class MarshallTest extends TestCase {

	/**
	 * Generates a valid Geocoder request and then attempts to marshal and unmarshal the request.
	 */
	@org.junit.Test
	public void testGeocoder() {
		XLSType xls = generateXMLRequest();
		
		boolean success = false;
		try {
			// Marshalls the JAXBElements to XML files
			StringWriter stringWriter = new StringWriter();
			MarshallerMediator.marshall(Standard.XLS, UnmarshallerMediator.XLS_XSD_PATH, MarshallerMediator.OBJECT_FACTORY.createXLS(xls), stringWriter);
			
			// Unmarshalls the XML file to JAXBElements		
			JAXBElement element = (JAXBElement) UnmarshallerMediator.unmarshall(Standard.XLS, UnmarshallerMediator.XLS_XSD_PATH, new StringReader(stringWriter.toString()));
			
			// If the JAXBElement is not null and is instance of the XLSType then the test was successful
			if (element != null && element.getValue() instanceof XLSType)
				success = true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.flush();
		}
		
		assertTrue("Testing Generating Geocode XML:",  success);
	}
	

	/**
	 * Generates a Geocode request.
	 * @return the xlstype that contains the Geocode request.
	 */
	private XLSType generateXMLRequest() {
		// Setup the XLS root element...	
		XLSType xlsRequest = new XLSType();
		xlsRequest.setVersion(new BigDecimal("1.1"));
		
		//Setup a <Header> and add to the <XLS>
		RequestHeaderType requestHeaderType = new RequestHeaderType();
		requestHeaderType.setClientName("OpenGIS-XLS-Demo");
		requestHeaderType.setClientPassword("OpenGIS-XLS-Demo");
		//requestHeaderType.setClientPassword("TODO - REQUIRED");
		xlsRequest.setHeader(MarshallerMediator.OBJECT_FACTORY.createRequestHeader(requestHeaderType));
		
		//Setup the <Request> element
		RequestType request = new RequestType();
		request.setMethodName("GeocodeRequest");
		request.setVersion("1.1");
		request.setRequestID("");
		request.setMaximumResponses(BigInteger.ONE);		
		xlsRequest.getBody().add(MarshallerMediator.OBJECT_FACTORY.createRequest(request));
		
		//Create the <GeocodeRequest> with either a freeForm Address or Tokenized Address
		GeocodeRequestType geoReq = new GeocodeRequestType();
		geoReq.getAddress().add(this.getAddressElement("38 GREENHILL RD WAYVILLE SA","AU"));		
		//geoReq.getAddress().add(this.getAddressElement("", "38", "", "GREENHILL", "RD", "WAYVILLE", "SA", "5034", "AU"));
		request.setRequestParameters(MarshallerMediator.OBJECT_FACTORY.createGeocodeRequest(geoReq));
		
		return xlsRequest;
	}

	/**
	 * Compose an <Address> with freeForm Address content.
	 * @param freeFormAddress example "38 Greenhill Rd Wayville SA"
	 * @param countryCode example "AU"
	 * @return the <Address> element.
	 */
	private AddressType getAddressElement(String freeFormAddress, String countryCode){
		AddressType address= new AddressType();
		address.setCountryCode(countryCode);
		address.setFreeFormAddress(freeFormAddress);
		return address;		
	}
}
