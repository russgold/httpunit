package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2001-2007, Russell Gold
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
import com.meterware.httpunit.dom.*;
import com.meterware.httpunit.protocol.ParameterProcessor;
import com.meterware.httpunit.protocol.UploadFileSpec;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;


/**
 * Represents a control in an HTML form.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public abstract class FormControl extends HTMLElementBase {

    final static String[] NO_VALUE = new String[0];

    private final String  _onChangeEvent;
    private final String  _onClickEvent;
    private final WebForm _form;
    private HTMLControl _control;

    public static final String UNDEFINED_TYPE     = "undefined";
    public static final String BUTTON_TYPE        = "button";
    public static final String RESET_BUTTON_TYPE  = "reset";
    public static final String SUBMIT_BUTTON_TYPE = "submit";
    public static final String IMAGE_BUTTON_TYPE  = "image";
    public static final String RADIO_BUTTON_TYPE  = "radio";
    public static final String CHECKBOX_TYPE      = "checkbox";
    public static final String TEXT_TYPE          = "text";
    public static final String PASSWORD_TYPE      = "password";
    public static final String HIDDEN_TYPE        = "hidden";
    public static final String TEXTAREA_TYPE      = "textarea";
    public static final String FILE_TYPE          = "file";
    public static final String SINGLE_TYPE        = "select-one";
    public static final String MULTIPLE_TYPE      = "select-multiple";


    /**
     * Return the type of the control, as seen from JavaScript.
     */
    abstract public String getType();

    static ScriptableDelegate newSelectionOption() {
        return new SelectionFormControl.Option();
    }


    FormControl( WebForm form ) {
        this( form, newEmptyControlElement( form ) );
    }


    private static HTMLControl newEmptyControlElement( WebForm form ) {
        return (HTMLControl) form.getElement().getOwnerDocument().createElement( "input" );
    }


    FormControl( WebForm form, HTMLControl control ) {
        super( control );
        _control = control;
        _form           = form;
        _onChangeEvent  = NodeUtils.getNodeAttribute( control, "onchange" );
        _onClickEvent   = NodeUtils.getNodeAttribute( control, "onclick" );

        supportAttribute( "tabindex" );
        supportAttribute( "disabled" );
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    abstract String[] getValues();


    /**
     * Returns either a single delegate object or potentially an array of delegates as needed, given the form control.
     * This default implementation returns the scriptable delegate for the control.
     */
    Object getDelegate() {
        return getScriptingHandler();
    }


    final protected WebForm getForm() {
        return _form;
    }


    public ScriptableDelegate getParentDelegate() {
        return (ScriptableDelegate) getForm().getScriptingHandler();
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
        return isDisabled() || _control.getReadOnly();
    }


    /**
     * Returns true if this control is hidden.
     **/
    boolean isHidden() {
        return false;
    }


    void setDisabled( boolean disabled ) {
        _control.setDisabled( disabled );
    }


    /**
     * Returns true if this control is disabled, meaning that it will not send a value to the server as part of a request.
     **/
    public boolean isDisabled() {
        return _control.getDisabled();
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
        _control.reset();
    }


    /**
     * Toggles the value of this control.
     */
    public void toggle() {
        throw new FormParameter.IllegalCheckboxParameterException( getName(), "toggleCheckbox" );
    }


    /**
     * Sets the state of this boolean control.
     */
    public void setState( boolean state ) {
        throw new FormParameter.IllegalCheckboxParameterException( getName(), "setCheckbox" );
    }


    /**
     * Performs the 'onChange' event defined for this control.
     */
    protected void sendOnChangeEvent() {
        if (_onChangeEvent.length() > 0) getScriptingHandler().doEvent( _onChangeEvent );
    }


    /**
     * Performs the 'onClick' event defined for this control.
     */
    protected void sendOnClickEvent() {
        if (_onClickEvent.length() > 0) getScriptingHandler().doEvent( _onClickEvent );
    }


    /**
     * Creates and returns a scriptable object for this control. Subclasses should override this if they use a different
     * implementation of Scriptable.
     */
    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }


    /**
     * Returns the value of this control in the form. If no value is specified, defaults to the empty string.
     **/
    protected String getValueAttribute() {
        return "";
    }

    /**
     * Sets the value of this control in the form.
     */
    protected void setValueAttribute( String value ) {}


    /**
     * Removes the specified required value from the list of values, throwing an exception if it is missing.
     **/
    final protected void claimValueIsRequired( List values, final String value ) {
        if (!values.contains( value )) throw new MissingParameterValueException( getName(), value, (String[]) values.toArray( new String[ values.size() ]) );
        values.remove( value );
    }


    static String[] getControlElementTags() {
        return new String[] { "textarea", "select", "button", "input" };
    }


    static FormControl newFormParameter( WebForm form, Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        } else if (node.getNodeName().equalsIgnoreCase( "textarea" )) {
            return new TextAreaFormControl( form, (HTMLTextAreaElementImpl) node );
        } else if (node.getNodeName().equalsIgnoreCase( "select" )) {
            return new SelectionFormControl( form, (HTMLSelectElementImpl) node );
        } else if (node.getNodeName().equalsIgnoreCase( "button" )) {
            HTMLControl control = (HTMLControl) node;
            final String type = control.getType();
            if (type.equalsIgnoreCase( SUBMIT_BUTTON_TYPE )) {
                return new SubmitButton( form, control );
            } else if (type.equalsIgnoreCase( RESET_BUTTON_TYPE )) {
                return new ResetButton( form, control );
            } else {
                return new Button( form, control );
            }
        } else if (!node.getNodeName().equalsIgnoreCase( "input" )) {
            return null;
        } else {
            HTMLInputElementImpl element = (HTMLInputElementImpl) node;
            final String type = element.getType();
            if (type.equalsIgnoreCase( TEXT_TYPE )) {
                return new TextFieldFormControl( form, element );
            } else if (type.equalsIgnoreCase( PASSWORD_TYPE )) {
                return new PasswordFieldFormControl( form, element );
            } else if (type.equalsIgnoreCase( HIDDEN_TYPE )) {
                return new HiddenFieldFormControl( form, element );
            } else if (type.equalsIgnoreCase( RADIO_BUTTON_TYPE )) {
                return new RadioButtonFormControl( form, element );
            } else if (type.equalsIgnoreCase( CHECKBOX_TYPE )) {
                return new CheckboxFormControl( form, element );
            } else if (type.equalsIgnoreCase( SUBMIT_BUTTON_TYPE ) || type.equalsIgnoreCase( IMAGE_BUTTON_TYPE )) {
                return new SubmitButton( form, element );
            } else if (type.equalsIgnoreCase( BUTTON_TYPE )) {
                return new Button( form, (HTMLControl) node );
            } else if (type.equalsIgnoreCase( RESET_BUTTON_TYPE )) {
                return new ResetButton( form, (HTMLControl) node );
            } else if (type.equalsIgnoreCase( FILE_TYPE )) {
                return new FileSubmitFormControl( form, element );
            } else {
                return new TextFieldFormControl( form, element );
            }
        }
    }


    protected String emptyIfNull( String value ) {
        return (value == null) ? "" : value;
    }

    class Scriptable extends HTMLElementScriptable implements Input {

        public String getName() {
            return FormControl.this.getName();
        }


        public String getID() {
            return FormControl.this.getID();
        }


        public Scriptable() {
            super( FormControl.this );
        }


        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "name" )) {
                return FormControl.this.getName();
            } else if (propertyName.equalsIgnoreCase( "type" )) {
                return FormControl.this.getType();
            } else {
                return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                setValueAttribute( value.toString() );
            } else if (propertyName.equalsIgnoreCase( "disabled" )) {
                setDisabled( value instanceof Boolean && ((Boolean) value).booleanValue() );
            } else {
                super.set( propertyName, value );
            }
        }


        public void click() throws IOException, SAXException {
        }
    }

}


