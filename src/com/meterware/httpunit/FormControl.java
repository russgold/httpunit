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
import com.meterware.httpunit.scripting.*;

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
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
abstract class FormControl {

    final static String[] NO_VALUE = new String[0];

    private       String  _name;
    private final String  _id;
    private       String  _valueAttribute;
    private final boolean _readOnly;
    private final boolean _disabled;
    private final String  _onChangeEvent;
    private final String  _onClickEvent;
    private final WebForm _form;

    private Scriptable _scriptable;


    FormControl( WebForm form ) {
        _form           = form;
        _id             = "";
        _name           = "";
        _valueAttribute = "";
        _readOnly       = false;
        _disabled       = false;
        _onChangeEvent  = "";
        _onClickEvent   = "";
    }


    FormControl( WebForm form, Node node ) {
        _form           = form;
        _id             = NodeUtils.getNodeAttribute( node, "id" );
        _name           = NodeUtils.getNodeAttribute( node, "name" );
        _valueAttribute = NodeUtils.getNodeAttribute( node, "value" );
        _readOnly       = NodeUtils.isNodeAttributePresent( node, "readonly" );
        _disabled       = NodeUtils.isNodeAttributePresent( node, "disabled" );
        _onChangeEvent  = NodeUtils.getNodeAttribute( node, "onchange" );
        _onClickEvent   = NodeUtils.getNodeAttribute( node, "onclick" );
    }


    /**
     * Returns the name of this control. If no name is specified, defaults to the empty string.
     **/
    public String getName() {
        return _name;
    }


    /**
     * Returns the ID associated with this control, if any.
     * @return the control ID, or an empty string if no ID is defined.
     **/
    public String getID() {
        return _id;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    abstract String[] getValues();


    Object getDelegate() {
        return getScriptableObject();
    }


    final protected WebForm getForm() {
        return _form;
    }


    /**
     * Returns a scriptable object which can act as a proxy for this control.
     */
    ScriptableDelegate getScriptableObject() {
        if (_scriptable == null) {
            _scriptable = newScriptable();
            _scriptable.setScriptEngine( getForm().getScriptableObject().getScriptEngine( _scriptable ) );
        }
        return _scriptable;
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
    String[] getDisplayedOptions() {
        return NO_VALUE;
    }


    /**
     * Returns true if this control is read-only.
     **/
    boolean isReadOnly() {
        return _readOnly || _disabled;
    }


    /**
     * Returns true if this control is disabled, meaning that it will not send a value to the server as part of a request.
     **/
    boolean isDisabled() {
        return _disabled;
    }


    /**
     * Returns true if this control accepts free-form text.
     **/
    boolean isTextControl() {
        return false;
    }


    /**
     * Returns true if only one control of this kind with this name can have a value. This is true for radio buttons.
     **/
    boolean isExclusive() {
        return false;
    }


    /**
     * Returns true if a single control can have multiple values.
     **/
    boolean isMultiValued() {
        return false;
    }


    /**
     * Returns true if this control accepts a file for upload.
     **/
    boolean isFileParameter() {
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
     * Performs the 'onChange' event defined for this control.
     */
    protected void sendOnChangeEvent() {
        if (_onChangeEvent.length() > 0) getScriptableObject().doEvent( _onChangeEvent );
    }


    /**
     * Performs the 'onClick' event defined for this control.
     */
    protected void sendOnClickEvent() {
        if (_onClickEvent.length() > 0) getScriptableObject().doEvent( _onClickEvent );
    }


    /**
     * Creates and returns a scriptable object for this control. Subclasses should override this if they use a different
     * implementation of Scriptable.
     */
    protected Scriptable newScriptable() {
        return new Scriptable();
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
            return new TextAreaFormControl( form, node );
        } else if (node.getNodeName().equals( "select" )) {
            return new SelectionFormControl( form, node );
        } else if (node.getNodeName().equalsIgnoreCase( "button" )) {
            final String type = NodeUtils.getNodeAttribute( node, "type", "submit" );
            if (type.equalsIgnoreCase( "submit" )) {
                return new SubmitButton( form, node );
            } else if (type.equalsIgnoreCase( "reset" )) {
                return new ResetButton( form, node );
            } else {
                return new Button( form, node );
            }
        } else if (!node.getNodeName().equals( "input" )) {
            return null;
        } else {
            final String type = NodeUtils.getNodeAttribute( node, "type", "text" );
            if (type.equalsIgnoreCase( "text" ) || type.equalsIgnoreCase( "password" )) {
                return new TextFieldFormControl( form, node );
            } else if (type.equalsIgnoreCase( "hidden" )) {
                return new HiddenFieldFormControl( form, node );
            } else if (type.equalsIgnoreCase( "radio" )) {
                return new RadioButtonFormControl( form, node );
            } else if (type.equalsIgnoreCase( "checkbox" )) {
                return new CheckboxFormControl( form, node );
            } else if (type.equalsIgnoreCase( "submit" ) || type.equalsIgnoreCase( "image" )) {
                return new SubmitButton( form, node );
            } else if (type.equalsIgnoreCase( "button" )) {
                return new Button( form, node );
            } else if (type.equalsIgnoreCase( "reset" )) {
                return new ResetButton( form, node );
            } else if (type.equalsIgnoreCase( "file" )) {
                return new FileSubmitFormControl( form, node );
            } else {
                return new TextFieldFormControl( form, node );
            }
        }
    }


    class Scriptable extends ScriptableDelegate implements Input {

        public String getName() {
            return FormControl.this.getName();
        }


        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "name" )) {
                return FormControl.this.getName();
            } else {
                return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                _valueAttribute = value.toString();
            } else {
                super.set( propertyName, value );
            }
        }
    }

}


