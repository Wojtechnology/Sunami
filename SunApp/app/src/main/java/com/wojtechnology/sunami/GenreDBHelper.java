package com.wojtechnology.sunami;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wojtekswiderski on 15-05-13.
 */
public class GenreDBHelper extends SQLiteOpenHelper{
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + GenreBase.GenreEntry.TABLE_NAME + " (" +
                    GenreBase.GenreEntry._ID + " INTEGER PRIMARY KEY," +
                    GenreBase.GenreEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    GenreBase.GenreEntry.COLUMN_NAME_GENRE + TEXT_TYPE + COMMA_SEP +
                    GenreBase.GenreEntry.COLUMN_NAME_SHORT_TERM + REAL_TYPE + COMMA_SEP +
                    GenreBase.GenreEntry.COLUMN_NAME_LONG_TERM + REAL_TYPE + COMMA_SEP +
                    " )";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    public GenreDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
