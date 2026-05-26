package com.youtube_ad_skipper;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SkipRecord.class}, version = 1, exportSchema = false)
public abstract class SkipDatabase extends RoomDatabase {
    public abstract SkipDao skipDao();
}
