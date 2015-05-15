package com.wojtechnology.sunami.archiveJava;

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
        public static final String COLUMN_NAME_GENRE = "genre";
        public static final String COLUMN_NAME_SHORT_TERM = "shortterm";
        public static final String COLUMN_NAME_LONG_TERM = "longterm";
    }
}

// Used to be included in GenreContainer.java, when databases were used
// No longer required
/*public class Genre{
        public double shortTerm;
        public double longTerm;
        public String genre;

        public Genre(String genre, double shortTerm, double longTerm){
            this.genre = genre;
            this.shortTerm = shortTerm;
            this.longTerm = longTerm;
        }
    }

    // First time, populate DB with zeroes
    public void populateDB(){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isPopulated = sharedPref.getBoolean(
                context.getString(R.string.saved_db_status), false);
        if(!isPopulated) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            SQLiteDatabase db = mDB.getWritableDatabase();
            Set<GenreVertex> genres = mEdges.keySet();
            int numGenres = 0;
            for (GenreVertex genre : genres) {
                ContentValues values = new ContentValues();
                values.put(GenreBase.GenreEntry.COLUMN_NAME_GENRE, genre.genre);
                values.put(GenreBase.GenreEntry.COLUMN_NAME_SHORT_TERM, 0.0);
                values.put(GenreBase.GenreEntry.COLUMN_NAME_LONG_TERM, 0.0);
                db.insert(GenreBase.GenreEntry.TABLE_NAME, null, values);
                numGenres++;
            }
            Log.i("GenreContainer: ", "Finished populateDB() in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis with numGenres: " + Integer.toString(numGenres) + ".");
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.saved_db_status), true);
            editor.commit();
        }
    }*/
