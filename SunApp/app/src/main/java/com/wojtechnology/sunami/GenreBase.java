package com.wojtechnology.sunami;

import android.provider.BaseColumns;

/**
 * Created by wojtekswiderski on 15-05-13.
 */
public final class GenreBase {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public GenreBase() {}

    /* Inner class that defines the table contents */
    public static abstract class GenreEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_GENRE = "genre";
        public static final String COLUMN_NAME_SHORT_TERM = "shortterm";
        public static final String COLUMN_NAME_LONG_TERM = "longterm";
    }
}
