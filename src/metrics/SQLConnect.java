package metrics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLConnect {
	public static String db = "change_prone";  
	public static String url = "jdbc:mysql://localhost/"+db;  
    public static final String name = "com.mysql.jdbc.Driver";  
    public static final String user = "root";  
    public static final String password = "111111";  
  
    public Connection conn = null;  
    private Statement stmt = null;
  
	public SQLConnect(String db){
		try {  
			url = "jdbc:mysql://localhost/"+db;  
            Class.forName(name);//指定连接类型  
            conn = DriverManager.getConnection(url, user, password);//获取连接  
            stmt = conn.createStatement();
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}
	
	public ResultSet Excute(String sql) throws SQLException{
		ResultSet res = null;
		//System.out.println(sql); 
		try {  
			
            res = stmt.executeQuery(sql);//执行语句，得到结果集  
            //res.close();  
            //this.close();//关闭连接  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
		
		return res;
	}
	
	public void close() {  
        try {  
        	this.stmt.close();
            this.conn.close();  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
    } 
	
	public Statement getStmt() {
		return stmt;
	}
	public String getDatabase() {
		return db;
	}
	public void setDatabase(String database) {
		this.db = database;
	}
	

}
