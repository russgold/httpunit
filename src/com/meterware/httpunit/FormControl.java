package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2001-2002, Russell Gold
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
import com.meterware.httpunit.scripting.SelectionOptions;
import com.meterware.httpunit.scripting.SelectionOption;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;


/**
 * Represents a control in an HTML form.
 **/
abstract class FormControl {

    final static String[] NO_VALUE = new String[0];

    private       String  _name;
    private final String  _valueAttribute;
    private final boolean _readOnly;
    private final boolean _disabled;


    FormControl() {
        _name           = "";
        _valueAttribute = "";
        _readOnly       = false;
        _disabled       = false;
    }


    FormControl( Node node ) {
        _name           = NodeUtils.getNodeAttribute( node, "name" );
        _valueAttribute = NodeUtils.getNodeAttribute( node, "value" );
        _readOnly       = NodeUtils.isNodeAttributePresent( node, "readonly" );
        _disabled       = NodeUtils.isNodeAttributePresent( node, "disabled" );
    }


    /**
     * Returns the name of this control. If no name is specified, defaults to the empty string.
     **/
    public String getName() {
        return _name;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    abstract public String[] getValues();


    /**
     * Returns a scriptable object which can act as a proxy for this control.
     */
    public ScriptableObject getScriptableObject() {
        return new Scriptable();
    }


    /**
     * Returns the values permitted in this control. Does not apply to text or file controls.
     **/
    public String[] getOptionValues() {
        return NO_VALUE;
    }


    /**
     * Returns the list of values displayed by this control, if any.
     **/
    public String[] getDisplayedOptions() {
        return NO_VALUE;
    }


    /**
     * Returns true if this control is read-only.
     **/
    public boolean isReadOnly() {
        return _readOnly;
    }


    /**
     * Returns true if this control is disabled, meaning that it will not send a value to the server as part of a request.
     **/
    public boolean isDisabled() {
        return _disabled;
    }


    /**
     * Returns true if this control accepts free-form text.
     **/
    public boolean isTextControl() {
        return false;
    }


    /**
     * Returns true if only one control of this kind can have a value. This is true for radio buttons.
     **/
    public boolean isExclusive() {
        return false;
    }


    /**
     * Returns true if a single control can have multiple values.
     **/
    public boolean isMultiValued() {
        return false;
    }


    /**
     * Returns true if this control accepts a file for upload.
     **/
    public boolean isFileParameter() {
        return false;
    }


    abstract void addValues( ParameterProcessor processor, String characterSet ) throws IOException;


    /**
     * Remove any required values for this control from the list, throwing an exception if they are missing.
     **/
    void claimRequiredValues( List values ) {
    }


    /**
     * Sets this control to the next compatible value from the list, removing it from the list.
     **/
    void claimValue( List values ) {
    }


    /**
     * Sets this control to the next compatible value from the list, removing it from the list.
     **/
    void claimUniqueValue( List values ) {
    }


    /**
     * Specifies a file to be uploaded via this control.
     **/
    void claimUploadSpecification( List files ) {
    }


    /**
     * Resets this control to its initial value.
     **/
    void reset() {
    }


    /**
     * Returns the default value of this control in the form. If no value is specified, defaults to the empty string.
     **/
    protected String getValueAttribute() {
        return _valueAttribute;
    }


    /**
     * Removes the specified required value from the list of values, throwing an exception if it is missing.
     **/
    final protected void claimValueIsRequired( List values, final String value ) {
        if (!values.contains( value )) throw new MissingParameterValueException( getName(), value, (String[]) values.toArray( new String[ values.size() ]) );
        values.remove( value );
    }


    static FormControl newFormParameter( WebForm form, Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        } else if (node.getNodeName().equals( "textarea" )) {
            return new TextAreaFormControl( node );
        } else if (node.getNodeName().equals( "select" )) {
            return new SelectionFormControl( node );
        } else if (node.getNodeName().equals( "button" )) {
            final String type = NodeUtils.getNodeAttribute( node, "type", "submit" );
            if (type.equalsIgnoreCase( "submit" )) {
                return new SubmitButton( form, node );
            } else {
                return null;
            }
        } else if (!node.getNodeName().equals( "input" )) {
            return null;
        } else {
            final String type = NodeUtils.getNodeAttribute( node, "type", "text" );
            if (type.equalsIgnoreCase( "text" ) || type.equalsIgnoreCase( "password" )) {
                return new TextFieldFormControl( node );
            } else if (type.equalsIgnoreCase( "hidden" )) {
                return new HiddenFieldFormControl( node );
            } else if (type.equalsIgnoreCase( "radio" )) {
                return new RadioButtonFormControl( node );
            } else if (type.equalsIgnoreCase( "checkbox" )) {
                return new CheckboxFormControl( node );
            } else if (type.equalsIgnoreCase( "submit" ) || type.equalsIgnoreCase( "image" )) {
                return new SubmitButton( form, node );
            } else if (type.equalsIgnoreCase( "file" )) {
                return new FileSubmitFormControl( node );
            } else {
                return null;
            }
        }
    }


