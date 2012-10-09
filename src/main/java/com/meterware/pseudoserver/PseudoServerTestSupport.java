package com.meterware.pseudoserver;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002-2012, Russell Gold
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
import java.io.IOException;

import org.junit.rules.ExternalResource;

/**
 * helper class for JUnit Tests of httpunit
 *
 */
public class PseudoServerTestSupport extends ExternalResource {
    private String _hostPath;
    private PseudoServer _server;

    @Override                                    
    public void before() throws Throwable {      
        setUpServer();                           
    }                                            
                                                 
    @Override                                    
    public void after() {                        
        tearDownServer();                        
    }                                            

    public void setUpServer() throws IOException {
        _server = new PseudoServer();
        _hostPath = "http://localhost:" + _server.getConnectedPort();
    }

    public void tearDownServer() {
        if (_server != null) _server.shutDown();
    }


    public void mapToClasspath( String directory ) {
        _server.mapToClasspath(directory);
    }

    public void defineResource(String resourceName, PseudoServlet servlet) {
        _server.setResource(resourceName, servlet);
    }

    public void defineResource(String resourceName, String value) {
        _server.setResource(resourceName, value);
    }

    public void defineResource(String resourceName, byte[] value, String contentType) {
        _server.setResource(resourceName, value, contentType);
    }

    public void defineResource(String resourceName, String value, int statusCode) {
        _server.setErrorResource(resourceName, statusCode, value);
    }

    public void defineResource(String resourceName, String value, String contentType) {
        _server.setResource(resourceName, value, contentType);
    }

    public void addResourceHeader(String resourceName, String header) {
        _server.addResourceHeader(resourceName, header);
    }

    public void setResourceCharSet(String resourceName, String setName, boolean reportCharSet) {
        _server.setCharacterSet(resourceName, setName);
        _server.setSendCharacterSet(resourceName, reportCharSet);
    }

    /**
     * define a Web Page with the given page name and boy adding the html and body tags with pageName as the title of the page
     * use the given xml names space if it is not null
     *
     * @param xmlns
     * @param pageName
     * @param body
     */
    public void defineWebPage(String xmlns, String pageName, String body) {
        String preamble = "";
        if (xmlns == null)
            xmlns = "";
        else {
            preamble = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
            preamble += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
            xmlns = " xmlns=\"" + xmlns + "\"";
        }
        defineResource(pageName + ".html", preamble + "<html" + xmlns + ">\n<head><title>" + pageName + "</title></head>\n" +
                "<body>\n" + body + "\n</body>\n</html>");
    }

    /**
     * define a Web Page with the given page name and boy adding the html and body tags with pageName as the title of the page
     *
     * @param pageName
     * @param body
     */
    public void defineWebPage(String pageName, String body) {
        defineWebPage(null, pageName, body);
    }

    public PseudoServer getServer() {
        return _server;
    }

    public void setServerDebug(boolean enabled) {
        _server.setDebug(enabled);
    }

    public String getHostPath() {
        return _hostPath;
    }

    public int getHostPort() throws IOException {
        return _server.getConnectedPort();
    }
}