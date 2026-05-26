package com.youtube_ad_skipper;

import android.Manifest;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private View statusDot;
    private TextView statusLabel;
    private TextView heroTitle;
    private TextView heroSubtitle;
    private TextView toggleSubtext;
    private LinearLayout heroCard;
    private TextView todayCountText;
    private TextView totalSavedText;
    private TextView totalSavedUnit;
    private TextView weekCountText;

    private static final int SECONDS_PER_SKIP = 5;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    requestQuickSettingsTile();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        statusDot = findViewById(R.id.statusDot);
        statusLabel = findViewById(R.id.statusLabel);
        heroTitle = findViewById(R.id.heroTitle);
        heroSubtitle = findViewById(R.id.heroSubtitle);
        toggleSubtext = findViewById(R.id.toggleSubtext);
        heroCard = findViewById(R.id.heroCard);
        todayCountText = findViewById(R.id.todayCountText);
        totalSavedText = findViewById(R.id.totalSavedText);
        totalSavedUnit = findViewById(R.id.totalSavedUnit);
        weekCountText = findViewById(R.id.weekCountText);

        Button btnOpenSettings = findViewById(R.id.btnOpenSettings);
        btnOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        requestNotificationPermission();
        observeStats();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                requestQuickSettingsTile();
            }
        }
    }

    private void requestQuickSettingsTile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            StatusBarManager statusBarManager = getSystemService(StatusBarManager.class);
            if (statusBarManager != null) {
                statusBarManager.requestAddTileService(
                        new ComponentName(this, AdSkipTileService.class),
                        "광고 스킵",
                        Icon.createWithResource(this, R.drawable.ic_tile_skip),
                        getMainExecutor(),
                        result -> { }
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        boolean isEnabled = enabledServices != null
                && enabledServices.contains(getPackageName() + "/" + AdSkipService.class.getName());

        if (isEnabled) {
            statusDot.setBackgroundResource(R.drawable.dot_live);
            statusLabel.setText("활성화 중");
            statusLabel.setTextColor(getColor(R.color.live));
            heroTitle.setText("광고를 자동으로\n스킵하고 있어요.");
            heroSubtitle.setText("백그라운드에서 접근성 서비스가 YouTube의 \"건너뛰기\" 버튼을 감지합니다.");
            toggleSubtext.setText("실행 중 · 데이터 사용 없음");
            heroCard.setBackgroundResource(R.drawable.bg_hero_card_active);
        } else {
            statusDot.setBackgroundResource(R.drawable.dot_inactive);
            statusLabel.setText("비활성화");
            statusLabel.setTextColor(getColor(R.color.md_on_surface_muted));
            heroTitle.setText("스킵 기능이\n꺼져 있어요.");
            heroSubtitle.setText("서비스를 켜려면 접근성 설정에서 활성화하세요.");
            toggleSubtext.setText("꺼짐");
            heroCard.setBackgroundResource(R.drawable.bg_hero_card);
        }
    }

    private void observeStats() {
        SkipDao dao = AdSkipperApp.getDatabase().skipDao();

        dao.getTodayCount().observe(this, count ->
                todayCountText.setText(String.valueOf(count)));

        long weekStart = getWeekStartMillis();
        dao.getWeekCount(weekStart).observe(this, count ->
                weekCountText.setText(count + "개 스킵"));

        dao.getTotalCount().observe(this, count -> {
            int totalSeconds = count * SECONDS_PER_SKIP;
            String[] formatted = formatDuration(totalSeconds);
            totalSavedText.setText(formatted[0]);
            totalSavedUnit.setText(formatted[1]);
        });
    }

    private long getWeekStartMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    // "4:32", "분" 또는 "1:05", "시간" 형태로 반환
    static String[] formatDuration(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        if (h > 0) {
            return new String[]{h + ":" + String.format("%02d", m), "시간"};
        }
        return new String[]{m + ":" + String.format("%02d", s), "분"};
    }

    static String formatSeconds(int totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + "초";
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes < 60) {
            return minutes + "분 " + seconds + "초";
        }
        int hours = minutes / 60;
        minutes = minutes % 60;
        return hours + "시간 " + minutes + "분";
    }
}
