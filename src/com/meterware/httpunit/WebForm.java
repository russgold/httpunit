package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
import com.meterware.httpunit.scripting.NamedDelegate;

import java.io.IOException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * This class represents a form in an HTML page. Users of this class may examine the parameters
 * defined for the form, the structure of the form (as a DOM), or the text of the form. They
 * may also create a {@link WebRequest} to simulate the submission of the form.
 **/
public class WebForm extends WebRequestSource {
    private static final FormParameter UNKNOWN_PARAMETER = new FormParameter();
    private Button[] _buttons;


    /**
     * Submits this form using the web client from which it was originally obtained.
     **/
    public WebResponse submit() throws IOException, SAXException {
        return submit( getDefaultButton() );
    }


    /**
     * Submits this form using the web client from which it was originally obtained.
     **/
    WebResponse submit( SubmitButton button ) throws IOException, SAXException {
        String event = NodeUtils.getNodeAttribute( getNode(), "onsubmit" );
        if (event.length() == 0 || getScriptableObject().doEvent( event )) return submitRequest( getRequest( button ) );
        return getBaseResponse();
    }


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
        return getDestination();
     }


    /**
     * Returns true if a parameter with given name exists in this form.
     **/
    public boolean hasParameterNamed( String soughtName ) {
        return getFormParameters().containsKey( soughtName );
    }


    /**
     * Returns true if a parameter starting with a given name exists,
     **/
    public boolean hasParameterStartingWithPrefix( String prefix ) {
        String[] names = getParameterNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].startsWith( prefix )) return true;
        }
        return false;
    }


    /**
     * Returns an array containing all of the buttons defined for this form.
     **/
    public Button[] getButtons() {
        if (_buttons == null) {
            FormControl[] controls = getFormControls();
            ArrayList buttonList = new ArrayList();
            for (int i = 0; i < controls.length; i++) {
                FormControl control = controls[ i ];
                if (control instanceof Button) buttonList.add( control );
            }
            _buttons = (Button[]) buttonList.toArray( new Button[ buttonList.size() ] );
        }
        return _buttons;
    }


    /**
     * Returns the button with the specified ID
     */
    public Button getButtonWithID( String buttonID ) {
        Button[] buttons = getButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getID().equalsIgnoreCase( buttonID )) return buttons[i];
        }
        return null;
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


    /**
     * Creates and returns a web request which will simulate the submission of this form by pressing the specified button.
     * If the button is null, simulates the pressing of the default button.
     **/
    public WebRequest getRequest( SubmitButton button ) {
        return getRequest( button, 0, 0 );
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form by pressing the specified button.
     * If the button is null, simulates the pressing of the default button.
     **/
    public WebRequest getRequest( SubmitButton button, int x, int y ) {
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

        SubmitButton[] buttons = getSubmitButtons();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setPressed( false );
        }
        button.setPressed( true );

        if (getMethod().equalsIgnoreCase( "post" )) {
            return new PostMethodWebRequest( this, button, x, y );
        } else {
            return new GetMethodWebRequest( this, button, x, y );
        }
    }


    private WebRequest getScriptedSubmitRequest() {
        SubmitButton[] buttons = getSubmitButtons();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setPressed( false );
        }

        if (getMethod().equalsIgnoreCase( "post" )) {
            return new PostMethodWebRequest( this );
        } else {
            return new GetMethodWebRequest( this );
        }

    }


    /**
     * Returns the default value of the named parameter.  If the parameter does not exist returns null.
     **/
    public String getParameterValue( String name ) {
        String[] values = getParameterValues( name );
        return values.length == 0 ? null : values[0];
    }


    /**
     * Returns the displayed options defined for the specified parameter name.
     **/
    public String[] getOptions( String name ) {
        return getParameter( name ).getOptions();
    }


    /**
     * Returns the option values defined for the specified parameter name.
     **/
    public String[] getOptionValues( String name ) {
        return getParameter( name ).getOptionValues();
    }


    /**
     * Returns true if the named parameter accepts multiple values.
     **/
    public boolean isMultiValuedParameter( String name ) {
        return getParameter( name ).isMultiValuedParameter();
    }


    /**
     * Returns the number of text parameters in this form with the specified name.
     **/
    public int getNumTextParameters( String name ) {
        return getParameter( name ).getNumTextParameters();
    }


    /**
     * Returns true if the named parameter accepts free-form text.
     **/
    public boolean isTextParameter( String name ) {
        return getParameter( name ).isTextParameter();
    }


    void setSubmitAsMime( boolean mimeEncoded ) {
        throw new IllegalStateException( "May not change the encoding for a validated request created from a form" );
    }


    /**
     * Returns true if this form is to be submitted using mime encoding (the default is URL encoding).
     **/
    public boolean isSubmitAsMime() {
        return "multipart/form-data".equalsIgnoreCase( NodeUtils.getNodeAttribute( getNode(), "enctype" ) );
    }


    /**
     * Resets all parameters to their initial values.
     */
    public void reset() {
        String event = NodeUtils.getNodeAttribute( getNode(), "onreset" );
        if (event.length() == 0 || getScriptableObject().doEvent( event )) resetControls();
    }


    private void resetControls() {
        FormControl[] controls = getFormControls();
        for (int i = 0; i < controls.length; i++) {
            controls[i].reset();
        }
    }


    /**
     * Returns an object which provides scripting access to this form.
     **/
    public Scriptable getScriptableObject() {
        if (_scriptable == null) _scriptable = new Scriptable();
        return _scriptable;
    }


