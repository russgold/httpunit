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
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebImage;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.mozilla.javascript.*;
import org.xml.sax.SAXException;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class JavaScript {

    private final static Object[] NO_ARGS = new Object[0];


    /**
     * Initiates JavaScript execution for the specified web response.
     */
    static void run( WebResponse response ) throws IllegalAccessException, InstantiationException,
            InvocationTargetException, ClassDefinitionException, NotAFunctionException,
            PropertyException, SAXException, JavaScriptException {
        Context context = Context.enter();
        Scriptable scope = context.initStandardObjects( null );
        initHTMLObjects( scope );

        Window w = (Window) context.newObject( scope, "Window" );
        w.initialize( null, response.getScriptableObject() );
    }


    private static void initHTMLObjects( Scriptable scope ) throws IllegalAccessException, InstantiationException,
            InvocationTargetException, ClassDefinitionException, PropertyException {
        ScriptableObject.defineClass( scope, Window.class );
        ScriptableObject.defineClass( scope, Document.class );
        ScriptableObject.defineClass( scope, Link.class );
        ScriptableObject.defineClass( scope, Form.class );
        ScriptableObject.defineClass( scope, Control.class );
        ScriptableObject.defineClass( scope, Link.class );
        ScriptableObject.defineClass( scope, Image.class );
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


        public boolean performEvent( String eventScript ) {
            try {
                final Context context = Context.getCurrentContext();
                Function f = context.compileFunction( this, "function x() { " + eventScript + "}", "httpunit", 0, null );
                Object result = f.call( context, this, null, NO_ARGS );
                return (result instanceof Boolean) ? ((Boolean) result).booleanValue() : true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException( "Script '" + eventScript + "' failed: " + e );
            }
        }


        void initialize( JavaScriptEngine parent, com.meterware.httpunit.ScriptableObject scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            _scriptable = scriptable;
            _scriptable.setScriptEngine( this );
            if (parent != null) setParentScope( parent );
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
         * This default implementation throws an exception.
         **/
        Scriptable toScriptable( com.meterware.httpunit.ScriptableObject delegate )
                throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            throw new UnsupportedOperationException();
        }

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


        void initialize( JavaScriptEngine parent, com.meterware.httpunit.ScriptableObject scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            _document = (Document) Context.getCurrentContext().newObject( this, "Document" );
            _document.initialize( this, getDelegate().getDocument() );

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
        private Scriptable _links;
        private Scriptable _images;


        public String getClassName() {
            return "Document";
        }


        void initialize( JavaScriptEngine parent, com.meterware.httpunit.ScriptableObject scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            initializeLinks();
            initializeForms();
            initializeImages();
        }


        public String jsGet_title() throws SAXException {
            return getDelegate().getTitle();
        }


        public Scriptable jsGet_images() {
            return _images;
        }


        public Scriptable jsGet_links() {
            return _links;
        }


        public Scriptable jsGet_forms() {
            return _forms;
        }


        private void initializeImages() throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            WebImage.Scriptable scriptables[] = getDelegate().getImages();
            Image[] images = new Image[ scriptables.length ];
            for (int i = 0; i < images.length; i++) {
                images[ i ] = (Image) toScriptable( scriptables[ i ] );
            }
            _images = Context.getCurrentContext().newArray( this, images );
        }


        private void initializeLinks() throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            WebLink.Scriptable scriptables[] = getDelegate().getLinks();
            Link[] links = new Link[ scriptables.length ];
            for (int i = 0; i < links.length; i++) {
                links[ i ] = (Link) toScriptable( scriptables[ i ] );
            }
            _links = Context.getCurrentContext().newArray( this, links );
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
            JavaScriptEngine element = (JavaScriptEngine) Context.getCurrentContext().newObject( this, getScriptableClassName( delegate ) );
            element.initialize( this, delegate );
            return element;
        }


        private String getScriptableClassName( com.meterware.httpunit.ScriptableObject delegate ) {
            if (delegate instanceof WebForm.Scriptable) {
                return "Form";
            } else if (delegate instanceof WebLink.Scriptable) {
                return "Link";
            } else if (delegate instanceof WebImage.Scriptable) {
                return "Image";
            } else {
                throw new IllegalArgumentException( "Unknown ScriptableObject class: " + delegate.getClass() );
            }
        }


        private HTMLPage.Scriptable getDelegate() {
            return (HTMLPage.Scriptable) _scriptable;
        }

    }


    abstract static public class HTMLElement extends JavaScriptEngine {

        private Document _document;


        public Document jsGet_document() {
            return _document;
        }


        void initialize( JavaScriptEngine parent, com.meterware.httpunit.ScriptableObject scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            _document = (Document) parent;
        }

    }


    static public class Image extends HTMLElement {

        public String getClassName() {
            return "Image";
        }
    }


    static public class Link extends HTMLElement {

        public Document jsGet_document() {
            return super.jsGet_document();
        }


        public String getClassName() {
            return "Link";
        }
    }


    static public class Form extends HTMLElement {

        public String getClassName() {
            return "Form";
        }


        Scriptable toScriptable( com.meterware.httpunit.ScriptableObject delegate )
                throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            final Control control = (Control) Context.getCurrentContext().newObject( this, "Control" );
            control.initialize( this, delegate );
            return control;
        }

    }


    static public class Control extends JavaScriptEngine {

        public String getClassName() {
            return "Control";
        }
    }

}
