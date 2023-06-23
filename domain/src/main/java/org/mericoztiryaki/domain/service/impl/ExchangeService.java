package org.mericoztiryaki.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.service.IExchangeService;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class ExchangeService implements IExchangeService {

    private final IPriceService priceService;

    @Override
    public Quotes exchange(LocalDate date, BigDecimal price, Currency source) throws PriceApiException {
        Quotes exchangeRates = priceService.getPrice(new Instrument(InstrumentType.CURRENCY, source.toString()), date);
        return QuotesUtil.multiply(exchangeRates, price);
    }

}
