package de.marcelsauer.jmsloadtester.tools;

import junit.framework.TestCase;

/**
 * JMS Load Tester Copyright (C) 2008 Marcel Sauer
 * <marcel[underscore]sauer[at]gmx.de>
 * 
 * This file is part of JMS Load Tester.
 * 
 * JMS Load Tester is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * JMS Load Tester is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * JMS Load Tester. If not, see <http://www.gnu.org/licenses/>.
 */
public class ArrayUtilsTest extends TestCase {

    public void testFindByteArrayByteArray() {
        byte[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        assertEquals(18, ArrayUtils.find(a, "STU".getBytes()));
    }

    public void testFindByteArrayIntByteArray() {
        byte[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        assertEquals(18, ArrayUtils.find(a, 0, "STU".getBytes()));
        assertEquals(44, ArrayUtils.find(a, 19, "STU".getBytes()));
    }

    public void testFindByteArrayIntIntByteArray() {
        byte[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();

        assertEquals(3, ArrayUtils.find(a, 0, 26, "DEF".getBytes()));
        assertEquals(-1, ArrayUtils.find(a, 0, 26, "FED".getBytes()));
        assertEquals(29, ArrayUtils.find(a, 4, 52, "DEF".getBytes()));
    }

    public void testAreaMatches() {
        byte[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        byte[] b = "FGH".getBytes();

        assertTrue(ArrayUtils.areaMatches(a, 5, b));
        assertFalse(ArrayUtils.areaMatches(a, 4, b));
    }

    public void testReplaceAll() {
        byte[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        byte[] b = "ABCDEFGHIJKLMNOPQRMARCELVWXYZABCDEFGHIJKLMNOPQRMARCELVWXYZ".getBytes();
        byte[] c = ArrayUtils.replaceAll(a, "STU".getBytes(), "MARCEL".getBytes());
        assertEquals(new String(b), new String(c));
        c = ArrayUtils.replaceAll(a, "SPU".getBytes(), "MARCEL".getBytes());
        assertEquals(new String(a), new String(c));
    }

    public void testReplaceFirst() {
        byte[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        byte[] b = "ABCDEFGHIJKLMNOPQRMARCELVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        byte[] c = ArrayUtils.replaceFirst(a, "STU".getBytes(), "MARCEL".getBytes());
        assertEquals(new String(b), new String(c));
        c = ArrayUtils.replaceAll(a, "SPU".getBytes(), "MARCEL".getBytes());
        assertEquals(new String(a), new String(c));
    }

    public void testReplaceAllUsingRealPattern() {
        byte[] a = "ABCDEFGHIJKLMNOPQR[[PLACEHOLDER]][[PLACEHOLDER]]STUVWXYZABCDEFGHIJKLMNOPQR[[PLACEHOLDER]]STUVWXYZ".getBytes();
        byte[] b = "ABCDEFGHIJKLMNOPQRBANGBANGSTUVWXYZABCDEFGHIJKLMNOPQRBANGSTUVWXYZ".getBytes();
        byte[] c = ArrayUtils.replaceAll(a, "[[PLACEHOLDER]]".getBytes(), "BANG".getBytes());
        assertEquals(new String(b), new String(c));
    }

    public void testContains() {
        byte[] a = "ABCDEFGHIJKLMNOPQR[[PLACEHOLDER]][[PLACEHOLDER]]STUVWXYZABCDEFGHIJKLMNOPQR[[PLACEHOLDER]]STUVWXYZ".getBytes();
        byte[] b = "[[".getBytes();
        byte[] c = "".getBytes();
        
        assertTrue(ArrayUtils.contains(a, "[".getBytes()[0]));
        assertFalse(ArrayUtils.contains(a, "@".getBytes()[0]));
        
        assertFalse(ArrayUtils.contains(c, "[".getBytes()[0]));
        assertFalse(ArrayUtils.contains(c, "]".getBytes()[0]));
    }

}
