package com.yiran.xingtian.common.util;

import org.apache.commons.lang3.RandomUtils;

/**
 * Created by Xingtian on 2017-01-13.
 */
public final class IdGenUtil {
    public static long generateUid() {
        final long now = System.currentTimeMillis();
        final int rand =  RandomUtils.nextInt();

        return (now << 4) | (rand & 0xf);
    }
}
