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
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.HTMLPage;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.ScriptEngine;

import java.util.Arrays;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.ClassDefinitionException;
import org.mozilla.javascript.PropertyException;
import org.mozilla.javascript.NotAFunctionException;
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
        Window w = (Window) context.newObject( scope, "Window" );

        w.initialize( response.getScriptableObject() );
    }


    abstract static class JavaScriptEngine extends ScriptableObject implements ScriptEngine {

        public void executeScript( String script ) {
            try {
                Context.getCurrentContext().evaluateString( this, script, "httpunit", 0, null );
            } catch (JavaScriptException e) {
                throw new RuntimeException( "Script '" + script + "' failed: " + e );
            }
        }
    }


    static public class Window extends JavaScriptEngine {

        private WebResponse.Scriptable _scriptable;
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


        void initialize( WebResponse.Scriptable scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException  {
            _scriptable = scriptable;
            _scriptable.setScriptEngine( this );
            _document = (Document) Context.getCurrentContext().newObject( this, "Document" );
            _document.initialize( _scriptable.getDocument() );

            _scriptable.load();
        }


        public void jsFunction_alert( String message ) {
            _scriptable.alert( message );
        }
    }


    static public class Document extends JavaScriptEngine {

        private HTMLPage.Scriptable _scriptable;
        private Scriptable _forms;


        public String getClassName() {
            return "Document";
        }


        public boolean has( String propertyName, Scriptable scriptable ) {
            return super.has( propertyName, scriptable ) || _scriptable.get( propertyName ) != null;
        }


        public Object get( String propertyName, Scriptable scriptable ) {
            Object result = super.get( propertyName, scriptable );
            if (result != NOT_FOUND) return result;
            if (_scriptable == null) return NOT_FOUND;

            final Object delegate = _scriptable.get( propertyName );
            if (delegate == null) return NOT_FOUND;

            try {
                return toScriptable( (WebForm.Scriptable) delegate );
            } catch (PropertyException e) {
                throw new RuntimeException( e.toString() );
            } catch (NotAFunctionException e) {
                throw new RuntimeException( e.toString() );
            } catch (JavaScriptException e) {
                throw new RuntimeException( e.toString() );
            }
        }


        void initialize( HTMLPage.Scriptable scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException {
            _scriptable = scriptable;
            _scriptable.setScriptEngine( this );
            initializeForms();
        }


        public String jsGet_title() throws SAXException {
            return _scriptable.getTitle();
        }


        public Scriptable jsGet_forms() {
            return _forms;
        }


        private void initializeForms() throws PropertyException, NotAFunctionException, JavaScriptException {
            WebForm.Scriptable scriptables[] = _scriptable.getForms();
            Form[] forms = new Form[ scriptables.length ];
            for (int i = 0; i < forms.length; i++) {
                forms[i] = toScriptable( scriptables[i] );
            }
            _forms = Context.getCurrentContext().newArray( this, forms );
        }


        private Form toScriptable( final WebForm.Scriptable scriptable )
                throws PropertyException, NotAFunctionException, JavaScriptException {
            final Form form = (Form) Context.getCurrentContext().newObject( this, "Form" );
            form.initialize( scriptable );
            return form;
        }
    }


    static public class Form extends JavaScriptEngine {

        private WebForm.Scriptable _scriptable;


        public String getClassName() {
            return "Form";
        }


        void initialize( WebForm.Scriptable scriptable ) {
            _scriptable = scriptable;
            scriptable.setScriptEngine( this );
        }
    }


}