abstract class BooleanFormControl extends FormControl {

    private String[] _displayedValue;

    private HTMLInputElementImpl _element;


    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }


    class Scriptable extends FormControl.Scriptable {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                return getQueryValue();
            } else if (propertyName.equalsIgnoreCase( "checked" )) {
                return isChecked() ? Boolean.TRUE : Boolean.FALSE;
            } else if (propertyName.equalsIgnoreCase( "defaultchecked" )) {
                return _element.getDefaultChecked() ? Boolean.TRUE : Boolean.FALSE;
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

    public BooleanFormControl( WebForm form, HTMLInputElementImpl element ) {
        super( form, element );
        _element = element;
        _displayedValue = new String[] { readDisplayedValue( element ) };
    }


    private String readDisplayedValue( Node node ) {
        Node nextSibling = node.getNextSibling();
        while (nextSibling != null && nextSibling.getNodeType() != Node.TEXT_NODE && nextSibling.getNodeType() != Node.ELEMENT_NODE) nextSibling = nextSibling.getNextSibling();
        if (nextSibling == null || nextSibling.getNodeType() != Node.TEXT_NODE) return "";
        return nextSibling.getNodeValue();
    }


    boolean isChecked() {
        return _element.getChecked();
    }


    protected String getValueAttribute() {
        return emptyIfNull( _element.getValue() );
    }


    protected void setValueAttribute( String value ) {
        _element.setValue( value );
    }


    public void setChecked( boolean checked ) {
        _element.setChecked( checked );
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


    String[] getDisplayedOptions() {
        return _displayedValue;
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
        return new String[] { value };
    }
}


class CheckboxFormControl extends BooleanFormControl {

    public String getType() {
        return CHECKBOX_TYPE;
    }

    public CheckboxFormControl( WebForm form, HTMLInputElementImpl element ) {
        super( form, element );
    }


    void claimUniqueValue( List values ) {
        if (isValueRequired()) return;
        setState( values.contains( getQueryValue() ) );
        if (isChecked()) values.remove( getQueryValue() );
    }


    String getQueryValue() {
        final String value = getValueAttribute();
        return value.length() == 0 ? "on" : value;
    }


    /**
     * Toggles the value of this control.
     */
    public void toggle() {
        setState( !isChecked() );
    }


    /**
     * Sets the state of this boolean control. Triggers the 'onclick' event if the state has changed.
     */
    public void setState( boolean state ) {
        boolean wasChecked = isChecked();
        setChecked( state );
        if (isChecked() != wasChecked) sendOnClickEvent();
    }
 }


abstract class TextFormControl extends FormControl {

    public TextFormControl( WebForm form, HTMLControl control ) {
        super( form, control );
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    public String[] getValues() {
        return new String[] { getValue() };
   }


    abstract protected String getDefaultValue();

    abstract protected String getValue();

    abstract protected void setValue( String value );


    /**
     * Returns true to indicate that this control accepts free-form text.
     **/
    public boolean isTextControl() {
        return true;
    }


    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (!isDisabled() && getName().length() > 0) processor.addParameter( getName(), getValues()[0], characterSet );
    }


    void claimValue( List values ) {
        if (isReadOnly()) return;

        String oldValue = getValue();
        if (values.isEmpty()) {
            setValue( "" );
        } else {
            setValue( (String) values.get(0) );
            values.remove(0);
        }
        if (!(oldValue.equals( getValue() ))) sendOnChangeEvent();
    }


    void claimRequiredValues( List values ) {
        if (isReadOnly()) claimValueIsRequired( values );
    }


    protected void claimValueIsRequired( List values ) {
        claimValueIsRequired( values, getDefaultValue() );
    }

    class Scriptable extends FormControl.Scriptable {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                return getValue();
            } else if (propertyName.equalsIgnoreCase( "defaultValue" )) {
                return getDefaultValue();
            } else {
                return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (!propertyName.equalsIgnoreCase( "value" )) {
                super.set( propertyName, value );
            } else if (value instanceof Number) {
                setValue( HttpUnitUtils.trimmedValue( (Number) value ) );
            } else {
                setValue( (value == null) ? null : value.toString() );
            }
        }
    }
}