//---------------------------------- WebRequestSource methods --------------------------------

    /**
     * Returns the character set encoding for this form.
     **/
    public String getCharacterSet() {
        return _characterSet;
    }


    /**
     * Returns true if the named parameter accepts files for upload.
     **/
    public boolean isFileParameter( String name ) {
        return getParameter( name ).isFileParameter();
    }


    /**
     * Returns an array containing the names of the parameters defined for this form.
     **/
    public String[] getParameterNames() {
        ArrayList parameterNames = new ArrayList( getFormParameters().keySet() );
        return (String[]) parameterNames.toArray( new String[ parameterNames.size() ] );
    }


    /**
     * Returns the multiple default values of the named parameter.
     **/
    public String[] getParameterValues( String name ) {
        final FormParameter parameter = getParameter( name );
        return parameter.getValues();
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form with an unnamed submit button.
     **/
    public WebRequest getRequest() {
        return getRequest( (SubmitButton) null );
    }


    /**
     * Returns the scriptable delegate.
     */

    ScriptableDelegate getScriptableDelegate() {
        return getScriptableObject();
    }


    /**
     * Records a parameter defined by including it in the destination URL.
     **/
    protected void addPresetParameter( String name, String value ) {
        _presets.add( new PresetFormParameter( name, value ) );
    }


//---------------------------------- ParameterHolder methods --------------------------------


    /**
     * Specifies the position at which an image button (if any) was clicked.
     **/
    public void selectImageButtonPosition( SubmitButton imageButton, int x, int y ) {
        imageButton.setLocation( x, y );
    }


    /**
     * Iterates through the fixed, predefined parameters in this holder, recording them in the supplied parameter processor.\
     * These parameters always go on the URL, no matter what encoding method is used.
     **/

    void recordPredefinedParameters( ParameterProcessor processor ) throws IOException {
        FormControl[] controls = getPresetParameters();
        for (int i = 0; i < controls.length; i++) {
            controls[i].addValues( processor, getCharacterSet() );
        }
    }


    /**
     * Iterates through the parameters in this holder, recording them in the supplied parameter processor.
     **/
    void recordParameters( ParameterProcessor processor ) throws IOException {
        FormControl[] controls = getFormControls();
        for (int i = 0; i < controls.length; i++) {
            controls[i].addValues( processor, getCharacterSet() );
        }
    }


    /**
     * Removes a parameter name from this collection.
     **/
    public void removeParameter( String name ) {
        setParameter( name, NO_VALUES );
    }


    /**
     * Sets the multiple values of a file upload parameter in a web request.
     **/
    public void setParameter( String name, UploadFileSpec[] files ) {
        FormParameter parameter = getParameter( name );
        if (parameter == null) throw new NoSuchParameterException( name );
        parameter.setFiles( files );
    }


    /**
     * Sets the value of a parameter in this form.
     **/
    public void setParameter( String name, String value ) {
        setParameter( name, new String[] { value } );
    }


    public void setParameter( String name, final String[] values ) {
        FormParameter parameter = getParameter( name );
        if (parameter == UNKNOWN_PARAMETER) throw new NoSuchParameterException( name );
        parameter.setValues( values );
    }


    public class Scriptable extends ScriptableDelegate implements NamedDelegate {
        public String getAction() { return WebForm.this.getAction(); }
        public void setAction( String newAction ) { setDestination( newAction ); }


        public void submit() throws IOException, SAXException {
            submitRequest( getScriptedSubmitRequest() );
        }


        public void reset() throws IOException, SAXException {
            resetControls();
        }


        public String getName() {
            return WebForm.this.getName();
        }


        public Object get( String propertyName ) {
            final FormParameter parameter = getParameter( propertyName );
            return parameter == UNKNOWN_PARAMETER ? null : parameter.getScriptableObject();
        }


        public void setParameterValue( String name, String value ) {
            final Object scriptableObject = getParameter( name ).getScriptableObject();
            if (scriptableObject instanceof ScriptableDelegate) {
                ((ScriptableDelegate) scriptableObject).set( "value", value );
            } else if (scriptableObject instanceof ScriptableDelegate[]) {
                ((ScriptableDelegate[]) scriptableObject)[0].set( "value", value );
            }
        }


        public ScriptableDelegate[] getElementDelegates() {
            FormControl[] controls = getFormControls();
            ScriptableDelegate[] result = new ScriptableDelegate[ controls.length ];
            for (int i = 0; i < result.length; i++) {
                result[i] = controls[i].getScriptableObject();
            }
            return result;
        }
    }


//---------------------------------- package members --------------------------------

    /**
     * Contructs a web form given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebForm( WebResponse response, URL baseURL, String frameName, Node node, String characterSet ) {
        super( response, node, baseURL, NodeUtils.getNodeAttribute( node, "action" ), frameName );
        _characterSet = characterSet;
    }


//---------------------------------- private members --------------------------------

    private final static String[] NO_VALUES = new String[0];

    /** The attributes of the form parameters. **/
    private FormControl[] _parameters;

    /** The submit buttons in this form. **/
    private SubmitButton[] _submitButtons;

    /** The character set in which the form will be submitted. **/
    private String         _characterSet;

    /** A map of parameter names to form parameter objects. **/
    private Map            _formParameters;

    /** The Scriptable object associated with this form. **/
    private Scriptable _scriptable;

    private Vector _buttonVector;

    private FormControl[] _presetParameters;
    private ArrayList     _presets;


    private Object getObject( String name ) {
        return getParameter( name ).getScriptableObject();
    }


    private SubmitButton getDefaultButton() {
        if (getSubmitButtons().length == 1) {
            return getSubmitButtons()[0];
        } else {
            return getSubmitButton( "" );
        }
    }


    private Vector getSubmitButtonVector() {
        if (_buttonVector == null) {
            _buttonVector = new Vector();
            FormControl[] controls = getFormControls();
            for (int i = 0; i < controls.length; i++) {
                FormControl control = controls[ i ];
                if (control instanceof SubmitButton) _buttonVector.add( control );
            }

            if (_buttonVector.isEmpty()) _buttonVector.addElement( new SubmitButton( this ) );
        }
        return _buttonVector;
    }


    private FormControl[] getPresetParameters() {
        if (_presetParameters == null) {
            _presets = new ArrayList();
            loadDestinationParameters();
            _presetParameters = (FormControl[]) _presets.toArray( new FormControl[ _presets.size() ] );
        }
        return _presetParameters;
    }


    /**
     * Returns an array of form parameter attributes for this form.
     **/
    private FormControl[] getFormControls() {
        if (_parameters == null) {
            Vector list = new Vector();
            if (getNode().hasChildNodes()) addFormParametersToList( getNode().getChildNodes(), list );
            _parameters = new FormControl[ list.size() ];
            list.copyInto( _parameters );
        }

        return _parameters;
    }


    private FormParameter getParameter( String name ) {
        final FormParameter parameter = ((FormParameter) getFormParameters().get( name ));
        return parameter != null ? parameter : UNKNOWN_PARAMETER;
    }


    /**
     * Returns a map of parameter name to form parameter objects. Each form parameter object represents the set of form
     * controls with a particular name. Unnamed parameters are ignored.
     */
    private Map getFormParameters() {
        if (_formParameters == null) {
            _formParameters = new HashMap();
            loadFormParameters( getPresetParameters() );
            loadFormParameters( getFormControls() );
        }
        return _formParameters;
    }


    private void loadFormParameters( FormControl[] controls ) {
        for (int i = 0; i < controls.length; i++) {
            if (controls[i].getName().length() == 0) continue;
            FormParameter parameter = (FormParameter) _formParameters.get( controls[i].getName() );
            if (parameter == null) {
                parameter = new FormParameter();
                _formParameters.put( controls[i].getName(), parameter );
            }
            parameter.addControl( controls[i] );
        }
    }


    private void addFormParametersToList( NodeList children, Vector list ) {
        for (int i = 0; i < children.getLength(); i++) {
            addFormParametersToList( children.item(i), list );
        }
    }


    private void addFormParametersToList( Node child, Vector list ) {
        final FormControl formParameter = FormControl.newFormParameter( this, child );
        if (formParameter != null) {
            list.addElement( formParameter );
        } else if (child.hasChildNodes()) {
            addFormParametersToList( child.getChildNodes(), list );
        }
    }

}


