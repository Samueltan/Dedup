package dao;
import java.sql.*;  
  
/**
 * @author Samuel Tan
 * Database connection management
 * Create Date: 	2014/03/25
 */
public class DatabaseConnection {  
    private static final String DBDRIVER = "org.sqlite.JDBC";  
    private static final String DBURL = "jdbc:sqlite:dedup.db";  
    private Connection conn = null;  
  
    public DatabaseConnection() throws Exception {  
        try {  
            Class.forName(DBDRIVER);  
            this.conn = DriverManager.getConnection(DBURL);  
//	        String conf = "PRAGMA synchronous = OFF;";
//	        PreparedStatement pstmt = this.conn.prepareStatement(conf); 
//	        pstmt.executeQuery();  
            this.conn.setAutoCommit(false);
        } catch (Exception e) {  
            throw e;  
        }  
    }  
  
    public Connection getConnection() {  
        return this.conn;  
    }  

    public void commit() {  
       try {
		conn.commit();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }  
    
    public void close() throws Exception {  
        if (this.conn != null) {  
            try {  
                this.conn.close();  
            } catch (Exception e) {  
                throw e;  
            }  
        }  
    }  
}  
