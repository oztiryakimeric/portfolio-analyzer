package org.mericoztiryaki.app.adapter;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Price;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.port.PriceSource;
import org.mericoztiryaki.domain.util.Environment;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class PriceApiAdapter implements PriceSource {

    public PriceApiAdapter() {
        log.info("PriceSource created for host: {}", Environment.PRICE_API_HOST);
    }

    @Override
    public List<Price> getPriceWindow(InstrumentType instrumentType,
                                      String symbol, LocalDate start, LocalDate end) throws PriceApiException {
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
            PriceListResponse resp =  gson.fromJson(response.string(), PriceListResponse.class);
            return resp.getData().stream()
                    .map(d -> new Price(d.getDay(), d.getQuotes())).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Price api exception: " + url, e);
            throw new PriceApiException(instrumentType, symbol, start, end);
        }
    }

    @Data
    private static class PriceListResponse {

        private List<Day> data;

        @Data
        public static class Day {
            private String day;
            private Map<String, String> quotes;
        }
    }
}
