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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
	final static int FILELOCKER_CAPACITY 					= 20 * 1024 * 1024;
	final static int HASHBLOCK_SIZE 						= 8*1024;
	final static int IOBLOCK_SIZE_MIN 						= 8*1024;
	final static int IOBLOCK_SIZE_MAX 						= 4*1024*1024;
	final static int SLIDING_WINDOW_SIZE 					= 48;
	final static String CHARSET 							= "ISO-8859-1";
	final static String DB_FILE 							= "dedup.db";
	final static String CONFIG_PROPERTIES_FILE 				= "filelocker.properties";
	final static String CONFIG_USEDSPACE 					="usedspace";

	public final static int ERR_LOCKER_FILENOTEXIST 		= -1;
	public final static int ERR_LOCKER_FILEALREADYEXIST		= -2;
	public final static int ERR_LOCKER_NOSPACE				= -3;
	public final static int ERR_LOCKER_FILENOTFOUND			= -4;
	
	public final static String MSG_LOCKER_FILENOTEXIST 		= "' doesn't exist in the file locker!  Please check again.";
	public final static String MSG_LOCKER_FILEALREADYEXIST	= "' is already stored! Please try another file.";
	public final static String MSG_LOCKER_NOSPACE			= "File locker has no enough space left to store this file!";
	public final static String MSG_LOCKER_FILENOTFOUND		= "File not found! Please check the file name.";
	
	static int ioblocksize 									= IOBLOCK_SIZE_MAX;
	int progressPercentage 									= 0;	// % of the file that has been stored into file locker (0 ~ 100)
	int spacePercentage 									= 0;	// % of the used space of the total file locker space (0 ~ 100)
	
	public boolean flagLocked								= false;
	public int getProgressPercentage() {
		return progressPercentage;
	}

	public void setProgressPercentage(int progressPercentage) {
		this.progressPercentage = progressPercentage;
	}

	public int getSpacePercentage() {
		return spacePercentage;
	}

	public void setSpacePercentage(int spacePercentage) {
		this.spacePercentage = spacePercentage;
	}

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
					
	/**
	 * Purpose: Search the file in database and 
	 * generate a new file based on the file content returned from the hash list
	 * @param filename
	 * @return the file size or -1 if failed
	 */
	public int loadFile(String filename){
		int returnSize = 0;

		long start = System.currentTimeMillis();
        List<HashRow> hashlist;
		try {
			File file = new File(filename);
			String loadFileName = file.getName();
			hashlist = dao.findFileHashes(loadFileName);
			if(hashlist.size() == 0){
				System.out.println("[Loading file error:] File '" + loadFileName + MSG_LOCKER_FILENOTEXIST);
				return ERR_LOCKER_FILENOTEXIST;
			}
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));
			for(HashRow hr: hashlist){
				byte[] bytes = hr.getHashBytes();
				bos.write(bytes);
				returnSize += bytes.length;
//				System.out.print("\rLoading file progress: " + returnSize + " Bytes");
			}
			bos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("loadFile Running time " + (end-start) + " mini secs.");
		return returnSize;		
	}	

	/**
	 * This function is disabled due to performance issue (written in storeFile() in a inline manner)
	 * @param chunksize
	 * @param windowLong
	 * @param fileSize
	 * @return true if the chunk is valid as per the validation rule
	 */
//	public boolean isValidChunk(int chunksize, long windowLong, long fileSize){
//		final int CHUNK_LIMIT_HIGH = 16 * 1024;
//		final int MAGIC_VALUE = 0X12;	// For sliding window verification
//		final int CHUNK_MASK = 0x1fff;	// For sliding window verification
//		int chunkLimitLow = 1024;
//		
//		if(fileSize < chunkLimitLow)
//			chunkLimitLow = (int)fileSize;
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
		int returnSize = 0;
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

			File file = new File(filename);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			long fileSize = file.length();
			filename = file.getName();
			if(fileSize == 0){
				System.out.println("[Storing file warning:] The file '" + filename + "' is empty! Please check.");
				bis.close();
				return -1;
			}
			
			
			// If the file is already stored, return
			if((dao.findFileHashes(filename).size()) != 0){
				System.out.println("[Storing file error:] The file '" + filename + MSG_LOCKER_FILEALREADYEXIST);
				bis.close();
				return ERR_LOCKER_FILEALREADYEXIST;
			}			
			
			// Get space information from properties file
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf\\" + CONFIG_PROPERTIES_FILE));
			int usedSpace = Integer.parseInt(prop.getProperty(CONFIG_USEDSPACE));
			if(usedSpace + fileSize > FILELOCKER_CAPACITY){
				System.out.println("[Storing file error:] " + MSG_LOCKER_NOSPACE);
				bis.close();
				return ERR_LOCKER_NOSPACE;
			}
			
			// The chunk limit feature below is disabled as it will greatly lower the performance!
