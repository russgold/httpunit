package com.meterware.httpunit;
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
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.applet.Applet;
import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * This class represents the embedding of an applet in a web page.
 *
 * @author <a href="Oliver.Imbusch.extern@HVBInfo.com">Oliver Imbusch</a>
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/ 
public class WebApplet {

    private WebResponse _response;
    private Node        _node;
    private String      _baseTarget;
    private URL         _codeBase;
    private String      _className;
    private Applet      _applet;

    final private String CLASS_EXTENSION = ".class";


    public WebApplet( WebResponse response, Node rootNode, String baseTarget ) {
        _response   = response;
        _node       = rootNode;
        _baseTarget = baseTarget;
    }


    /**
     * Returns the URL of the codebase used to find the applet classes
     */
    public URL getCodeBase() throws MalformedURLException {
        if (_codeBase == null) {
            _codeBase = new URL( _response.getURL(), getAttribute( "codebase", "/" ) );
        }
        return _codeBase;
    }


    /**
     * Returns the name of the applet main class.
     */
    public String getMainClassName() {
        if (_className == null) {
            _className = getAttribute( "code" );
            if (_className.endsWith( CLASS_EXTENSION )) {
                _className = _className.substring( 0, _className.lastIndexOf( CLASS_EXTENSION ));
            }
            _className = _className.replace( '/', '.' ).replace( '\\', '.' );
        }
        return _className;
    }


    String getAttribute( final String name ) {
        return NodeUtils.getNodeAttribute( _node, name );
    }


    String getAttribute( final String name, String defaultValue ) {
        return NodeUtils.getNodeAttribute( _node, name, defaultValue );
    }


    /**
     * Returns the name of the applet.
     */
    public String getName() {
        return getAttribute( "name" );
    }


    /**
     * Returns the width of the panel in which the applet will be drawn.
     */
    public int getWidth() {
        return Integer.parseInt( getAttribute( "width" ) );
    }


    /**
     * Returns the height of the panel in which the applet will be drawn.
     */
    public int getHeight() {
        return Integer.parseInt( getAttribute( "height" ) );
    }


    public Applet getApplet() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (_applet == null) {
            ClassLoader cl = new URLClassLoader( getClassPath() );
            Object o = cl.loadClass( getMainClassName() ).newInstance();
            if (!(o instanceof Applet)) throw new RuntimeException( getMainClassName() + " is not an Applet" );
            _applet = (Applet) o;
            _applet.setStub( new AppletStubImpl( this ) );
        }
        return _applet;
    }


    private URL[] getClassPath() throws MalformedURLException {
        return new URL[] { getCodeBase() };
    }


    String getBaseTarget() {
        return _baseTarget;
    }


    WebApplet[] getAppletsInPage() {
        try {
            return _response.getApplets();
        } catch (SAXException e) {
            e.printStackTrace();  // should never happen.
            return null;
        }
    }


    void sendRequest( URL url, String target ) {
        WebRequest wr = new GetMethodWebRequest( null, url.toExternalForm(), target );
        try {
            _response.getWindow().getResponse( wr );
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            throw new RuntimeException( e.toString() );
        } catch (SAXException e) {
        }
    }


}
