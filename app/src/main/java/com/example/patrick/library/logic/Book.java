package com.example.patrick.library.logic;

/**
 * Created by Patrick on 17/01/2018.
 */

public class Book {
    public static Book[] books = new Book[10];

    public String name;
    public String authorFirstName;
    public String authorLastName;
    public String datePublished;
    public boolean checkedOut;
    public String dateCheckedOut;

    public Book (String name, String authFN, String authLN, String dateP, String dateCO) {
        this.name = name;
        authorFirstName = authFN;
        authorLastName = authLN;
        datePublished = dateP;
        checkedOut = false;
        dateCheckedOut = dateCO;
    }
}
