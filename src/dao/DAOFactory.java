package dao;


/**
 * @author Samuel Tan
 * Provide a DAO proxy class from the factory
 * Create Date: 	2014/03/25
 */
public class DAOFactory {  
    public static IHashDAO getIHashDAOInstance()throws Exception{  
        return new HashDAOProxy();  
    }  
}  
