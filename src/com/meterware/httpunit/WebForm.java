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
import java.net.URL;

import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class represents a form in an HTML page. Users of this class may examine the parameters
 * defined for the form, the structure of the form (as a DOM), or the text of the form. They
 * may also create a {@link WebRequest} to simulate the submission of the form.
 **/
public class WebForm extends WebRequestSource {


    /**
     * Returns the method defined for this form.
     **/
    public String getMethod() {
        return NodeUtils.getNodeAttribute( getNode(), "method", "GET" );
    }


    /**
     * Returns the action defined for this form.
     **/
    public String getAction() {
        return NodeUtils.getNodeAttribute( getNode(), "action" );
     }


    /**
     * Returns the character set encoding for this form.
     **/
    public String getCharacterSet() {
        return _characterSet;
    }


    /**
     * Returns true if a parameter with given name exists in this form.
     **/
    public boolean hasParameterNamed( String soughtName ) {
        FormControl[] parameters = getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals( soughtName )) return true;
        }
        return false;
    }


    /**
     * Returns true if a parameter starting with a given name exists,
     **/
    public boolean hasParameterStartingWithPrefix( String prefix ) {
        FormControl[] parameters = getParameters();
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            if (name.startsWith( prefix )) return true;
        }
        return false;
    }


    /**
     * Returns an array containing the names of the parameters defined for this form,
     * in the order in which they appear.
     **/
    public String[] getParameterNames() {
        Vector parameterNames = new Vector();
        FormControl[] parameters = getParameters();

        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            if (!parameterNames.contains( name )) parameterNames.addElement( name );
        }

        String[] result = new String[ parameterNames.size() ];
        parameterNames.copyInto( result );
        return result;
    }


    /**
     * Returns an array containing the submit buttons defined for this form.
     **/
    public SubmitButton[] getSubmitButtons() {
        if (_submitButtons == null) {
            Vector buttons = getSubmitButtonVector();
            _submitButtons = new SubmitButton[ buttons.size() ];
            buttons.copyInto( _submitButtons );
        }
        return _submitButtons;
    }



    private Vector _buttonVector;

    private Vector getSubmitButtonVector() {
        if (_buttonVector == null) {
            _buttonVector = new Vector();

            NodeList nl = ((Element) getNode()).getElementsByTagName( "input" );
            for (int i = 0; i < nl.getLength(); i++) {
                if (hasMatchingAttribute( nl.item(i), "type", "submit" ) || hasMatchingAttribute( nl.item(i), "type", "image" )) {
                    _buttonVector.addElement( new SubmitButton( nl.item(i) ) );
                }
            }

            nl = ((Element) getNode()).getElementsByTagName( "button" );
            for (int i = 0; i < nl.getLength(); i++) {
                if ((hasMatchingAttribute( nl.item(i), "type", "submit" ) || hasMatchingAttribute( nl.item(i), "type", "" ))
                     && nl.item(i).getAttributes().getNamedItem( "disabled" ) == null) {
                    _buttonVector.addElement( new SubmitButton( nl.item(i) ) );
                }
            }
            if (_buttonVector.isEmpty()) _buttonVector.addElement( SubmitButton.UNNAMED_BUTTON );
        }
        return _buttonVector;
    }


    private boolean hasMatchingAttribute( Node node, String attributeName, String attributeValue ) {
        return NodeUtils.getNodeAttribute( node, attributeName ).equalsIgnoreCase( attributeValue );
    }


    /**
     * Returns the submit button defined in this form with the specified name.
     * If more than one such button exists, will return the first found.
     * If no such button is found, will return null.
     **/
    public SubmitButton getSubmitButton( String name ) {
        SubmitButton[] buttons = getSubmitButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getName().equals( name )) {
                return buttons[i];
            }
        }
        return null;
    }


    /**
     * Returns the submit button defined in this form with the specified name and value.
     * If more than one such button exists, will return the first found.
     * If no such button is found, will return null.
     **/
    public SubmitButton getSubmitButton( String name, String value ) {
        SubmitButton[] buttons = getSubmitButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getName().equals( name ) && buttons[i].getValue().equals( value )) {
                return buttons[i];
            }
        }
        return null;
    }


    /**
     * Returns the submit button defined in this form with the specified ID.
     * If more than one such button exists, will return the first found.
     * If no such button is found, will return null.
     **/
    public SubmitButton getSubmitButtonWithID( String ID ) {
        SubmitButton[] buttons = getSubmitButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getID().equals( ID )) {
                return buttons[i];
            }
        }
        return null;
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form with an unnamed submit button.
     **/
    public WebRequest getRequest() {
        return getRequest( (SubmitButton) null );
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form with a button with the specified name and value.
     **/
    public WebRequest getRequest( String submitButtonName, String submitButtonValue ) {
        SubmitButton sb = getSubmitButton( submitButtonName, submitButtonValue );
        if (sb == null) throw new IllegalSubmitButtonException( submitButtonName, submitButtonValue );
        return getRequest( sb );
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form with a button with the specified name.
     **/
    public WebRequest getRequest( String submitButtonName ) {
        SubmitButton sb = getSubmitButton( submitButtonName );
        if (sb == null) throw new IllegalSubmitButtonException( submitButtonName, "" );
        return getRequest( sb );
    }


    private SubmitButton getDefaultButton() {
        if (getSubmitButtons().length == 1) {
            return getSubmitButtons()[0];
        } else if (getSubmitButtonVector().contains( SubmitButton.UNNAMED_BUTTON )) {
            return getSubmitButton( "" );
        } else {
            return null;
        }
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form by pressing the specified button.
     * If the button is null, simulates the pressing of the default button.
     **/
    public WebRequest getRequest( SubmitButton button, int x, int y ) {
        WebRequest request = getRequest( button );
        request.setSubmitPosition( x, y );
        return request;
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form by pressing the specified button.
     * If the button is null, simulates the pressing of the default button.
     **/
    public WebRequest getRequest( SubmitButton button ) {
        if (button == null) button = getDefaultButton();

        if (HttpUnitOptions.getParameterValuesValidated()) {
            if (button == null) {
                throw new IllegalUnnamedSubmitButtonException();
            } else if (!getSubmitButtonVector().contains( button )) {  
                throw new IllegalSubmitButtonException( button );
            } else if (button.isDisabled()) {
                throw new DisabledSubmitButtonException( button );
            }
        }

        String action = getAction();
        if (action.trim().length() == 0) action = getBaseURL().getFile();

        WebRequest request;
        if (getMethod().equalsIgnoreCase( "post" )) {
            request = new PostMethodWebRequest( getBaseURL(), action, getTarget(), this, button );
        } else {
            request = new GetMethodWebRequest( getBaseURL(), action, getTarget(), this, button );
        }

        String[] parameterNames = getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].length() > 0 && getParameterDefaults().get( parameterNames[i] ) != null) {
                Object value = getParameterDefaults().get( parameterNames[i] );
                if (value instanceof String) {
                    request.setParameter( parameterNames[i], (String) value);
                } else {
                    request.setParameter( parameterNames[i], (String[]) value);
                }
            }
        }
	    request.setHeaderField( "Referer", getBaseURL().toExternalForm() );
        return request;
    }


    /**
     * Returns the default value of the named parameter.
     **/
    public String getParameterValue( String name ) {
        Object result = getParameterDefaults().get( name );
        if (result instanceof String) return (String) result;
        if (result instanceof String[] && ((String[]) result).length > 0) return ((String[]) result)[0];
        return "";
    }


    /**
     * Returns the multiple default values of the named parameter.
     **/
    public String[] getParameterValues( String name ) {
        Object result = getParameterDefaults().get( name );
        if (result instanceof String) return new String[] { (String) result };
        if (result instanceof String[]) return (String[]) result;
        return new String[0];
    }


    /**
     * Returns the displayed options defined for the specified parameter name.
     **/
    public String[] getOptions( String name ) {
        Object result = getParameterOptions().get( name );
        if (result instanceof String[]) return (String[]) result;
        return new String[0];
    }


    /**
     * Returns the option values defined for the specified parameter name.
     **/
    public String[] getOptionValues( String name ) {
        Object result = getParameterOptionValues().get( name );
        if (result instanceof String[]) return (String[]) result;
        return new String[0];
    }


    /**
     * Returns true if the named parameter accepts multiple values.
     **/
    public boolean isMultiValuedParameter( String name ) {
        return FormControl.TYPE_MULTI_VALUED.equals( getParameterTypes().get( name ) );
    }


    /**
     * Returns true if the named parameter accepts free-form text.
     **/
    public boolean isTextParameter( String name ) {
        return FormControl.TYPE_TEXT.equals( getParameterTypes().get( name ) );
    }


    /**
     * Returns the number of text parameters in this form with the specified name.
     **/
    public int getNumTextParameters( String name ) {
        Object count = getTextParameterCounts().get( name );
        if (count == null) return 0;
        return ((Number) count ).intValue();
    }


    private Hashtable getTextParameterCounts() {
        if (_textParameterCounts == null) {
            Hashtable parameterCounts = new Hashtable();
            FormControl[] parameters = getParameters();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].updateTextParameterCounts( parameterCounts );
            }
            _textParameterCounts = parameterCounts;
        }
        return _textParameterCounts;
    }


    /**
     * Returns true if the named parameter accepts files for upload.
     **/
    public boolean isFileParameter( String name ) {
        return FormControl.TYPE_FILE.equals( getParameterTypes().get( name ) );
    }


    /**
     * Returns true if this form is to be submitted using mime encoding (the default is URL encoding).
     **/
    public boolean isSubmitAsMime() {
        return "multipart/form-data".equalsIgnoreCase( NodeUtils.getNodeAttribute( getNode(), "enctype" ) );
    }


