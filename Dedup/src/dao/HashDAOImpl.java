package dao;

import java.sql.Connection;  
import java.sql.PreparedStatement;  
import java.sql.ResultSet;  
import java.sql.SQLException;
import java.util.ArrayList;  
import java.util.HashMap;
import java.util.List;  

import dao.vo.HashRow;
    
/**
 * @author Samuel Tan
 * DAO implementation (including search, insert, commit operations, etc.)
 * Create Date: 	2014/03/28
 */
public class HashDAOImpl implements IHashDAO {  
    private Connection conn=null; //数据库连接对象  
    public void setAutoCommit(boolean autocommit) {
		try {
			conn.setAutoCommit(autocommit);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private PreparedStatement pstmt = null;//数据库操作对象  
    public HashDAOImpl(Connection conn){//通过构造方法取得数据库连接  
        this.conn=conn;  
    }  
    
//    public boolean insert(HashRow hash) throws Exception {  
//        boolean flag=false;  
//        String sql="INSERT INTO hashes (string, hash)VALUES(?,?)";  
//        this.pstmt=this.conn.prepareStatement(sql);//实例化PreparedStatement对象  
//        this.pstmt.setString(1, hash.getString());  
//        this.pstmt.setString(2, hash.getHash());  
//        if(this.pstmt.executeUpdate()>0){  
//            flag=true;  
//        }  
//        this.pstmt.close();  
//        return flag;  
//    }  

	public void close() throws Exception { 
    	conn.close();
    }

    /* Find all the hash string and their hash id in the hash table and return them in a hash map
     * @see dao.IHashDAO#findAllHashes()
     */
    public HashMap<String, Integer> findAllHashes() throws Exception {  
    	HashMap<String, Integer> all= new HashMap<String, Integer>();  
        String sql="SELECT h.hash, h.id FROM hashes h;";

            this.pstmt=this.conn.prepareStatement(sql); 
            ResultSet rs=this.pstmt.executeQuery();  
            while(rs.next()){  
                all.put(rs.getString(1), rs.getInt(2));  
            }  
            this.pstmt.close();  
            return all;  
    }  
    
    /* Find the hash list of a given file
     * @see dao.IHashDAO#findFileHashes(java.lang.String)
     */
    public List<HashRow> findFileHashes(String filename) throws Exception {  
        List<HashRow> all= new ArrayList<HashRow>();  
        String sql="SELECT h.id, h.string, h.hash FROM hashes h, mapping m WHERE m.filename = '";
        sql += filename + "' and h.id = m.hashid;";

            this.pstmt=this.conn.prepareStatement(sql);  
//            this.pstmt.setString(1, filename);  
            ResultSet rs=this.pstmt.executeQuery();  
            HashRow hash=null;  
            while(rs.next()){  
            	hash=new HashRow();  
            	hash.setId(rs.getInt(1)); 
            	hash.setHashBytes(rs.getBytes(2)); 
            	hash.setHash(rs.getString(3));  
                all.add(hash);  
            }  
            this.pstmt.close();  
            return all;  
    }  
  
    /* Find the corresponding hash information of a given hash value
     * @see dao.IHashDAO#findByHash(java.lang.String)
     */
    public HashRow findByHash(String hash) throws Exception {  
        HashRow hr=null;  
        String sql="SELECT id, string, hash FROM hashes WHERE hash=?";  
        this.pstmt = this.conn.prepareStatement(sql);  
        this.pstmt.setString(1, hash);  
        ResultSet rs = this.pstmt.executeQuery();  
        if(rs.next()){  
        	hr = new HashRow();  
        	hr.setId(rs.getInt(1));  
        	hr.setHashBytes(rs.getBytes(2));  
        	hr.setHash(rs.getString(3));
        }  
        this.pstmt.close();  
        return hr;  
    }

	//    public boolean insert(HashRow hash) throws Exception {  
	//        boolean flag=false;  
	//        String sql="INSERT INTO hashes (string, hash)VALUES(?,?)";  
	//        this.pstmt=this.conn.prepareStatement(sql);//实例化PreparedStatement对象  
	//        this.pstmt.setString(1, hash.getString());  
	//        this.pstmt.setString(2, hash.getHash());  
	//        if(this.pstmt.executeUpdate()>0){  
	//            flag=true;  
	//        }  
	//        this.pstmt.close();  
	//        return flag;  
	//    }  
	
		/* Insert a hash entry
		 * @see dao.IHashDAO#insertHash(java.lang.String, java.lang.String)
		 */
		@Override
		public boolean insertHash(String string, String hash) throws Exception { 
	        boolean flag=false;  
	        
	        String sql="INSERT INTO hashes (string, hash)VALUES(?,?)";  
	        this.pstmt=this.conn.prepareStatement(sql);//实例化PreparedStatement对象  
	        this.pstmt.setString(1, string);  
	        this.pstmt.setString(2, hash);  
	        if(this.pstmt.executeUpdate()>0){  
	            flag=true;  
	        }  
	        this.pstmt.close();  
	        return flag;  
		}

		/* Insert a hash entry
		 * @see dao.IHashDAO#insertHash(java.lang.String, java.lang.String)
		 */
		@Override
		public boolean insertHashByte(byte[] chunk, String hash) throws Exception { 
	        boolean flag=false;  
	        
	        String sql="INSERT INTO hashes (string, hash)VALUES(?,?)";  
	        this.pstmt=this.conn.prepareStatement(sql);//实例化PreparedStatement对象  
	        this.pstmt.setBytes(1, chunk);
	        this.pstmt.setString(2, hash);  
	        if(this.pstmt.executeUpdate()>0){  
	            flag=true;  
	        }  
	        this.pstmt.close();  
	        return flag;  
		}
		
	/* Insert a mapping entry of the hash and the file it belongs to
	 * @see dao.IHashDAO#insertMapping(java.lang.String, int, int, java.lang.String)
	 */
	@Override
	public boolean insertMapping(String filename, int seqid, int hashid,
			String foldername) throws Exception {
        boolean flag=false;  
        String sql="INSERT INTO mapping (filename, seqid, hashid, folder) VALUES(?,?,?,?)";  
        this.pstmt=this.conn.prepareStatement(sql);//实例化PreparedStatement对象  
        this.pstmt.setString(1, filename);  
        this.pstmt.setInt(2, 0);  
        this.pstmt.setInt(3, hashid);  
        this.pstmt.setString(4, foldername);  
        if(this.pstmt.executeUpdate()>0){  
            flag=true;  
        }  
        this.pstmt.close();  
        return flag;  
	}

	@Override
	public void commit() throws Exception {
		conn.commit();		
	}

	/* Return the current max hash id in the hash table
	 * @see dao.IHashDAO#getMaxhashID()
	 */
	@Override
	public int getMaxhashID() throws SQLException {
		int maxid=0;
        String sql="SELECT max(id) FROM hashes";  
        this.pstmt = this.conn.prepareStatement(sql);  
        ResultSet rs = this.pstmt.executeQuery();  
        if(rs.next()){  
        	maxid = rs.getInt(1);  
        }  
        this.pstmt.close();  
		return maxid;
	}
    
  
}  