class FormParameter {

    void addControl( FormControl control ) {
        _controls = null;
        if (_name == null) _name = control.getName();
        if (!_name.equalsIgnoreCase( control.getName() )) throw new RuntimeException( "all controls should have the same name" );
        if (control.isExclusive()) {
            getRadioGroup().addRadioButton( (RadioButtonFormControl) control );
        } else {
            _controlList.add( control );
        }
    }


    private FormControl[] getControls() {
        if (_controls == null) _controls = (FormControl[]) _controlList.toArray( new FormControl[ _controlList.size() ] );
        return _controls;
    }


    Object getScriptableObject() {
        if (getControls().length == 1) {
            return getControls()[0].getDelegate();
        } else {
            ArrayList list = new ArrayList();
            for (int i = 0; i < _controls.length; i++) {
                FormControl control = _controls[i];
                if (control instanceof CheckboxFormControl) {
                    list.add( control.getScriptableObject() );
                }
            }
            return list.toArray( new ScriptableDelegate[ list.size() ] );
        }
    }


    String[] getValues() {
        ArrayList valueList = new ArrayList();
        FormControl[] controls = getControls();
        for (int i = 0; i < controls.length; i++) {
            valueList.addAll( Arrays.asList( controls[i].getValues() ) );
        }
        return (String[]) valueList.toArray( new String[ valueList.size() ] );
    }


