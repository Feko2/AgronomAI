import java.sql.*;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;

/**
 * Herramienta para probar la conexión a Oracle ATP
 * 
 * Para ejecutar:
 * javac -cp "./target/dependency/*:./target/classes:." TestOracleConnection.java 
 * java -cp "./target/dependency/*:./target/classes:." TestOracleConnection
 */
public class TestOracleConnection {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    Oracle ATP Connection Test Tool    ");
        System.out.println("========================================");
        
        try {
            // Configurar ruta del wallet
            String walletPath = "src/main/resources/wallet";
            System.setProperty("oracle.net.tns_admin", walletPath);
            System.setProperty("TNS_ADMIN", walletPath);
            System.out.println("TNS_ADMIN path: " + walletPath);
            
            // Cargar el driver
            System.out.println("\n[1] Cargando driver Oracle JDBC...");
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("    ✓ Driver cargado correctamente");
            
            // Construir la URL de conexión
            String url = "jdbc:oracle:thin:@u4zr6n0c83b5fnr2_high?TNS_ADMIN=" + walletPath;
            String username = "ADMIN";
            String password = "Micontrasenasecreta1";
            System.out.println("\n[2] Intentando conexión con:");
            System.out.println("    URL: " + url);
            System.out.println("    Usuario: " + username);
            
            // Verificar que exista el wallet
            java.io.File walletDir = new java.io.File(walletPath);
            if (!walletDir.exists()) {
                System.out.println("    ✗ ERROR: El directorio del wallet no existe: " + walletPath);
                return;
            } else {
                System.out.println("    ✓ Wallet encontrado en: " + walletPath);
                java.io.File[] files = walletDir.listFiles();
                System.out.println("    Archivos en wallet: " + (files != null ? files.length : 0));
                if (files != null) {
                    for (java.io.File file : files) {
                        System.out.println("       - " + file.getName());
                    }
                }
            }
            
            // Lista de posibles contraseñas de wallet (common Oracle default passwords)
            List<String> possiblePasswords = Arrays.asList(
            "Micontrasenasecreta1" 
            );
            
            boolean connected = false;
            System.out.println("\n[3] Probando diferentes contraseñas para el wallet...");
            
            for (String walletPass : possiblePasswords) {
                try {
                    System.out.println("    Intentando con contraseña: " + walletPass);
                    System.setProperty("WALLET_PASSWORD", walletPass);
                    
                    // Establecer la conexión
                    Properties props = new Properties();
                    props.setProperty("user", username);
                    props.setProperty("password", password);
                    
                    Connection conn = DriverManager.getConnection(url, props);
                    System.out.println("    ✓ ¡Conexión exitosa con contraseña: " + walletPass + "!");
                    
                    // Probar funcionamiento básico
                    System.out.println("\n[4] Probando consulta básica...");
                    Statement stmt = conn.createStatement();
                    
                    // Intentamos seleccionar el usuario actual
                    ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL");
                    if (rs.next()) {
                        System.out.println("    ✓ Consulta exitosa: USER = " + rs.getString(1));
                    }
                    
                    // Verificamos si existe la tabla de sensores
                    System.out.println("\n[5] Verificando existencia de tabla SENSOR_READINGS...");
                    try {
                        rs = stmt.executeQuery("SELECT COUNT(*) FROM SENSOR_READINGS");
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            System.out.println("    ✓ Tabla SENSOR_READINGS existe con " + count + " registros");
                        }
                    } catch (SQLException e) {
                        System.out.println("    ✗ La tabla SENSOR_READINGS no existe: " + e.getMessage());
                    }
                    
                    rs.close();
                    stmt.close();
                    conn.close();
                    
                    // Actualizar application.properties con la contraseña correcta
                    System.out.println("\n[6] La contraseña correcta del wallet es: " + walletPass);
                    System.out.println("    Recuerde actualizar el archivo ojdbc.properties con esta contraseña");
                    
                    connected = true;
                    break;
                } catch (Exception e) {
                    // Solo mostrar error detallado para la última contraseña
                    if (walletPass.equals(possiblePasswords.get(possiblePasswords.size() - 1))) {
                        System.out.println("    ✗ No se pudo conectar con ninguna contraseña");
                        System.out.println("\n[!] Último error de conexión: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            if (!connected) {
                System.out.println("\n[!] No se pudo establecer la conexión con ninguna contraseña común.");
                System.out.println("    Revise la contraseña que utilizó al descargar el wallet de Oracle Cloud.");
            }
            
        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n========================================");
    }
} 