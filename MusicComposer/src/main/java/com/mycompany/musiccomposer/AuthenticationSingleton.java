/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.musiccomposer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
    private static final String USER_PATH = "src/main/java/com/mycompany/musiccomposer/users/";
    
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
    
    public String getCurrentUserFolderPath() {
        if (!isLoggedIn || currentUser.isEmpty()) {
            return null;
        }
        return getUserFolderPath(currentUser);
    }
    
    public String getUserFolderPath(String username) {
        return USER_PATH + username + "/";
    }
    
    public boolean createUserFolder(String username) {
        String folderPath = getUserFolderPath(username);
        File userFolder = new File(folderPath);
        
        if (userFolder.exists()) {
            return true; // Folder already exists
        }
        
        boolean created = userFolder.mkdirs();
        if (!created) {
            System.err.println("Failed to create user folder at: " + folderPath);
            return false;
        }
        
        return true;
    }
    
    public boolean storeUserData(String filename, String data) {
        if (!isLoggedIn || currentUser.isEmpty()) {
            return false;
        }
        
        String userFolder = getCurrentUserFolderPath();
        if (userFolder == null) {
            return false;
        }
        
        try {
            File file = new File(userFolder + filename);
            // Create parent directories if they don't exist
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(data);
            writer.close();
            return true;
            
        } catch (IOException e) {
            System.err.println("Error writing user data: " + e.getMessage());
            return false;
        }
    }
    
    public String retrieveUserData(String filename) {
        if (!isLoggedIn || currentUser.isEmpty()) {
            return null;
        }
        
        String userFolder = getCurrentUserFolderPath();
        if (userFolder == null) {
            return null;
        }
        
        try {
            File file = new File(userFolder + filename);
            if (!file.exists()) {
                return null;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder data = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                data.append(line).append("\n");
            }
            
            reader.close();
            return data.toString();
        } catch (IOException e) {
            System.err.println("Error reading user data: " + e.getMessage());
            return null;
        }
    }
    
    public String[] listUserFiles() {
        if (!isLoggedIn || currentUser.isEmpty()) {
            return null;
        }
        
        String userFolder = getCurrentUserFolderPath();
        if (userFolder == null) {
            return null;
        }
        
        File folder = new File(userFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            return new String[0];
        }
        
        return folder.list();
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
                createUserFolder(username);
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
    
    public boolean deleteUserFile(String filename) {
        if (!isLoggedIn || currentUser.isEmpty()) {
            return false;
        }
        
        String userFolder = getCurrentUserFolderPath();
        if (userFolder == null) {
            return false;
        }
        
        File file = new File(userFolder + filename);
        if (!file.exists()) {
            return false;
        }
        
        return file.delete();
    }
}