    class Scriptable extends ScriptableObject {
    }

}


abstract
class BooleanFormControl extends FormControl {

    private boolean  _isChecked;
    private String[] _value = new String[1];

    private final boolean _isCheckedDefault;



    public BooleanFormControl( Node node ) {
        super( node );
        _isChecked      = _isCheckedDefault = NodeUtils.isNodeAttributePresent( node, "checked" );
    }


    boolean isChecked() {
        return _isChecked;
    }


    public void setChecked( boolean checked ) {
        _isChecked = checked;
    }


    void reset() {
        _isChecked = _isCheckedDefault;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    public String[] getValues() {
        return isChecked() ? toArray( getQueryValue() ) : NO_VALUE;
    }


    /**
     * Returns the values permitted in this control.
     **/
    public String[] getOptionValues() {
        return (isReadOnly() && !isChecked()) ? NO_VALUE : toArray( getQueryValue() );
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (isChecked()) processor.addParameter( getName(), getQueryValue(), characterSet );
    }


    /**
     * Remove any required values for this control from the list, throwing an exception if they are missing.
     **/
    void claimRequiredValues( List values ) {
        if (isValueRequired()) claimValueIsRequired( values, getQueryValue() );
    }


    protected boolean isValueRequired() {
        return isReadOnly() && isChecked();
    }


    abstract String getQueryValue();


    private String[] toArray( String value ) {
        _value[0] = value;
        return _value;
    }
}



class RadioButtonFormControl extends BooleanFormControl {

    public RadioButtonFormControl( Node node ) {
        super( node );
    }


    /**
     * Returns true if only one control of this kind can have a value.
     **/
    public boolean isExclusive() {
        return true;
    }


    String getQueryValue() {
        return getValueAttribute();
    }
}


class RadioGroupFormControl extends FormControl {

    private List _buttonList = new ArrayList();
    private RadioButtonFormControl[] _buttons;
    private String[] _allowedValues;


    public RadioGroupFormControl() {
    }

    void addRadioButton( RadioButtonFormControl control ) {
        _buttonList.add( control );
        _buttons = null;
        _allowedValues = null;
    }


    public String[] getValues() {
        for (int i = 0; i < getButtons().length; i++) {
            if (getButtons()[i].isChecked()) return getButtons()[i].getValues();
        }
        return NO_VALUE;
    }


    /**
     * Returns the option values defined for this radio button group.
     **/
    public String[] getOptionValues() {
        ArrayList valueList = new ArrayList();
        FormControl[] buttons = getButtons();
        for (int i = 0; i < buttons.length; i++) {
            valueList.addAll( Arrays.asList( buttons[i].getOptionValues() ) );
        }
        return (String[]) valueList.toArray( new String[ valueList.size() ] );
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        for (int i = 0; i < getButtons().length; i++) getButtons()[i].addValues( processor, characterSet );
    }


    /**
     * Remove any required values for this control from the list, throwing an exception if they are missing.
     **/
    void claimRequiredValues( List values ) {
        for (int i = 0; i < getButtons().length; i++) {
            getButtons()[i].claimRequiredValues( values );
        }
    }


    void claimUniqueValue( List values ) {
        int matchingButtonIndex = -1;
        for (int i = 0; i < getButtons().length && matchingButtonIndex < 0; i++) {
            if (!getButtons()[i].isReadOnly() && values.contains( getButtons()[i].getQueryValue() )) matchingButtonIndex = i;
        }
        if (matchingButtonIndex <0) throw new IllegalParameterValueException( getButtons()[0].getName(), (String) values.get(0), getAllowedValues() );

        for (int i = 0; i < getButtons().length; i++) {
            if (!getButtons()[i].isReadOnly()) getButtons()[i].setChecked( i == matchingButtonIndex );
        }
        values.remove( getButtons()[ matchingButtonIndex ].getQueryValue() );
    }


