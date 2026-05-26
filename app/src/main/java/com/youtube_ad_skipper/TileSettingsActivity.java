package com.youtube_ad_skipper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

public class TileSettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "adskipper_prefs";
    private static final String KEY_ENABLED = "skip_enabled";

    private TextView accessibilityStatus;
    private MaterialSwitch skipToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_settings);

        accessibilityStatus = findViewById(R.id.accessibilityStatus);
        skipToggle = findViewById(R.id.skipToggle);
        Button btnAccessibility = findViewById(R.id.btnAccessibility);
        Button btnClose = findViewById(R.id.btnClose);

        // 스킵 ON/OFF 토글
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        skipToggle.setChecked(prefs.getBoolean(KEY_ENABLED, true));
        skipToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_ENABLED, isChecked).apply();
            AdSkipTileService.isSkipEnabled = isChecked;
        });

        // 접근성 설정 열기
        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        btnClose.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccessibilityStatus();
    }

    private void updateAccessibilityStatus() {
        String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        boolean isEnabled = enabledServices != null
                && enabledServices.contains(getPackageName() + "/" + AdSkipService.class.getName());

        if (isEnabled) {
            accessibilityStatus.setText("접근성 서비스: 활성화 중");
            accessibilityStatus.setTextColor(getColor(R.color.live));
        } else {
            accessibilityStatus.setText("접근성 서비스: 비활성화");
            accessibilityStatus.setTextColor(getColor(R.color.accent));
        }
    }
}
