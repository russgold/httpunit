package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2006, Russell Gold
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
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLFormElementImpl extends HTMLElementImpl implements HTMLFormElement {

    ElementImpl create() {
        return new HTMLFormElementImpl();
    }


    public String getAcceptCharset() {
        return getAttributeWithDefault( "accept-charset", "UNKNOWN" );
    }


    public void setAcceptCharset( String acceptCharset ) {
        setAttribute( "accept-charset", acceptCharset );
    }


    public String getAction() {
        return getAttribute( "action" );
    }


    public void setAction( String action ) {
        setAttribute( "action", action );
    }


    public String getEnctype() {
        return getAttributeWithDefault( "enctype", "application/x-www-form-urlencoded" );
    }


    public void setEnctype( String enctype ) {
        setAttribute( "enctype", enctype );
    }


    public String getMethod() {
        return getAttributeWithDefault( "method", "get" );
    }


    public void setMethod( String method ) {
        setAttribute( "method", method );
    }


    public String getName() {
        return getAttributeWithNoDefault( "name" );
    }


    public void setName( String name ) {
        setAttribute( "name", name );
    }


    public String getTarget() {
        return getAttributeWithDefault( "target", "_self" );
    }


    public void setTarget( String target ) {
        setAttribute( "target", target );
    }


    public HTMLCollection getElements() {
        ArrayList elements = new ArrayList();
        appendElementsWithTags( new String[] { "INPUT", "TEXTAREA", "BUTTON", "SELECT" }, elements );
        return HTMLCollectionImpl.createHTMLCollectionImpl( new NodeListImpl( elements ) );
    }


    public int getLength() {
        return 0;
    }


    public void reset() {
        HTMLCollection elements = getElements();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node instanceof HTMLControl) ((HTMLControl) node).reset();
        }
    }


    public void submit() {
    }

}