class TextFieldFormControl extends TextFormControl {

    private HTMLInputElementImpl _element;

    public String getType() {
        return TEXT_TYPE;
    }

    public TextFieldFormControl( WebForm form, HTMLInputElementImpl element ) {
        super( form, element );
        _element = element;
        supportAttribute( "maxlength" );
    }


    protected String getDefaultValue() {
        return _element.getDefaultValue();
    }


    protected String getValue() {
        return emptyIfNull( _element.getValue() );
    }


    protected void setValue( String value ) {
        _element.setValue( value );
    }
}

class PasswordFieldFormControl extends TextFieldFormControl {

    public String getType() {
        return PASSWORD_TYPE;
    }

    public PasswordFieldFormControl(WebForm form, HTMLInputElementImpl element) {
        super(form, element);
    }
}

class HiddenFieldFormControl extends TextFieldFormControl {

    public String getType() {
        return HIDDEN_TYPE;
    }

    public HiddenFieldFormControl( WebForm form, HTMLInputElementImpl element ) {
        super( form, element );
    }


    void claimRequiredValues( List values ) {
        claimValueIsRequired( values );
    }


    void claimValue( List values ) {
    }


    boolean isHidden() {
        return true;
    }
}


class TextAreaFormControl extends TextFormControl {

    private HTMLTextAreaElementImpl _element;

