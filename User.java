package com.library.management.librarymanagementsystem;
import java.util.ArrayList;

/**
 * User.java
 * 
 * Defines a User object to be used for storing user information in Library Management System.
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class User {
    
    private String username;
    private String password;
    private boolean admin;
    private ArrayList<Book> books = new ArrayList<>();

    /**
     * Constructor for User class if books is empty
     * @param username username tied with user
     * @param password password tied with user
     * @param admin is permmisions for user admin
     */
    public User(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    /**
     * Constructor for User class if user has books
     * @param username username tied with user
     * @param password password tied with user
     * @param admin is permmisions for user admin
     * @param books books user has checked out
     */
    public User(String username, String password, boolean admin, ArrayList<Book> books) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.books = books;

    }

    //Getter Methods
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public boolean getAdmin() {return admin;}
    public ArrayList<Book> getBooks() {return books;}

    //Setter Methods
    public void setUsername(String newUsername) {this.username = newUsername;}
    public void setPassword(String newPassword) {this.password = newPassword;}
    public void setAdmin(boolean newAdmin) {this.admin = newAdmin;}
    public void setBooks(ArrayList<Book> newBooks) {this.books = newBooks;}
    
    /**
     * Converts a csv String to User object
     * @param csvLine line from csv file
     * @return User object created from csvLine
     */
    public static User fromCSVString(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        
        csvLine = csvLine.trim();
        String[] parts = csvLine.split(";");
        String username = parts[0].toLowerCase();
        String password = parts[1];
        boolean admin = Boolean.parseBoolean(parts[2]);
        if (parts.length > 3) {
            ArrayList<Book> books = new ArrayList<>();
            String[] book = parts[3].split(","); //Creates a new array containing only book elements
            for(int i = 0; i<book.length; i+=4) { //Iterates through book elements incrementing by 4 because each book object has 4 parameters
                books.add(new Book(book[i], book[i+1], book[i+2], book[i+3]));
            }
            return new User(username, password, admin, books);
        }
        else {
            return new User(username, password, admin); //If user has no books a different constructor is called
        }
    }

    /**
     * Converts User object to CSV String
     * @return csvString of User object
     */
    public String toCSVString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.username + ";" + this.password + ";" + this.admin);

        if(!this.books.isEmpty()) {
            for(int i = 0; i < this.books.size(); i++) {
                if(i == 0) {
                    sb.append(";" + this.books.get(i).getAuthor() + "," + this.books.get(i).getTitle() + "," + this.books.get(i).getBook_id() + "," + this.books.get(i).getGenre());
                }
                else {
                    sb.append("," + this.books.get(i).getAuthor() + "," + this.books.get(i).getTitle() + "," + this.books.get(i).getBook_id() + "," + this.books.get(i).getGenre());
                }
            }
        }
        String csvString = sb.toString();
        return csvString;
    }
}
