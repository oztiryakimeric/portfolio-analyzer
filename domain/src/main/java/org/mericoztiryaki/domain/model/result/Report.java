package org.mericoztiryaki.domain.model.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.result.AggregatedAnalyzeResult;
import org.mericoztiryaki.domain.model.result.InstrumentAnalyzeResult;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Report {

    private final AggregatedAnalyzeResult aggregatedResult;

    private final List<InstrumentAnalyzeResult> openPositions;

}
