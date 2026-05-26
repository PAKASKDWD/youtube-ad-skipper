package com.youtube_ad_skipper;

import org.junit.Test;
import static org.junit.Assert.*;

public class MainActivityTest {

    @Test
    public void formatSeconds_zero() {
        assertEquals("0초", MainActivity.formatSeconds(0));
    }

    @Test
    public void formatSeconds_underMinute() {
        assertEquals("30초", MainActivity.formatSeconds(30));
    }

    @Test
    public void formatSeconds_minutesAndSeconds() {
        assertEquals("4분 32초", MainActivity.formatSeconds(272));
    }

    @Test
    public void formatSeconds_overHour() {
        assertEquals("1시간 5분", MainActivity.formatSeconds(3900));
    }

    @Test
    public void formatSeconds_typicalDaily() {
        assertEquals("1분 55초", MainActivity.formatSeconds(115));
    }
}
