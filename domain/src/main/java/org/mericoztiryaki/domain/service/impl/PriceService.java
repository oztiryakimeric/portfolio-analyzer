package org.mericoztiryaki.domain.service.impl;

import com.google.inject.internal.util.ImmutableMap;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.codehaus.plexus.util.CachedMap;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Price;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.port.PriceSource;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.util.Environment;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class PriceService implements IPriceService {

    private final PriceSource priceSource;
    private final PriceCache cache;

    public PriceService(PriceSource priceSource) {
        this.priceSource = priceSource;
        this.cache = new PriceCache();

        this.cache.initialize(Environment.PRICE_CACHE_PATH);
    }

    @Override
    public Quotes getPrice(Instrument instrument, LocalDate date) throws PriceApiException {
        CacheKey cacheKey = new CacheKey(instrument, date);

        // Return directly from cache
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        System.out.println("Price cache MISS: " + instrument + " " + date);
        Map<LocalDate, Quotes> response = fetchPrices(instrument, date);

        // Put prices to cache
        cache.putAll(response.keySet()
                .stream()
                .collect(Collectors.toMap((d) -> new CacheKey(instrument, d), (d) -> response.get(d))));

        return response.get(date);
    }

    private Map<LocalDate, Quotes> fetchPrices(Instrument instrument, LocalDate date) {
        List<Price> response = priceSource.getPriceWindow(instrument.getInstrumentType(), instrument.getSymbol(),
                date.minusDays(60), date.plusDays(60));

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

    @Override
    public void save() {
        this.cache.persist(Environment.PRICE_CACHE_PATH);
    }

    public class PriceCache {

        private ConcurrentMap<CacheKey, Quotes> internalCache;

        public PriceCache() {
            this.internalCache = new ConcurrentHashMap<>();
        }

        public boolean containsKey(CacheKey key) {
            return internalCache.containsKey(key);
        }

        public void putAll(Map<CacheKey, Quotes> values) {
            internalCache.putAll(values);
        }

        public Quotes get(CacheKey key) {
            return internalCache.get(key);
        }

        // Initializes cache from starting point by reading file.
        public void initialize(String path) {
            System.out.println("Opening cache file.");
            File cacheFile = new File(path);

            if (!cacheFile.exists() || cacheFile.isDirectory()){
                System.out.println("There is no cache file.");
                return;
            }

            try (FileInputStream fi = new FileInputStream(path);
                 ObjectInputStream oi = new ObjectInputStream(fi)){

                internalCache = new ConcurrentHashMap<>((HashMap<CacheKey, Quotes>) oi.readObject());
                System.out.println("Cache file opened.");
            } catch (Exception e) {
                System.out.println("Cache can't read from file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Saves cache to file in order to initialize it from this point
        public void persist(String path) {
            // Remove curreny day's prices
            Map<CacheKey, Quotes> pricesToCache =
                    internalCache.keySet().stream()
                            .filter(k -> !k.getDate().isEqual(LocalDate.now()))
                            .collect(Collectors.toMap((k) -> k, (k) -> internalCache.get(k)));

            try (FileOutputStream f = new FileOutputStream(path);
                 ObjectOutputStream o = new ObjectOutputStream(f)) {

                o.writeObject(pricesToCache);
            } catch (IOException e) {
                System.out.println("Cache can't saved: e" + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    @Data
    private static class CacheKey implements Serializable {

        private static final long serialVersionUID = 3;

        private final Instrument instrument;
        private final LocalDate date;
    }
}
