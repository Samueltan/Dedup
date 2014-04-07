package hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

	/**
	 * Converts an array of bytes into a readable set of characters in the range ! through ~
	 * @param bytes The array of bytes
	 * @return A string with characters in the range ! through ~
	 */
	public static String makeReadable(byte[] bytes) {
		for (int ii=0; ii<bytes.length; ii++) {
			bytes[ii]=(byte) ((bytes[ii] & 0x5E)+32); // Convert to character ! through ~
		}
		return new String(bytes);
	}

	/**
	 * Converts a byte array into a long value
	 * @param byte array
	 * @return A long value of the byte array
	 */
	public static long byteToLong(byte[] b) {  
		long s = 0;  
		long s0 = b[0] & 0xff;
		long s1 = b[1] & 0xff;  
		long s2 = b[2] & 0xff;  
		long s3 = b[3] & 0xff;  
		long s4 = b[4] & 0xff;
		long s5 = b[5] & 0xff;  
		long s6 = b[6] & 0xff;  
		long s7 = b[7] & 0xff;
		s1 <<= 8;  
		s2 <<= 16;  
		s3 <<= 24;  
		s4 <<= 8 * 4;  
		s5 <<= 8 * 5;  
		s6 <<= 8 * 6;  
		s7 <<= 8 * 7;  
		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;  
		return s;  
	}  
	
	public static long longValue(String str){
		return byteToLong(compactByte(str));
	}
	
	/**
	 * produce a hash of a given string
	 * @param str The string to hash
	 * @return Returns a collection of sixteen "readable" characters (! through ~) corresponding to this string.
	 */
	public static String compact(String str) {
		return makeReadable(compactByte(str));
	}

	public static byte[] compactByte(String str) {
		// setup the digest
		MessageDigest md = null;
		str += "foo"; // random text added to the string
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Hash digest format not known!");
			System.exit(-1);
		}
		return md.digest();
	}
	
	public static boolean hashEquals(String str1, String str2){
		return compact(str1).equals(compact(str2));
	}
}
