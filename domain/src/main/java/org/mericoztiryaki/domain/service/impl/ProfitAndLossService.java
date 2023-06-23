package org.mericoztiryaki.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.service.IProfitAndLossService;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ProfitAndLossService implements IProfitAndLossService {

    private final IPriceService priceService;

    @Override
    public Quotes calculatePNL(List<ITransaction> transactions, LocalDate portfolioDate) {
        if (transactions.isEmpty()) {
            return Quotes.ZERO;
        }

        Quotes totalOutcome = Quotes.ZERO;
        Quotes totalIncome = Quotes.ZERO;
        BigDecimal remainingAmount = BigDecimal.ZERO;

        for (ITransaction t: transactions) {
            if (t.getTransactionType() == TransactionType.BUY) {
                totalOutcome = QuotesUtil.add(totalOutcome, QuotesUtil.multiply(t.getPurchasePrice(), t.getAmount()));
                remainingAmount = remainingAmount.add(t.getAmount());
            } else {
                totalIncome = QuotesUtil.add(totalIncome, QuotesUtil.multiply(t.getPurchasePrice(), t.getAmount()));
                remainingAmount = remainingAmount.subtract(t.getAmount());
            }
        }

        Quotes currentHoldings = Quotes.ZERO;
        if (!remainingAmount.equals(BigDecimal.ZERO)) {
            Quotes currentPrice = priceService.getPrice(transactions.get(0).getInstrument(), portfolioDate);
            currentHoldings = QuotesUtil.multiply(currentPrice, remainingAmount);
        }

        return QuotesUtil.subtract(QuotesUtil.add(totalIncome, currentHoldings), totalOutcome);
    }

}
