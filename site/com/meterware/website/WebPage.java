package com.meterware.website;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

import com.meterware.website.MenuTarget;
import com.meterware.website.PageFragment;
import com.meterware.website.SiteTemplate;



/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class WebPage extends MenuTarget {

    private String _title;
    private boolean _mainPage;
    private boolean _license;

    private ArrayList _fragments = new ArrayList();
    private static File _root;


    public String getTitle() {
        return _title;
    }


    public void setTitle( String title ) {
        _title = title;
    }


    public String getItem() {
        String item = super.getItem();
        return item != null ? item : _title;
    }


    public void setMainPage( boolean mainPage ) {
        _mainPage = mainPage;
    }


    public boolean isMainPage() {
        return _mainPage;
    }


    public boolean isLicense() {
        return _license;
    }


    public void setLicense( boolean license ) {
        _license = license;
    }


    public PageFragment createFragment() {
        PageFragment fragment = new PageFragment();
        _fragments.add( fragment );
        return fragment;
    }


    public void generatePage( SiteTemplate template ) {
        try {
            StringBuffer sb = new StringBuffer();
            template.appendPageHeader( sb, this );
            for (int i = 0; i < _fragments.size(); i++) {
                PageFragment pageFragment = (PageFragment) _fragments.get( i );
                sb.append( pageFragment.asText() ).append( FragmentTemplate.LINE_BREAK );
            }
            template.appendPageFooter( sb, this );

            File file = new File( _root, _location );
            file.getParentFile().mkdir();
            FileWriter fw = new FileWriter( file );
            fw.write( sb.toString() );
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException( "Error writing page '" + e );
        }
    }


    private void ensureExists( File directory ) {
        if (!directory.exists()) {
            ensureExists( directory.getParentFile() );
        }
        directory.mkdir();
    }


    public void appendMenuItem( StringBuffer sb, SiteTemplate template, String currentLocation ) {
        template.appendMenuItem( sb, currentLocation, getItem(), getLocation() );
    }


    public static void setRoot( File directory ) {
        _root = directory;
    }


}
