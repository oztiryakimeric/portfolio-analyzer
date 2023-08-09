package org.mericoztiryaki.domain.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.mericoztiryaki.domain.model.Quotes;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class HistoricalAnalyzeResult {
    private final LocalDate start;
    private final LocalDate end;

    @Setter
    private Quotes pnl = Quotes.ZERO;
    private Quotes roi;

}
