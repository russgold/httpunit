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
import java.net.URL;

import java.util.Stack;
import java.util.Vector;

import org.w3c.dom.*;


/**
 * A frame in a web page.
 **/
class WebFrame {

    
//---------------------------------------- package methods -----------------------------------------


    WebFrame( URL baseURL, Node frameNode ) {
        _element = frameNode;
        _baseURL = baseURL;
    }


    String getName() {
        if (_name == null) {
            _name = NodeUtils.getNodeAttribute( _element, "name" );
            if (_name.length() == 0) _name = toString();
        }
        return _name;
    }


    WebRequest getInitialRequest() {
        return new GetMethodWebRequest( _baseURL, 
                                        NodeUtils.getNodeAttribute( _element, "src" ),
                                        getName() );
    }


    boolean hasInitialRequest() {
        return NodeUtils.getNodeAttribute( _element, "src" ).length() > 0;
    }


//----------------------------------- private fields and methods -----------------------------------


    Node _element;

    URL  _baseURL;

    String _name;
}

