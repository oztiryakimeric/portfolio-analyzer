package org.mericoztiryaki.domain.util;

import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QuotesUtil {

    public static boolean isZero(Quotes q1) {
        return q1.getValue().values().stream().anyMatch(BigDecimalUtil::isZero);
    }

    // Quotes - BigDecimal Functions
    public static Quotes multiply(Quotes q1, BigDecimal constant) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().multiply(constant)));
    }
    public static Quotes divide(Quotes q1, BigDecimal constant) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().divide(constant, 5, RoundingMode.HALF_UP)));
    }
    public static Quotes add(Quotes q1, BigDecimal constant) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().add(constant)));
    }
    public static Quotes subtract(Quotes q1, BigDecimal constant) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().subtract(constant)));
    }

    // Quotes - Quotes Functions
    public static Quotes add(Quotes q1, Quotes q2) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().add(q2.getValue().get(e.getKey()))));
    }

    public static Quotes subtract(Quotes q1, Quotes q2) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().subtract(q2.getValue().get(e.getKey()))));
    }

    public static Quotes multiply(Quotes q1, Quotes q2) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().multiply(q2.getValue().get(e.getKey()))));
    }

    public static Quotes divide(Quotes q1, Quotes q2) {
        return new Quotes(applyToQuotes(q1.getValue(), e -> e.getValue().divide(q2.getValue().get(e.getKey()), 5, RoundingMode.HALF_UP)));
    }

    private static Map<Currency, BigDecimal> applyToQuotes(Map<Currency, BigDecimal> quotes,
                                                           Function<Map.Entry<Currency, BigDecimal>, BigDecimal> valueMapper) {
        return quotes.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), valueMapper));
    }

}