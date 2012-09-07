package com.meterware.httpunit;

/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2000-2001, Russell Gold
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
 *
 *******************************************************************************************************************/
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A unit test to verify miscellaneous requests with message bodies.
 **/
public class MessageBodyRequestTest extends HttpUnitTest {

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		return new TestSuite(MessageBodyRequestTest.class);
	}

	public MessageBodyRequestTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * make a Request from the given parameters
	 * @param resourceName
	 * @param sourceData
	 * @param contentType
	 * @return the new WebRequest
	 * @throws UnsupportedEncodingException
	 */
	public WebRequest makeRequest(String resourceName,String sourceData, String contentType) throws UnsupportedEncodingException{
		defineResource(resourceName, new BodyEcho());
		InputStream source = new ByteArrayInputStream(sourceData
				.getBytes("iso-8859-1"));
		WebRequest wr = new PostMethodWebRequest(getHostPath() + "/"+resourceName,
				source, contentType);
		return wr;
	}

	/**
	 * test a generic Post request
	 * @throws Exception
	 */
	public void testGenericPostRequest() throws Exception {
		WebConversation wc = new WebConversation();
		String sourceData="This is an interesting test\nWith two lines";
		WebRequest wr = makeRequest("ReportData",sourceData, "text/sample");
		WebResponse response = wc.getResponse(wr);
		assertEquals("Body response", "\nPOST\n" + sourceData, response
				.getText());
		assertEquals("Content-type", "text/sample", response.getContentType());
	}
	
	/**
	 * test for Patch by Serge Maslyukov for empty content Types
	 * @throws Exception
	 */
	public void testEmptyContentType() throws Exception {
		WebConversation wc = new WebConversation();
		String emptyContentType=""; // this is an emptyContentType
		WebRequest wr = makeRequest("something","some data",emptyContentType);
		WebResponse response = wc.getResponse(wr);
		assertEquals("Content-type", "",wr.getContentType());
	}

	public void testPutRequest() throws Exception {
		defineResource("ReportData", new BodyEcho());
		String sourceData = "This is an interesting test\nWith two lines";
		InputStream source = new ByteArrayInputStream(sourceData
				.getBytes("iso-8859-1"));

		WebConversation wc = new WebConversation();
		WebRequest wr = new PutMethodWebRequest(getHostPath() + "/ReportData",
				source, "text/plain");
		WebResponse response = wc.getResponse(wr);
		assertEquals("Body response", "\nPUT\n" + sourceData, response
				.getText());
	}

	/**
	 * test for download problem described by Oliver Wahlen	 
	 */
	public void testDownloadRequestUsingGetText() throws Exception {
    	defineResource( "ReportData", new BodyEcho() );
    	byte[] binaryData = new byte[256];
    	for(int i=0;i<=255;i++) {
    		binaryData[i] = (byte)i;
    	}

    	InputStream source = new ByteArrayInputStream( binaryData );

    	WebConversation wc = new WebConversation();
    	WebRequest wr = new PutMethodWebRequest( getHostPath() + "/ReportData", source, "application/random" );
    	WebResponse response = wc.getResponse( wr );
    	// currently the following line does not work:
    	byte[] download = response.getText().getBytes();
    	// 	currently the following line works:
    	// byte[] download = getDownload( response );
    	// and this one is now available ...
    	download=response.getBytes();
		assertEquals("Body response", binaryData, download);
	}
	
	public void testDownloadRequest() throws Exception {
		defineResource("ReportData", new BodyEcho());
		byte[] binaryData = new byte[] { 0x01, 0x05, 0x0d, 0x0a, 0x02 };

		InputStream source = new ByteArrayInputStream(binaryData);

		WebConversation wc = new WebConversation();
		WebRequest wr = new PutMethodWebRequest(getHostPath() + "/ReportData",
				source, "application/random");
		WebResponse response = wc.getResponse(wr);
		byte[] download = response.getBytes();
		assertEquals("Body response", binaryData, download);

		download = getDownload(response);
		assertEquals("Body response", binaryData, download);
		
		download = response.getBytes();
		assertEquals("Body response", binaryData, download);
	}

	/**
	 * test for BR [ 1964665 ] HeaderOnlyRequest cannot be constructed
	 */
	public void testHeaderOnlyWebRequest() throws Exception {
		HeaderOnlyWebRequest r = new HeaderOnlyWebRequest(
				"http://www.google.com");
	}

	/**
	 * please do not copy this function any more - use getBytes instead ...
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private byte[] getDownload(WebResponse response) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = response.getInputStream();

		byte[] buffer = new byte[8 * 1024];
		int count = 0;
		do {
			outputStream.write(buffer, 0, count);
			count = inputStream.read(buffer, 0, buffer.length);
		} while (count != -1);

		inputStream.close();
		return outputStream.toByteArray();
	}

}

class BodyEcho extends PseudoServlet {
	/**
	 * Returns a resource object as a result of a get request.
	 **/
	public WebResource getResponse(String method) {
		String contentType = getHeader("Content-type");
		if (contentType.startsWith("text")) {
			return new WebResource(
					"\n" + method + "\n" + new String(getBody()), contentType);
		} else {
			return new WebResource(getBody(), contentType);
		}
	}
}
