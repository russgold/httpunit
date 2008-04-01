package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002-2007, Russell Gold
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
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingHandler;
import com.meterware.httpunit.dom.*;
import com.meterware.httpunit.parsing.ScriptHandler;
import com.meterware.httpunit.protocol.ParameterProcessor;

import java.io.IOException;

import org.xml.sax.SAXException;


/**
 * A button in a form.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class Button extends FormControl {

    static final public HTMLElementPredicate WITH_ID;
    static final public HTMLElementPredicate WITH_LABEL;
    
    private WebResponse _baseResponse;


    public String getType() {
        return BUTTON_TYPE;
    }


    Button( WebForm form ) {
        super( form );
    }


    /**
     * construct a button from the given html control and assign the event handlers for onclick, onmousedown and onmouseup
     * @param form
     * @param control
     */
    Button( WebForm form, HTMLControl control ) {
        super( form, control );
    }


    Button( WebResponse response, HTMLControl control ) {
        super( null, control );
        _baseResponse = response;
    }


    /**
     * Returns the value associated with this button.
     **/
    public String getValue() {
        return emptyIfNull( getNode() instanceof HTMLInputElementImpl
                                ? ((HTMLInputElementImpl) getNode()).getValue()
                                : ((HTMLButtonElementImpl) getNode()).getValue() );
    }


    /**
     * Performs the action associated with clicking this button after running any 'onClick' script.
     * For a submit button this typically submits the form.
     */
    public void click() throws IOException, SAXException {
        verifyButtonEnabled();
        if (doOnClickEvent()) doButtonAction();
    }


    protected void verifyButtonEnabled() {
        if (isDisabled()) throw new IllegalStateException( "Button" + (getName().length() == 0 ? "" : " '" + getName() + "'") + " is disabled and may not be clicked." );
    }


    /**
     * Returns true if this button is disabled, meaning that it cannot be clicked.
     **/
    public boolean isDisabled() {
        return super.isDisabled();
    }
   
  
    /**
     * Perform the normal action of this button.
     */
    protected void doButtonAction() throws IOException, SAXException {}


//-------------------------------------------------- FormControl methods -----------------------------------------------


    protected String[] getValues() {
        return new String[ 0 ];
    }


    protected void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
    }


    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }


    public ScriptableDelegate getParentDelegate() {
        if (getForm() != null) return super.getParentDelegate();
        return _baseResponse.getDocumentScriptable();
    }


    class Scriptable extends FormControl.Scriptable {

        public void click() throws IOException, SAXException {
            doButtonAction();
        }
    }


    static {
        WITH_ID = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object button, Object id ) {
                return ((Button) button).getID().equals( id );
            }
        };

        WITH_LABEL = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object button, Object label ) {
                return ((Button) button).getValue().equals( label );
            }
        };

    }
}
