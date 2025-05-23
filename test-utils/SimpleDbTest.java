import java.sql.*;

/**
 * Simple database test using the exact same settings as the application
 */
public class SimpleDbTest {
    public static void main(String[] args) {
        System.out.println("=== Simple Oracle Database Connection Test ===");
        
        // Variables from application.properties
        String walletPath = "src/main/resources/wallet";
        String url = "jdbc:oracle:thin:@u4zr6n0c83b5fnr2_high?TNS_ADMIN=" + walletPath;
        String username = "ADMIN";
        String password = "Micontrasenasecreta1";
        
        // Set system properties
        System.setProperty("oracle.net.tns_admin", walletPath);
        System.setProperty("TNS_ADMIN", walletPath);
        
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("Wallet Path: " + walletPath);
        
        try {
            System.out.println("Loading driver...");
            Class.forName("oracle.jdbc.OracleDriver");
            
            System.out.println("Connecting to database...");
            Connection conn = DriverManager.getConnection(url, username, password);
            
            System.out.println("Connected! Testing a simple query...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM SENSOR_READINGS");
            
            if (rs.next()) {
                System.out.println("Query successful. Found " + rs.getInt(1) + " records in SENSOR_READINGS table.");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            System.out.println("Connection closed successfully.");
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Test Complete ===");
    }
} 