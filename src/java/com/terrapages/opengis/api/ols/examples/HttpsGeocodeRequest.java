package com.terrapages.opengis.api.ols.examples;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import net.opengis.xls.AddressType;
import net.opengis.xls.BuildingLocatorType;
import net.opengis.xls.GeocodeRequestType;
import net.opengis.xls.NamedPlaceClassification;
import net.opengis.xls.NamedPlaceType;
import net.opengis.xls.RequestHeaderType;
import net.opengis.xls.RequestType;
import net.opengis.xls.StreetAddressType;
import net.opengis.xls.StreetNameType;
import net.opengis.xls.XLSType;

import com.terrapages.opengis.api.util.HttpsClient;
import com.terrapages.opengis.api.util.MarshallerMediator;
import com.terrapages.opengis.api.util.Standard;
import com.terrapages.opengis.api.util.UnmarshallerMediator;

/**
 * Example GeocodeRouteRequest sending to the TerraPages OpenGIS-XLS
 * over (un-signed) https. 
 * 
 * <strong>THIS EXAMPLE DOES NOT PERFORM XML VALIDATION!</strong>
 * 
 * @see com.terrapages.opengis.api.util.HttpsClient
 */
public class HttpsGeocodeRequest {

	HttpsClient httpsClient = new HttpsClient();	
	
	/**
	 * Run the example
	 * @param args not used
	 */
	public static void main(String[] args) {
		HttpsGeocodeRequest geo = new HttpsGeocodeRequest();
		geo.runExample();
	}

	/**
	 * Compose an XML Request (in java OpenGIS-API objects).
	 * Then send the request via https to the TerraPages
	 * OpenGIS-XLS service.
	 * 
	 * Output is to std. out.
	 */
	public void runExample() {

		try {
			//Setup the XLS root element...	
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
			
			System.out.println("We have composed the request document:");
			StringWriter stringWriter = new StringWriter();
			// Marshalls the JAXBElements to XML files	
			MarshallerMediator.marshall(Standard.XLS, MarshallerMediator.XLS_XSD_PATH, MarshallerMediator.OBJECT_FACTORY.createXLS(xlsRequest), stringWriter);
			String xmlRequest = stringWriter.toString();
			
			//Now call the Geocoder with the request		
			System.out.println("\n\r\n\rWe got the response document:");			

			// Unmarshalls the XML file to JAXBElements	
			UnmarshallerMediator.unmarshall(Standard.XLS, UnmarshallerMediator.XLS_XSD_PATH, httpsClient.sendRequest(xmlRequest));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	/**
	 * Creates the <Address> that contains a tokenized format, rather than a freeFormAddress.
	 * @param buildingName the building name
	 * @param number the building/street/lot number
	 * @param subdivision the subdivision or flat number
	 * @param streetOfficialName the streets name
	 * @param streetTypeSuffix street suffix eg 'ROAD' or 'RD' or 'ST'
	 * @param municipality (aka suburb)
	 * @param countrySubdivision (aka state)
	 * @param postalCode self explanitory
	 * @param countryCode (eg 'AU' or 'US')
	 * @return the <Address> element
	 */
	private AddressType getAddressElement(String buildingName, String number, String subdivision, String streetOfficialName, 
			String streetTypeSuffix, String municipality, String countrySubdivision, String postalCode, String countryCode){
		AddressType address= new AddressType();
		address.setCountryCode(countryCode);
		address.setPostalCode(postalCode);
		//Create the <Street> for the <Address>
		StreetAddressType streetAddress = new StreetAddressType();
		// Create the <Building> for the <Street>
		BuildingLocatorType building = new BuildingLocatorType();
		building.setBuildingName(buildingName);
		building.setNumber(number);
		building.setSubdivision(subdivision);
		// Add  the <Building> to the <StreetAddress>
		streetAddress.setStreetLocation(MarshallerMediator.OBJECT_FACTORY.createBuilding(building));
		address.setStreetAddress(streetAddress);
		//Create the <Street> for the <StreetAddress>
		StreetNameType street = new StreetNameType();
		street.setOfficialName(streetOfficialName);
		street.setTypeSuffix(streetTypeSuffix);
		street.setValue(streetOfficialName);
		//Add the <Street> to <StreetAddress>
		streetAddress.getStreet().add(street);
		//Create the <Place> Municipality / Suburb for the <Address>
		NamedPlaceType municipalityType = new NamedPlaceType();
		municipalityType.setType(NamedPlaceClassification.MUNICIPALITY);
		municipalityType.setValue(municipality);
		//Create the <Place> State for the <Address>
		NamedPlaceType countrySubdivisionType = new NamedPlaceType();
		countrySubdivisionType.setType(NamedPlaceClassification.COUNTRY_SUBDIVISION);
		countrySubdivisionType.setValue(countrySubdivision);
		//Add the suburb and state to the <Address>
		address.getPlace().add(municipalityType);
		address.getPlace().add(countrySubdivisionType);		
		return address;
	}	
	
}
