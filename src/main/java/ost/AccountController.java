package ost;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import org.mindrot.jbcrypt.BCrypt;

public class AccountController {

    private Account account;
    
    @FXML
    private Button btLogin;
    
    @FXML
    private Button btLogout;

    @FXML
    private Button btSignUp;

    @FXML
    private Label lbLoginMessage;

    @FXML
    private Label lbSignUpMessage;

    @FXML
    private PasswordField pfLoginPassword;

    @FXML
    private PasswordField pfSignUpConfirmPassword;

    @FXML
    private PasswordField pfSignUpPassword;
     
    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tfSignUpEmail;

    @FXML
    private TextField tfUsername;

    @FXML
    private PasswordField pfOldPassword;

    @FXML
    private PasswordField pfNewPassword;

    @FXML
    private PasswordField pfConfirmNewPassword;

    @FXML
    private Label lbChangePasswordMessage;
 
    @FXML
    private void initialize() throws Exception {
        // create and init DB-Tables
        account = new Account();
        //account.initAccount();
    }   

    @FXML
    private void onSignUp(ActionEvent event) throws Exception  {
        // verify name, password and confirmed password
        String name = tfSignUpEmail.getText();
        if (name.isEmpty()) {
            lbSignUpMessage.setText("Type in email");
            return;
        }
    
        String pw = pfSignUpPassword.getText().trim();
        if (pw.equals("")) {
            lbSignUpMessage.setText("Enter a plausible password");
            return;
        }
    
        if (!pw.equals(pfSignUpConfirmPassword.getText())) {
            lbSignUpMessage.setText("Password and confirmed password are not identical");
            return;
        }
    
        // check password strength
        if (!isPasswordStrong(pw)) {
            lbSignUpMessage.setText("Password is too weak. It must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one digit.");
            return;
        }
    
        // verify account 
        if (account.verifyAccount(name)) {
            lbSignUpMessage.setText("Email " + name + " has already an account");
            return;
        }
        
        // add new account
        account.addAccount(name, pw);
        
        // select tab 'Log In'
        tabPane.getTabs().get(0).setDisable(true);
        
        // reset login and signup
        resetLogin();
        resetSignup();
        
        // select tab 'Log in'
        tabPane.getSelectionModel().select(1);
    }
    
    // Method to check if the password is strong enough
    private boolean isPasswordStrong(String password) {
        // Check password length
        if (password.length() < 8) {
            return false;
        }
    
        // Check for at least one uppercase letter, one lowercase letter, and one digit
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
    
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
    
        return hasUppercase && hasLowercase && hasDigit;
    }
    
    
    @FXML
    private void onLogin(ActionEvent event) {
        String name = tfUsername.getText();
        String pw = pfLoginPassword.getText();
                        
        if (account.verifyPassword(name, pw)) {
            tabPane.getTabs().get(0).setDisable(true);
            tabPane.getTabs().get(1).setDisable(true);
            tabPane.getTabs().get(2).setDisable(false);
            tabPane.getSelectionModel().select(2);
        } else {
            lbLoginMessage.setText("'Email' or 'Password' are wrong");
            tabPane.getTabs().get(0).setDisable(false);
        }
    }
   
    @FXML
    private void onLogout(ActionEvent event) {
        // set tabs
        tabPane.getTabs().get(0).setDisable(false);
        tabPane.getTabs().get(1).setDisable(false);
        tabPane.getTabs().get(2).setDisable(true);
        
        // reset login and select tab 'Log in'
        resetLogin();   
        tabPane.getSelectionModel().select(1);      
    }

    private void resetLogin() {
        tfUsername.setText("");
        pfLoginPassword.setText("");
        lbLoginMessage.setText("Login with your account");
    } 

    private void resetSignup() {
        tfSignUpEmail.setText("");
        pfSignUpPassword.setText("");
        pfSignUpConfirmPassword.setText("");
        lbSignUpMessage.setText("Create Account");
    }

    @FXML
    private void onChangePassword(ActionEvent event) {
    String email = tfUsername.getText();
    String oldPassword = pfOldPassword.getText();
    String newPassword = pfNewPassword.getText();
    String confirmNewPassword = pfConfirmNewPassword.getText();

    if (account.changePassword(email, oldPassword, newPassword, confirmNewPassword)) {
        lbChangePasswordMessage.setText("Passwort erfolgreich geändert.");
    } else {
        lbChangePasswordMessage.setText("Passwort konnte nicht geändert werden.");
    }
}
}