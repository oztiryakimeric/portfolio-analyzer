package org.mericoztiryaki.domain.exception;

import org.mericoztiryaki.domain.model.constant.InstrumentType;

import java.text.MessageFormat;
import java.time.LocalDate;

public class PriceApiException extends ReportGenerationException {

    private InstrumentType instrument;

    private String symbol;

    private LocalDate start;

    private LocalDate end;

    public PriceApiException(Throwable cause, InstrumentType instrument, String symbol, LocalDate start, LocalDate end) {
        super(cause);
        this.instrument = instrument;
        this.symbol = symbol;
        this.start = start;
        this.end = end;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("{0} price can't accessed for window {1} - {2}", symbol, start, end);
    }
}
