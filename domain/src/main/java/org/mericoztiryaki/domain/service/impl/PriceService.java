package org.mericoztiryaki.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Price;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.port.PriceSource;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PriceService implements IPriceService {

    private final PriceSource priceSource;
    private final Map<Instrument, Map<LocalDate, Quotes>> priceMap = new ConcurrentHashMap<>();

    @Override
    public Quotes getPrice(Instrument instrument, LocalDate date) throws PriceApiException {
        Map<LocalDate, Quotes> prices = priceMap.computeIfAbsent(instrument, (i) -> new ConcurrentHashMap<>());

        // Return directly from cache
        if (prices.containsKey(date)) {
            return prices.get(date);
        }

        System.out.println("Price cache MISS: " + instrument + " " + date);
        Map<LocalDate, Quotes> response = fetchPrices(instrument, date);

        // Put prices to cache
        response.keySet()
                .stream()
                .forEach(d -> prices.put(d, response.get(d)));

        return response.get(date);
    }

    private Map<LocalDate, Quotes> fetchPrices(Instrument instrument, LocalDate date) {
        List<Price> response = priceSource.getPriceWindow(instrument.getInstrumentType(), instrument.getSymbol(),
                date.minusDays(365 * 2), date.plusDays(365 * 2));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Map<LocalDate, Quotes> prices = new HashMap<>();

        response.forEach(r -> {
            Quotes price = new Quotes();

            r.getQuotes()
                    .keySet()
                    .forEach(k -> {
                        Currency c = Currency.valueOf(k);
                        price.getValue().computeIfAbsent(c, (_c) -> new BigDecimal(r.getQuotes().get(k)));
                    });

            prices.put(LocalDate.parse(r.getDay(), formatter), price);
        });

        return prices;
    }

    @Override
    public Quotes calculateExchangeRates(LocalDate date, BigDecimal price, Currency source) throws PriceApiException {
        Quotes exchangeRates = getPrice(new Instrument(InstrumentType.CURRENCY, source.toString()), date);
        return QuotesUtil.multiply(exchangeRates, price);
    }

}
