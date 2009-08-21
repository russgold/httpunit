/********************************************************************************************************************
 * $Id$
 * $URL$
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

import org.mozilla.javascript.ScriptableObject;

/**
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id$
 */
public abstract class Event
    extends ScriptableObject
{
    // -- Scriptable interface ------------------------------------------------------------------

    /** JavaScript class name. */
    private static final String JS_CLASS_NAME = "Event";

    /**
     * {@inheritDoc}
     */
    public String getClassName()
    {
        return JS_CLASS_NAME;
    }

    // -- DOM2 Events specification -------------------------------------------------------------

    /**
     * The current event phase is the capturing phase.
     */
    public static final short CAPTURING_PHASE = 1;

    /**
     * The event is currently being evaluated at the target EventTarget.
     */
    public static final short AT_TARGET = 2;

    /**
     * The current event phase is the bubbling phase.
     */
    public static final short BUBBLING_PHASE = 3;

    /**
     * The name of the event (case-insensitive). The name must be an XML name.
     */
    private String type;

    /**
     * Used to indicate whether or not an event is a bubbling event. If the event can bubble the
     * value is true, else the value is false.
     */
    private boolean bubbles;

    /**
     * Used to indicate whether or not an event can have its default action prevented. If the
     * default action can be prevented the value is true, else the value is false.
     */
    private boolean cancelable;

    /**
     * Used to specify the time (in milliseconds relative to the epoch) at which the event was
     * created. Due to the fact that some systems may not provide this information the value of
     * timeStamp may be not available for all events. When not available, a value of 0 will be
     * returned. Examples of epoch time are the time of the system start or 0:0:0 UTC 1st January
     * 1970.
     */
    private long timeStamp;

    /**
     * The name of the event (case-insensitive). The name must be an XML name.
     */
    public String jsGet_type()
    {
        return type;
    }

    /**
     * Used to indicate whether or not an event is a bubbling event. If the event can bubble the
     * value is true, else the value is false.
     */
    public boolean jsGet_bubbles()
    {
        return bubbles;
    }

    /**
     * Used to specify the time (in milliseconds relative to the epoch) at which the event was
     * created. Due to the fact that some systems may not provide this information the value of
     * timeStamp may be not available for all events. When not available, a value of 0 will be
     * returned. Examples of epoch time are the time of the system start or 0:0:0 UTC 1st January
     * 1970.
     */
    public boolean jsGet_cancelable()
    {
        return cancelable;
    }

    /**
     * Used to specify the time (in milliseconds relative to the epoch) at which the event was
     * created. Due to the fact that some systems may not provide this information the value of
     * timeStamp may be not available for all events. When not available, a value of 0 will be
     * returned. Examples of epoch time are the time of the system start or 0:0:0 UTC 1st January
     * 1970.
     */
    public long jsGet_timeStamp()
    {
        return timeStamp;
    }

    /**
     * Used to indicate the EventTarget to which the event was originally dispatched.
     */
    public abstract EventTarget jsGet_target();

    /**
     * Used to indicate the EventTarget whose EventListeners are currently being processed. This is
     * particularly useful during capturing and bubbling.
     */
    public abstract EventTarget jsGet_currentTarget();

    /**
     * Used to indicate which phase of event flow is currently being evaluated.
     */
    public abstract short jsGet_eventPhase();

    /**
     * The stopPropagation method is used prevent further propagation of an event during event flow.
     * If this method is called by any EventListener the event will cease propagating through the
     * tree. The event will complete dispatch to all listeners on the current EventTarget before
     * event flow stops. This method may be used during any stage of event flow.
     */
    public abstract void jsFunction_stopPropagation();

    /**
     * If an event is cancelable, the preventDefault method is used to signify that the event is to
     * be canceled, meaning any default action normally taken by the implementation as a result of
     * the event will not occur. If, during any stage of event flow, the preventDefault method is
     * called the event is canceled. Any default action associated with the event will not occur.
     * Calling this method for a non-cancelable event has no effect. Once preventDefault has been
     * called it will remain in effect throughout the remainder of the event's propagation. This
     * method may be used during any stage of event flow.
     */
    public abstract void jsFunction_preventDefault();

    /**
     * The initEvent method is used to initialize the value of an Event created through the
     * DocumentEvent interface. This method may only be called before the Event has been dispatched
     * via the dispatchEvent method, though it may be called multiple times during that phase if
     * necessary. If called multiple times the final invocation takes precedence. If called from a
     * subclass of Event interface only the values specified in the initEvent method are modified,
     * all other attributes are left unchanged.
     * 
     * @param eventTypeArg Specifies the event type. This type may be any event type currently
     *        defined in this specification or a new event type.. The string must be an XML name.
     *        Any new event type must not begin with any upper, lower, or mixed case version of the
     *        string "DOM". This prefix is reserved for future DOM event sets. It is also strongly
     *        recommended that third parties adding their own events use their own prefix to avoid
     *        confusion and lessen the probability of conflicts with other new events.
     * @param canBubbleArg Specifies whether or not the event can bubble.
     * @param cancelableArg Specifies whether or not the event's default action can be prevented.
     */
    public void jsFunction_initEvent(String eventTypeArg, boolean canBubbleArg,
        boolean cancelableArg)
    {
        type = eventTypeArg;
        bubbles = canBubbleArg;
        cancelable = cancelableArg;
    }
}
