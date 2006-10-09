package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2006, Russell Gold
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

import org.mozilla.javascript.*;
import com.meterware.httpunit.scripting.ScriptingEngine;
import com.meterware.httpunit.ScriptException;

import java.util.ArrayList;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public abstract class ScriptingEngineImpl extends ScriptableObject implements ScriptingEngine {

    private final static Object[] NO_ARGS = new Object[0];

    private static ArrayList _errorMessages = new ArrayList();


    static public void clearErrorMessages() {
        _errorMessages.clear();
    }


    static public String[] getErrorMessages() {
        return (String[]) _errorMessages.toArray( new String[ _errorMessages.size() ] );
    }


    static public void handleScriptException( Exception e, String badScript ) {
        final String errorMessage = badScript + " failed: " + e;
        if (!(e instanceof EcmaError) && !(e instanceof EvaluatorException)) {
            e.printStackTrace();
            throw new RuntimeException( errorMessage );
        } else if (JavaScript.isThrowExceptionsOnError()) {
            e.printStackTrace();
            throw new ScriptException( errorMessage );
        } else {
            _errorMessages.add( errorMessage );
        }
    }

//--------------------------------------- ScriptingEngine methods ------------------------------------------------------

    public boolean supportsScriptLanguage( String language ) {
        return language == null || language.toLowerCase().startsWith( "javascript" );
    }


    public String runScript( String language, String script ) {
        if (!supportsScriptLanguage( language )) return "";
        try {
            script = script.trim();
            if (script.startsWith( "<!--" )) {
                script = withoutFirstLine( script );
                if (script.endsWith( "-->" )) script = script.substring( 0, script.lastIndexOf( "-->" ));
            }
            Context.getCurrentContext().evaluateString( this, script, "httpunit", 0, null );
            StringBuffer buffer = getDocumentWriteBuffer();
            return buffer.toString();
        } catch (Exception e) {
            handleScriptException( e, "Script '" + script + "'" );
            return "";
        } finally {
            discardDocumentWriteBuffer();
        }
    }


    public boolean doEvent( String eventScript ) {
        try {
            final Context context = Context.getCurrentContext();
            context.setOptimizationLevel( -1 );
            Function f = context.compileFunction( this, "function x() { " + eventScript + "}", "httpunit", 0, null );
            Object result = f.call( context, this, this, NO_ARGS );
            return (!(result instanceof Boolean)) || ((Boolean) result).booleanValue();
        } catch (Exception e) {
            handleScriptException( e, "Event '" + eventScript + "'" );
            return false;
        }
    }


    /**
     * Evaluates the specified string as JavaScript. Will return null if the script has no return value.
     */
    public String evaluateExpression( String expression ) {
        try {
            Object result = Context.getCurrentContext().evaluateString( this, expression, "httpunit", 0, null );
            return (result == null || result instanceof Undefined) ? null : result.toString();
        } catch (Exception e) {
            handleScriptException( e, "URL '" + expression + "'" );
            return null;
        }
    }

//------------------------------------------ protected methods ---------------------------------------------------------

    protected StringBuffer getDocumentWriteBuffer() {
        throw new IllegalStateException( "may not run runScript() from " + getClass() );
    }

    protected void discardDocumentWriteBuffer() {
        throw new IllegalStateException( "may not run runScript() from " + getClass() );
    }

    private String withoutFirstLine( String script ) {
        for (int i=0; i < script.length(); i++) {
            if (isLineTerminator( script.charAt(i) )) return script.substring( i ).trim();
        }
        return "";
    }

    private boolean isLineTerminator( char c ) {
        return c == 0x0A || c == 0x0D;
    }
}
