package org.mericoztiryaki.domain.model.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.mericoztiryaki.domain.model.Quotes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
@RequiredArgsConstructor
public class HistoricalAnalyzeResult {
    private final LocalDate start;
    private final LocalDate end;

    private Quotes initialValue = Quotes.ZERO;

    private Quotes totalValue = Quotes.ZERO;

    private Quotes cashFlow = Quotes.ZERO;

    private Quotes pnl = Quotes.ZERO;

    private Quotes change;

    private Map<String, Quotes> marketData = new LinkedHashMap<>();
}
