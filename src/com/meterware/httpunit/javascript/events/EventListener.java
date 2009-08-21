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

import org.mozilla.javascript.Scriptable;

/**
 * The EventListener interface is the primary method for handling events. Users implement the
 * EventListener interface and register their listener on an EventTarget using the AddEventListener
 * method. The users should also remove their EventListener from its EventTarget after they have
 * completed using the listener. When a Node is copied using the cloneNode method the EventListeners
 * attached to the source Node are not attached to the copied Node. If the user wishes the same
 * EventListeners to be added to the newly created copy the user must add them manually.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id$
 */
public interface EventListener
    extends Scriptable
{
    /**
     * This method is called whenever an event occurs of the type for which the EventListener
     * interface was registered.
     * 
     * @param evt The Event contains contextual information about the event. It also contains the
     *        stopPropagation and preventDefault methods which are used in determining the event's
     *        flow and default action.
     */
    public void jsFunction_handleEvent(Scriptable evt);
}
