package org.mericoztiryaki.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.service.IProfitAndLossService;
import org.mericoztiryaki.domain.service.IReturnOfInvestmentService;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ReturnOfInvestmentService implements IReturnOfInvestmentService {

    private final IProfitAndLossService profitAndLossService;

    @Override
    public Quotes calculateROI(List<ITransaction> transactions, LocalDate portfolioDate) {
        Quotes pnl = profitAndLossService.calculatePNL(transactions, portfolioDate);

        Quotes totalCost = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.BUY)
                .map(t -> QuotesUtil.multiply(t.getPurchasePrice(), t.getAmount()))
                .reduce(Quotes.ZERO, (acc, cost) -> QuotesUtil.add(acc, cost));

        return QuotesUtil.multiply(QuotesUtil.divide(pnl, totalCost), new BigDecimal(100));
    }

}
