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

        HTMLSelectElement[] selections = getSelections();
        for (int i = 0; i < selections.length; i++) {
            if (soughtName.equals( selections[ i ].getName() )) return true;
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

        HTMLSelectElement[] selections = getSelections();
        for (int i = 0; i < selections.length; i++) {
            if (selections[ i ].getName().startsWith( prefix )) return true;
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

        HTMLSelectElement[] selections = getSelections();
        for (int i = 0; i < selections.length; i++) {
            parameterNames.addElement( selections[i].getName() );
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

        NamedNodeMap nnm = getNode().getAttributes();
        String action = getAction();
        if (action.trim().length() == 0) {
            action = getBaseURL().getFile();
        }

        WebRequest result;

        if (getValue( nnm.getNamedItem( "method" ) ).equalsIgnoreCase( "post" )) {
            result = new PostMethodWebRequest( getBaseURL(), action, getTarget(), this, button );
        } else {
            result = new GetMethodWebRequest( getBaseURL(), action, getTarget(), this, button );
        }

        String[] parameterNames = getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].length() > 0 && getParameterDefaults().get( parameterNames[i] ) != null) {
                Object value = getParameterDefaults().get( parameterNames[i] );
                if (value instanceof String) {
                    result.setParameter( parameterNames[i], (String) value);
                } else {
                    result.setParameter( parameterNames[i], (String[]) value);
                }
            }
        }
	    result.setHeaderField( "Referer", getBaseURL().toExternalForm() );
        return result;
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
        return TYPE_MULTI_VALUED.equals( getParameterTypes().get( name ) );
    }


    /**
     * Returns true if the named parameter accepts free-form text.
     **/
    public boolean isTextParameter( String name ) {
        return TYPE_TEXT.equals( getParameterTypes().get( name ) );
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
        getParameterTypes();
        return _textParameterCounts;
    }


    /**
     * Returns true if the named parameter accepts files for upload.
     **/
    public boolean isFileParameter( String name ) {
        return TYPE_FILE.equals( getParameterTypes().get( name ) );
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

    /** The type of a parameter which accepts any text. **/
    private final static Integer TYPE_TEXT = new Integer(1);

    /** The type of a parameter which accepts single predefined values. **/
    private final static Integer TYPE_SCALAR = new Integer(2);

    /** The type of a parameter which accepts multiple predefined values. **/
    private final static Integer TYPE_MULTI_VALUED = new Integer(3);

    /** The type of a parameter which accepts files for upload. **/
    private final static Integer TYPE_FILE = new Integer(4);

    /** A constant set to indicate one parameter. **/
    private final static Integer ONE_PARAMETER = new Integer(1);


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

    /** The selections in this form. **/
    private HTMLSelectElement[] _selections;

    /** The character set in which the form will be submitted. **/
    private String         _characterSet;


    private String getValue( Node node ) {
        return (node == null) ? "" : emptyIfNull( node.getNodeValue() );
    }


    private String emptyIfNull( String value ) {
        return (value == null) ? "" : value;
    }


    private Hashtable getParameterDefaults() {
        if (_defaults == null) {
            FormControl[] parameters = getParameters();
            Hashtable defaults = new Hashtable();
            for (int i = 0; i < parameters.length; i++) {
                String name  = parameters[i].getName();
                String value = parameters[i].getValue();
                String type  = getValue( parameters[i].getNamedItem( "type" ) ).toUpperCase();
                if (type == null || type.length() == 0) type = "TEXT";

                if (type.equals( "TEXT" ) || type.equals( "HIDDEN" ) || type.equals( "PASSWORD" )) {
                    defaults.put( name, value );
                } else if (type.equals( "RADIO" ) && parameters[i].getNamedItem( "checked" ) != null) {
                    defaults.put( name, value );
                } else if (type.equals( "CHECKBOX" ) && parameters[i].getNamedItem( "checked" ) != null) {
                    if (value.length() == 0) value = "on";
                    String[] currentDefaults = (String[]) defaults.get( name );
                    if (currentDefaults == null) {
                        defaults.put( name, new String[] { value } );
                    } else {
                        defaults.put( name, withNewValue( currentDefaults, value ) );
                    }
                }
            }
            HTMLSelectElement[] selections = getSelections();
            for (int i = 0; i < selections.length; i++) {
                defaults.put( selections[i].getName(), selections[i].getSelected() );
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
                String name  = parameters[i].getName();
                String value = parameters[i].getValue();
                String type  = getValue( parameters[i].getNamedItem( "type" ) ).toUpperCase();
                if (type == null || type.length() == 0) type = "TEXT";
                if (!parameters[i].isReadOnly()) continue;

                if (type.equals( "TEXT" ) || type.equals( "HIDDEN" ) || type.equals( "PASSWORD" )) {
                    required.put( name, value );
                } else if (type.equals( "RADIO" ) && parameters[i].getNamedItem( "checked" ) != null) {
                    required.put( name, value );
                } else if (type.equals( "CHECKBOX" ) && parameters[i].getNamedItem( "checked" ) != null) {
                    if (value.length() == 0) value = "on";
                    String[] currentDefaults = (String[]) required.get( name );
                    if (currentDefaults == null) {
                        required.put( name, new String[] { value } );
                    } else {
                        required.put( name, withNewValue( currentDefaults, value ) );
                    }
                }
            }
            HTMLSelectElement[] selections = getSelections();
            for (int i = 0; i < selections.length; i++) {
                if (selections[i].isDisabled()) required.put( selections[i].getName(), selections[i].getSelected() );
            }
            _required = required;
        }
        return _required;
    }


    private Hashtable getParameterOptions() {
        if (_options == null) {
            Hashtable options = new Hashtable();
            HTMLSelectElement[] selections = getSelections();
            for (int i = 0; i < selections.length; i++) {
                options.put( selections[i].getName(), selections[i].getOptions() );
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
                String name  = parameters[i].getName();
                String value = parameters[i].getValue();
                String type  = getValue( parameters[i].getNamedItem( "type" ) ).toUpperCase();
                if (type == null || type.length() == 0) type = "TEXT";

                if ((type.equals( "RADIO" ) || type.equals( "CHECKBOX" ))
                     && (!parameters[i].isReadOnly()
                         || parameters[i].getNamedItem( "checked" ) != null)) {
                    if (value.length() == 0 && type.equals( "CHECKBOX" )) value = "on";
                    String[] radioOptions = (String[]) options.get( name );
                    if (radioOptions == null) {
                        options.put( name, new String[] { value } );
                    } else {
                        options.put( name, withNewValue( radioOptions, value ) );
                    }
                }
            }
            HTMLSelectElement[] selections = getSelections();
            for (int i = 0; i < selections.length; i++) {
                options.put( selections[i].getName(), selections[i].getOptionValues() );
            }
            _optionValues = options;
        }
        return _optionValues;
    }


    /**
     * Adds a string to an array of strings and returns the result.
     **/
    private String[] withNewValue( String[] group, String value ) {
        String[] result = new String[ group.length+1 ];
        System.arraycopy( group, 0, result, 0, group.length );
        result[ group.length ] = value;
        return result;
    }


    private Hashtable getParameterTypes() {
        if (_dataTypes == null) {
            FormControl[] parameters = getParameters();
            Hashtable types = new Hashtable();
            _textParameterCounts = new Hashtable();
            for (int i = 0; i < parameters.length; i++) {
                String name  = parameters[i].getName();
                String type  = getValue( parameters[i].getNamedItem( "type" ) ).toUpperCase();
                if (type == null || type.length() == 0) type = "TEXT";

                if (type.equals( "RADIO" )) {
                    types.put( name, TYPE_SCALAR );
                } else if (type.equals( "CHECKBOX" )) {
                    types.put( name, TYPE_MULTI_VALUED );
                } else if (type.equals( "FILE" )) {
                    types.put( name, TYPE_FILE );
                } else if (type.equals( "TEXT" ) || type.equals( "HIDDEN" ) || type.equals( "PASSWORD" )) {
                    types.put( name, TYPE_TEXT );
                    Integer oldCount = (Integer) _textParameterCounts.get( name );
                    if (oldCount == null) {
                        _textParameterCounts.put( name, ONE_PARAMETER );
                    } else {
                        _textParameterCounts.put( name, new Integer( oldCount.intValue() + 1 ) );
                    }
                }
            }
            HTMLSelectElement[] selections = getSelections();
            for (int i = 0; i < selections.length; i++) {
                types.put( selections[i].getName(), selections[i].isMultiSelect() ? TYPE_MULTI_VALUED : TYPE_SCALAR );
            }
            _dataTypes = types;
        }
        return _dataTypes;
    }


    /**
     * Returns an array of select control descriptors for this form.
     **/
    private HTMLSelectElement[] getSelections() {
        if (_selections == null) {
            NodeList nl = ((Element) getNode()).getElementsByTagName( "select" );
            HTMLSelectElement[] result = new HTMLSelectElement[ nl.getLength() ];
            for (int i = 0; i < result.length; i++) {
                result[i] = new HTMLSelectElement( nl.item(i) );
            }
            _selections = result;
        }
        return _selections;
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


    class HTMLSelectElement {
        HTMLSelectElement( Node node ) {
            if (!node.getNodeName().equalsIgnoreCase( "select" )) {
                throw new RuntimeException( "Not a select element" );
            }
            _node = node;
        }
    
    
        private Node _node;
    
    
        String getName() {
            NamedNodeMap nnm = _node.getAttributes();
            return getValue( nnm.getNamedItem( "name" ) );
        }


        String[] getSelected() {
            Vector selected = new Vector();
            NodeList nl = ((Element) _node).getElementsByTagName( "option" );
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getAttributes().getNamedItem( "selected" ) != null) {
                    selected.addElement( getOptionValue( nl.item(i) ) );
                }
            }

            if (!isMultiSelect() && selected.size() == 0 && nl.getLength() > 0) {
                selected.addElement( getOptionValue( nl.item(0) ) );
            }

            String[] result = new String[ selected.size() ];
            selected.copyInto( result );
            return result;
        }


        String[] getOptions() {
            Vector options = new Vector();
            NodeList nl = ((Element) _node).getElementsByTagName( "option" );
            for (int i = 0; i < nl.getLength(); i++) {
                options.addElement( getValue( nl.item(i).getFirstChild() ) );
            }
            String[] result = new String[ options.size() ];
            options.copyInto( result );
            return result;
        }


        String[] getOptionValues() {
            Vector options = new Vector();
            NodeList nl = ((Element) _node).getElementsByTagName( "option" );
            for (int i = 0; i < nl.getLength(); i++) {
                options.addElement( getOptionValue( nl.item(i) ) );
            }
            String[] result = new String[ options.size() ];
            options.copyInto( result );
            return result;
        }


        boolean isMultiSelect() {
            return _node.getAttributes().getNamedItem( "multiple" ) != null;
        }


        boolean isDisabled() {
            return _node.getAttributes().getNamedItem( "disabled" ) != null;
        }


        private String getOptionValue( Node optionNode ) {
            NamedNodeMap nnm = optionNode.getAttributes();
            if (nnm.getNamedItem( "value" ) != null) {
                return getValue( nnm.getNamedItem( "value" ) );
            } else {
                return getValue( optionNode.getFirstChild() );
            }
        }


    }

}



