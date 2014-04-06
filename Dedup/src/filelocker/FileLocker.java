package filelocker;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import dao.DAOFactory;
import dao.IHashDAO;
import dao.vo.HashRow;
import hash.Hash;

/**
 * @author Samuel Tan
 * File processing functions
 * Create Date: 	2014/03/25
 */
public class FileLocker {
	final static int HASHBLOCK_SIZE = 8*1024;
	final static int IOBLOCK_SIZE_MIN = 8*1024;
	final static int IOBLOCK_SIZE_MAX = 4*1024*1024;
	static int ioblocksize = IOBLOCK_SIZE_MAX;
	IHashDAO dao;
	File file = null;
	
	public FileLocker() {
		try {
			dao = DAOFactory.getIHashDAOInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeDB() {
		try {
			dao.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isSameFile(String fileName1, String fileName2, boolean debug) throws IOException {
		byte[] buf1 = new byte[ioblocksize];
		byte[] buf2 = new byte[ioblocksize];
		String str1, str2;
		str1 = str2 = null;
		boolean result = false;
		long start = System.currentTimeMillis();
		
		// Prepare the test files
		File file1 = new File(fileName1);
		File file2 = new File(fileName2);
		BufferedInputStream bis1 = new BufferedInputStream(new FileInputStream(file1));
		BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file2));

		// read file and push 1024 bytes each time for calculating the finger print
		// Once the hash is different, stop and announce the difference
		while((bis1.read(buf1) != -1) && (bis2.read(buf2) != -1)){
//		while((size1 = bis1.read(buf1)) != -1){
			str1 = new String(buf1);
			str2 = new String(buf2);
			
			if(!Hash.hashEquals(str1, str2)){
				System.out.println("file 1 and file 2 are different!");	
				bis1.close();
				bis2.close();
				return false;
			}
		}
		if(Hash.hashEquals(str1, str2)){
			System.out.println("file 1 and file 2 are the same!");
			result = true;
		}
		
		bis1.close();
		bis2.close();

		long end = System.currentTimeMillis();
		if(debug)
			System.out.println("isSameFile Running time " + (end-start) + " mini secs.");
		return result;
	}

	/**
	 * Purpose: Search the file in database and 
	 * generate a new file based on the file content returned from the hash list
	 * @param filename
	 * @return the file size or -1 if failed
	 */
	public int loadFile(String filename){
		int size = 0;
        String s = null;

		long start = System.currentTimeMillis();
        List<HashRow> hashlist;
		try {
			hashlist = dao.findFileHashes(filename);
			if(hashlist.size() == 0){
				System.out.println("[[Loading file error:] File doesn't exist!  Please check again.");
				return -1;
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter("output\\" + filename));
			for(HashRow hr: hashlist){
				s = hr.getString();
				bw.write(s);
				size += s.length();
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("storeFile Running time " + (end-start) + " mini secs.");
		return size;		
	}
	

	/**
	 * Purpose: Save the file to hash database
	 * Split the file into different pieces (as per the hash block size),
	 * Then check if the hash block exists in the database, if yes, update the file-hash-mapping table
	 * Or else, create a new hash entry in the hashes table and update the file-hash-mapping table accordingly
	 * @param filename
	 * @return the file size that has been saved
	 */
	public int storeFile(String filename){
		int size = 0;
		byte[] buf = new byte[ioblocksize];
		StringBuffer sbBlock = null;
		String strPiece = null;
		String hash = null;
		int currentMaxHashID = 0;

		long start = System.currentTimeMillis();
		try {
			// If the file is already stored, return
			if((dao.findFileHashes(filename).size()) != 0){
				System.out.println("[Storing file error:] The file is already stored! Please try another file.");
				return -1;
			}
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream("input\\" + filename));
			
			// Read the file content into memory on a block basis and then process
			int buflen,hashlen;
			int hashid = 0;
			
			HashMap<String, Integer> all = dao.findAllHashes();
			currentMaxHashID = dao.getMaxhashID();
			while((buflen=bis.read(buf))!=-1){
				sbBlock = new StringBuffer(new String(buf, 0, buflen));
				while((hashlen = sbBlock.length()) > 0){
					// Get the hash piece string from the buffer
					if(hashlen > HASHBLOCK_SIZE)
						hashlen = HASHBLOCK_SIZE;
					strPiece = sbBlock.substring(0, hashlen);
					
					hash = Hash.compact(strPiece.toString());
					// 1. check if the hash string does not exist, 
					// if so, insert the hash as a new entry in the hashes table
					if(!all.containsKey(hash)){
						hashid = ++currentMaxHashID;
						dao.insertHash(strPiece, hash);
						all.put(hash, hashid);
					}else{
						hashid = all.get(hash);
					}
					
					// 2. then modify mapping table accordingly
					dao.insertMapping(filename, 0, hashid, null);
					
					sbBlock = sbBlock.delete(0, hashlen);
				}
				size += buflen;
			}
			
			bis.close();
			dao.commit();
		} catch(FileNotFoundException nfe){
			System.out.println("[Storing file error:] File not found! Please check the file name.");
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		System.out.println("storeFile Running time " + (end-start) + " mini secs.");
		return size;		
	}
	
	public static void main(String[] args) throws IOException {
		FileLocker test = new FileLocker();
		try {
			// Store the file under input folder to the locker
			test.storeFile("PacMan.exe");
			
			// Load the file to output folder from the locker
			test.loadFile("PacMan.exe");
			test.closeDB();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
