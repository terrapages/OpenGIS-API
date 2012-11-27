<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%
			response.setHeader("Cache-Control",
			"max-age=0, no-store, must-revalidate, private");
	response.setHeader("cache-request-directive", "no-cache");
	response.setHeader("cache-response-directive", "no-cache");

	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
%>
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<link rel="stylesheet" href='css/terrapages.css' type="text/css" />
<link rel="shortcut icon" type="image/icon" href='images/terrapages.ico' />
</head>
<body>





<table width="800" align="center">
	<tr>
		<td align="justify">
		<table width="800" align="center">

			<tr>
				<td align="left">
				<h1><%=((String) getServletConfig().getServletContext().getInitParameter("projectCompany"))%> <%=((String) getServletConfig().getServletContext().getInitParameter("projectName"))%></h1>
				Release: <%=((String) getServletConfig().getServletContext().getInitParameter("tag"))%></td>
				<td align="right"><img src="images/terrapageslogo.gif" border="0" /></td>
			</tr>
		</table>
		<div class="rightColumnTitle">Welcome</div>







				<h2>About TerraPages OpenGIS-API</h2>
				
				
				<a href="http://www.terrapages.com" target="_new"><b>TerraPages</b></a> welcomes you to OpenGIS-API: A Java implementation of the Open Geospatial Consortium, Inc. (OGC) XML service interface specifications. This will provide OpenGIS specified XML communication within Java applications. 
This may include service providers and clients wishing to composing and interpret XML requests that validate against the OGC service schema specifications.

<h2>Why use the TerraPages OpenGIS-API?</h2>
This provides Java developers with the ability to develop against Java Object Classes that represent the exact OGC service specification. You don't need to write code to traverse and parse your XML!    

<div class="rightColumnTitle">TerraPages Location Services</div>
<h2>TerraPages OpenGIS-XLS Location Services</h2>
<a href="https://terrapages.net/opengis-xls" target="_blank">TerraPages OpenGIS-XLS</a> is a service implementation of the OGC XML for Location Services (OLS)/(XLS) and is available NOW! This includes Geocoding, Reverse Geocoding, Routing and Information Directory services.
				

		<div class="rightColumnTitle">Distrobution</div>
<h2>Future Direction and OpenSourcing...</h2>
Currently distrobution of this API is under contractual agreement... however TerraPages is looking to OpenSource the API.
Please contact us regarding access to this product, or inquires regarding the progress of OpenSourcing this API.
<a href="mailto:sales@terrapages.com">Contact TerraPages</a>.  

				
				<div class="rightColumnTitle">OGC Information Service Models</div>
				<h2>XSD XML Schemas</h2>
				Available for your interest or validation <a href="xsd/">HERE</a>.
				
				
				<h2>Who are the OGC?</h2>
				Visit: <img src="images/ogc.ico" border="0" /> <a
					href='http://www.opengeospatial.org/' target="_new">Open Geospatial
				Consortium, Inc. (OGC)</a>.

		<div class="line"><br />
		</div>
		</td>
	</tr>
</table>


</body>
</html>
