package com.meterware.httpunit;

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
                                       "<tr><td>One</td><td>1</td></tr>" +
                                       "<tr><td>Two</td><td>2</td></tr>" +
                                       "<tr><td>Three</td><td>3</td></tr>" +
                                       "</table></body></html>" );
        _nestTable = new ReceivedPage( baseURL, HEADER + "<body><h2>Interesting data</h2>" +
                                       "<table summary=\"outer one\">" +
                                       "<tr><td><table summary=\"inner one\">" +
                                       "        <tr><td>Red</td><td>1</td></tr>" +
                                       "        <tr><td>Blue</td><td>2</td></tr>" +
                                       "</table></td></tr>" +
                                       "</table></body></html>" );
        _spanTable = new ReceivedPage( baseURL, HEADER + "<body><h2>Interesting data</h2>" +
                                       "<table summary=\"tough luck\">" +
                                       "<tr><td colspan=2>Colors</td><td>Names</td></tr>" +
                                       "<tr><td>Red</td><td rowspan=\"2\"><b>gules</b></td><td>rot</td></tr>" +
                                       "<tr><td>Green</td><td>vert</td></tr>" +
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
        assertEquals( 3, table.getRowCount() );
        assertEquals( 2, table.getColumnCount() );
        try {
            table.getCell( 5, 0 );
            fail( "Should throw out of range exception" );
        } catch (IndexOutOfBoundsException e ) {
        }
        try {
            table.getCell( 0, 2 );
            fail( "Should throw out of range exception" );
        } catch (RuntimeException e ) {
        }
    }


    public void testNestedTable() {
        WebTable[] tables = _nestTable.getTables();
        assertEquals( "top level tables count", 1, tables.length );
        assertEquals( "rows", 1, tables[0].getRowCount() );
        assertEquals( "columns", 1, tables[0].getColumnCount() );
    }


    public void testFindTableCell() {
        WebTable table = _oneTable.getTables()[0];
        assertEquals( "Two", table.getCell( 1, 0 ) );
        assertEquals( "3",   table.getCell( 2, 1 ) );
    }

    public void testColumnSpan() {
        WebTable table = _spanTable.getTables()[0];
        assertEquals( "Colors", table.getCell( 0, 0 ) );
        assertEquals( "Colors", table.getCell( 0, 1 ) );
        assertEquals( "Names",  table.getCell( 0, 2 ) );
    }

    public void testRowSpan() {
        WebTable table = _spanTable.getTables()[0];
        assertEquals( 3, table.getRowCount() );
        assertEquals( 3, table.getColumnCount() );
        assertEquals( "gules", table.getCell( 1, 1 ) );
        assertEquals( "gules", table.getCell( 2, 1 ) );
        assertEquals( "vert",  table.getCell( 2, 2 ) );
    }



    private final static String HEADER = "<html><head><title>A Sample Page</title></head>";
    private ReceivedPage _noTables;
    private ReceivedPage _oneTable;
    private ReceivedPage _nestTable;
    private ReceivedPage _spanTable;
}