abstract
class BooleanFormControl extends FormControl {

    private boolean  _isChecked;
    private String[] _value = new String[1];

    private final boolean _isCheckedDefault;

    protected FormControl.Scriptable newScriptable() {
        return new Scriptable();
    }


    class Scriptable extends FormControl.Scriptable {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                return getQueryValue();
            } else if (propertyName.equalsIgnoreCase( "checked" )) {
                return isChecked() ? Boolean.TRUE : Boolean.FALSE;
            } else if (propertyName.equalsIgnoreCase( "defaultchecked" )) {
                return _isCheckedDefault ? Boolean.TRUE : Boolean.FALSE;
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

    public BooleanFormControl( WebForm form, Node node ) {
        super( form, node );
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
        if (isChecked() && !isDisabled()) processor.addParameter( getName(), getQueryValue(), characterSet );
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

    public RadioButtonFormControl( WebForm form, Node node ) {
        super( form, node );
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


    public RadioGroupFormControl( WebForm form ) {
        super( form );
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


    Object getDelegate() {
        ScriptableDelegate[] delegates = new ScriptableDelegate[ getButtons().length ];
        for (int i = 0; i < delegates.length; i++) {
            delegates[i] = getButtons()[i].getScriptableObject();
        }
        return delegates;
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

        boolean wasChecked = getButtons()[ matchingButtonIndex ].isChecked();
        for (int i = 0; i < getButtons().length; i++) {
            if (!getButtons()[i].isReadOnly()) getButtons()[i].setChecked( i == matchingButtonIndex );
        }
        values.remove( getButtons()[ matchingButtonIndex ].getQueryValue() );
        if (!wasChecked) getButtons()[ matchingButtonIndex ].sendOnClickEvent();
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


    public CheckboxFormControl( WebForm form, Node node ) {
        super( form, node );
    }


    void claimUniqueValue( List values ) {
        boolean wasChecked = isChecked();
        if (isValueRequired()) return;
        setChecked( values.contains( getQueryValue() ) );
        if (isChecked()) values.remove( getQueryValue() );
        if (isChecked() != wasChecked) sendOnClickEvent();
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

    private String[]   _value = new String[1];
    private String[]   _defaultValue;


    public TextFormControl( WebForm form, Node node, String defaultValue ) {
        super( form, node );
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


    protected FormControl.Scriptable newScriptable() {
        return new Scriptable();
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (!isDisabled() && getName().length() > 0) processor.addParameter( getName(), getValues()[0], characterSet );
    }


    void claimValue( List values ) {
        if (isReadOnly()) return;

        String oldValue = getValues()[0];
        if (values.isEmpty()) {
            _value[0] = "";
        } else {
            _value[0] = (String) values.get(0);
            values.remove(0);
        }
        if (!(oldValue.equals( _value[0] ))) sendOnChangeEvent();
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
    public TextFieldFormControl( WebForm form, Node node ) {
        super( form, node, NodeUtils.getNodeAttribute( node, "value" ) );
    }

}


class HiddenFieldFormControl extends TextFieldFormControl {
    public HiddenFieldFormControl( WebForm form, Node node ) {
        super( form, node );
    }


    void claimRequiredValues( List values ) {
        claimValueIsRequired( values );
    }


    void claimValue( List values ) {
    }

}


class TextAreaFormControl extends TextFormControl {

    public TextAreaFormControl( WebForm form, Node node ) {
        super( form, node, getDefaultValue( node ) );

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


    public FileSubmitFormControl( WebForm form, Node node ) {
        super( form, node );
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


    SelectionFormControl( WebForm form, Node node ) {
        super( form, node );
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
            } else if (propertyName.equalsIgnoreCase( "selectedIndex" )) {
                if (!(value instanceof Number)) throw new RuntimeException( "selectedIndex must be set to an integer" );
                _selectionOptions.setSelectedIndex( ((Number) value).intValue() );
            } else {
                super.set( propertyName, value );
            }
        }
    }


    protected FormControl.Scriptable newScriptable() {
        return new Scriptable();
    }


    void updateRequiredParameters( Hashtable required ) {
        if (isReadOnly()) required.put( getName(), getValues() );
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (isDisabled()) return;
        for (int i = 0; i < getValues().length; i++) {
            processor.addParameter( getName(), getValues()[i], characterSet );
        }
    }


    void claimUniqueValue( List values ) {
        boolean changed = _selectionOptions.claimUniqueValues( values );
        if (changed) sendOnChangeEvent();
    }


    final void reset() {
        _selectionOptions.reset();
    }


    static class Option extends ScriptableDelegate implements SelectionOption {

        private String  _text;
        private String  _value;
        private boolean _defaultSelected;
        private boolean _selected;
        private int     _index;
        private Options _container;


        Option() {
        }


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


        void setIndex( Options container, int index ) {
            _container = container;
            _index = index;
        }


 //------------------------- SelectionOption methods ------------------------------


        public void initialize( String text, String value, boolean defaultSelected, boolean selected ) {
            _text = text;
            _value = value;
            _defaultSelected = defaultSelected;
            _selected = selected;
        }


        public int getIndex() {
            return _index;
        }


        public String getText() {
            return _text;
        }


        public void setText( String text ) {
            _text = text;
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
            if (selected) _container.optionSet( _index );
        }


        public boolean isSelected() {
            return _selected;
        }
    }


    class Options extends ScriptableDelegate implements SelectionOptions {

        private Option[] _options;


        Options( Node selectionNode ) {
            NodeList nl = ((Element) selectionNode).getElementsByTagName( "option" );

            _options = new Option[ nl.getLength() ];
            for (int i = 0; i < _options.length; i++) {
                _options[i] = new Option( getValue( nl.item(i).getFirstChild() ),
                                          getOptionValue( nl.item(i) ),
                                          nl.item(i).getAttributes().getNamedItem( "selected" ) != null );
                _options[i].setIndex( this, i );
            }
        }


        boolean claimUniqueValues( List values ) {
            boolean changed = false;
            int numMatches = 0;
            for (int i = 0; i < _options.length; i++) {
                final boolean newValue = values.contains( _options[i].getValue() );
                if (newValue != _options[i].isSelected()) changed = true;
                _options[i].setSelected( newValue );
                if (newValue) {
                    values.remove( _options[i].getValue() );
                    numMatches++;
                    if (!_multiSelect) for (++i; i < _options.length; i++) _options[i].setSelected( false );
                }
            }
            if (!_listBox && numMatches == 0) {
                throw new IllegalParameterValueException( getName(), (String) values.get(0), getOptionValues() );
            }
            return changed;
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
            String[] displayedText = new String[ _options.length ];
            for (int i = 0; i < displayedText.length; i++) displayedText[i] = _options[i].getText();
            return displayedText;
        }


        String[] getValues() {
            String[] values = new String[ _options.length ];
            for (int i = 0; i < values.length; i++) values[i] = _options[i].getValue();
            return values;
        }


        /**
         * Selects the matching item and deselects the others.
         **/
        void setSelectedIndex( int index ) {
            for (int i = 0; i < _options.length; i++) {
                _options[ i ]._selected = (i == index);
            }
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


        public void setLength( int length ) {
            if (length < 0 || length >= _options.length) return;
            Option[] newArray = new Option[ length ];
            System.arraycopy( _options, 0, newArray, 0, length );
            _options = newArray;
        }


        public void put( int i, SelectionOption option ) {
            if (i < 0) return;

            if (option == null) {
                if (i >= _options.length) return;
                deleteOptionsEntry( i );
            } else {
                if (i >= _options.length) {
                    i = _options.length;
                    expandOptionsArray();
                }
                _options[i] = (Option) option;
                _options[i].setIndex( this, i );
                if (option.isSelected()) ensureUniqueOption(i);
            }
        }


        private void ensureUniqueOption( int i ) {
            if (_multiSelect) return;
            for (int j = 0; j < _options.length; j++) {
                _options[j]._selected = (i == j);
            }
        }


        private void deleteOptionsEntry( int i ) {
            Option[] newArray = new Option[ _options.length-1 ];
            System.arraycopy( _options, 0, newArray, 0, i );
            System.arraycopy( _options, i+1, newArray, i, newArray.length - i );
            _options = newArray;
        }


        private void expandOptionsArray() {
            Option[] newArray = new Option[ _options.length+1 ];
            System.arraycopy( _options, 0, newArray, 0, _options.length );
            _options = newArray;
        }


        public Object get( int index ) {
            return _options[ index ];
        }


        /** Invoked when an option is set true. **/
        void optionSet( int i ) {
            ensureUniqueOption(i);
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