class FormControl {
    protected Node _node;


    FormControl( Node node ) {
        _node = node;
    }


    Node getNamedItem( String attributeName ) {
        return _node.getAttributes().getNamedItem( attributeName );
    }


    /**
     * Returns the name of this control. If no name is specified, defaults to the empty string.
     **/
    String getName() {
        return NodeUtils.getNodeAttribute( _node, "name" );
    }


    /**
     * Returns the value of this control. If no value is specified, defaults to the empty string.
     **/
    String getValue() {
        return NodeUtils.getNodeAttribute( _node, "value" );
    }


    /**
     * Returns true if this control is read-only.
     **/
    boolean isReadOnly() {
        return _node.getAttributes().getNamedItem( "readonly" ) != null;
    }


    static FormControl newFormParameter( Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        } else if (node.getNodeName().equals( "textarea" )) {
            return new TextAreaFormControl( node );
        } else if (!node.getNodeName().equals( "input" )) {
            return null;
        } else {
            NamedNodeMap nnm = node.getAttributes();
            Node n = nnm.getNamedItem( "type" );
            if (n == null) {
                return new FormControl( node );
            } else if (n.getNodeValue().equalsIgnoreCase( "submit" )) {
                return null;
            } else if (n.getNodeValue().equalsIgnoreCase( "reset" )) {
                return null;
            } else {
                return new FormControl( node );
            }
        }
    }

}


class TextAreaFormControl extends FormControl {

    public TextAreaFormControl( Node node ) {
        super( node );

        if (!node.getNodeName().equalsIgnoreCase( "textarea" )) {
            throw new RuntimeException( "Not a textarea element" );
        }
    }


    String getValue() {
        return NodeUtils.asText( _node.getChildNodes() );
    }

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



