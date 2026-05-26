package com.youtube_ad_skipper;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "skip_records")
public class SkipRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long timestamp;

    public SkipRecord(long timestamp) {
        this.timestamp = timestamp;
    }
}
