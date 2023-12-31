package org.mericoztiryaki.domain.model.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.constant.PnlHistoryUnit;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class Report {

    private final List<ITransaction> transactions;

    private final AggregatedAnalyzeResult aggregatedResult;

    private final List<InstrumentAnalyzeResult> openPositions;

    private final Map<PnlHistoryUnit, List<HistoricalAnalyzeResult>> historicalAnalyzeResult;

}
