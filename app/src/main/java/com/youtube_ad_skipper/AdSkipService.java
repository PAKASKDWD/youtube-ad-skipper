package com.youtube_ad_skipper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.SharedPreferences;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdSkipService extends AccessibilityService {

    public static boolean isRunning = false;

    private static final List<String> SKIP_KEYWORDS = Arrays.asList(
            "건너뛰기",
            "광고 건너뛰기",
            "Skip",
            "Skip Ad",
            "Skip ad",
            "skip ad"
    );

    private static final long COOLDOWN_MS = 2000;
    private long lastSkipTime = 0;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }

        // Quick Settings 타일에서 OFF로 설정한 경우 스킵하지 않음
        if (!AdSkipTileService.isSkipEnabled) {
            return;
        }

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        findAndClickSkipButton(source);
        source.recycle();
    }

    private void findAndClickSkipButton(AccessibilityNodeInfo node) {
        if (node == null) return;

        String nodeText = node.getText() != null ? node.getText().toString() : "";
        String nodeDesc = node.getContentDescription() != null
                ? node.getContentDescription().toString() : "";

        if (containsSkipKeyword(nodeText) || containsSkipKeyword(nodeDesc)) {
            long now = System.currentTimeMillis();
            if (now - lastSkipTime < COOLDOWN_MS) {
                return;
            }

            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                lastSkipTime = now;
                recordSkip();
                return;
            }

            AccessibilityNodeInfo parent = node.getParent();
            if (parent != null) {
                if (parent.isClickable()) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    lastSkipTime = now;
                    recordSkip();
                }
                parent.recycle();
                return;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                findAndClickSkipButton(child);
                child.recycle();
            }
        }
    }

    static boolean containsSkipKeyword(String text) {
        if (text == null || text.isEmpty()) return false;

        for (String keyword : SKIP_KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void recordSkip() {
        dbExecutor.execute(() -> {
            SkipDatabase db = AdSkipperApp.getDatabase();
            if (db != null) {
                db.skipDao().insert(new SkipRecord(System.currentTimeMillis()));
            }
        });
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        isRunning = true;

        // SharedPreferences에서 타일 상태 복원
        SharedPreferences prefs = getSharedPreferences("adskipper_prefs", MODE_PRIVATE);
        AdSkipTileService.isSkipEnabled = prefs.getBoolean("skip_enabled", true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        dbExecutor.shutdown();
    }
}