    public TextAreaFormControl( WebForm form, HTMLTextAreaElementImpl element ) {
        super( form, element );
        _element = element;
    }

    public String getType() {
        return TEXTAREA_TYPE;
    }


    protected String getDefaultValue() {
        return _element.getDefaultValue();
    }


    protected String getValue() {
        return _element.getValue();
    }


    protected void setValue( String value ) {
        _element.setValue( value );
    }

}


class FileSubmitFormControl extends FormControl {

    public String getType() {
        return FILE_TYPE;
    }

    private UploadFileSpec _fileToUpload;


    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }


    class Scriptable extends FormControl.Scriptable {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                return getSelectedName();
           } else {
                return super.get( propertyName );
            }
        }


    }

    public FileSubmitFormControl( WebForm form, HTMLInputElementImpl node ) {
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
        return new String[] { getSelectedName() };
    }


    private String getSelectedName() {
        return _fileToUpload == null ? "" : _fileToUpload.getFileName();
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


    public String getType() {
        return (isMultiValued()?MULTIPLE_TYPE:SINGLE_TYPE);
    }

    SelectionFormControl( WebForm form, HTMLSelectElementImpl element ) {
        super( form, element );
        if (!element.getNodeName().equalsIgnoreCase( "select" )) throw new RuntimeException( "Not a select element" );

        int size     = NodeUtils.getAttributeValue( element, "size", 0);
        _multiSelect = NodeUtils.isNodeAttributePresent( element, "multiple" );
        _listBox     = size > 1 || (_multiSelect && size != 1);

        _selectionOptions = _listBox ? (Options) new MultiSelectOptions( element ) : (Options) new SingleSelectOptions( element );
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
            } else if (propertyName.equalsIgnoreCase( "value" )) {
                return getSelectedValue();
            } else if (propertyName.equalsIgnoreCase( "selectedIndex" )) {
                return new Integer( _selectionOptions.getFirstSelectedIndex() );
            } else {
                return super.get( propertyName );
            }
        }


        public Object get(int index) {
            return _selectionOptions.get( index );
        }


        private String getSelectedValue() {
            String[] values = getValues();
            return (values.length == 0 ? "" : values[0] );
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                ArrayList values = new ArrayList();
                values.add( value );
                _selectionOptions.claimUniqueValues( values );
            } else if (propertyName.equalsIgnoreCase( "selectedIndex" )) {
                if (!(value instanceof Number)) throw new RuntimeException( "selectedIndex must be set to an integer" );
                _selectionOptions.setSelectedIndex( ((Number) value).intValue() );
            } else {
                super.set( propertyName, value );
            }
        }
    }


    public ScriptableDelegate newScriptable() {
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


    abstract class Options extends ScriptableDelegate implements SelectionOptions {

        private Option[] _options;

        Options( Node selectionNode ) {
            NodeList nl = ((Element) selectionNode).getElementsByTagName( "option" );

            _options = new Option[ nl.getLength() ];
            for (int i = 0; i < _options.length; i++) {
                final String displayedText = getValue( nl.item(i).getFirstChild() ).trim();
                _options[i] = new Option( displayedText,
                                          getOptionValue( nl.item(i), displayedText ),
                                          nl.item(i).getAttributes().getNamedItem( "selected" ) != null );
                _options[i].setIndex( this, i );
            }
        }


        boolean claimUniqueValues( List values ) {
            return claimUniqueValues( values, _options );
        }


        protected abstract boolean claimUniqueValues( List values, Option[] options );


        final protected void reportNoMatches( List values ) {
            if (!_listBox) throw new IllegalParameterValueException( getName(), (String) values.get(0), getOptionValues() );
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
            return noOptionSelectedIndex();
        }


        protected abstract int noOptionSelectedIndex();


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
                if (option.isSelected()) ensureUniqueOption( _options, i);
            }
        }


        protected abstract void ensureUniqueOption( Option[] options, int i );


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

				/**
				 * get the Object at the given index
				 * check that the index is not out of bounds 
				 * @param index - the index of the object to get
				 * @throw RuntimeException if index is out of bounds
				 */
        public Object get( int index ) {
        		// if the index is out of bounds
            if (index < 0 || index >= _options.length) {
            	// create a user friendly error message
            	String msg="invalid index "+index+" for Options ";
            	// by listing all possible options
	            for (int i = 0; i < _options.length; i++) {
                msg=msg+(_options[i]._text);
                if (i>0)
                  msg=msg+",";
        	  	} // for
	            // now throw a RunTimeException that would
	            // have happened anyways with a less friendly message
        	  	throw new RuntimeException(msg);
        	  }	// if
            return _options[ index ];
        } // get
        

        /** Invoked when an option is set true. **/
        void optionSet( int i ) {
            ensureUniqueOption( _options, i);
        }


        private String getOptionValue( Node optionNode, String displayedText ) {
            NamedNodeMap nnm = optionNode.getAttributes();
            if (nnm.getNamedItem( "value" ) != null) {
                return getValue( nnm.getNamedItem( "value" ) );
            } else {
                return displayedText;
            }
        }

        private String getValue( Node node ) {
            return (node == null) ? "" : emptyIfNull( node.getNodeValue() );
        }
    }


    class SingleSelectOptions extends Options {

        public SingleSelectOptions( Node selectionNode ) {
            super( selectionNode );
        }


        protected void ensureUniqueOption( Option[] options, int i ) {
            for (int j = 0; j < options.length; j++) {
                options[j]._selected = (i == j);
            }
        }


        protected int noOptionSelectedIndex() {
            return 0;
        }


        protected boolean claimUniqueValues( List values, Option[] options ) {
            boolean changed = false;
            for (int i = 0; i < values.size(); i++) {
                String value = (String) values.get( i );
                for (int j = 0; j < options.length; j++) {
                    boolean selected = value.equals( options[j].getValue() );
                    if (selected != options[j].isSelected()) changed = true;
                    options[j].setSelected( selected );
                    if (selected) {
                        values.remove( value );
                        for (++j; j < options.length; j++) options[j].setSelected( false );
                        return changed;
                    }
                }
            }
            reportNoMatches( values );
            return changed;
        }
    }


    class MultiSelectOptions extends Options {

        public MultiSelectOptions( Node selectionNode ) {
            super( selectionNode );
        }


        protected void ensureUniqueOption( Option[] options, int i ) {}


        protected int noOptionSelectedIndex() {
            return -1;
        }


        protected boolean claimUniqueValues( List values, Option[] options ) {
            boolean changed = false;
            for (int i = 0; i < options.length; i++) {
                final boolean newValue = values.contains( options[i].getValue() );
                if (newValue != options[i].isSelected()) changed = true;
                options[i].setSelected( newValue );
                if (newValue) values.remove( options[i].getValue() );
            }
            return changed;
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


