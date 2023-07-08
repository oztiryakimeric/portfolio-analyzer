package org.mericoztiryaki.domain.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    // Two BigDecimal objects that are equal in value but have a different scale (like 2.0 and 2.00) are considered
    // equal by this method.
    public static boolean isEquals(BigDecimal d1, BigDecimal d2) {
        return d1.compareTo(d2) == 0;
    }

    public static boolean isZero(BigDecimal d1) {
        return isEquals(d1, BigDecimal.ZERO);
    }
}
