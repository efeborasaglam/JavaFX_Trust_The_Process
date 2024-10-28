
package ost;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseAPI {

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // Treiber manuell registrieren
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC-Treiber nicht gefunden.");
            e.printStackTrace();
        }
    }

    private Connection connection;

    public DatabaseAPI() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/Users.db");
            System.out.println("Verbindung zur Datenbank hergestellt.");
        } catch (SQLException e) {
            System.out.println("Fehler beim Herstellen der Verbindung: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            // Immer eine neue Verbindung öffnen, um sicherzustellen, dass keine geschlossene Verbindung verwendet wird
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Fehler beim Öffnen der Verbindung: " + e.getMessage());
            return null; // Gibt `null` zurück, falls die Verbindung fehlschlägt
        }
    }
    
    
    protected final String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/src/main/resources/Users.db";
 
    public void createTable(String tableName, String fields) {  
        try (var conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                var meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                
                var sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';";
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(sql);
                try {
                    var exS = rs.getString(1);       
                    System.out.println("Table " + exS + " already exists.");        
                }
                catch (SQLException e) {
                    sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(\n " + fields + ");";
                    stmt.executeUpdate(sql);
                    System.out.println("A new table " + tableName + " has been created.");
                }
                stmt.close();
                conn.close();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insert(String tableName, String fields, String values) {
        try (Connection conn = DriverManager.getConnection(url)) {
 
            if (conn != null) {            
                conn.setAutoCommit(false);
                var stmt = conn.createStatement();
                var sql = "INSERT INTO " + tableName + "("  + fields + ") VALUES (" + values +")";
                stmt.executeUpdate(sql);

                stmt.close();
                conn.commit();
                conn.close();
            }
            System.out.println("Insert in " + tableName + " is done");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } 
    }
 
    public String getValue(String tableName, String keyName, String keyValue, String fieldName) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {            
                var stmt = conn.createStatement();
                
                var sql = "SELECT * FROM " + tableName + " WHERE " + keyName + " == " + keyValue;
                var rs = stmt.executeQuery(sql);
                try {
                    var exS = rs.getString(fieldName);
                    stmt.close();
                    conn.close();
                    return exS;
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    stmt.close();
                }
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    public boolean isKeyAvailable(String tableName, String keyName, String keyValue) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {  
                Statement stmt = conn.createStatement();
                var sql = "SELECT * FROM " + tableName + " WHERE " + keyName + " == " + keyValue; 

                ResultSet rs = stmt.executeQuery(sql);
                try {
                    String exS = rs.getString(keyName);       
                    System.out.println("Key value " + exS + " from table " + tableName + " exists.");
                    stmt.close();
                    conn.close();
                    return true;
                } catch (SQLException e) {
                    System.out.println("Key value " + keyValue + " from table " + tableName + "  not exists.");
                    stmt.close();
                }
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
