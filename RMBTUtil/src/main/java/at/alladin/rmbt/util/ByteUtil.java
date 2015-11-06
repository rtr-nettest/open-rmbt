/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.alladin.rmbt.util;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Utility class for bytes and bits
 * @author lb
 *
 */
public class ByteUtil {

	/**
	 * get an int value from a byte array
	 * @param b the byte array
	 * @param start the starting index position inside byte array
	 * @param end the ending index position inside byte array
	 * @param byteOrder byte order
	 * @return
	 */
	public static int getInt(byte[] b, int start, int end, ByteOrder byteOrder) {
		return (int) getLong(b, start, end, byteOrder);
	}
	
	/**
	 * 
	 * @param b
	 * @param start
	 * @param end
	 * @param byteOrder
	 * @return
	 */
	public static long getLong(byte[] b, int start, int end, ByteOrder byteOrder) {
		long i = 0;
		for (int n = 0; n <= (end-start); n++) {
			i <<= 8;
			i += ByteOrder.BIG_ENDIAN.equals(byteOrder) ? (b[n+start] & 0xff) : (b[(end-start)-(n-start)] & 0xff);
		}
		
		return i;
	}
	
	/**
	 * set an int value in a byte array
	 * @param bytes the byte array
	 * @param start starting index position inside byte array
	 * @param end ending position inside byte array
	 * @param value the value
	 * @param byteOrder byte order
	 * @return
	 */
	public static byte[] setInt(byte[] bytes, int start, int end, int value, ByteOrder byteOrder) {
		return setLong(bytes, start, end, value, byteOrder);
	}
	
	/**
	 * set a long value in a byte array 
	 * @param bytes the byte array
	 * @param start starting index position inside byte array
	 * @param end ending position inside byte array
	 * @param value the value
	 * @param byteOrder byte order
	 * @return
	 */
	public static byte[] setLong(byte[] bytes, int start, int end, long value, ByteOrder byteOrder) {
		for (int n = 0; n <= (end-start); n++) {
			byte b = (byte) (value % 256);
			bytes[ByteOrder.BIG_ENDIAN.equals(byteOrder) ? (end-start)-(n-start) : n+start] = b;
			value >>= 8;
		}
		
		return bytes;		
	}

	/**
	 * get the value of a specific bit of a byte
	 * @param b the byte
	 * @param bit the bit number
	 * @return
	 */
    public static boolean getBit(byte b, int bit) {
        return (b >> bit) == 1;
    }

    /**
     * set the value of a specific bit of a byte
     * @param b the byte
     * @param bit the bit number
     * @param value the value
     * @return
     */
	public static byte setBit(byte b, int bit, boolean value) {
        if (value) {
                return (byte) (b | (1 << bit));
        }
        else {
                return (byte) ((b | (1 << bit)) ^ (1 << bit));
        }
	}
	
	/**
	 * set a specific amount of bits on the right side to a value
	 * @param b the byte
	 * @param bitlen the amount of bits on the right side
	 * @param value the new value
	 * @return
	 */
	public static byte setRightBitsValue(byte b, int bitlen, int value) {
		final int bitmask = (0xff >> bitlen) << bitlen;
		final byte r = (byte) ((b & bitmask) | (value & (~bitmask)));
		return r;
	}

	/**
	 * set a specific amount of bits on the left side to a value
	 * @param b the byte
	 * @param bitlen the amount of bits on the right side
	 * @param value the new value
	 * @return
	 */
	public static byte setLeftBitsValue(byte b, int bitlen, int value) {
		final int bitmask = ~((0xff >> (8-bitlen)) << (8-bitlen));
		final int valueBitmask = ~((0xff >> bitlen) << bitlen);
		final byte r = (byte) ((b & bitmask) | (byte)(value & valueBitmask) << (8-bitlen));
		return r;
	}

	/**
	 * functionality same as {@link Arrays#toString(byte[])} but all byte values are being treated as unsigned
	 * @param a byte array
	 * @return
	 */
	public static String toStringUnsigned(byte[] a) {
        if (a == null) {
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i] & 0xff);
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
	}
	
	/**
	 * this method copies a byte array to an int array and treats the byte values as unsigned
	 * @param a
	 * @return
	 */
	public static int[] toUnsignedInt(byte[] a) {
		int[] b = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[i] & 0xff;
		}
		
		return b;
	}
}
