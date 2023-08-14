package org.mericoztiryaki.domain.model;

import lombok.Data;
import org.mericoztiryaki.domain.model.constant.InstrumentType;

import java.io.Serializable;
import java.text.MessageFormat;

@Data
public class Instrument implements Serializable {

    private static final long serialVersionUID = 1;

    private InstrumentType instrumentType;

    private String symbol;

    public Instrument(InstrumentType instrumentType, String symbol) {
        this.instrumentType = instrumentType;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return instrumentType + " - " + symbol;
    }
}
