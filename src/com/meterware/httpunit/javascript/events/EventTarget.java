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
 * The EventTarget interface is implemented by all Nodes in an implementation which supports the DOM
 * Event Model. Therefore, this interface can be obtained by using binding-specific casting methods
 * on an instance of the Node interface. The interface allows registration and removal of
 * EventListeners on an EventTarget and dispatch of events to that EventTarget.
 * 
 * @author W3C
 * @version $Id$
 */
public interface EventTarget
    extends Scriptable
{
    /**
     * This method allows the registration of event listeners on the event target. If an
     * EventListener is added to an EventTarget while it is processing an event, it will not be
     * triggered by the current actions but may be triggered during a later stage of event flow,
     * such as the bubbling phase. If multiple identical EventListeners are registered on the same
     * EventTarget with the same parameters the duplicate instances are discarded. They do not cause
     * the EventListener to be called twice and since they are discarded they do not need to be
     * removed with the removeEventListener method.
     * 
     * @param type The event type for which the user is registering.
     * @param listener The listener parameter takes an interface implemented by the user which
     *        contains the methods to be called when the event occurs.
     * @param useCapture If true, useCapture indicates that the user wishes to initiate capture.
     *        After initiating capture, all events of the specified type will be dispatched to the
     *        registered EventListener before being dispatched to any EventTargets beneath them in
     *        the tree. Events which are bubbling upward through the tree will not trigger an
     *        EventListener designated to use capture.
     */
    public void jsFunction_addEventListener(String type, Scriptable listener, boolean useCapture);
    
    /**
     * This method allows the removal of event listeners from the event target. If an EventListener
     * is removed from an EventTarget while it is processing an event, it will not be triggered by
     * the current actions. EventListeners can never be invoked after being removed. Calling
     * removeEventListener with arguments which do not identify any currently registered
     * EventListener on the EventTarget has no effect.
     * 
     * @param type Specifies the event type of the EventListener being removed.
     * @param listener The EventListener parameter indicates the EventListener to be removed.
     * @param useCapture Specifies whether the EventListener being removed was registered as a
     *        capturing listener or not. If a listener was registered twice, one with capture and
     *        one without, each must be removed separately. Removal of a capturing listener does not
     *        affect a non-capturing version of the same listener, and vice versa.
     */
    public void jsFunction_removeEventListener(String type, Scriptable listener, boolean useCapture);
    
    /**
     * This method allows the dispatch of events into the implementations event model. Events
     * dispatched in this manner will have the same capturing and bubbling behavior as events
     * dispatched directly by the implementation. The target of the event is the EventTarget on
     * which dispatchEvent is called.
     * 
     * @param evt Specifies the event type, behavior, and contextual information to be used in
     *        processing the event.
     * @return The return value of dispatchEvent indicates whether any of the listeners which
     *         handled the event called preventDefault. If preventDefault was called the value is
     *         false, else the value is true.
     * @throws EventException UNSPECIFIED_EVENT_TYPE_ERR: Raised if the Event's type was not
     *         specified by initializing the event before dispatchEvent was called. Specification of
     *         the Event's type as null or an empty string will also trigger this exception.
     */
    public boolean jsFunction_dispatchEvent(Scriptable evt) throws EventException;
}
