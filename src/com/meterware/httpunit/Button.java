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
import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class Button extends FormControl {

    protected final WebForm  _form;
    private String _onClickEvent = "";


    Button( WebForm form ) {
        _form = form;
    }


    Button( WebForm form, Node node ) {
        super( node );
        _form = form;
        _onClickEvent = NodeUtils.getNodeAttribute( node, "onclick" );
    }


    /**
     * Returns the value associated with this button.
     **/
    public String getValue() {
        return getValueAttribute();
    }


    /**
     * Performs the action associated with clicking this button. For a submit button this typically
     * submits the form.
     */
    public void click() throws IOException, SAXException  {
        doOnClickEvent();
    }


    final
    protected boolean doOnClickEvent() {
        return _onClickEvent.length() == 0 || getScriptableObject().doEvent( _onClickEvent );
    }


    final
    protected WebForm getForm() {
        return _form;
    }


//-------------------------------------------------- FormControl methods -----------------------------------------------


    String[] getValues() {
        return new String[ 0 ];
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
    }

}