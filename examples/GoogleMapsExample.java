/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2008, Wolfgang Fahl, BITPlan GmbH (http://www.bitplan.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.meterware.httpunit.*;

/**
 * get a Route from Google Maps and convert it as a GPX file useable by Garmin
 * tracking devices - can be imported in MapSource software then ...
 * 
 * As of 2011-01 the old example did not work properly any more. Tricks for
 * accessing Google Maps via HTML / Javascript can be found at:
 * http://www.codeproject.com/KB/scripting/Use_of_Google_Map.aspx
 * 
 * this example was modified to use the Google Directions API now 
 * @see http://code.google.com/intl/de-DE/apis/maps/documentation/directions/
 * 
 */
public class GoogleMapsExample {

	/**
	 * shall we show debug information?
	 */
	private boolean DEBUG = true;
	
	 /** 
	  * @param filePath the name of the file to open. Not sure if it can accept URLs or just filenames. Path handling could be better, and buffer sizes are hardcoded
	  */ 
   private static String readFileAsString(String filePath)
   throws java.io.IOException{
       StringBuffer fileData = new StringBuffer(1000);
       BufferedReader reader = new BufferedReader(
               new FileReader(filePath));
       char[] buf = new char[1024];
       int numRead=0;
       while((numRead=reader.read(buf)) != -1){
           String readData = String.valueOf(buf, 0, numRead);
           fileData.append(readData);
           buf = new char[1024];
       }
       reader.close();
       return fileData.toString();
   }


	/**
	 * get a Route description as a Garmin compatible GPX file
	 * 
	 * @param startPoint
	 * @param destPoint
	 */
	public void getRouteAsGPX(String startPoint, String destPoint,
			String filename, boolean withDisplay) throws Exception {
		String kml = getRouteFromGoogleMaps(startPoint, destPoint, withDisplay,
				false);
		if (DEBUG) {
			System.out.println(kml);
		}
		// http://wiki.openstreetmap.org/index.php/JOSM
		// here is a so called open source kml to gpx converter
		// http://www.fish-track.com/?page_id=3
		Document gpxdoc = convertKMLtoGPX(kml);
		// output the result
		boolean tostdout=filename==null;
		if (tostdout){
			File f = File.createTempFile("httpUnit", ".xml");
			f.deleteOnExit();
			filename=f.getAbsolutePath();
		}	
		xmlSerialize(gpxdoc, filename);
		if (tostdout){
			String xmltext=this.readFileAsString(filename);
			System.out.println(xmltext);
		}
	}

	/**
	 * create the xml output for the given document
	 * 
	 * @param document
	 * @param filename
	 * @throws Exception
	 */
	public void xmlSerialize(Document document, String filename) throws Exception {
		OutputFormat outputOptions = new OutputFormat();
		// outputOptions.setOmitXMLDeclaration(true);
		outputOptions.setIndent(4);
		outputOptions.setMethod("xml");
		// if (System.getProperty("os.name").startsWith("Windows")) {
		outputOptions.setEncoding("ISO-8859-1");
		Writer writer = new BufferedWriter(new FileWriter(new File(filename)));
		DOMSerializer serializer = new XMLSerializer(writer, outputOptions);
		serializer.serialize(document);
		writer.close();
	}

	/**
	 * get the subnode with the given tagname
	 * 
	 * @param parent
	 * @param tagName
	 * @return
	 */
	public Element getSubNode(Element parent, String tagName,
			boolean throwException) {
		NodeList subNodes = parent.getElementsByTagName(tagName);
		if (subNodes.getLength() != 1) {
			if (throwException)
				throw new RuntimeException("getSubNode failed for " + parent
						+ " expected 1 child with tag name '" + tagName + "' but got "
						+ subNodes.getLength());
			else
				return null;
		}
		return (Element) subNodes.item(0);
	}

	/**
	 * convert the given Google Map Direction API file to gpx format
	 * 
	 * @param xml
	 *          - the xml version of the file
	 * @return
	 */
	public Document convertKMLtoGPX(String googlexml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document kmldoc = builder.parse(new InputSource(new StringReader(googlexml)));
		Document gpxdoc = builder.newDocument();
		String comment = "Converted by httpunit GoogleMapsExample";
		gpxdoc.appendChild(gpxdoc.createComment(comment));

		org.w3c.dom.Element root = gpxdoc.createElement("gpx");
		root.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
		root.setAttribute("creator", "KML2GPX Example by BITPlan");
		root.setAttribute("version", "1.2");
		gpxdoc.appendChild(root);

		org.w3c.dom.Element metadata = gpxdoc.createElement("metadata");
		org.w3c.dom.Element metadatalink = gpxdoc.createElement("link");
		metadatalink.setAttribute("href", "http://www.bitplan.com");
		metadata.appendChild(metadatalink);
		org.w3c.dom.Element metadatatext = gpxdoc.createElement("text");
		metadatatext.setTextContent("BITPlan GmbH, Willich, Germany");
		metadatalink.appendChild(metadatatext);
		root.appendChild(metadata);

		org.w3c.dom.Element route = gpxdoc.createElement("rte");
		root.appendChild(route);
		NodeList routePoints = kmldoc.getElementsByTagName("step");
		for (int i = 0; i < routePoints.getLength(); i++) {
			Element stepNode = (Element) routePoints.item(i);
			String instructions = getSubNode(stepNode, "html_instructions", true).getTextContent();
			if (DEBUG)
				System.out.println("found route point " + i + ": " + instructions);

			Element endNode=getSubNode(stepNode,"end_location",true);
			Element latNode=getSubNode(endNode, "lat",true);
			Element lonNode=getSubNode(endNode,"lng",true);
			if (latNode != null && lonNode!=null) {
				org.w3c.dom.Element routePoint = gpxdoc.createElement("rtept");
				routePoint.setAttribute("lon", lonNode.getTextContent());
				routePoint.setAttribute("lat", latNode.getTextContent());
				org.w3c.dom.Element routePointName = gpxdoc.createElement("name");
				routePointName.setTextContent("step"+i);
				routePoint.appendChild(routePointName);
				route.appendChild(routePoint);
			}
		}
		return gpxdoc;
	}

	/**
	 * the url to use
	 */
	public static String url = "http://maps.google.com/maps/api/directions/xml";


	/**
	 * error handling in case the Google Maps WebPage has changed
	 * 
	 * @throws Exception
	 */
	public void notifyGoogleMapsHasChanged(String msg) throws Exception {
		throw new Exception(
				"GoogleMaps lookupForm at "
						+ msg
						+ "\n has changed - please notify the author of this example via the httpunit-develop mailing list");
	}

	/**
	 * get a route from google maps with the given start and destination points
	 * 
	 * @param startPoint
	 * @param destPoint
	 * @param withDisplay
	 * @param asKML
	 */
	public String getRouteFromGoogleMaps(String startPoint, String destPoint,
			boolean withDisplay, boolean asKML) throws Exception {
		// and now indirectly
		// create the conversation object which will maintain state for us
		WebConversation wc = new WebConversation();

		// Obtain the main page on the google maps web site
		WebRequest request = new GetMethodWebRequest(url);
		request.setParameter("origin", startPoint);
		request.setParameter("destination", destPoint);
		request.setParameter("sensor", "false");
		if (DEBUG)
			System.out.println("requesting "+url+"?"+request.getQueryString());
		WebResponse response=null;
		try {
			response = wc.getResponse(request);
		} catch (Throwable th) {
			String[] paramnames = request.getRequestParameterNames();
			System.err.print("valid parameter names are: ");
			String delim = "";
			for (int i = 0; i < paramnames.length; i++) {
				System.err.print(delim + i + ":" + paramnames[i]);
				delim = ",";
			}
			System.err.println();
			this.notifyGoogleMapsHasChanged(request.toString() + ":"
					+ th.getMessage());
		}
		//if (withDisplay)
		//	BrowserDisplayer.showResponseInBrowser(response);
		return (response.getText());
	}

	/**
	 * get the distance between two given locations from the Google Maps API xml
	 * file
	 * 
	 * @param
	 */
	public String getDistance(String startPoint, String endPoint,
			boolean withDisplay) throws Exception {
		String route = getRouteFromGoogleMaps(startPoint, endPoint, withDisplay,
				false);
		String[] driveLocale = { "Driving directions", "Route" };
		String[] unitLocale = { "mi", "km" };
		for (int i = 0; i < driveLocale.length; i++) {
			int drivePos = route.indexOf(driveLocale[i]);
			if (drivePos > 0) {
				String driveString = route.substring(drivePos);
				int unitPos = driveString.indexOf(unitLocale[i]);
				if (unitPos > 0) {
					String distanceString = driveString.substring(1, unitPos
							+ unitLocale[i].length());
					int divPos;
					while ((divPos = distanceString.indexOf("<div>")) > 0) {
						distanceString = distanceString.substring(divPos + 5);
					}
					distanceString = distanceString.replace("&#160;", " ");
					distanceString = distanceString.replace("<b>", "");
					return distanceString;
				}
			}
		}
		return "?";
	}

	/**
	 * Start the Route as GPX converter with the given command line parameters
	 * display usage with and example if no parameters are given
	 * 
	 * @param params
	 */
	public static void main(String[] params) {
		try {
			if (params.length < 3) {
				System.out
						.println("Usage: java RouteAsGPX [from] [to] [filename] [nodisplay]");
				System.out
						.println("      e.g. java RouteAsGPX sfo CA-94526 sfoexample.gpx");
				System.out
						.println("         to get the route as a Garmin compatible GPX file");
				System.out.println("      e.g. java RouteAsGPX sfo CA-94526");
				System.out.println("         to output to stdout/screen");
				String[] defaultParams = { "sfo", "CA-94526", };
				// defaultParams={"sfo","CA-94526","sfoexample.gpx"};
				params = defaultParams;
				System.out.println("will demonstrate usage with the route "
						+ defaultParams[0] + " - " + defaultParams[1] + " to stdout ");
			}
			/**
			 * This is how to use the example in germany:
			 * GoogleMapsExample.url="http://maps.google.de/maps";
			 * GoogleMapsExample.directions="Route berechnen";
			 */
			GoogleMapsExample routeAsGPX = new GoogleMapsExample();
			HttpUnitOptions.setScriptingEnabled(false);

			boolean withDisplay = true;
			if (params.length >= 4)
				withDisplay = false;
			String startPoint = params[0];
			String endPoint = params[1];
			String filename = null;
			if (params.length>2)
			  filename=params[2];
	  	routeAsGPX.getRouteAsGPX(startPoint, endPoint, filename, withDisplay);
		} catch (Exception e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
	}
}
