package org.mericoztiryaki.domain.model;

import lombok.Data;
import org.mericoztiryaki.domain.model.constant.InstrumentType;

import java.text.MessageFormat;

@Data
public class Instrument {

    private InstrumentType instrumentType;

    private String symbol;

    public Instrument(InstrumentType instrumentType, String symbol) {
        this.instrumentType = instrumentType;
        this.symbol = symbol;
    }

    public String getId() {
        return generateId(this.instrumentType, this.symbol);
    }

    public static String generateId(InstrumentType instrumentType, String symbol) {
        return MessageFormat.format("{0}-{1}", instrumentType, symbol);
    }

    @Override
    public String toString() {
        return instrumentType + " - " + symbol;
    }
}
