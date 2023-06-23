package org.mericoztiryaki.domain.exception;

import lombok.AllArgsConstructor;
import org.mericoztiryaki.domain.model.constant.InstrumentType;

import java.time.LocalDate;

@AllArgsConstructor
public class PriceApiException extends RuntimeException {

    private InstrumentType instrument;

    private String symbol;

    private LocalDate start;

    private LocalDate end;
}
