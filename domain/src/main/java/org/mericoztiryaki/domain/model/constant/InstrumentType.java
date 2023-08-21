package org.mericoztiryaki.domain.model.constant;

public enum InstrumentType {
    BIST,
    CURRENCY,
    FUND;

    public static InstrumentType parse(String s) {
        switch (s) {
            case "bist":
                return BIST;
            case "currency":
                return CURRENCY;
            case "fund":
                return FUND;
            default:
                throw new IllegalArgumentException("InstrumentType value is invalid: " + s);
        }
    }
}
