/********************************************************************************************************************
 * $Id: FormScriptingTest.java 1031 2009-08-17 12:15:24Z wolfgang_fahl $
 * $URL: https://httpunit.svn.sourceforge.net/svnroot/httpunit/trunk/httpunit/test/com/meterware/httpunit/javascript/FormScriptingTest.java $
 *
 * Copyright (c) 2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J., 
 * Copyright (c) 2009, Wolfgang Fahl
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
package com.meterware.httpunit.javascript.events;

/**
 * Event operations may throw an EventException as specified in their method descriptions.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id$
 */
public class EventException
    extends Exception
{
    /**
     * If the Event's type was not specified by initializing the event before the method was called.
     * Specification of the Event's type as null or an empty string will also trigger this
     * exception.
     */
    private static final short UNSPECIFIED_EVENT_TYPE_ERR = 0;
    
    /**
     * An integer indicating the type of error generated.
     */
    private final short code;

    /**
     * Creates new EventException instance.
     * 
     * @param codeArg An integer indicating the type of error generated.
     */
    public EventException(short codeArg) {
        this.code = codeArg;
    }

    /**
     * An integer indicating the type of error generated.
     */
    public short getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage() {
        return Short.toString(code);
    }
}
