package org.mericoztiryaki.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Wallet {

    private String id;

    private final LocalDate date;

    private final Instrument instrument;

    private final BigDecimal amount;

    private final List<ITransaction> transactions;

    private final List<ITransaction> inDayTransactions;

    private Map<Period, List<ITransaction>> periods;

    private Map<Period, Quotes> pnlCalculation;

    private Map<Period, Quotes> roiCalculation;

    public Wallet(String id, LocalDate date, Instrument instrument, BigDecimal amount, List<ITransaction> transactions,
                  List<ITransaction> inDayTransactions) {
        this(date, instrument, amount, transactions, inDayTransactions);
        this.id = id;
    }

    public BigDecimal getAmountWithInDayTransactions() {
        BigDecimal inDayTransactionsTotalAmount = inDayTransactions.stream()
                .map(t -> t.getAmount().multiply(t.getTransactionType() == TransactionType.BUY ?
                        BigDecimal.ONE : BigDecimal.ONE.negate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return this.amount.add(inDayTransactionsTotalAmount);
    }

    public List<ITransaction> getAllTransactions() {
        List<ITransaction> unifiedTransactions = new ArrayList<>(transactions);
        unifiedTransactions.addAll(inDayTransactions);
        return unifiedTransactions;
    }

    public Wallet nextDaysWallet() {
        List<ITransaction> nextDaysTransactions = new ArrayList<>(transactions);
        nextDaysTransactions.addAll(inDayTransactions);
        return new Wallet(id, date.plusDays(1), instrument, amount, nextDaysTransactions, new ArrayList<>());
    }
}