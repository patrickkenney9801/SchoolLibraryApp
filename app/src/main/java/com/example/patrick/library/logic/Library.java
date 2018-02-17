package com.example.patrick.library.logic;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Patrick on 11/02/2018.
 */

public class Library {
    public static ArrayList<Library> libraries = new ArrayList<>();

    public String name;
    public String librarianPassword;
    public String teacherPassword;
    public String generalPassword;
    public int generalCheckOutLimit;
    public String libraryKey;
    public String libraryMap;
    public int lendDays;
    public float dailyFee;

    public Library (String name, String lPass, String tPass, String gPass, int coLimit, String lKey, String lMap, int lDays, float dFee) {
        this.name = name;
        librarianPassword = lPass;
        teacherPassword = tPass;
        generalPassword = gPass;
        generalCheckOutLimit = coLimit;
        libraryKey = lKey;
        libraryMap = lMap;
        lendDays = lDays;
        dailyFee = dFee;
    }
}