    void reset() {
        for (int i = 0; i < getButtons().length; i++) getButtons()[i].reset();
    }


    private String[] getAllowedValues() {
        if (_allowedValues == null) {
            _allowedValues = new String[ getButtons().length ];
            for (int i = 0; i < _allowedValues.length; i++) {
                _allowedValues[i] = getButtons()[i].getQueryValue();
            }
        }
        return _allowedValues;
    }


    private RadioButtonFormControl[] getButtons() {
        if (_buttons == null) _buttons = (RadioButtonFormControl[]) _buttonList.toArray( new RadioButtonFormControl[ _buttonList.size() ] );
        return _buttons;
    }
}


class CheckboxFormControl extends BooleanFormControl {


    public CheckboxFormControl( Node node ) {
        super( node );
    }


    void claimUniqueValue( List values ) {
        if (isValueRequired()) return;
        setChecked( values.contains( getQueryValue() ) );
        if (isChecked()) values.remove( getQueryValue() );
    }


    public ScriptableObject getScriptableObject() {
        return new Scriptable();
    }


    class Scriptable extends FormControl.Scriptable {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "checked" )) {
                return isChecked() ? Boolean.TRUE : Boolean.FALSE;
            } else {
                return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "checked" )) {
                setChecked( value instanceof Boolean && ((Boolean) value).booleanValue() );
            } else {
                super.set( propertyName, value );
            }
        }
    }


    String getQueryValue() {
        final String value = getValueAttribute();
        return value.length() == 0 ? "on" : value;
    }


    private void addValue( Hashtable valueMap, String name, String value ) {
        String[] currentValues = (String[]) valueMap.get( name );
        if (currentValues == null) {
            valueMap.put( name, new String[] { value } );
        } else {
            valueMap.put( name, withNewValue( currentValues, value ) );
        }
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

}


class TextFormControl extends FormControl {

    private String[] _value = new String[1];
    private String[] _defaultValue;


    public TextFormControl( Node node, String defaultValue ) {
        super( node );
        _defaultValue = new String[] { defaultValue };
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    public String[] getValues() {
        return (_value[0] != null) ? _value : _defaultValue;
   }


    /**
     * Returns true to indicate that this control accepts free-form text.
     **/
    public boolean isTextControl() {
        return true;
    }


    public ScriptableObject getScriptableObject() {
        return new Scriptable();
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (getName().length() > 0) processor.addParameter( getName(), getValues()[0], characterSet );
    }


    void claimValue( List values ) {
        if (isReadOnly()) return;
        if (values.isEmpty()) {
            _value[0] = "";
        } else {
            _value[0] = (String) values.get(0);
            values.remove(0);
        }
    }


    void reset() {
        _value[0] = null;
    }


    void claimRequiredValues( List values ) {
        if (isReadOnly()) claimValueIsRequired( values );
    }


    protected void claimValueIsRequired( List values ) {
        claimValueIsRequired( values, _defaultValue[0] );
    }

    class Scriptable extends FormControl.Scriptable {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                return getValues()[0];
            } else {
                return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                _value[0] =_defaultValue[0] = value.toString();
            } else {
                super.set( propertyName, value );
            }
        }
    }
}


class TextFieldFormControl extends TextFormControl {
    public TextFieldFormControl( Node node ) {
        super( node, NodeUtils.getNodeAttribute( node, "value" ) );
    }

}


class HiddenFieldFormControl extends TextFieldFormControl {
    public HiddenFieldFormControl( Node node ) {
        super( node );
    }


    void claimRequiredValues( List values ) {
        claimValueIsRequired( values );
    }


    void claimValue( List values ) {
    }

}


class TextAreaFormControl extends TextFormControl {

    public TextAreaFormControl( Node node ) {
        super( node, getDefaultValue( node ) );

        if (!node.getNodeName().equalsIgnoreCase( "textarea" )) {
            throw new RuntimeException( "Not a textarea element" );
        }
    }


