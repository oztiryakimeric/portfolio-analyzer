package org.mericoztiryaki.domain.model.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.Quotes;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class Report {

    private final AggregatedAnalyzeResult aggregatedResult;

    private final List<InstrumentAnalyzeResult> openPositions;

    private final Map<String, Quotes> weeklyPnlHistory;

    private final Map<String, Quotes> dailyPnlHistory;

}
