package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000, Russell Gold
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
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * A unit test of the httpunit parsing classes.
 **/
public class HtmlTablesTest extends TestCase {

	public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
	}
	
	
	public static Test suite() {
		return new TestSuite( HtmlTablesTest.class );
	}


	public HtmlTablesTest( String name ) {
	    super( name );
	}


    public void setUp() throws Exception {
        URL baseURL = new URL( "http://www.meterware.com" );
        _noTables  = new ReceivedPage( baseURL, HEADER + "<body>This has no tables but it does" +
                                       "have <a href=\"/other.html\">an active link</A>" +
                                       " and <a name=here>an anchor</a>" +
                                       "</body></html>" );
        _oneTable  = new ReceivedPage( baseURL, HEADER + "<body><h2>Interesting data</h2>" +
                                       "<table summary=\"tough luck\">" +
                                       "<tr><td>One</td><td>&nbsp;</td><td>1</td></tr>" +
                                       "<tr><td colspan=3><IMG SRC=\"/images/spacer.gif\" ALT=\"\" WIDTH=1 HEIGHT=1></td></tr>" + 
                                       "<tr><td>Two</td><td>&nbsp;</td><td>2</td></tr>" +
                                       "<tr><td colspan=3><IMG SRC=\"/images/spacer.gif\" ALT=\"\" WIDTH=1 HEIGHT=1></td></tr>" + 
                                       "<tr><td>Three</td><td>&nbsp;</td><td>3</td></tr>" +
                                       "</table></body></html>" );
        _nestTable = new ReceivedPage( baseURL, HEADER + "<body><h2>Interesting data</h2>" +
                                       "<table summary=\"outer one\">" +
                                       "<tr><td>" +
                                       "Inner Table<br>" +
                                       "<table summary=\"inner one\">" +
                                       "        <tr><td>Red</td><td>1</td></tr>" +
                                       "        <tr><td>Blue</td><td>2</td></tr>" +
                                       "</table></td></tr>" +
                                       "</table></body></html>" );
        _spanTable = new ReceivedPage( baseURL, HEADER + "<body><h2>Interesting data</h2>" +
                                       "<table summary=\"tough luck\">" +
                                       "<tr><th colspan=2>Colors</th><th>Names</th></tr>" +
                                       "<tr><td>Red</td><td rowspan=\"2\"><b>gules</b></td><td>rot</td></tr>" +
                                       "<tr><td>Green</td><td><a href=\"nowhere\">vert</a></td></tr>" +
                                       "</table></body></html>" );
    }
	
	
	public void testFindNoTables() {
        WebTable[] tables = _noTables.getTables();
        assertNotNull( tables );
        assertEquals( 0, tables.length );
    }


    public void testFindOneTable() {
        WebTable[] tables = _oneTable.getTables();
        assertEquals( 1, tables.length );
    }


    public void testFindTableSize() {
        WebTable table = _oneTable.getTables()[0];
        assertEquals( 5, table.getRowCount() );
        assertEquals( 3, table.getColumnCount() );
        try {
            table.getCellAsText( 5, 0 );
            fail( "Should throw out of range exception" );
        } catch (IndexOutOfBoundsException e ) {
        }
        try {
            table.getCellAsText( 0, 3 );
            fail( "Should throw out of range exception" );
        } catch (RuntimeException e ) {
        }
    }


    public void testFindTableCell() {
        WebTable table = _oneTable.getTables()[0];
        assertEquals( "Two", table.getCellAsText( 2, 0 ) );
        assertEquals( "3",   table.getCellAsText( 4, 2 ) );
    }


    public void testTableAsText() {
       WebTable table = _oneTable.getTables()[0];
       table.purgeEmptyCells();
       String[][] text = table.asText();
       assertEquals( "rows with text", 3, text.length );
       assertEquals( "Two", text[1][0] );
       assertEquals( "3", text[2][1] );
       assertEquals( "columns with text", 2, text[0].length );
    }



    public void testNestedTable() {
        WebTable[] tables = _nestTable.getTables();
        assertEquals( "top level tables count", 1, tables.length );
        assertEquals( "rows", 1, tables[0].getRowCount() );
        assertEquals( "columns", 1, tables[0].getColumnCount() );
        WebTable[] nested = tables[0].getTableCell( 0, 0 ).getTables();
        assertEquals( "nested tables count", 1, nested.length );
        assertEquals( "nested rows", 2, nested[0].getRowCount() );
        assertEquals( "nested columns", 2, nested[0].getColumnCount() );

        String nestedString = tables[0].getCellAsText( 0, 0 );
        System.out.println( "Nested string=" + nestedString ); 
        assert( "Cannot find 'Red' in string", nestedString.indexOf( "Red" ) >= 0 );
        assert( "Cannot find 'Blue' in string", nestedString.indexOf( "Blue" ) >= 0 );
    }


    public void testColumnSpan() {
        WebTable table = _spanTable.getTables()[0];
        assertEquals( "Colors", table.getCellAsText( 0, 0 ) );
        assertEquals( "Colors", table.getCellAsText( 0, 1 ) );
        assertEquals( "Names",  table.getCellAsText( 0, 2 ) );
    }

    public void testRowSpan() {
        WebTable table = _spanTable.getTables()[0];
        assertEquals( 3, table.getRowCount() );
        assertEquals( 3, table.getColumnCount() );
        assertEquals( "gules", table.getCellAsText( 1, 1 ) );
        assertEquals( "gules", table.getCellAsText( 2, 1 ) );
        assertEquals( "vert",  table.getCellAsText( 2, 2 ) );
    }


    private final static String HEADER = "<html><head><title>A Sample Page</title></head>";
    private ReceivedPage _noTables;
    private ReceivedPage _oneTable;
    private ReceivedPage _nestTable;
    private ReceivedPage _spanTable;
}

