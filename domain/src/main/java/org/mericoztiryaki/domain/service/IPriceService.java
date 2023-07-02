package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IPriceService {

    Quotes getPrice(Instrument instrument, LocalDate date) throws PriceApiException;

    Quotes calculateExchangeRates(LocalDate date, BigDecimal price, Currency source) throws PriceApiException;

}
