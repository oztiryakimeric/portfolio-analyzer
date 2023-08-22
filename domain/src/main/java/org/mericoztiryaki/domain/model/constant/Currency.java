package org.mericoztiryaki.domain.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    USD("$"),
    EUR("€"),
    TRY("₺");

    private final String prefix;

    public static Currency parse(String s) {
        switch (s) {
            case "usd":
                return USD;
            case "eur":
                return EUR;
            case "try":
                return TRY;
            default:
                throw new IllegalArgumentException("Currency value is invalid: " + s);
        }
    }
}
