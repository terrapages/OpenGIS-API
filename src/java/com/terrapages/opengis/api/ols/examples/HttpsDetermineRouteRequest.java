package com.terrapages.opengis.api.ols.examples;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.opengis.xls.DetermineRouteRequestType;
import net.opengis.xls.PositionType;
import net.opengis.xls.RequestHeaderType;
import net.opengis.xls.RequestType;
import net.opengis.xls.RouteInstructionsRequestType;
import net.opengis.xls.RoutePlanType;
import net.opengis.xls.RoutePreferenceType;
import net.opengis.xls.WayPointListType;
import net.opengis.xls.WayPointType;
import net.opengis.xls.XLSType;

import com.terrapages.opengis.api.util.HttpsClient;
import com.terrapages.opengis.api.util.MarshallerMediator;
import com.terrapages.opengis.api.util.Standard;
import com.terrapages.opengis.api.util.UnmarshallerMediator;

/**
 * Example GeocodeRouteRequest sending to the TerraPages OpenGIS-XLS over (un-signed) https.
 * <strong>THIS EXAMPLE DOES NOT PERFORM XML VALIDATION!</strong>
 * @see com.terrapages.opengis.api.util.HttpsClient
 */
public class HttpsDetermineRouteRequest {

	HttpsClient httpsClient = new HttpsClient();

	/**
	 * Run the example
	 * @param args not used
	 */
	public static void main(String[] args) {
		HttpsDetermineRouteRequest geo = new HttpsDetermineRouteRequest();
		geo.runExample();
	}

	/**
	 * Compose an XML Request (in java OpenGIS-API objects). Then send the request via https to the
	 * TerraPages OpenGIS-XLS service. Output is to std. out.
	 */
	public void runExample() {

		try {
			XLSType xlsRequest = new XLSType();
			xlsRequest.setVersion(new BigDecimal("1.1"));
			// Set the <Header>
			xlsRequest.setHeader(MarshallerMediator.OBJECT_FACTORY.createRequestHeader(createHeaderElement()));
			// Set the <GeocodeRequest>
			xlsRequest.getBody().add(MarshallerMediator.OBJECT_FACTORY.createRequest(createDetermineRouteRequestElement()));

			StringWriter stringWriter = new StringWriter();
			// Marshalls the JAXBElements to XML files	
			MarshallerMediator.marshall(Standard.XLS, UnmarshallerMediator.XLS_XSD_PATH, MarshallerMediator.OBJECT_FACTORY.createXLS(xlsRequest), stringWriter);
			String xmlRequest = stringWriter.toString();
			
			System.out.println("We have composed the request document:");
			System.out.println(xmlRequest);

			// Now call the Geocoder with the request, and display result
			System.out.println("\n\r\n\rWe got the response document:");

			// Unmarshalls the XML file to JAXBElements	
			UnmarshallerMediator.unmarshall(Standard.XLS, UnmarshallerMediator.XLS_XSD_PATH, httpsClient.sendRequest(xmlRequest));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Create the <Request> header element using the OpenGIS-API
	 * @return the request header object
	 */
	private RequestHeaderType createHeaderElement() {
		RequestHeaderType header = new RequestHeaderType();
		header.setClientName("OpenGIS-XLS-Demo");
		header.setClientPassword("OpenGIS-XLS-Demo");
		// header.setMSID("123456");
		// header.setSessionID("blah-todo");
		// header.setSrsName("EPSG4238");
		return header;
	}

	/**
	 * Create the <determineRouteRequest> element with content to select a specific route using the
	 * OpenGIS-API
	 * @return the elements object
	 */
	private RequestType createDetermineRouteRequestElement() {
		// Create the <Request>
		RequestType request = new RequestType();
		request.setMethodName("DetermineRouteRequest");
		request.setVersion("1.1");
		request.setMaximumResponses(BigInteger.ONE);
		request.setRequestID("");
		// Create the <RouteInstructionsRequest>
		RouteInstructionsRequestType reqType = new RouteInstructionsRequestType();
		reqType.setFormat("text/plain");
		reqType.setProvideGeometry(true);
		// START POINT
		DirectPositionType startDirPosition = new DirectPositionType();
		startDirPosition.getValue().add(-34.94226099);
		startDirPosition.getValue().add(138.58988501);
		PointType startPoint = new PointType();
		startPoint.setPos(startDirPosition);
		PositionType startPositionType = new PositionType();
		startPositionType.setPoint(startPoint);
		WayPointType startWayPoint = new WayPointType();
		startWayPoint.setLocation(MarshallerMediator.OBJECT_FACTORY.createPosition(startPositionType));
		// END Point
		DirectPositionType endDirPosition = new DirectPositionType();
		endDirPosition.getValue().add(-34.9395461);
		endDirPosition.getValue().add(138.57242751);
		PointType endPoint = new PointType();
		endPoint.setPos(endDirPosition);
		PositionType endPositionType = new PositionType();
		endPositionType.setPoint(endPoint);
		WayPointType endWayPoint = new WayPointType();
		endWayPoint.setLocation(MarshallerMediator.OBJECT_FACTORY.createPosition(endPositionType));
		// Add START and END to RoutePlan
		WayPointListType waypoints = new WayPointListType();
		waypoints.setStartPoint(startWayPoint);
		waypoints.setEndPoint(endWayPoint);
		// Create a <RoutePlan>
		RoutePlanType routePlan = new RoutePlanType();
		routePlan.setRoutePreference(RoutePreferenceType.FASTEST);
		routePlan.setWayPointList(waypoints);
		// Create the <DetermineRouteRequest>
		DetermineRouteRequestType determineRouteReq = new DetermineRouteRequestType();
		determineRouteReq.setRouteInstructionsRequest(reqType);
		determineRouteReq.setRoutePlan(routePlan);
		// Add the <GeocodeRequest> to the <Request>
		request.setRequestParameters(MarshallerMediator.OBJECT_FACTORY.createDetermineRouteRequest(determineRouteReq));
		return request;

	}

}