    private static String getDefaultValue( Node node ) {
        return NodeUtils.asText( node.getChildNodes() );
    }

}


class FileSubmitFormControl extends FormControl {

    private UploadFileSpec _fileToUpload;


    public FileSubmitFormControl( Node node ) {
        super( node );
    }


    /**
     * Returns true if this control accepts a file for upload.
     **/
    public boolean isFileParameter() {
        return true;
    }


    /**
     * Returns the name of the selected file, if any.
     */
    public String[] getValues() {
        return new String[] { _fileToUpload == null ? "" : _fileToUpload.getFileName() };
    }


    /**
     * Specifies a number of file upload specifications for this control.
     **/
    void claimUploadSpecification( List files ) {
        if (files.isEmpty()) {
            _fileToUpload = null;
        } else {
            _fileToUpload = (UploadFileSpec) files.get(0);
            files.remove(0);
        }
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (!isDisabled() && _fileToUpload != null) {
            processor.addFile( getName(), _fileToUpload );
        }
    }
}


class SelectionFormControl extends FormControl {

    private final boolean _multiSelect;
    private final boolean _listBox;

    private Options _selectionOptions;
    private Scriptable _scriptable;


    SelectionFormControl( Node node ) {
        super( node );
        if (!node.getNodeName().equalsIgnoreCase( "select" )) throw new RuntimeException( "Not a select element" );

        _multiSelect      = NodeUtils.isNodeAttributePresent( node, "multiple" );
        _listBox          = _multiSelect || NodeUtils.isNodeAttributePresent( node, "size" );

        _selectionOptions = new Options( node );
    }


    public String[] getValues() {
        return _selectionOptions.getSelectedValues();
    }


    public String[] getOptionValues() {
        return _selectionOptions.getValues();
    }


    public String[] getDisplayedOptions() {
        return _selectionOptions.getDisplayedText();
    }


    /**
     * Returns true if a single control can have multiple values.
     **/
    public boolean isMultiValued() {
        return _multiSelect;
    }


