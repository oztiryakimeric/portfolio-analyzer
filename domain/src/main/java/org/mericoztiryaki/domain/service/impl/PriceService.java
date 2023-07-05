package org.mericoztiryaki.domain.service.impl;

import com.google.gson.Gson;
import lombok.Data;
import okhttp3.*;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.util.Environment;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PriceService implements IPriceService {

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
        PriceListResponse response = getPriceWindow(instrument.getInstrumentType(), instrument.getSymbol(),
                date.minusDays(365 * 2), date.plusDays(365 * 2));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Map<LocalDate, Quotes> prices = new HashMap<>();

        response.getData().forEach(r -> {
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

    private PriceListResponse getPriceWindow(InstrumentType instrumentType, String symbol,
                                             LocalDate start, LocalDate end) throws PriceApiException {
        OkHttpClient client = new OkHttpClient();

        String service = MessageFormat.format("{0}/price_window/{1}/{2}",
                Environment.PRICE_API_HOST, instrumentType, symbol);

        HttpUrl.Builder urlBuilder
                = HttpUrl.parse(service).newBuilder();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        urlBuilder.addQueryParameter("start", formatter.format(start));
        urlBuilder.addQueryParameter("end", formatter.format(end));

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);

        try(ResponseBody response = call.execute().body()) {
            Gson gson = new Gson();
            return gson.fromJson(response.string(), PriceListResponse.class);
        } catch (Exception e) {
            System.out.println("Price api exception: " + url);
            e.printStackTrace();
            throw new PriceApiException(instrumentType, symbol, start, end);
        }
    }

    @Data
    private static class PriceListResponse {

        private List<PriceResponse> data;

        @Data
        public static class PriceResponse {
            private String day;
            private Map<String, String> quotes;
        }
    }

    @Override
    public Quotes calculateExchangeRates(LocalDate date, BigDecimal price, Currency source) throws PriceApiException {
        Quotes exchangeRates = getPrice(new Instrument(InstrumentType.CURRENCY, source.toString()), date);
        return QuotesUtil.multiply(exchangeRates, price);
    }

}
