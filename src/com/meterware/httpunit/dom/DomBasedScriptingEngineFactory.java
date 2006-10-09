package com.meterware.httpunit.dom;
import com.meterware.httpunit.scripting.ScriptingEngineFactory;
import com.meterware.httpunit.scripting.ScriptingHandler;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.HTMLElement;

import java.util.logging.Logger;

/**
 * The scripting engine factory which relies directly on the DOM.
 */
public class DomBasedScriptingEngineFactory implements ScriptingEngineFactory {

    public boolean isEnabled() {
        try {
            Class.forName( "org.mozilla.javascript.Context" );
            return true;
        } catch (Exception e) {
            Logger.getLogger( "httpunit.org" ).warning( "Rhino classes (js.jar) not found - Javascript disabled" );
            return false;
        }
    }


    public void associate( WebResponse response ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void load( WebResponse response ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void setThrowExceptionsOnError( boolean throwExceptions ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean isThrowExceptionsOnError() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public String[] getErrorMessages() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void clearErrorMessages() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public ScriptingHandler createHandler( HTMLElement elementBase ) {
        return (ScriptingHandler) elementBase.getNode();
    }
}
