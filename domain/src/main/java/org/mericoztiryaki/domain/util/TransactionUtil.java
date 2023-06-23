package org.mericoztiryaki.domain.util;

import lombok.Getter;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class TransactionUtil {

    private final List<ITransaction> transactions;
    private Quotes totalCost = Quotes.ZERO;
    private Quotes totalIncome = Quotes.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public TransactionUtil(List<ITransaction> transactions) {
        this.transactions = transactions;
        this.calculate();
    }

    public void calculate() {
        for (ITransaction t: transactions) {
            if (t.getTransactionType() == TransactionType.BUY) {
                totalCost = QuotesUtil.add(totalCost, QuotesUtil.multiply(t.getPurchasePrice(), t.getAmount()));
                totalAmount = totalAmount.add(t.getAmount());
            } else {
                totalIncome = QuotesUtil.add(totalIncome, QuotesUtil.multiply(t.getPurchasePrice(), t.getAmount()));
                totalAmount = totalAmount.subtract(t.getAmount());
            }
        }
    }

}
