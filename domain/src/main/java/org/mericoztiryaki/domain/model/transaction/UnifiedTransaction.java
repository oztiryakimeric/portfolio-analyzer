package org.mericoztiryaki.domain.model.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UnifiedTransaction implements ITransaction {

    private final LocalDateTime date;

    private final Instrument instrument;

    @Setter
    private Quotes purchasePrice;

    private BigDecimal amount = BigDecimal.ZERO;

    private Quotes commissionPrice = Quotes.ZERO;

    private List<ITransaction> unifiedTransactions = new ArrayList<>();

    public void addTransaction(ITransaction t) {
        unifiedTransactions.add(t);
        this.commissionPrice = QuotesUtil.add(this.commissionPrice, t.getCommissionPrice());

        if (t.getTransactionType() == TransactionType.BUY) {
            this.amount = this.amount.add(t.getAmount());
        } else {
            this.amount =  this.amount.subtract(t.getAmount());
        }
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BUY;
    }

}
