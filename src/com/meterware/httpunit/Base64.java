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
public class Base64 {

    final static String encodingChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String encode( String source ) {
        byte[] sourceBytes = getPaddedBytes( source );
        int numGroups = (sourceBytes.length + 2) / 3;
        byte[] targetBytes = new byte[4];
        char[] target = new char[ 4 * numGroups ];

        for (int group = 0; group < numGroups; group++) {
            convert3To4( sourceBytes, group*3, targetBytes );
            for (int i = 0; i < targetBytes.length; i++) {
                target[ i + 4*group ] = encodingChar.charAt( targetBytes[i] );
            }
        }

        int numPadBytes = sourceBytes.length - source.length();

        for (int i = target.length-numPadBytes; i < target.length; i++) target[i] = '=';
        return new String( target );
    }


    private static byte[] getPaddedBytes( String source ) {
        byte[] converted = source.getBytes();
        int requiredLength = 3 * ((converted.length+2) /3);
        byte[] result = new byte[ requiredLength ];
        System.arraycopy( converted, 0, result, 0, converted.length );
        return result;
    }


    private static void convert3To4( byte[] source, int sourceIndex, byte[] target ) {
        target[0] = (byte) ( source[ sourceIndex ] >>> 2);
        target[1] = (byte) (((source[ sourceIndex   ] & 0x03) << 4) | (source[ sourceIndex+1 ] >>> 4));
        target[2] = (byte) (((source[ sourceIndex+1 ] & 0x0f) << 2) | (source[ sourceIndex+2 ] >>> 6));
        target[3] = (byte) (  source[ sourceIndex+2 ] & 0x3f);
    }


    public static String decode( String source ) {
        if (source.length()%4 != 0) throw new RuntimeException( "valid Base64 codes have a multiple of 4 characters" );
        int numGroups = source.length() / 4;
        int numExtraBytes = source.endsWith( "==" ) ? 2 : (source.endsWith( "=" ) ? 1 : 0);
        byte[] targetBytes = new byte[ 3*numGroups ];
        byte[] sourceBytes = new byte[4];
        for (int group = 0; group < numGroups; group++) {
            for (int i = 0; i < sourceBytes.length; i++) {
                sourceBytes[i] = (byte) Math.max( 0, encodingChar.indexOf( source.charAt( 4*group+i ) ) );
            }
            convert4To3( sourceBytes, targetBytes, group*3 );
        }
        return new String( targetBytes, 0, targetBytes.length - numExtraBytes );
    }


    private static void convert4To3( byte[] source, byte[] target, int targetIndex ) {
        target[ targetIndex  ]  = (byte) (( source[0] << 2) | (source[1] >>> 4));
        target[ targetIndex+1 ] = (byte) (((source[1] & 0x0f) << 4) | (source[2] >>> 2));
        target[ targetIndex+2 ] = (byte) (((source[2] & 0x03) << 6) | (source[3]));
    }

}

