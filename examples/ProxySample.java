/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002, Russell Gold
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

import com.meterware.httpunit.WebConversation;

import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/ 
public class ProxySample extends TestCase {


	// modify proxyURL and proxyPort to your personal preferences
	// e.g. a proxy server that is accessible from your network
	public static String proxyURL="www-proxy.us.oracle.com";
	public static int proxyPort=80;
	
		/**
		 * main routine to test 
		 * @param args
		 */
    public static void main(String args[]) {
    		System.out.println("Will run this sample JUnit Testcase with proxy URL set to "+proxyURL+" port "+proxyPort);
    		System.out.println("You might want to modify the proxy server URL (see proxyURL,proxyPort fields in ProxySample.java) to one that you may use at your location for quicker response time");
    		// run this example as a Unit test
        junit.textui.TestRunner.run( suite() );
    }


    /**
     * create a Testsuite containing this ProxySample class
     * @return the testsuite
     */
    public static TestSuite suite() {
        return new TestSuite( ProxySample.class );
    }


    /**
     * constructor which just calls the super constructor
     * @param name
     */
    public ProxySample( String name ) {
        super( name );
    }


    /**
     * test the proxy access
     * - set the proxy server to one that you may use at your location for quicker response time
     * @throws Exception
     */
    public void testProxyAccess() throws Exception {
        WebConversation wc = new WebConversation();
        wc.setProxyServer( proxyURL, proxyPort );
        wc.getResponse( "http://www.meterware.com" );
    }



}

