package org.mericoztiryaki.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mericoztiryaki.domain.model.constant.Currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quotes implements Serializable {

    private static final long serialVersionUID = 2;

    public static Quotes ZERO = new Quotes(Map.of(
            Currency.USD, BigDecimal.ZERO,
            Currency.EUR, BigDecimal.ZERO,
            Currency.TRY, BigDecimal.ZERO)
    );

    private Map<Currency, BigDecimal> value = new HashMap<>();

    @Override
    public String toString() {
        return value.toString();
    }

}