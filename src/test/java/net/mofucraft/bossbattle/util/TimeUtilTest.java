package net.mofucraft.bossbattle.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TimeUtilTest {

    @Test
    void formatTime_padsMinutesSecondsAndMillis() {
        assertEquals("00:00.000", TimeUtil.formatTime(0));
        assertEquals("00:01.005", TimeUtil.formatTime(1_005));
        assertEquals("01:05.250", TimeUtil.formatTime(65_250));
    }

    @Test
    void formatTime_doesNotWrapMinutesAtOneHour() {
        assertEquals("60:00.000", TimeUtil.formatTime(3_600_000));
        assertEquals("100:00.000", TimeUtil.formatTime(6_000_000));
    }

    @Test
    void formatTimeShort_truncatesMillis() {
        assertEquals("00:00", TimeUtil.formatTimeShort(999));
        assertEquals("02:03", TimeUtil.formatTimeShort(123_999));
    }

    @Test
    void formatSecondsReadable_usesMinutesOnlyWhenExact() {
        assertEquals("30秒", TimeUtil.formatSecondsReadable(30));
        assertEquals("1分", TimeUtil.formatSecondsReadable(60));
        assertEquals("5分", TimeUtil.formatSecondsReadable(300));
        assertEquals("1分1秒", TimeUtil.formatSecondsReadable(61));
        assertEquals("0秒", TimeUtil.formatSecondsReadable(0));
    }

    @Test
    void parseTimeString_acceptsPlainNumberAndMinuteSecondForms() {
        assertEquals(330, TimeUtil.parseTimeString("330"));
        assertEquals(330, TimeUtil.parseTimeString("5m30s"));
        assertEquals(300, TimeUtil.parseTimeString("5m"));
        assertEquals(45, TimeUtil.parseTimeString("45s"));
        assertEquals(330, TimeUtil.parseTimeString("  5M30S "));
    }

    @Test
    void parseTimeString_returnsZeroForEmptyOrUnparsable() {
        assertEquals(0, TimeUtil.parseTimeString(null));
        assertEquals(0, TimeUtil.parseTimeString(""));
        assertEquals(0, TimeUtil.parseTimeString("abc"));
    }

    /** 現状の挙動を固定するテスト: 単位の無い末尾の数字は無視される("5m30" は 300 秒)。 */
    @Test
    void parseTimeString_ignoresTrailingNumberWithoutUnit() {
        assertEquals(300, TimeUtil.parseTimeString("5m30"));
    }
}
