package com.meterware.httpunit.scripting;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public interface FormScriptable {

    void setAction( String newAction );


    boolean doEvent( String eventScript );


    void setParameterValue( String name, String value );
}
