package com.illposed.osc.utility;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Vector;
import java.nio.*;

/**
 * OSCJavaToByteArrayConverter is a helper class that translates
 * from Java types to their byte stream representations according to
 * the OSC spec.
 * <p>
 * The implementation is based on <a href=" http://www.emergent.de/">Markus Gaelli</a> and
 * Iannis Zannos' OSC implementation in Squeak.
 * <p>
 * This version includes bug fixes and improvements from 
 * Martin Kaltenbrunner and Alex Potsides.
 * <p>
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 * <p>
 * See license.txt (or license.rtf) for license information.
 *
 * @author Chandrasekhar Ramakrishnan
 * @author Martin Kaltenbrunner
 * @author Alex Potsides
 * @version 1.0
 */
public class OSCJavaToByteArrayConverter {

    protected ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private byte[] intBytes = new byte[4];
    private byte[] longintBytes = new byte[8];
    // this should be long enough to accomodate any string
    private char[] stringChars = new char[2048];
    private byte[] stringBytes = new byte[2048];

    public OSCJavaToByteArrayConverter() {
        super();
    }

    /**
     * Line up the Big end of the bytes to a 4 byte boundry
     * @return byte[]
     * @param bytes byte[]
     */
    private byte[] alignBigEndToFourByteBoundry(byte[] bytes) {
        int mod = bytes.length % 4;
		// if the remainder == 0 then return the bytes otherwise pad the bytes to
        // lineup correctly
        if (mod == 0) {
            return bytes;
        }
        int pad = 4 - mod;
        byte[] newBytes = new byte[pad + bytes.length];
        for (int i = 0; i < pad; i++) {
            newBytes[i] = 0;
        }
        for (int i = 0; i < bytes.length; i++) {
            newBytes[pad + i] = bytes[i];
        }
        return newBytes;
    }

    /**
     * Pad the stream to have a size divisible by 4.
     */
    public void appendNullCharToAlignStream() {
        int mod = stream.size() % 4;
        int pad = 4 - mod;
        for (int i = 0; i < pad; i++) {
            stream.write(0);
        }
    }

    /**
     * Convert the contents of the output stream to a byte array.
     * @return the byte array containing the byte stream
     */
    public byte[] toByteArray() {
        return stream.toByteArray();
    }

    /**
     * Write bytes into the byte stream.
     * @param bytes byte[]
     */
    public void write(byte[] bytes) {
        writeUnderHandler(bytes);
    }

    /**
     * Write an int into the byte stream.
     * @param i int
     */
    public void write(int i) {
        writeInteger32ToByteArray(i);
    }

    /**
     * Write a float into the byte stream.
     * @param f java.lang.Float
     */
    public void write(Float f) {
        writeInteger32ToByteArray(Float.floatToIntBits(f.floatValue()));
    }

    /**
     * @param i java.lang.Integer
     */
    public void write(Integer i) {
        writeInteger32ToByteArray(i.intValue());
    }

    /**
     * @param i java.lang.Integer
     */
    public void write(BigInteger i) {
        writeInteger64ToByteArray(i.longValue());
    }

    /**
     * Write a string into the byte stream.
     * @param aString java.lang.String
     */
    public void write(String aString) {
        int stringLength = aString.length();
			// this is a deprecated method -- should use get char and convert
        // the chars to bytes
//		aString.getBytes(0, stringLength, stringBytes, 0);
        aString.getChars(0, stringLength, stringChars, 0);
        // pad out to align on 4 byte boundry
        int mod = stringLength % 4;
        int pad = 4 - mod;
        for (int i = 0; i < pad; i++) {
            stringChars[stringLength++] = 0;
        }
        // convert the chars into bytes and write them out
        for (int i = 0; i < stringLength; i++) {
            stringBytes[i] = (byte) (stringChars[i] & 0x00FF);
        }
        stream.write(stringBytes, 0, stringLength);
    }

    /**
     * Write a char into the byte stream.
     * @param c char
     */
    public void write(char c) {
        stream.write(c);
    }

    /**
     * Write an object into the byte stream.
     * @param anObject one of Float, String, Integer, BigInteger, or array of these.
     */
    public void write(Object anObject) {
        // Can't do switch on class
        if (null == anObject) {
            return;
        }
        if (anObject instanceof Object[]) {
            Object[] theArray = (Object[]) anObject;
            for (int i = 0; i < theArray.length; ++i) {
                write(theArray[i]);
            }
            return;
        }
        if (anObject instanceof Float) {
            write((Float) anObject);
            return;
        }
        if (anObject instanceof String) {
            write((String) anObject);
            return;
        }
        if (anObject instanceof Integer) {
            write((Integer) anObject);
            return;
        }
        if (anObject instanceof BigInteger) {
            write((BigInteger) anObject);
            return;
        }
    }

