package com.meterware.website;

import java.util.ArrayList;

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


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public abstract class FragmentTemplate {

    public final static String LINE_BREAK = System.getProperty( "line.separator" );
    private static ArrayList _templates = new ArrayList();

    public abstract FragmentTemplate newFragment();


    public abstract String asText();


    protected abstract String getRootNodeName();


    public static void registerTemplate( FragmentTemplate template ) {
        _templates.add( template );
    }


    static FragmentTemplate getTemplateFor( String nodeName ) {
        for (int i = 0; i < _templates.size(); i++) {
            FragmentTemplate fragmentTemplate = (FragmentTemplate) _templates.get( i );
            if (fragmentTemplate.getRootNodeName().equals( nodeName )) return fragmentTemplate.newFragment();
        }
        throw new RuntimeException( "No template defined for root node " + nodeName );
    }

}
