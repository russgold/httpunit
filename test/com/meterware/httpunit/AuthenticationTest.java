package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000, Russell Gold
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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the basic authentication.
 **/
public class AuthenticationTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( AuthenticationTest.class );
    }


    public AuthenticationTest( String name ) {
        super( name );
    }

	
    public void testRejection() throws Exception {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( "http://www.adhq.com/adnet" );
        try {
            WebResponse response = wc.getResponse( request );
            fail( "Should have rejected this no-password request" );
        } catch (AuthorizationRequiredException e) {
            System.out.println( "Realm = " + e.getAuthenticationParameter( "realm" ) );
        }
    }


    public void testAuthorization() throws Exception {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( "http://www.adhq.com/adnet" );
        wc.setAuthorization( "helpguy", "chester5" );
        WebResponse response = wc.getResponse( request );
        System.out.println( "response=" + response );
    }

}

