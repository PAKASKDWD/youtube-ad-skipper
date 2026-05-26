package com.youtube_ad_skipper;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdSkipServiceTest {

    @Test
    public void containsSkipKeyword_korean() {
        assertTrue(AdSkipService.containsSkipKeyword("건너뛰기"));
    }

    @Test
    public void containsSkipKeyword_koreanAdSkip() {
        assertTrue(AdSkipService.containsSkipKeyword("광고 건너뛰기"));
    }

    @Test
    public void containsSkipKeyword_english() {
        assertTrue(AdSkipService.containsSkipKeyword("Skip"));
    }

    @Test
    public void containsSkipKeyword_englishSkipAd() {
        assertTrue(AdSkipService.containsSkipKeyword("Skip Ad"));
    }

    @Test
    public void containsSkipKeyword_embeddedInText() {
        assertTrue(AdSkipService.containsSkipKeyword("동영상 광고 건너뛰기 버튼"));
    }

    @Test
    public void containsSkipKeyword_noMatch() {
        assertFalse(AdSkipService.containsSkipKeyword("재생"));
    }

    @Test
    public void containsSkipKeyword_null() {
        assertFalse(AdSkipService.containsSkipKeyword(null));
    }

    @Test
    public void containsSkipKeyword_empty() {
        assertFalse(AdSkipService.containsSkipKeyword(""));
    }
}
