package com.youtube_ad_skipper;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface SkipDao {

    @Insert
    void insert(SkipRecord record);

    @Query("SELECT COUNT(*) FROM skip_records WHERE date(timestamp/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    LiveData<Integer> getTodayCount();

    @Query("SELECT COUNT(*) FROM skip_records WHERE timestamp >= :weekStartMillis")
    LiveData<Integer> getWeekCount(long weekStartMillis);

    @Query("SELECT COUNT(*) FROM skip_records")
    LiveData<Integer> getTotalCount();

    // Quick Settings 타일용 동기 쿼리 (백그라운드 스레드에서 호출)
    @Query("SELECT COUNT(*) FROM skip_records WHERE timestamp >= :todayStartMillis")
    int getTodayCountSync(long todayStartMillis);
}
