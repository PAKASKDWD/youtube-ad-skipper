package com.youtube_ad_skipper;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private TextView todayCountText;
    private TextView weekCountText;
    private TextView totalSavedText;

    private static final int SECONDS_PER_SKIP = 5;

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

        statusText = findViewById(R.id.statusText);
        todayCountText = findViewById(R.id.todayCountText);
        weekCountText = findViewById(R.id.weekCountText);
        totalSavedText = findViewById(R.id.totalSavedText);
        Button btnOpenSettings = findViewById(R.id.btnOpenSettings);

        btnOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        observeStats();
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

        statusText.setText(isEnabled ? "서비스 활성화 중" : "서비스 비활성화 상태");
    }

    private void observeStats() {
        SkipDao dao = AdSkipperApp.getDatabase().skipDao();

        dao.getTodayCount().observe(this, count ->
                todayCountText.setText("오늘: " + count + "개 스킵"));

        long weekStart = getWeekStartMillis();
        dao.getWeekCount(weekStart).observe(this, count ->
                weekCountText.setText("이번 주: " + count + "개 스킵"));

        dao.getTotalCount().observe(this, count -> {
            int totalSeconds = count * SECONDS_PER_SKIP;
            totalSavedText.setText("총 절약 시간: " + formatSeconds(totalSeconds));
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
