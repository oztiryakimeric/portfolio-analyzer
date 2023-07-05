package org.mericoztiryaki.domain.service.impl;

import lombok.Getter;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.service.IAnalyzer;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class Analyzer implements IAnalyzer {

    private final IPriceService priceService;
    private final List<ITransaction> transactions;
    private final LocalDate portfolioDate;

    private Quotes totalCost = Quotes.ZERO;
    private Quotes totalIncome = Quotes.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public Analyzer(IPriceService priceService, List<ITransaction> transactions, LocalDate portfolioDate) {
        this.priceService = priceService;
        this.transactions = transactions;
        this.portfolioDate = portfolioDate;
        this.calculate();
    }

    public void calculate() {
        for (ITransaction t: transactions) {
            if (t.getTransactionType() == TransactionType.BUY) {
                totalCost = QuotesUtil.add(
                        totalCost,
                        QuotesUtil.multiply(t.getPurchasePrice(), t.getAmount())
                );
                totalAmount = totalAmount.add(t.getAmount());
            } else {
                totalIncome = QuotesUtil.add(
                        totalIncome,
                        QuotesUtil.multiply(t.getPurchasePrice(), t.getAmount())
                );
                totalAmount = totalAmount.subtract(t.getAmount());
            }
        }
    }

    @Override
    public Quotes calculateTotalValue() {
        if (totalAmount.equals(BigDecimal.ZERO)) {
            return Quotes.ZERO;
        }

        Quotes price = priceService.getPrice(transactions.get(0).getInstrument(), portfolioDate);
        return QuotesUtil.multiply(price, totalAmount);
    }

    @Override
    public Quotes calculatePNL() {
        return QuotesUtil.subtract(
                QuotesUtil.add(totalIncome, calculateTotalValue()),
                totalCost
        );
    }

    @Override
    public Quotes calculateROI() {
        return QuotesUtil.multiply(
                QuotesUtil.divide(calculatePNL(), totalCost),
                new BigDecimal(100)
        );
    }

    @Override
    public Quotes calculateUnitCost() {
        return QuotesUtil.divide(
                QuotesUtil.subtract(calculateTotalValue(), calculatePNL()),
                totalAmount
        );
    }
}
