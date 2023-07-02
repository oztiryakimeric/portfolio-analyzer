package org.mericoztiryaki.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Report {

    private final AggregatedAnalyzeResult aggregatedResult;

    private final List<InstrumentAnalyzeResult> openPositions;

}