//---------------------------------- package members --------------------------------

    /**
     * Contructs a web form given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebForm( URL baseURL, String parentTarget, Node node, String characterSet ) {
        super( node, baseURL, parentTarget );
        _characterSet = characterSet;
    }


    /**
     * Returns the values which *must* for the specified parameter name.
     **/
    String[] getRequiredValues( String name ) {
        Object result = getParameterRequiredValues().get( name );
        if (result instanceof String[]) return (String[]) result;
        if (result instanceof String) return new String[] { (String) result };
        return new String[0];
    }


//---------------------------------- private members --------------------------------

    /** The attributes of the form parameters. **/
    private FormControl[] _parameters;

    /** The parameters with their default values. **/
    private Hashtable      _defaults;

    /** The parameters with their displayed options. **/
    private Hashtable      _options;

    /** The parameters with their options. **/
    private Hashtable      _optionValues;

    /** The parameters mapped to the type of data which they accept. **/
    private Hashtable      _dataTypes;

    /** The parameters mapped to their number of occurrences as text parameters. **/
    private Hashtable      _textParameterCounts;

    /** The submit buttons in this form. **/
    private SubmitButton[] _submitButtons;

    /** The character set in which the form will be submitted. **/
    private String         _characterSet;


    private Hashtable getParameterDefaults() {
        if (_defaults == null) {
            FormControl[] parameters = getParameters();
            Hashtable defaults = new Hashtable();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].updateParameterDefaults( defaults );
            }
            _defaults = defaults;
        }
        return _defaults;
    }

    private Hashtable _required;

    private Hashtable getParameterRequiredValues() {
        if (_required == null) {
            FormControl[] parameters = getParameters();
            Hashtable required = new Hashtable();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].updateRequiredValues( required );
            }
            _required = required;
        }
        return _required;
    }


    private Hashtable getParameterOptions() {
        if (_options == null) {
            Hashtable options = new Hashtable();
            FormControl[] parameters = getParameters();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].updateParameterOptions( options );
            }
            _options = options;
        }
        return _options;
    }


    private Hashtable getParameterOptionValues() {
        if (_optionValues == null) {
            Hashtable options = new Hashtable();
            FormControl[] parameters = getParameters();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].updateParameterOptionValues( options );
            }
            _optionValues = options;
        }
        return _optionValues;
    }


    private Hashtable getParameterTypes() {
        if (_dataTypes == null) {
            FormControl[] parameters = getParameters();
            Hashtable types = new Hashtable();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].updateParameterTypes( types );
            }
            _dataTypes = types;
        }
        return _dataTypes;
    }


    /**
     * Returns an array of select control descriptors for this form.
     **/
    /**
     * Returns an array of form parameter attributes for this form.
     **/
    private FormControl[] getParameters() {
        if (_parameters == null) {
            Vector list = new Vector();
            if (getNode().hasChildNodes()) addFormParametersToList( getNode().getChildNodes(), list );
            _parameters = new FormControl[ list.size() ];
            list.copyInto( _parameters );
        }

        return _parameters;
    }


    private void addFormParametersToList( NodeList children, Vector list ) {
        for (int i = 0; i < children.getLength(); i++) {
            addFormParametersToList( children.item(i), list );
        }
    }


    private void addFormParametersToList( Node child, Vector list ) {
        final FormControl formParameter = FormControl.newFormParameter( child );
        if (formParameter != null) {
            list.addElement( formParameter );
        } else if (child.hasChildNodes()) {
            addFormParametersToList( child.getChildNodes(), list );
        }
    }

}