    class Scriptable extends FormControl.Scriptable {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "options" )) {
                return _selectionOptions;
            } else if (propertyName.equalsIgnoreCase( "length" )) {
                return new Integer( getOptionValues().length );
            } else if (propertyName.equalsIgnoreCase( "selectedIndex" )) {
                return new Integer( _selectionOptions.getFirstSelectedIndex() );
            } else {
                return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
            } else {
                super.set( propertyName, value );
            }
        }
    }


    public ScriptableObject getScriptableObject() {
        if (_scriptable == null) _scriptable = new Scriptable();
        return _scriptable;
    }


    void updateRequiredParameters( Hashtable required ) {
        if (isReadOnly()) required.put( getName(), getValues() );
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        for (int i = 0; i < getValues().length; i++) {
            processor.addParameter( getName(), getValues()[i], characterSet );
        }
    }


    void claimUniqueValue( List values ) {
        _selectionOptions.claimUniqueValues( values );
    }


    final void reset() {
        _selectionOptions.reset();
    }


    class Option extends ScriptableObject implements SelectionOption {

        private String  _text;
        private String  _value;
        private boolean _defaultSelected;
        private boolean _selected;
        private int     _index;


        Option( String text, String value, boolean selected ) {
            _text = text;
            _value = value;
            _defaultSelected = _selected = selected;
        }


        void reset() {
            _selected = _defaultSelected;
        }


        void addValueIfSelected( List list ) {
            if (_selected) list.add( _value );
        }


        void setIndex( int index ) {
            _index = index;
        }


 //------------------------- SelectionOption methods ------------------------------


        public int getIndex() {
            return _index;
        }


        public String getText() {
            return _text;
        }


        public String getValue() {
            return _value;
        }


        public void setValue( String value ) {
            _value = value;
        }


        public boolean isDefaultSelected() {
            return _defaultSelected;
        }


        public void setSelected( boolean selected ) {
            _selected = selected;
        }


        public boolean isSelected() {
            return _selected;
        }
    }


    class Options extends ScriptableObject implements SelectionOptions {

        private Option[] _options;

        private String[]  _text;
        private String[]  _value;


        Options( Node selectionNode ) {
            NodeList nl = ((Element) selectionNode).getElementsByTagName( "option" );

            _options = new Option[ nl.getLength() ];
            for (int i = 0; i < _options.length; i++) {
                _options[i] = new Option( getValue( nl.item(i).getFirstChild() ),
                                          getOptionValue( nl.item(i) ),
                                          nl.item(i).getAttributes().getNamedItem( "selected" ) != null );
                _options[i].setIndex(i);
            }
        }


        void claimUniqueValues( List values ) {
            for (int i = 0; i < _options.length; i++) _options[i].setSelected( false );

            int numMatches = 0;
            for (int i = 0; i < _options.length; i++) {
                if (values.contains( _options[i].getValue() )) {
                    _options[i].setSelected( true );
                    values.remove( _options[i].getValue() );
                    numMatches++;
                    if (!_multiSelect) break;
                }
            }
            if (!_listBox && numMatches == 0) {
                throw new IllegalParameterValueException( getName(), (String) values.get(0), getOptionValues() );
            }
        }


        String[] getSelectedValues() {
            ArrayList list = new ArrayList();
            for (int i = 0; i < _options.length; i++) {
                _options[i].addValueIfSelected( list );
            }
            if (!_listBox && list.isEmpty() && _options.length > 0) list.add( _options[0].getValue() );
            return (String[]) list.toArray( new String[ list.size() ] );
        }


        void reset() {
            for (int i = 0; i < _options.length; i++) {
                _options[i].reset();
            }
        }


        String[] getDisplayedText() {
            if (_text == null) {
                _text = new String[ _options.length ];
                for (int i = 0; i < _text.length; i++) _text[i] = _options[i].getText();
            }
            return _text;
        }


        String[] getValues() {
            if (_value == null) {
                _value = new String[ _options.length ];
                for (int i = 0; i < _value.length; i++) _value[i] = _options[i].getValue();
            }
            return _value;
        }


        /**
         * Returns the index of the first item selected, or -1 if none is selected.
         */
        int getFirstSelectedIndex() {
            for (int i = 0; i < _options.length; i++) {
                if (_options[i].isSelected()) return i;
            }
            return -1;
        }


        public int getLength() {
            return _options.length;
        }


        public Object get( int index ) {
            return _options[ index ];
        }


        private String getOptionValue( Node optionNode ) {
            NamedNodeMap nnm = optionNode.getAttributes();
            if (nnm.getNamedItem( "value" ) != null) {
                return getValue( nnm.getNamedItem( "value" ) );
            } else {
                return getValue( optionNode.getFirstChild() );
            }
        }

        private String getValue( Node node ) {
            return (node == null) ? "" : emptyIfNull( node.getNodeValue() );
        }


        private String emptyIfNull( String value ) {
            return (value == null) ? "" : value;
        }

    }


}
//============================= exception class IllegalParameterValueException ======================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class IllegalParameterValueException extends IllegalRequestParameterException {


    IllegalParameterValueException( String parameterName, String badValue, String[] allowed ) {
        _parameterName = parameterName;
        _badValue      = badValue;
        _allowedValues = allowed;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append( "May not set parameter '" ).append( _parameterName ).append( "' to '" );
        sb.append( _badValue ).append( "'. Value must be one of: { " );
        for (int i = 0; i < _allowedValues.length; i++) {
            if (i != 0) sb.append( ", " );
            sb.append( _allowedValues[i] );
        }
        sb.append( " }" );
        return sb.toString();
    }


    private String   _parameterName;
    private String   _badValue;
    private String[] _allowedValues;
}

//============================= exception class MissingParameterValueException ======================================


/**
 * This exception is thrown on an attempt to remove a required value from a form parameter.
 **/
class MissingParameterValueException extends IllegalRequestParameterException {


    MissingParameterValueException( String parameterName, String missingValue, String[] proposed ) {
        _parameterName  = parameterName;
        _missingValue   = missingValue;
        _proposedValues = proposed;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append( "Parameter '" ).append( _parameterName ).append( "' must have the value '" );
        sb.append( _missingValue ).append( "'. Attempted to set it to: { " );
        for (int i = 0; i < _proposedValues.length; i++) {
            if (i != 0) sb.append( ", " );
            sb.append( _proposedValues[i] );
        }
        sb.append( " }" );
        return sb.toString();
    }


    private String   _parameterName;
    private String   _missingValue;
    private String[] _proposedValues;
}


