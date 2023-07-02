package org.mericoztiryaki.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class AggregatedAnalyzeResult {

    private final List<ITransaction> transactions;

    private final Map<Period, Quotes> pnlCalculation = new HashMap<>();

    private Quotes totalValue = Quotes.ZERO;

}
