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
import com.meterware.website.*;
import com.meterware.xml.DocumentSemantics;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class Site {

    private ArrayList _pages = new ArrayList();

    private String _project;
    private String _logo;
    private int _groupId;
    private SiteTemplate _template = new Template();
    private CopyRight _copyRight;


    public static void generate( File siteFile, File directory ) throws SAXException, IOException, ParserConfigurationException, IntrospectionException, IllegalAccessException, InvocationTargetException, ParseException {
        Document document = parseDocument( siteFile );
        Site site = new Site();
        DocumentSemantics.build( document, site );

        WebPage.setRoot( directory );
        for (int i = 0; i < site._pages.size(); i++) {
            SiteElement siteElement = (SiteElement) site._pages.get( i );
            siteElement.generatePage( site._template );
        }
    }


    private static Document parseDocument( File file ) throws SAXException, IOException, ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( file );
    }


    public WebPage createPage() {
        WebPage page = new WebPage();
        _pages.add( page );
        return page;
    }


    public ExternalPage createExternal() {
        ExternalPage page = new ExternalPage();
        _pages.add( page );
        return page;
    }


    public ProjectPage createProjectPage() {
        ProjectPage page = new ProjectPage();
        _pages.add( page );
        return page;
    }


    public MenuSpace createSpace() {
        MenuSpace page = new MenuSpace();
        _pages.add( page );
        return page;
    }


    public String getProject() {
        return _project;
    }


    public void setProject( String project ) {
        _project = project;
    }


    public String getLogo() {
        return _logo;
    }


    public void setLogo( String logo ) {
        _logo = logo;
    }


    public int getGroupId() {
        return _groupId;
    }


    public void setGroupId( int groupId ) {
        _groupId = groupId;
    }


    public CopyRight createCopyright() {
        _copyRight = new CopyRight();
        return _copyRight;
    }


    private String getBugEntryURL() {
        return getTrackerURL( 100000 );
    }


    private String getFeatureEntryURL() {
        return getTrackerURL( 350000 );
    }


    private String getTrackerURL( int trackerType ) {
        return "http://sourceforge.net/tracker/?group_id=" + _groupId + "&atid=" + (trackerType + _groupId);
    }


    public class ProjectPage extends ExternalPage {

        public void setType( String type ) {
            if (type.equalsIgnoreCase( "bugs" )) {
                super.setLocation( getBugEntryURL() );
            } else if (type.equalsIgnoreCase( "enhancements" )) {
                super.setLocation( getFeatureEntryURL() );
            } else if (type.equalsIgnoreCase( "cvs" )) {
                super.setLocation( "http://sourceforge.net/cvs/?group_id=" + _groupId );
            }
        }


        public void setLocation( String location ) {}

    }


    public class CopyRight {
        private String _owner;
        private String _dates;

        String getNotice() {
            StringBuffer sb = new StringBuffer();
            sb.append( "Copyright &copy; ").append( _dates ).append( ", " ).append( _owner );
            return sb.toString();
        }


        public String getOwner() {
            return _owner;
        }


        public void setOwner( String owner ) {
            _owner = owner;
        }


        public String getDates() {
            return _dates;
        }


        public void setDates( String dates ) {
            _dates = dates;
        }
    }


    class Template implements SiteTemplate {

        public void appendPageHeader( StringBuffer sb, WebPage currentPage ) {
            sb.append( "<html><head><title>" ).append( _project ).append( ' ' ).append( currentPage.getTitle() ).append( "</title>" ).append( FragmentTemplate.LINE_BREAK );
            sb.append( "<link rel='stylesheet' href='" ).append( relativeURL( currentPage.getLocation(), "site.css"  ) ).append( "' type='text/css'>" ).append( FragmentTemplate.LINE_BREAK );
            sb.append( "</head>" ).append( FragmentTemplate.LINE_BREAK );
            sb.append( "<body>" ).append( FragmentTemplate.LINE_BREAK );
            sb.append( "<image src='" ).append( relativeURL( currentPage.getLocation(), _logo ) ).append( "'/>" ).append( FragmentTemplate.LINE_BREAK );
            sb.append( "<h1>" ).append( currentPage.getTitle() ).append( "</h1>" ).append( FragmentTemplate.LINE_BREAK );
            appendMenu( sb, currentPage.getLocation() );
            sb.append( "<div id='Content'>" ).append( FragmentTemplate.LINE_BREAK );
            if (currentPage.isLicense()) sb.append( "<p>" ).append( _copyRight.getNotice() ).append( "</p>" ). append( FragmentTemplate.LINE_BREAK );
         }


        public void appendPageFooter( StringBuffer sb, WebPage currentPage ) {
            if (!currentPage.isLicense()) sb.append( "<hr/><p>" ).append( _copyRight.getNotice() ).append( FragmentTemplate.LINE_BREAK );
            if (currentPage.isMainPage()) {
                sb.append( "<P class='sourceforge'>Hosted by <IMG SRC='http://sourceforge.net/sflogo.php?group_id=" );
                sb.append( _groupId ).append( "&type=1' width='88' height='31' border='0' alt='SourceForge Logo' " );
                sb.append( "naturalsizeflag='0' align='TOP'></p>" );
            }
            sb.append( "</div>" ).append( FragmentTemplate.LINE_BREAK );
            sb.append( "</body></html>" );
        }


        public void appendMenuItem( StringBuffer sb, String source, String title, String destination ) {
            if (source.equals( destination )) {
                sb.append( "  <b>" ).append( title ).append( "</b>" );
            } else {
                sb.append( "  <a href='" ).append( relativeURL( source, destination ) ).append( "'>" ).append( title ).append( "</a>" );
            }
            appendEndOfItem( sb );
        }


        public void appendMenuSpace( StringBuffer sb ) {
            appendEndOfItem( sb );
        }


        private void appendEndOfItem( StringBuffer sb ) {
            sb.append( "<br/>" ).append( FragmentTemplate.LINE_BREAK );
        }


        private void appendMenu( StringBuffer sb, String currentLocation ) {
            sb.append( "<div id='Menu'>" ).append( FragmentTemplate.LINE_BREAK );
            for (int i = 0; i < _pages.size(); i++) {
                SiteElement siteElement = (SiteElement) _pages.get( i );
                siteElement.appendMenuItem( sb, this, currentLocation );
            }
            sb.append( "</div>" ).append( FragmentTemplate.LINE_BREAK );
        }


        private String relativeURL( String currentPage, String nextPage ) {
            if (nextPage.indexOf(':') >= 0) return nextPage;
            String[] currentPath = getRelativePath( currentPage );
            String[] nextPath = getRelativePath( nextPage );

            int baseIndex = 0;
            int maxIndex = Math.min( currentPath.length-1, nextPath.length-1 );
            while (baseIndex < maxIndex && currentPath[ baseIndex ].equals( nextPath[ baseIndex ] )) baseIndex++;

            StringBuffer sb = new StringBuffer();
            for (int i = baseIndex; i < currentPath.length-1; i++) sb.append( "../" );
            for (int i = baseIndex; i < nextPath.length-1; i++) sb.append( nextPath[i] ).append( '/' );
            sb.append( nextPath[ nextPath.length-1] );
            return sb.toString();
        }

    }


    private String[] getRelativePath( String url ) {
        ArrayList parts = new ArrayList();
        int start = 0;
        int end;
        do {
            end = url.indexOf( '/', start );
            if (end >= 0) {
                parts.add( url.substring( start, end ));
                start = end+1;
            }
        } while (end >= 0);
        parts.add( url.substring( start ) );
        return (String[]) parts.toArray( new String[ parts.size() ]);
    }


}
