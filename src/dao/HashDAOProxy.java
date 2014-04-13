package dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;  

import dao.vo.HashRow;
  
 
/**
 * @author Samuel Tan
 * Proxy for a better user interaction.  It will delegate the real operations to the dao class
 * Create Date: 	2014/03/25
 */
public class HashDAOProxy implements IHashDAO {  
    private DatabaseConnection dbc = null;  
    private IHashDAO dao = null;  
    public HashDAOProxy()throws Exception{  
        this.dbc=new DatabaseConnection();  
        this.dao=new HashDAOImpl(this.dbc.getConnection());  
    }  
//    public boolean insert(HashRow hash) throws Exception {  
//        boolean flag=false;  
//        try{  
//            if(this.dao.findByHash(hash.getHash())==null){  
//                flag=this.dao.insert(hash);  
//            }  
//        }catch(Exception e){  
//            throw e;  
////        }finally{  
////            this.dbc.close();  
//        }  
//        return flag;  
//    }  

    public boolean insertHash(String string, String hash) throws Exception {  
        boolean flag=false;  
        try{  
            if(this.dao.findByHash(hash)==null){  
                flag=this.dao.insertHash(string, hash);  
            }  
        }catch(Exception e){  
            throw e;  
//        }finally{  
//            this.dbc.close();  
        }  
        return flag;  
    }  

    public boolean insertHashByte(byte[] chunk, String hash) throws Exception {  
        boolean flag=false;  
        try{  
            if(this.dao.findByHash(hash)==null){  
                flag=this.dao.insertHashByte(chunk, hash);  
            }  
        }catch(Exception e){  
            throw e;  
//        }finally{  
//            this.dbc.close();  
        }  
        return flag;  
    }  
    
    public boolean insertMapping(String filename, int seqid, int hashid, String foldername) throws Exception {  
        boolean flag=false;  
        try{              
        	flag=this.dao.insertMapping(filename, seqid, hashid, foldername);  
        }catch(Exception e){  
            throw e;  
//        }finally{  
//            this.dbc.close();  
        }  
        return flag;  
    }  
    
    @Override
	public HashMap<String, Integer> findAllHashes() throws Exception {		
		return this.dao.findAllHashes();
	}

	public List<HashRow> findFileHashes(String filename) throws Exception {  
        List<HashRow> all=null;  
        try{  
            all=this.dao.findFileHashes(filename);  
        }catch(Exception e){  
            throw e;  
//        }finally{  
//            this.dbc.close();  
        }  
        return all;  
    }  
  
    public HashRow findByHash(String hash) throws Exception {  
        HashRow hr = null;  
        try{  
            hr = this.dao.findByHash(hash);  
        }catch(Exception e){  
            throw e;  
//        }finally{  
//            this.dbc.close();  
        }  
        return hr;  
    }  

	@Override
	public boolean deleteMapping(String filename) throws Exception {
        boolean flag=false;  
        try{              
        	flag=this.dao.deleteMapping(filename);  
        }catch(Exception e){  
            throw e;  
//        }finally{  
//            this.dbc.close();  
        }  
        return flag;  
	}
	
    public void close() throws Exception{
    	this.dao.close();
    }

	@Override
	public void commit() throws Exception {
		this.dao.commit();		
	}

	@Override
	public void setAutoCommit(boolean autocommit) {
		this.dao.setAutoCommit(autocommit);		
	}

	@Override
	public int getMaxhashID() throws SQLException {		
		return this.dao.getMaxhashID();
	}

	@Override
	public List<String> findFiles() throws Exception {
        List<String> filenames = new ArrayList<String>();  
        try{  
        	filenames = this.dao.findFiles();  
        }catch(Exception e){  
            throw e;  
//        }finally{  
//            this.dbc.close();  
        }  
        return filenames;  
	}

}  
