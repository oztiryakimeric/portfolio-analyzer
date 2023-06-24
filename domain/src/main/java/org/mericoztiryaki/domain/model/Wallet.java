package org.mericoztiryaki.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Wallet {

    private final Instrument instrument;

    private final List<ITransaction> transactions;

    private BigDecimal totalAmount;

    private Quotes price;

    // Calculations
    private Quotes totalValue;

    private Map<Period, Quotes> pnlCalculation = new HashMap<>();

    private Map<Period, Quotes> roiCalculation = new HashMap<>();

}