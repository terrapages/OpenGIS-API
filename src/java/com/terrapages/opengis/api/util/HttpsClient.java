package com.terrapages.opengis.api.util;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * This method posts the XLS request to the TerraPages OpenGIS-XLS service
 * <strong>IMPORTANT: </strong> this uses the EasySSLProtocolSocketFactory
 * to trust unsigned SSL certificates. 
 * @author ahughes
 * @see com.terrapages.opengis.api.util.EasySSLProtocolSocketFactory
 */
public class HttpsClient {

	
	/**
	 * This will post via https the XLSType.
	 * Un-Sign SSL certificates are trusted with use
	 * of the EasySSLProtocolSocketFactory
	 * @param request the XLS Request
	 * @return the response from the XLS Web Service
	 * @see EasySSLProtocolSocketFactory
	 */
	public InputStream sendRequest(String xmlRequest) throws Exception {
		try {
			//Setup the HTTPS Client
			Protocol myhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
			HttpClient httpClient = new HttpClient();
			httpClient.getHostConfiguration().setHost("terrapages.net", 443, myhttps);
			//Creat the post to the server
			PostMethod post = new PostMethod("/opengis-xls/xls");
			NameValuePair[] data = { new NameValuePair("xls", xmlRequest) };
			post.setRequestBody(data); 
			httpClient.executeMethod(post); //note.. this returns the status code.
			//Read the response
			InputStream in = post.getResponseBodyAsStream();
			return in;
		} catch (Exception e) {
			System.out.println("ERROR: " + e.toString());
			e.printStackTrace();
		}
		return null;
	}	
	
	
	/**
	 * Marshalls the XLSType java object to a String
	 * @param xls the XLS document java object
	 * @return the object in an text/xml format
	 */
	
	/**
	public String marshall(XLSType xls) {
		StringWriter stringWriter = new StringWriter();
		try {
			JAXBContext jc = JAXBContext.newInstance("net.opengis.xls", this
					.getClass().getClassLoader());
			ObjectFactory factory = new ObjectFactory();
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			marshaller.marshal(factory.createXLS(xls), stringWriter);
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stringWriter.toString();
	}
	*/

}

