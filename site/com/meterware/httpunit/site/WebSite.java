package com.meterware.httpunit.site;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2003, Russell Gold
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
import com.meterware.website.FragmentTemplate;
import com.meterware.website.Site;
import com.meterware.website.FilePageGenerator;
import com.meterware.website.PageGenerator;
import com.meterware.website.BasicSiteTemplate;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class WebSite extends Site {


    public static void main( String[] args ) {
        try {
            if (args.length == 0) {
                printUsage();
            } else {
                FragmentTemplate.registerTemplate( new Faq() );
                FragmentTemplate.registerTemplate( new News() );
                FragmentTemplate.registerTemplate( new Citations() );
                FragmentTemplate.registerTemplate( new Developers() );
                generate( new File( "sitedocs/site.xml" ), new File( args[0] ) );
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    public static void generate( File siteFile, File directory ) throws SAXException, IOException {
        generate( siteFile, new FilePageGenerator( directory ) );
    }


    public static void generate( File siteFile, PageGenerator generator ) throws SAXException, IOException {
        Site site = new WebSite();
        BasicSiteTemplate template = new BasicSiteTemplate();
        generate( site, siteFile, template, generator );
    }



    private static void printUsage() {
        System.out.println( "Usage: " + WebSite.class.getName() + " <target-directory>" );
    }

}
