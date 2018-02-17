package com.example.patrick.library.logic;

import java.util.ArrayList;

/**
 * Created by Patrick on 17/01/2018.
 */

public class Book {
    public static ArrayList<Book> books = new ArrayList<>();

    public String name;
    public String authorFirstName;
    public String authorLastName;
    public String datePublished;
    public boolean reserved;
    public String dateReserved;
    public boolean checkedOut;
    public String dateCheckedOut;
    public String userKey;
    public String libraryKey;
    public String bookKey;

    public Book (String name, String authFN, String authLN, String dateP, char r, String dateR, char co, String dateCO, String uKey, String lKey, String bKey) {
        this.name = name;
        authorFirstName = authFN;
        authorLastName = authLN;
        datePublished = dateP;
        reserved = r == '1';
        dateReserved = dateR;
        checkedOut = co == '1';
        dateCheckedOut = dateCO;
        userKey = uKey;
        libraryKey = lKey;
        bookKey = bKey;
    }
}