    void setValues( String[] values ) {
        ArrayList list = new ArrayList( values.length );
        list.addAll( Arrays.asList( values ) );
        for (int i = 0; i < getControls().length; i++) getControls()[i].claimRequiredValues( list );
        for (int i = 0; i < getControls().length; i++) getControls()[i].claimUniqueValue( list );
        for (int i = 0; i < getControls().length; i++) getControls()[i].claimValue( list );
        if (!list.isEmpty()) throw new UnusedParameterValueException( _name, (String) list.get(0) );
    }


    void setFiles( UploadFileSpec[] fileArray ) {
        ArrayList list = new ArrayList( fileArray.length );
        list.addAll( Arrays.asList( fileArray ) );
        for (int i = 0; i < getControls().length; i++) getControls()[i].claimUploadSpecification( list );
        if (!list.isEmpty()) throw new UnusedUploadFileException( _name, fileArray.length - list.size(), fileArray.length );
    }


    String[] getOptions() {
        ArrayList optionList = new ArrayList();
        FormControl[] controls = getControls();
        for (int i = 0; i < controls.length; i++) {
            optionList.addAll( Arrays.asList( controls[i].getDisplayedOptions() ) );
        }
        return (String[]) optionList.toArray( new String[ optionList.size() ] );
    }


    String[] getOptionValues() {
        ArrayList valueList = new ArrayList();
        for (int i = 0; i < getControls().length; i++) {
            valueList.addAll( Arrays.asList( getControls()[i].getOptionValues() ) );
        }
        return (String[]) valueList.toArray( new String[ valueList.size() ] );
    }


