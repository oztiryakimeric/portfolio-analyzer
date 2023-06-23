package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;

import java.time.LocalDate;

public interface IPriceService {

    Quotes getPrice(Instrument instrument, LocalDate date) throws PriceApiException;

}
