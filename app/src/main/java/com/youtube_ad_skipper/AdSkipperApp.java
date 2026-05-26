package com.youtube_ad_skipper;

import android.app.Application;
import androidx.room.Room;

public class AdSkipperApp extends Application {

    private static SkipDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(
                getApplicationContext(),
                SkipDatabase.class,
                "adskipper-db"
        ).build();
    }

    public static SkipDatabase getDatabase() {
        return database;
    }
}
