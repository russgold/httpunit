package com.meterware.httpunit;
/*******************************************************************************************
 * $ID$
 ******************************************************************************************/

import java.net.URL;

import java.util.Vector;

import org.w3c.dom.*;

/**
 * This class represents a table in an HTML page.
 **/
public class WebTable {


    /**
     * Returns the number of rows in the table.
     **/
    public int getRowCount() {
        return _dom.getElementsByTagName( "tr" ).getLength();
    }


    /**
     * Returns the number of columns in the table.
     **/
    public int getColumnCount() {
        NodeList nl = _dom.getElementsByTagName( "tr" );
        int maxColumnCount = 0;

        for (int i = 0; i < nl.getLength(); i++) {
            maxColumnCount = Math.max( maxColumnCount, getColumnCount( (Element) nl.item(i) ) );
        }
        return maxColumnCount;
    }


    /**
     * Returns the contents of the specified table cell as text. 
     * The row and column numbers are zero-based.
     * @throws IndexOutOfBoundsException if the specified cell numbers are not valid
     **/
    public String getCell( int row, int column ) {
        NodeList nl = _dom.getElementsByTagName( "tr" );
        if (row < 0 || row >= nl.getLength()) throw new IndexOutOfBoundsException( "Table has no row " + row );

        nl = ((Element) nl.item( row )).getElementsByTagName( "td" );
        if (column < 0 || column >= nl.getLength()) throw new IndexOutOfBoundsException( "Table has no column " + column );
        return nl.item( column ).getFirstChild().getNodeValue();
    }


//----------------------------------- package members -----------------------------------


    /**
     * Returns the top-level tables found in the specified DOM.
     **/
    static WebTable[] getTables( Document document ) {
        NodeList nl = document.getElementsByTagName( "table" );
        Vector topLevelTables = new Vector();

        for (int i = 0; i < nl.getLength(); i++) {
            if (isTopLevelTable( nl.item(i) )) {
                topLevelTables.addElement( new WebTable( nl.item(i) ) );
            }
        }

        WebTable[] result = new WebTable[ topLevelTables.size() ];
        topLevelTables.copyInto( result );
        return result;
    }



//----------------------------------- private members -----------------------------------

    private Element _dom;


    private WebTable( Node domTreeRoot ) {
        _dom = (Element) domTreeRoot;
    }


    /**
     * Returns the number of columns in the specified table row.
     **/
    private int getColumnCount( Element tableRow ) {
        NodeList nl = tableRow.getElementsByTagName( "td" );
        return nl.getLength();
    }


    private static boolean isTopLevelTable( Node tableNode ) {
        Node node = tableNode.getParentNode();
        while (true) {
            if (node.getNodeName().equalsIgnoreCase( "body" )) {
                return true;
            } else if (node.getNodeName().equalsIgnoreCase( "table" )) {
                return false;
            } else if (node.getParentNode() == null) {
                return true;
            }
            node = node.getParentNode();
        }
    }
}

