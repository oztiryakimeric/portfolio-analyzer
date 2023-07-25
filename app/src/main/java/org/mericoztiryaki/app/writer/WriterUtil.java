package org.mericoztiryaki.app.writer;

import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;

public class WriterUtil {
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat RATE_FORMAT = new DecimalFormat("#0.00");

    public static String asCurrency(Quotes value, Currency currency) {
        return CURRENCY_FORMAT.format(value.getValue().get(currency));
    }

    public static String asRate(BigDecimal value) {
        return RATE_FORMAT.format(value);
    }

    public static String withCurrencyLabel(String text, Currency currency) {
        return MessageFormat.format("{0} ({1})", text, String.valueOf(currency));
    }
}
