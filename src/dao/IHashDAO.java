package dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List; 

import dao.vo.HashRow;
  
/**
 * @author Samuel Tan
 * The DAO interface defines the db operations
 * Create Date: 	2014/03/25
 */
public interface IHashDAO {  
	// Search operations
    public HashMap<String, Integer> findAllHashes() throws Exception;   
    public List<HashRow> findFileHashes(String filename) throws Exception;    
    public HashRow findByHash(String hash) throws Exception;  
    public int getMaxhashID() throws SQLException;

	// Insert operations
//  public boolean insert(HashRow hashrow) throws Exception;  
    public boolean insertHash(String string, String hash) throws Exception;
    public boolean insertHashByte(byte[] chunk, String hash) throws Exception;
    public boolean insertMapping(String filename, int seqid, int hashid, String foldername) throws Exception;

    // Delete operations
    public boolean deleteMapping(String filename) throws Exception;
    
	// Other operations
    public void setAutoCommit(boolean autocommit);
    public void commit() throws Exception;  
    public void close() throws Exception;  
}  
