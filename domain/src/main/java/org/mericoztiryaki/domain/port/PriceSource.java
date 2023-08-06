package org.mericoztiryaki.domain.port;

import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Price;
import org.mericoztiryaki.domain.model.constant.InstrumentType;

import java.time.LocalDate;
import java.util.List;

public interface PriceSource {

    List<Price> getPriceWindow(InstrumentType instrumentType,
                               String symbol,
                               LocalDate start,
                               LocalDate end) throws PriceApiException;

}
