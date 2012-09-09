package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2007, Russell Gold
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class DomEventScriptingTest extends AbstractHTMLElementTest {

    private Context _context;
    private static final Object[] NO_ARGS = new Object[0];


    @Before
    public void setUp() throws Exception {
        _context = Context.enter();
        _context.initStandardObjects(null);
    }


    @After
    public void tearDown() throws Exception {
        Context.exit();
    }


    /**
     * Verifies that the 'onload' event for a body element is initially undefined if no corresponding attribute
     * is defined.
     */
    @Test
    public void testNoOnloadEvent() throws Exception {
        HTMLBodyElementImpl body = (HTMLBodyElementImpl) createElement("body");
        assertNull("Found a default definition for 'onLoad' event", body.getOnloadEvent());
    }


    /**
     * Verifies that the 'onload' event for a body element is initially defined if a corresponding attribute
     * is defined.
     */
    @Test
    public void testInlineOnloadEvent() throws Exception {
        HTMLBodyElementImpl body = (HTMLBodyElementImpl) createElement("body", new Object[][]{{"onload", "title='here'"}});
        assertNotNull("Found no definition for 'onLoad' event", body.getOnloadEvent());
        body.getOnloadEvent().call(_context, body, body, NO_ARGS);
        assertEquals("Updated title", "here", body.getTitle());
    }

}