    boolean isMultiValuedParameter() {
        FormControl[] controls = getControls();
        for (int i = 0; i < controls.length; i++) {
            if (controls[i].isMultiValued()) return true;
            if (!controls[i].isExclusive() && controls.length > 1) return true;
        }
        return false;
    }


    int getNumTextParameters() {
        int result = 0;
        FormControl[] controls = getControls();
        for (int i = 0; i < controls.length; i++) {
            if (controls[i].isTextControl()) result++;
        }
        return result;
    }


    boolean isTextParameter() {
        FormControl[] controls = getControls();
        for (int i = 0; i < controls.length; i++) {
            if (controls[i].isTextControl()) return true;
        }
        return false;
    }


    boolean isFileParameter() {
        FormControl[] controls = getControls();
        for (int i = 0; i < controls.length; i++) {
            if (controls[i].isFileParameter()) return true;
        }
        return false;
    }


    private FormControl[] _controls;
    private ArrayList _controlList = new ArrayList();
    private RadioGroupFormControl _group;
    private String _name;

    private RadioGroupFormControl getRadioGroup() {
        if (_group == null) {
            _group = new RadioGroupFormControl();
            _controlList.add( _group );
        }
        return _group;
    }
}


//========================================== class PresetFormParameter =================================================


    class PresetFormParameter extends FormControl {

        PresetFormParameter( String name, String value ) {
            _name   = name;
            _value  = value;
        }


        /**
         * Returns the name of this control..
         **/
        public String getName() {
            return _name;
        }


        /**
         * Returns true if this control is read-only.
         **/
        public boolean isReadOnly() {
            return true;
        }


        /**
         * Returns true if this control accepts free-form text.
         **/
        public boolean isTextControl() {
            return true;
        }


        /**
         * Remove any required values for this control from the list, throwing an exception if they are missing.
         **/
        void claimRequiredValues( List values ) {
            if (_value != null) claimValueIsRequired( values, _value );
        }


        /**
         * Returns the current value(s) associated with this control. These values will be transmitted to the server
         * if the control is 'successful'.
         **/
        public String[] getValues() {
            if (_values == null) _values = new String[] { _value };
            return _values;
        }


        void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
            processor.addParameter( _name, _value, characterSet );
        }


        private String   _name;
        private String   _value;
        private String[] _values;
    }


//===========================---===== exception class NoSuchParameterException =========================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class NoSuchParameterException extends IllegalRequestParameterException {


    NoSuchParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "No parameter named '" + _parameterName + "' is defined in the form";
    }


    private String _parameterName;

}


//============================= exception class UnusedParameterValueException ======================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class UnusedParameterValueException extends IllegalRequestParameterException {


    UnusedParameterValueException( String parameterName, String badValue ) {
        _parameterName = parameterName;
        _badValue      = badValue;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append( "Attempted to assign to parameter '" ).append( _parameterName );
        sb.append( "' the extraneous value '" ).append( _badValue ).append( "'." );
        return sb.toString();
    }


    private String   _parameterName;
    private String   _badValue;
}


//============================= exception class UnusedUploadFileException ======================================


/**
 * This exception is thrown on an attempt to upload more files than permitted by the form.
 **/
class UnusedUploadFileException extends IllegalRequestParameterException {


    UnusedUploadFileException( String parameterName, int numFilesExpected, int numFilesSupplied ) {
        _parameterName = parameterName;
        _numExpected   = numFilesExpected;
        _numSupplied   = numFilesSupplied;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer( HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE );
        sb.append( "Attempted to upload " ).append( _numSupplied ).append( " files using parameter '" ).append( _parameterName );
        if (_numExpected == 0) {
            sb.append( "' which is not a file parameter." );
        } else {
            sb.append( "' which only has room for " ).append( _numExpected ).append( '.' );
        }
        return sb.toString();
    }


    private String _parameterName;
    private int    _numExpected;
    private int    _numSupplied;
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



