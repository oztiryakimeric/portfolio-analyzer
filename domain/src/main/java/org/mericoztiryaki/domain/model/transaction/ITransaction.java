package org.mericoztiryaki.domain.model.transaction;

import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.constant.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ITransaction {

    LocalDateTime getDate();

    Instrument getInstrument();

    TransactionType getTransactionType();

    BigDecimal getAmount();

    Quotes getPurchasePrice();

    Quotes getCommissionPrice();

}
