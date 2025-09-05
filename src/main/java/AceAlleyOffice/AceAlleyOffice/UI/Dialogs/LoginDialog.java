// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/LoginDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.Core.User;

public class LoginDialog extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private User authenticatedUser = null;
    private static final String LAST_EMAIL_PREF = "last_user_email";
    public LoginDialog(Frame owner, FirebaseDataManager dataManager, String webApiKey) {
        super(owner, "Ace Alley Office Login", true);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; emailField = new JTextField(20); panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; passwordField = new JPasswordField(); panel.add(passwordField, gbc);
        
        loginButton = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);
        
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        gbc.gridy = 3; panel.add(statusLabel, gbc);

        Preferences prefs = Preferences.userNodeForPackage(LoginDialog.class);
        emailField.setText(prefs.get(LAST_EMAIL_PREF, "")); // Pre-fill the email field

        loginButton.addActionListener(e -> {
            loginButton.setEnabled(false);
            statusLabel.setText("Authenticating...");
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            dataManager.authenticateUser(email, password, webApiKey, (user, errorMessage) -> {
                if (user != null) {
                    this.authenticatedUser = user;
                    // --- NEW: Save the email on successful login ---
                    prefs.put(LAST_EMAIL_PREF, email);
                    dispose();
                } else {
                    statusLabel.setText(errorMessage);
                    loginButton.setEnabled(true);
                }
            });
        });


        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public User showDialogAndGetUser() {
        setVisible(true);
        return this.authenticatedUser;
    }
}