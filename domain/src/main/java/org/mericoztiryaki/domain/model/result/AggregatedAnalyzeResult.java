package org.mericoztiryaki.domain.model.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Period;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class AggregatedAnalyzeResult {

    private final String id;

    private final Map<String, AggregatedAnalyzeResult> children = new HashMap<>();

    private final Map<Period, Quotes> pnlCalculation = new HashMap<>();

    private Quotes totalValue = Quotes.ZERO;

}