//			if(fileSize < chunkLimitLow)
//				chunkLimitLow = (int)fileSize;
			
			// Read the file content into memory on a block basis and then process
			int buflen,hashlen;
			int hashid = 0;
			
			HashMap<String, Integer> all = dao.findAllHashes();
			currentMaxHashID = dao.getMaxhashID();
			while((buflen=bis.read(buf))!=-1){
				// Note: 
				// The charset parameter 'CHARSET' is a mandatory in this case when the program handles a binary file
				// By default the getBytes uses OS default charset (e.g. UTF-8) which may cause the inconsistency problem
				// between the generated string and the orignial byte array
				sbBlock = new StringBuffer(new String(buf, 0, buflen,CHARSET));
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
					returnSize += hashlen;
					progressPercentage = (int)((returnSize * 1.0 / fileSize) * 100);
					System.out.print("\rStoring file progress: " + progressPercentage + "%");
					
					// 3. Save the hash string into database (or update the reference for existing ones)
					// Note: 
					// The charset parameter 'CHARSET' is a mandatory in this case when the program handles a binary file
					// By default the getBytes uses OS default charset (e.g. UTF-8) which may cause the inconsistency problem
					// between the generated byte array and the orignial string
					byte[] hashbytes = strPiece.getBytes(CHARSET);
					hash = Hash.hashByte2String(hashbytes);
					// 3.1 check if the hash string does not exist, 
					// if so, insert the hash as a new entry in the hashes table
					if(!all.containsKey(hash)){
						hashid = ++currentMaxHashID;
						dao.insertHashByte(hashbytes, hash);
						all.put(hash, hashid);
					}else{
						// 3.2 Else just return the existing hashid from the hash string
						hashid = all.get(hash);
					}
					
					// 4. then modify mapping table accordingly
					dao.insertMapping(filename, 0, hashid, null);					
				}
			}

			// Update the space usage information
			File dbfile = new File(DB_FILE);
			usedSpace += dbfile.length();
			spacePercentage = (int)((usedSpace * 1.0 / FILELOCKER_CAPACITY) * 100);
			prop.setProperty(CONFIG_USEDSPACE, String.valueOf(usedSpace));
			System.out.println("\nFile locker space usage: " + spacePercentage + "%");
			
			bis.close();
			dao.commit();
		} catch(FileNotFoundException nfe){
			System.out.println("[Storing file error:] " + MSG_LOCKER_FILENOTFOUND);
			return ERR_LOCKER_FILENOTFOUND;
		}catch (Exception e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		System.out.println("storeFile Running time " + (end-start) + " mini secs.");
		return returnSize;		
	}
	
	/**
	 * Purpose: delete the file from file locker
	 * @param filename
	 * @return
	 */
	public int deleteFile(String filename){
		int returnSize = 0;
		long start = System.currentTimeMillis();
        List<HashRow> hashlist;
		try {
			hashlist = dao.findFileHashes(filename);
			if(hashlist.size() == 0){
				System.out.println("[[Deleting file error:] File '" + filename + MSG_LOCKER_FILENOTEXIST);
				return ERR_LOCKER_FILENOTEXIST;
			}
			
			dao.deleteMapping(filename);
			dao.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		System.out.println("deleteFile Running time " + (end-start) + " mini secs.");
		return returnSize;
	}

	public List<String> getStoredFiles(){
		List<String> files = new ArrayList<String>();
		try {
			files = dao.findFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(files.size()>0){
			return files;			
		}else{
			return null;
		}
	}
	
	/**
	 * For performance reason, this function is put inside the storeFile() function and not invoked independently
	 * The param transition cost affects the performance!
	 * @param block
	 * @return The position of a valid chunk's right border or -1 if reach the end of the block
	 */
//	public int locateCDCPos(){
//		final int MAGIC_VALUE = 0X12;	// For sliding window verification
//		final int CHUNK_MASK = 0x1fff;	// For sliding window verification
//		int currentPos = 0;		// Current position of the sliding window (will move one byte a time until a proper position is found)
//		long windowLong = 0;	// Long value of the string hash, which is used to verify if the current window match the criteria
//		String windowStr;		// The string in the sliding window
//		int blockSize = sbBlock.length();
//		int chunkSize = currentPos + SLIDING_WINDOW_SIZE;
//		
//		// Keep moving the sliding window by one character each time
//		// The verification rule is: 
//		// Calculate the hash value of the sliding window string, do a bit (&) operation with the CHUNK_MASK
//		// if the result is equal to a predefined MAGIC_VALUE, 
//		// then the right border of the sliding window will be marked as a valid chunk border
//		while(chunkSize < blockSize){
//			windowStr = sbBlock.substring(currentPos, chunkSize);
//			windowLong = Hash.longValue(windowStr);
//			if((windowLong & CHUNK_MASK) == MAGIC_VALUE){
//				break;
//			}
//			++currentPos; 
//		}
//		if(currentPos < blockSize - SLIDING_WINDOW_SIZE){
//			// If the proper chunk position is found
//			return currentPos + SLIDING_WINDOW_SIZE;
//		}else{
//			// Else if block is reached
//			return -1;
//		}
//	}
	
}