class FormParameter {

    void addControl( FormControl control ) {
        _controls.add( control )
    }


    private ArrayList _controls = new ArrayList();
}




//============================= exception class IllegalUnnamedSubmitButtonException ======================================


/**
 * This exception is thrown on an attempt to define a form request with a button not defined on that form.
 **/
class IllegalUnnamedSubmitButtonException extends IllegalRequestParameterException {


    IllegalUnnamedSubmitButtonException() {
    }


    public String getMessage() {
        return "This form has more than one submit button, none unnamed. You must specify the button to be used.";
    }

}


//============================= exception class IllegalSubmitButtonException ======================================


/**
 * This exception is thrown on an attempt to define a form request with a button not defined on that form.
 **/
class IllegalSubmitButtonException extends IllegalRequestParameterException {


    IllegalSubmitButtonException( SubmitButton button ) {
        _name  = button.getName();
        _value = button.getValue();
    }


    IllegalSubmitButtonException( String name, String value ) {
        _name = name;
        _value = value;
    }


    public String getMessage() {
        return "Specified submit button (name=\"" + _name + "\" value=\"" + _value + "\") not part of this form.";
    }


    private String _name;
    private String _value;

}

//============================= exception class IllegalUnnamedSubmitButtonException ======================================


/**
 * This exception is thrown on an attempt to define a form request with a button not defined on that form.
 **/
class DisabledSubmitButtonException extends IllegalRequestParameterException {


    DisabledSubmitButtonException( SubmitButton button ) {
        _name  = button.getName();
        _value = button.getValue();
    }


    public String getMessage() {
        return "The specified button (name='" + _name + "' value='" + _value
               + "' is disabled and may not be used to submit this form.";
    }


    private String _name;
    private String _value;

}



