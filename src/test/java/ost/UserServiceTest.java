package ost;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Rest deiner Testklasse

import org.junit.jupiter.api.Test;
import ost.UserService;
import ost.User;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setUp() {
        // Hier solltest du die Initialisierung für userService vornehmen
        userService = new UserService();
        // Falls erforderlich, initialisiere hier auch die Datenbank oder
        // Mock-Datenbank-Verbindung
    }

    @Test
    public void testPasswordRegistration_ValidPassword() {
        // Testdaten: ein gültiges Passwort
        String email = "testuser@example.com";
        String validPassword = "Valid123!"; // min 8 Zeichen, Groß/Kleinschreibung, Zahl, Sonderzeichen

        // Methode zur Benutzerregistrierung aufrufen
        boolean result = userService.registerUser(email, validPassword);

        // Erwartetes Ergebnis: Die Registrierung sollte erfolgreich sein
        assertTrue(result, "Die Benutzerregistrierung sollte erfolgreich sein");

        // Datenbankabfrage (dieser Teil ist optional, je nach Implementierung)
        User registeredUser = userService.findUserByEmail(email);
        assertNotNull(registeredUser, "Der Benutzer sollte in der Datenbank existieren");
        assertNotNull(registeredUser.getPasswordHash(), "Der Passwort-Hash sollte gespeichert sein");
        assertNotNull(registeredUser.getSalt(), "Der Salt-Wert sollte gespeichert sein");

        // Überprüfen, ob der Passwort-Hash korrekt ist
        String hashedPassword = hashPassword(validPassword, registeredUser.getSalt());
        assertEquals(hashedPassword, registeredUser.getPasswordHash(), "Der Passwort-Hash sollte korrekt sein");
    }

    // Hilfsmethode für Passwort-Hashing (falls nicht bereits in einer anderen
    // Klasse)
    private String hashPassword(String password, String salt) {
        // Hier die Logik für das Hashing implementieren oder die entsprechende Methode
        // aufrufen
        return password + salt; // Beispiel, passe dies an deine Logik an
    }

    @Test
    public void testPasswordRegistration_ShortPassword() {
        // Testdaten: ein zu kurzes Passwort
        String email = "shortpass@example.com";
        String shortPassword = "Short1!"; // Nur 7 Zeichen

        // Methode zur Benutzerregistrierung aufrufen
        boolean result = userService.registerUser(email, shortPassword);

        // Erwartetes Ergebnis: Die Registrierung sollte fehlschlagen
        assertFalse(result, "Die Registrierung sollte aufgrund eines zu kurzen Passworts fehlschlagen");
    }

    @Test
    public void testPasswordRegistration_NoSpecialCharacter() {
        // Testdaten: ein Passwort ohne Sonderzeichen
        String email = "nospecial@example.com";
        String passwordNoSpecialChar = "Password1"; // Keine Sonderzeichen

        // Methode zur Benutzerregistrierung aufrufen
        boolean result = userService.registerUser(email, passwordNoSpecialChar);

        // Erwartetes Ergebnis: Die Registrierung sollte fehlschlagen
        assertFalse(result, "Die Registrierung sollte aufgrund des fehlenden Sonderzeichens fehlschlagen");
    }

}
