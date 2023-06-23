package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IExchangeService {

    Quotes exchange(LocalDate date, BigDecimal price, Currency source) throws PriceApiException;

}
