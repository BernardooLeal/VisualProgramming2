/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.musiccomposer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author bernardoleal
 */
public class AuthenticationSingleton {
    private static AuthenticationSingleton instance;
    private String currentUser;
    private boolean isLoggedIn;
    //I asked chat for the path and he helped me remembering to use static final. I kept the var name he gave tho
    private static final String PASSWORD_FILE = "src/main/java/com/mycompany/musiccomposer/passwords.txt";
    
    private AuthenticationSingleton() {
        this.currentUser = "";
        this.isLoggedIn = false;
    }
    
    public static AuthenticationSingleton getInstance() {
        if (instance == null) {
            synchronized (AuthenticationSingleton.class) {
                if (instance == null) {
                    instance = new AuthenticationSingleton();
                }
            }
        }
        return instance;
    }
    
    public void logout() {
        this.currentUser = "";
        this.isLoggedIn = false;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            byte byteData[] = md.digest();
            
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xFF) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // It's fun to see the difference in the musicSingleton and this one because most of the code there
    // I did before some of our classes talking about communication and all those things
    // Now I'm just passing as a parameter instead of creating a field and passing it in every optionPane alert
    public boolean login(String username, char[] password, JFrame login) {
        try {
            BufferedReader input = new BufferedReader(new FileReader(PASSWORD_FILE));
            String storedPasswordHash = null;
            String line = input.readLine();
            
            // Look for the username in the passwords file
            while(line != null) {
                StringTokenizer st = new StringTokenizer(line);
                if (username.equals(st.nextToken())) {
                    storedPasswordHash = st.nextToken();
                }
                line = input.readLine();
            }
            input.close();
            
            // If username not found
            if (storedPasswordHash == null) {
                JOptionPane.showMessageDialog(login, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Hash the provided password
            String passwordHash = hashPassword(new String(password));
            
            // Compare with stored hash
            if(storedPasswordHash.equals(passwordHash)) {
                // Login successful
                this.currentUser = username;
                this.isLoggedIn = true;
                return true;
            } else {
                // Password incorrect
                JOptionPane.showMessageDialog(login, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch(FileNotFoundException e) {
            JOptionPane.showMessageDialog(login, "Password file not found", "Login Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("File not found error: " + e.getMessage());
            return false;
        } catch(IOException e) {
            JOptionPane.showMessageDialog(login, "Error reading password file", "Login Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("IO error: " + e.getMessage());
            return false;
        }
    }
    
    
    public boolean register(String username, char[] password, char[] confirmPassword, JFrame register) {
        if(password.length == 0 || username.length() == 0) {
            JOptionPane.showMessageDialog(register, "Fill with info", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String passwordStr = new String(password);
        String confirmStr = new String(confirmPassword);
        
        if(!passwordStr.equals(confirmStr)) {
            JOptionPane.showMessageDialog(register, "The passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            // Check if user already exists
            BufferedReader input = new BufferedReader(new FileReader(PASSWORD_FILE));
            String line = input.readLine();
            while(line != null) {
                StringTokenizer st = new StringTokenizer(line);
                if(username.equals(st.nextToken())) {
                    JOptionPane.showMessageDialog(register, "User already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    input.close();
                    return false;
                }
                line = input.readLine();
            }
            input.close();
            
            // Hash the password
            String passwordHash = hashPassword(passwordStr);
            
            // Write the new user to the password file
            BufferedWriter output = new BufferedWriter(new FileWriter(PASSWORD_FILE, true));
            output.write(username + " " + passwordHash + "\n");
            output.close();
            
            JOptionPane.showMessageDialog(register, "Registration successful! You can now log in.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch(FileNotFoundException e) {
            JOptionPane.showMessageDialog(register, "Password file not found", "Registration Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        } catch(IOException e) {
            JOptionPane.showMessageDialog(register, "Error writing to password file", "Registration Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        } catch(NoSuchElementException e) {
            JOptionPane.showMessageDialog(register, "Error processing password file", "Registration Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
}
