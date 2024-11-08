package ost;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

public class Account extends DatabaseAPI {

    public void initAuthTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Auth (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER, " +
                     "pepper TEXT UNIQUE, " +
                     "otp_secret TEXT, " +
                     "FOREIGN KEY(user_id) REFERENCES Users(id)" +
                     ");";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("Tabelle Auth erfolgreich erstellt oder bereits vorhanden.");
        } catch (SQLException e) {
            System.out.println("Fehler beim Erstellen der Auth-Tabelle: " + e.getMessage());
        }
    }
    

    public void addAuthRecord(int userId, String pepper, String otpSecret) {
        String sql = "INSERT INTO Auth (user_id, pepper, otp_secret) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, pepper);
            pstmt.setString(3, otpSecret);
            pstmt.executeUpdate();
            System.out.println("Auth-Eintrag erfolgreich hinzugefügt.");
        } catch (SQLException e) {
            System.out.println("Fehler beim Hinzufügen des Auth-Eintrags: " + e.getMessage());
        }
    }
    
    public void addSaltRecord(int userId, int saltValue) {
        String sql = "INSERT INTO Salt (salt_id, saltwert) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, saltValue);
            pstmt.executeUpdate();
            System.out.println("Salt-Eintrag erfolgreich hinzugefügt.");
        } catch (SQLException e) {
            System.out.println("Fehler beim Hinzufügen des Salt-Eintrags: " + e.getMessage());
        }
    }
    

    public void initAccount() {
        // Initialisiere die Account-Datenbank (z. B. Tabellen erstellen)
        String sql = "CREATE TABLE IF NOT EXISTS Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP);";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("Tabelle Users erfolgreich erstellt oder bereits vorhanden.");
        } catch (SQLException e) {
            System.out.println("Fehler beim Erstellen der Tabelle: " + e.getMessage());
        }
    }

    public boolean verifyAccount(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Gibt true zurück, wenn die Email existiert
            }
        } catch (SQLException e) {
            System.out.println("Fehler bei der Überprüfung des Accounts: " + e.getMessage());
        }
        return false;
    }

    // Der Pepper kann eine festgelegte Zeichenfolge sein
    private static final String PEPPER = "your-secret-pepper"; // Ändern Sie dies zu einem sicheren, geheimen Wert

    // Hinzufügen eines Accounts mit Passwort-Hashing (BCrypt und Pepper)
    public void addAccount(String email, String password) {
        String saltedHashedPassword = hashPassword(password + PEPPER); // Passwort mit Pepper hashen

        String sql = "INSERT INTO Users (email, password) VALUES (?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (pstmt != null) {
                pstmt.setString(1, email);
                pstmt.setString(2, saltedHashedPassword);
                pstmt.executeUpdate();
                System.out.println("Account erfolgreich hinzugefügt.");
            } else {
                System.out.println("Fehler: Datenbankverbindung nicht verfügbar.");
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Hinzufügen des Accounts: " + e.getMessage());
        }
    }

    // Methode zum Passwort-Hashing mit BCrypt
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean verifyPassword(String email, String password) {
        String sql = "SELECT password FROM Users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                // Compare entered password (with pepper) with stored hashed password
                return BCrypt.checkpw(password + PEPPER, storedHashedPassword);
            } else {
                System.out.println("No user found with this email.");
            }
        } catch (SQLException e) {
            System.out.println("Database error in verifyPassword: " + e.getMessage());
        }
        return false;
    }
    
    public boolean changePassword(String email, String oldPassword, String newPassword, String confirmNewPassword) {
        // Prüfen, ob das neue Passwort und die Bestätigung übereinstimmen
        if (!newPassword.equals(confirmNewPassword)) {
            System.out.println("Das neue Passwort und die Bestätigung stimmen nicht überein.");
            return false;
        }
    
        // Stärke des neuen Passworts überprüfen (optional)
        if (!isPasswordStrong(newPassword)) {
            System.out.println("Das neue Passwort ist zu schwach.");
            return false;
        }
    
        String sql = "SELECT password FROM Users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                // Altes Passwort prüfen
                if (BCrypt.checkpw(oldPassword + PEPPER, storedHashedPassword)) {
                    // Neues Passwort hashen und speichern
                    String newHashedPassword = hashPassword(newPassword + PEPPER);
                    
                    String updateSql = "UPDATE Users SET password = ? WHERE email = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, newHashedPassword);
                        updatePstmt.setString(2, email);
                        updatePstmt.executeUpdate();
                        System.out.println("Passwort erfolgreich geändert.");
                        return true;
                    }
                } else {
                    System.out.println("Das alte Passwort ist falsch.");
                    return false;
                }
            } else {
                System.out.println("Benutzer nicht gefunden.");
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Ändern des Passworts: " + e.getMessage());
        }
        return false;
    }

    private boolean isPasswordStrong(String password) {
        // Beispiel: Mindestlänge und Anforderung von Zahlen und Sonderzeichen
        if (password.length() < 8) {
            return false;
        }
        if (!password.matches(".*\\d.*")) { // enthält mindestens eine Zahl
            return false;
        }
        if (!password.matches(".*[!@#$%^&*()].*")) { // enthält mindestens ein Sonderzeichen
            return false;
        }
        return true;
    }
    
    
}