    /**
     * Write the type tag for the type represented by the class
     * @param c Class of a Java object in the arguments
     */
    public void writeType(Class c) {
		// A big ol' case statement -- what's polymorphism mean, again?
        // I really wish I could extend the base classes!

		// use the appropriate flags to tell SuperCollider what kind of 
        // thing it is looking at
        if (Integer.class.equals(c)) {
            stream.write('i');
            return;
        }
        if (java.math.BigInteger.class.equals(c)) {
            stream.write('h');
            return;
        }
        if (Float.class.equals(c)) {
            stream.write('f');
            return;
        }
        if (Double.class.equals(c)) {
            stream.write('d');
            return;
        }
        if (String.class.equals(c)) {
            stream.write('s');
            return;
        }
        if (Character.class.equals(c)) {
            stream.write('c');
            return;
        }
    }

    /**
     * Write the types for an array element in the arguments.
     * @param array java.lang.Object[]
     */
    public void writeTypesArray(Object[] array) {
		// A big ol' case statement in a for loop -- what's polymorphism mean, again?
        // I really wish I could extend the base classes!

        for (int i = 0; i < array.length; i++) {
            if (null == array[i]) {
                continue;
            }
            // Create a way to deal with Boolean type objects
            if (Boolean.TRUE.equals(array[i])) {
                stream.write('T');
                continue;
            }
            if (Boolean.FALSE.equals(array[i])) {
                stream.write('F');
                continue;
            }
            // this is an object -- write the type for the class
            writeType(array[i].getClass());
        }
    }

    /**
     * Write types for the arguments (use a vector for jdk1.1 compatibility, rather than an ArrayList).
     * @param vector  the arguments to an OSCMessage
     */
    public void writeTypes(Vector vector) {
		// A big ol' case statement in a for loop -- what's polymorphism mean, again?
        // I really wish I could extend the base classes!

        Enumeration enm = vector.elements();
        Object nextObject;
        while (enm.hasMoreElements()) {
            nextObject = enm.nextElement();
            if (null == nextObject) {
                continue;
            }
			// if the array at i is a type of array write a [
            // This is used for nested arguments
            if (nextObject.getClass().isArray()) {
                stream.write('[');
				// fill the [] with the SuperCollider types corresponding to the object
                // (e.g., Object of type String needs -s).
                writeTypesArray((Object[]) nextObject);
                // close the array
                stream.write(']');
                continue;
            }
            // Create a way to deal with Boolean type objects
            if (Boolean.TRUE.equals(nextObject)) {
                stream.write('T');
                continue;
            }
            if (Boolean.FALSE.equals(nextObject)) {
                stream.write('F');
                continue;
            }
			// go through the array and write the superCollider types as shown in the 
            // above method. the Classes derived here are used as the arg to the above method
            writeType(nextObject.getClass());
        }
        // align the stream with padded bytes
        appendNullCharToAlignStream();
    }

    /**
     * Write bytes to the stream, catching IOExceptions and converting them to RuntimeExceptions.
     * @param bytes byte[]
     */
    private void writeUnderHandler(byte[] bytes) {

        try {
            stream.write(alignBigEndToFourByteBoundry(bytes));
        } catch (IOException e) {
            throw new RuntimeException("You're screwed: IOException writing to a ByteArrayOutputStream");
        }
    }

    /**
     * Write a 32 bit integer to the byte array without allocating memory.
     * @param value a 32 bit int.
     */
    private void writeInteger32ToByteArray(int value) {
		//byte[] intBytes = new byte[4];
        //I allocated the this buffer globally so the GC has less work

        intBytes[3] = (byte) value;
        value >>>= 8;
        intBytes[2] = (byte) value;
        value >>>= 8;
        intBytes[1] = (byte) value;
        value >>>= 8;
        intBytes[0] = (byte) value;

        try {
            stream.write(intBytes);
        } catch (IOException e) {
            throw new RuntimeException("You're screwed: IOException writing to a ByteArrayOutputStream");
        }
    }

    /**
     * Write a 64 bit integer to the byte array without allocating memory.
     * @param value a 64 bit int.
     */
    private void writeInteger64ToByteArray(long value) {
        longintBytes[7] = (byte) value;
        value >>>= 8;
        longintBytes[6] = (byte) value;
        value >>>= 8;
        longintBytes[5] = (byte) value;
        value >>>= 8;
        longintBytes[4] = (byte) value;
        value >>>= 8;
        longintBytes[3] = (byte) value;
        value >>>= 8;
        longintBytes[2] = (byte) value;
        value >>>= 8;
        longintBytes[1] = (byte) value;
        value >>>= 8;
        longintBytes[0] = (byte) value;

        try {
            stream.write(longintBytes);
        } catch (IOException e) {
            throw new RuntimeException("You're screwed: IOException writing to a ByteArrayOutputStream");
        }
    }
    
    public int size() {
        return stream.size();
    }
    
    public void writeTo(OutputStream out) throws IOException {
        stream.writeTo(out);
    }
    
    public ByteArrayOutputStream getStream() {
        return stream;
    }
}
