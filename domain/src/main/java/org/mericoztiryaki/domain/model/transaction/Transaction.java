package org.mericoztiryaki.domain.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements ITransaction {

    private LocalDateTime date;

    private Instrument instrument;

    private TransactionType transactionType;

    private BigDecimal amount;

    private Quotes purchasePrice;

    private Quotes commissionPrice;

    private Currency currency;

}
