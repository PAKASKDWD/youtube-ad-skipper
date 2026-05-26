package com.youtube_ad_skipper;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdSkipTileService extends TileService {

    private static final String PREFS_NAME = "adskipper_prefs";
    private static final String KEY_ENABLED = "skip_enabled";

    public static boolean isSkipEnabled = true;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileWithStats();
    }

    @Override
    public void onClick() {
        super.onClick();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean newState = !prefs.getBoolean(KEY_ENABLED, true);

        prefs.edit().putBoolean(KEY_ENABLED, newState).apply();
        isSkipEnabled = newState;

        updateTileWithStats();
    }

    private void updateTileWithStats() {
        // DB에서 오늘 스킵 수를 가져와서 subtitle에 표시
        dbExecutor.execute(() -> {
            int todayCount = 0;
            try {
                SkipDatabase db = AdSkipperApp.getDatabase();
                if (db != null) {
                    // 오늘 0시 기준 밀리초
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    todayCount = db.skipDao().getTodayCountSync(cal.getTimeInMillis());
                }
            } catch (Exception ignored) {
            }

            int count = todayCount;
            // UI 업데이트는 메인 스레드에서
            Tile tile = getQsTile();
            if (tile == null) return;

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean enabled = prefs.getBoolean(KEY_ENABLED, true);
            isSkipEnabled = enabled;

            if (enabled) {
                tile.setState(Tile.STATE_ACTIVE);
                tile.setLabel("광고 스킵");
                tile.setSubtitle("오늘 " + count + "개 스킵");
            } else {
                tile.setState(Tile.STATE_INACTIVE);
                tile.setLabel("광고 스킵");
                tile.setSubtitle("일시 정지");
            }

            tile.updateTile();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
