package org.mericoztiryaki.domain.model.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class InstrumentAnalyzeResult {

    private final Instrument instrument;

    private final List<ITransaction> transactions;

    private BigDecimal totalAmount;

    private Quotes price;

    private Quotes unitCost;

    private Quotes totalValue;

    private Map<Period, Quotes> pnlCalculation = new HashMap<>();

    private Map<Period, Quotes> roiCalculation = new HashMap<>();

}