package filelocker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import dao.DAOFactory;
import dao.IHashDAO;
import dao.vo.HashRow;
import hash.Hash;

/**
 * File Locker: A file duplicator that can support data deduplication
 * The basic features include:
 * 		- File storing and loading, deleting, 
 * 		- Network, 
 * 		- GUI, command line
 * 		- Binary file support
 * @author Samuel Tan
 * File processing functions
 * Create Date: 	2014/03/25
 */
public class FileLocker {
	final static int HASHBLOCK_SIZE = 8*1024;
	final static int IOBLOCK_SIZE_MIN = 8*1024;
	final static int IOBLOCK_SIZE_MAX = 4*1024*1024;
	final static int SLIDING_WINDOW_SIZE = 48;
	static int ioblocksize = IOBLOCK_SIZE_MAX;
	StringBuffer sbBlock = null;
	IHashDAO dao;
	File file = null;
		
	/**
	 * The constructor will generate a DAO instance from the DAO factory
	 */
	public FileLocker() {
		try {
			dao = DAOFactory.getIHashDAOInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeDB() {
		try {
			dao.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public static boolean isSameFile(String fileName1, String fileName2, boolean debug) throws IOException {
//		byte[] buf1 = new byte[ioblocksize];
//		byte[] buf2 = new byte[ioblocksize];
//		String str1, str2;
//		str1 = str2 = null;
//		boolean result = false;
//		long start = System.currentTimeMillis();
//		
//		// Prepare the test files
//		File file1 = new File(fileName1);
//		File file2 = new File(fileName2);
//		BufferedInputStream bis1 = new BufferedInputStream(new FileInputStream(file1));
//		BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file2));
//
//		// read file and push 1024 bytes each time for calculating the finger print
//		// Once the hash is different, stop and announce the difference
//		while((bis1.read(buf1) != -1) && (bis2.read(buf2) != -1)){
////		while((size1 = bis1.read(buf1)) != -1){
//			str1 = new String(buf1);
//			str2 = new String(buf2);
//			
//			if(!Hash.hashEquals(str1, str2)){
//				System.out.println("file 1 and file 2 are different!");	
//				bis1.close();
//				bis2.close();
//				return false;
//			}
//		}
//		if(Hash.hashEquals(str1, str2)){
//			System.out.println("file 1 and file 2 are the same!");
//			result = true;
//		}
//		
//		bis1.close();
//		bis2.close();
//
//		long end = System.currentTimeMillis();
//		if(debug)
//			System.out.println("isSameFile Running time " + (end-start) + " mini secs.");
//		return result;
//	}
				
	/**
	 * Purpose: Search the file in database and 
	 * generate a new file based on the file content returned from the hash list
	 * @param filename
	 * @return the file size or -1 if failed
	 */
	public int loadFile(String filename){
		int returnsize = 0;
        String s = null;

		long start = System.currentTimeMillis();
        List<HashRow> hashlist;
		try {
			hashlist = dao.findFileHashes(filename);
			if(hashlist.size() == 0){
				System.out.println("[[Loading file error:] File '" + filename + "' doesn't exist!  Please check again.");
				return -1;
			}
			
//			BufferedWriter bw = new BufferedWriter(new FileWriter("output\\" + filename));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("output\\" + filename));
			for(HashRow hr: hashlist){
				s = hr.getString();
				bos.write(s.getBytes());
				returnsize += s.length();
			}
			bos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("storeFile Running time " + (end-start) + " mini secs.");
		return returnsize;		
	}	

//	public int loadFile(String filename){
//		int size = 0;
//        String s = null;
//
//		long start = System.currentTimeMillis();
//        List<HashRow> hashlist;
//		try {
//			hashlist = dao.findFileHashes(filename);
//			if(hashlist.size() == 0){
//				System.out.println("[[Loading file error:] File doesn't exist!  Please check again.");
//				return -1;
//			}
//			
//			BufferedWriter bw = new BufferedWriter(new FileWriter("output\\" + filename));
//			for(HashRow hr: hashlist){
//				s = hr.getString();
//				bw.write(s);
//				size += s.length();
//			}
//			bw.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("storeFile Running time " + (end-start) + " mini secs.");
//		return size;		
//	}	

	/**
	 * This function is disabled due to performance issue (written in storeFile() in a inline manner)
	 * @param chunksize
	 * @param windowLong
	 * @param filesize
	 * @return true if the chunk is valid as per the validation rule
	 */
//	public boolean isValidChunk(int chunksize, long windowLong, long filesize){
//		final int CHUNK_LIMIT_HIGH = 16 * 1024;
//		final int MAGIC_VALUE = 0X12;	// For sliding window verification
//		final int CHUNK_MASK = 0x1fff;	// For sliding window verification
//		int chunkLimitLow = 1024;
//		
//		if(filesize < chunkLimitLow)
//			chunkLimitLow = (int)filesize;
//		if(chunksize < chunkLimitLow || chunksize > CHUNK_LIMIT_HIGH)
//			return false;
//		else
//			// do a bit (&) operation with the CHUNK_MASK
//			// if the result is equal to a predefined MAGIC_VALUE, the chunk is valid
//			return (windowLong & CHUNK_MASK) == MAGIC_VALUE;
//	}
	
	/**
	 * Purpose: Save the file to hash database
	 * Split the file into different pieces (as per the hash block size),
	 * Then check if the hash block exists in the database, if yes, update the file-hash-mapping table
	 * Or else, create a new hash entry in the hashes table and update the file-hash-mapping table accordingly
	 * @param filename
	 * @return the file size that has been saved or -1 if error returned
	 */
	public int storeFile(String filename){
		int size = 0;
		byte[] buf = new byte[ioblocksize];
		String strPiece = null;
		String hash = null;
		String windowStr = null;
		long windowLong = 0;
		int currentMaxHashID = 0;
		int currentPos = 0;
		int blockSize = 0;

//		final int CHUNK_LIMIT_HIGH = 16 * 1024;
		final int MAGIC_VALUE = 0X12;	// For sliding window verification
		final int CHUNK_MASK = 0x1fff;	// For sliding window verification
//		int chunkLimitLow = 1024;
				
		long start = System.currentTimeMillis();
		try {
			// If the file is already stored, return
			if((dao.findFileHashes(filename).size()) != 0){
				System.out.println("[Storing file error:] The file '" + filename + "' is already stored! Please try another file.");
				return -1;
			}
			
			File file = new File("input\\" + filename);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//			long filesize = file.length();
//			if(filesize < chunkLimitLow)
//				chunkLimitLow = (int)filesize;
			
			// Read the file content into memory on a block basis and then process
			int buflen,hashlen;
			int hashid = 0;
			
			HashMap<String, Integer> all = dao.findAllHashes();
			currentMaxHashID = dao.getMaxhashID();
			while((buflen=bis.read(buf))!=-1){
				sbBlock = new StringBuffer(new String(buf, 0, buflen));
				while((blockSize = sbBlock.length()) > 0){
					// 1. Use CDC (Content Defined Chunking) algorithm to locate the proper sliding window
					boolean cdcFlag = false;
					int chunkSize;
					while((chunkSize = currentPos + SLIDING_WINDOW_SIZE) < blockSize){
						// Keep moving the sliding window by one character each time
						// Calculate the hash value of the sliding window string, check if the sliding window offers a proper chunk position
						// If valid then the right border of the sliding window will be marked as a valid chunk border
						windowStr = sbBlock.substring(currentPos, chunkSize);
						windowLong = Hash.longValue(windowStr);

						// If the chunk size is in a valid range then do a bit (&) operation with the CHUNK_MASK
						// if the result is equal to a predefined MAGIC_VALUE, the chunk is valid
//						if(chunkSize > chunkLimitLow && chunkSize < CHUNK_LIMIT_HIGH &&	// This check will lower the performance
						if((windowLong & CHUNK_MASK) == MAGIC_VALUE){
							cdcFlag = true;		// matched chunk is found
							break;
						}
						++currentPos; 
					}
					
					// 2. Get the hash piece string from the buffer based on CDC algorithm (Content Defined Chunking)
//					if((hashlen = locateCDCPos()) == -1){	// For performance reason, this function is disabled and put inline
					if(cdcFlag){
						// If a matched position is found
						hashlen = chunkSize;		
					}else{
						// If block end is reached
						hashlen = blockSize;
					}
					strPiece = sbBlock.substring(0, hashlen);
					sbBlock = sbBlock.delete(0, hashlen);
					
					// 3. Save the hash string into database (or update the reference for existing ones)
					hash = Hash.hash(strPiece.toString());
					// 3.1 check if the hash string does not exist, 
					// if so, insert the hash as a new entry in the hashes table
					if(!all.containsKey(hash)){
						hashid = ++currentMaxHashID;
						dao.insertHash(strPiece, hash);
						all.put(hash, hashid);
					}else{
						// 3.2 Else just return the existing hashid from the hash string
						hashid = all.get(hash);
					}
					
					// 4. then modify mapping table accordingly
					dao.insertMapping(filename, 0, hashid, null);					
				}
				size += buflen;
			}
			
			bis.close();
			dao.commit();
		} catch(FileNotFoundException nfe){
			System.out.println("[Storing file error:] File not found! Please check the file name.");
		}catch (Exception e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		System.out.println("storeFile Running time " + (end-start) + " mini secs.");
		return size;		
	}
	
	public int deleteFile(String filename){
		
		return -1;
	}

	/**
	 * For performance reason, this function is put inside the storeFile() function and not invoked independently
	 * The param transition cost affects the performance!
	 * @param block
	 * @return The position of a valid chunk's right border or -1 if reach the end of the block
	 */
	public int locateCDCPos(){
		final int MAGIC_VALUE = 0X12;	// For sliding window verification
		final int CHUNK_MASK = 0x1fff;	// For sliding window verification
		int currentPos = 0;		// Current position of the sliding window (will move one byte a time until a proper position is found)
		long windowLong = 0;	// Long value of the string hash, which is used to verify if the current window match the criteria
		String windowStr;		// The string in the sliding window
		int blockSize = sbBlock.length();
		int chunkSize = currentPos + SLIDING_WINDOW_SIZE;
		
		// Keep moving the sliding window by one character each time
		// The verification rule is: 
		// Calculate the hash value of the sliding window string, do a bit (&) operation with the CHUNK_MASK
		// if the result is equal to a predefined MAGIC_VALUE, 
		// then the right border of the sliding window will be marked as a valid chunk border
		while(chunkSize < blockSize){
			windowStr = sbBlock.substring(currentPos, chunkSize);
			windowLong = Hash.longValue(windowStr);
			if((windowLong & CHUNK_MASK) == MAGIC_VALUE){
				break;
			}
			++currentPos; 
		}
		if(currentPos < blockSize - SLIDING_WINDOW_SIZE){
			// If the proper chunk position is found
			return currentPos + SLIDING_WINDOW_SIZE;
		}else{
			// Else if block is reached
			return -1;
		}
	}
	
	public static void main(String[] args) throws IOException {
		FileLocker test = new FileLocker();
		try {
			// Store the file under input folder to the locker
			test.storeFile("1.exe");
			
			// Load the file to output folder from the locker
			test.loadFile("1.exe");
			test.closeDB();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
