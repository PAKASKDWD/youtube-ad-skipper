package com.youtube_ad_skipper;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class AdSkipTileService extends TileService {

    private static final String PREFS_NAME = "adskipper_prefs";
    private static final String KEY_ENABLED = "skip_enabled";

    // 다른 컴포넌트에서 스킵 활성화 여부 확인용
    public static boolean isSkipEnabled = true;

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean currentState = prefs.getBoolean(KEY_ENABLED, true);
        boolean newState = !currentState;

        prefs.edit().putBoolean(KEY_ENABLED, newState).apply();
        isSkipEnabled = newState;

        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_ENABLED, true);
        isSkipEnabled = enabled;

        if (enabled) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel("광고 스킵 ON");
            tile.setSubtitle("자동 스킵 중");
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("광고 스킵 OFF");
            tile.setSubtitle("일시 정지");
        }

        tile.updateTile();
    }
}
