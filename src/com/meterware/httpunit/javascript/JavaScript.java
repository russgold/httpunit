package com.meterware.httpunit.javascript;
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
import com.meterware.httpunit.HTMLPage;
import com.meterware.httpunit.ScriptEngine;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

import java.lang.reflect.InvocationTargetException;

import org.mozilla.javascript.ClassDefinitionException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NotAFunctionException;
import org.mozilla.javascript.PropertyException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.xml.sax.SAXException;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class JavaScript {

    /**
     * Initiates JavaScript execution for the specified web response.
     */
    static public void run( WebResponse response ) throws IllegalAccessException, InstantiationException, InvocationTargetException,
            ClassDefinitionException, NotAFunctionException, PropertyException, SAXException, JavaScriptException {
        Context context = Context.enter();
        Scriptable scope = context.initStandardObjects( null );
        ScriptableObject.defineClass( scope, Window.class );
        ScriptableObject.defineClass( scope, Document.class );
        ScriptableObject.defineClass( scope, Form.class );
        ScriptableObject.defineClass( scope, Control.class );
        Window w = (Window) context.newObject( scope, "Window" );

        w.initialize( response.getScriptableObject() );
    }


    abstract static class JavaScriptEngine extends ScriptableObject implements ScriptEngine {

        protected com.meterware.httpunit.ScriptableObject _scriptable;


        public void executeScript( String script ) {
            try {
                Context.getCurrentContext().evaluateString( this, script, "httpunit", 0, null );
            } catch (JavaScriptException e) {
                throw new RuntimeException( "Script '" + script + "' failed: " + e );
            }
        }


        void initialize( com.meterware.httpunit.ScriptableObject scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            _scriptable = scriptable;
            _scriptable.setScriptEngine( this );
        }


        public boolean has( String propertyName, Scriptable scriptable ) {
            return super.has( propertyName, scriptable ) ||
                    (_scriptable != null && _scriptable.get( propertyName ) != null);
        }


        public Object get( String propertyName, Scriptable scriptable ) {
            Object result = super.get( propertyName, scriptable );
            if (result != NOT_FOUND) return result;
            if (_scriptable == null) return NOT_FOUND;

            final Object property = _scriptable.get( propertyName );
            if (property == null) return NOT_FOUND;
            if (!(property instanceof com.meterware.httpunit.ScriptableObject)) return property;

            try {
                return toScriptable( (com.meterware.httpunit.ScriptableObject) property );
            } catch (PropertyException e) {
                throw new RuntimeException( e.toString() );
            } catch (NotAFunctionException e) {
                throw new RuntimeException( e.toString() );
            } catch (JavaScriptException e) {
                throw new RuntimeException( e.toString() );
            } catch (SAXException e) {
                throw new RuntimeException( e.toString() );
            }
        }


        public void put( String propertyName, Scriptable scriptable, Object value ) {
            if (_scriptable == null || _scriptable.get( propertyName ) == null) {
                super.put( propertyName, scriptable, value );
            } else {
                _scriptable.set( propertyName, value );
            }
        }


        /**
         * Converts a scriptable delegate obtained from a subobject into the appropriate Rhino-compatible Scriptable.
         **/
        abstract Scriptable toScriptable( com.meterware.httpunit.ScriptableObject delegate )
                throws PropertyException, NotAFunctionException, JavaScriptException, SAXException;

    }


    static public class Window extends JavaScriptEngine {

        private Document _document;


        public String getClassName() {
            return "Window";
        }


        public Window jsGet_window() {
            return this;
        }


        public Window jsGet_self() {
            return this;
        }


        public Document jsGet_document() {
            return _document;
        }


        void initialize( com.meterware.httpunit.ScriptableObject scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( scriptable );
            _document = (Document) Context.getCurrentContext().newObject( this, "Document" );
            _document.initialize( getDelegate().getDocument() );

            getDelegate().load();
        }


        public void jsFunction_alert( String message ) {
            getDelegate().alert( message );
        }


        Scriptable toScriptable( com.meterware.httpunit.ScriptableObject delegate ) {
            return null;
        }


        private WebResponse.Scriptable getDelegate() {
            return (WebResponse.Scriptable) _scriptable;
        }
    }


    static public class Document extends JavaScriptEngine {

        private Scriptable _forms;


        public String getClassName() {
            return "Document";
        }


        void initialize( com.meterware.httpunit.ScriptableObject scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( scriptable );
            initializeForms();
        }


        public String jsGet_title() throws SAXException {
            return getDelegate().getTitle();
        }


        public Scriptable jsGet_forms() {
            return _forms;
        }


        private void initializeForms() throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            WebForm.Scriptable scriptables[] = getDelegate().getForms();
            Form[] forms = new Form[ scriptables.length ];
            for (int i = 0; i < forms.length; i++) {
                forms[ i ] = (Form) toScriptable( scriptables[ i ] );
            }
            _forms = Context.getCurrentContext().newArray( this, forms );
        }


        Scriptable toScriptable( com.meterware.httpunit.ScriptableObject delegate )
                throws PropertyException, JavaScriptException, NotAFunctionException, SAXException {
            final Form form = (Form) Context.getCurrentContext().newObject( this, "Form" );
            form.initialize( delegate );
            return form;
        }


        private HTMLPage.Scriptable getDelegate() {
            return (HTMLPage.Scriptable) _scriptable;
        }

    }


    static public class Form extends JavaScriptEngine {

        public String getClassName() {
            return "Form";
        }


        Scriptable toScriptable( com.meterware.httpunit.ScriptableObject delegate )
                throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            final Control control = (Control) Context.getCurrentContext().newObject( this, "Control" );
            control.initialize( delegate );
            return control;
        }

    }


    static public class Control extends JavaScriptEngine {

        public String getClassName() {
            return "Control";
        }


        Scriptable toScriptable( com.meterware.httpunit.ScriptableObject delegate ) {
            return null;
        }
    }

}
