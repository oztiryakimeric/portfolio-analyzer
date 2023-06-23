package org.mericoztiryaki.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.model.transaction.Transaction;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;
import org.mericoztiryaki.domain.model.transaction.UnifiedTransaction;
import org.mericoztiryaki.domain.service.IExchangeService;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.service.ITransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final IPriceService priceService;
    private final IExchangeService exchangeService;

    @Override
    public ITransaction buildTransactionObject(TransactionDefinition definition) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        LocalDateTime transactionTime = LocalDateTime.parse(definition.getDate(), formatter);
        Currency transactionCurrency = Currency.valueOf(definition.getCurrency());

        return new Transaction(
                transactionTime,
                new Instrument(InstrumentType.valueOf(definition.getInstrumentType()), definition.getSymbol()),
                TransactionType.valueOf(definition.getTransactionType()),
                new BigDecimal(definition.getAmount()),
                exchangeService.exchange(transactionTime.toLocalDate(),
                        new BigDecimal(definition.getPurchasePrice()), transactionCurrency),
                exchangeService.exchange(transactionTime.toLocalDate(),
                        new BigDecimal(definition.getCommissionPrice()), transactionCurrency),
                transactionCurrency);
    }

    @Override
    public Map<Period, List<ITransaction>> createTransactionSetsByPeriods(List<ITransaction> transactions, Set<Period> periods,
                                                                          LocalDate portfolioDate) {
        List<ITransaction> sortedTransactions = transactions
                .stream()
                .sorted(Comparator.comparing(ITransaction::getDate))
                .collect(Collectors.toList());

        Map<Period, List<ITransaction>> transactionSets = new HashMap<>();
        periods.forEach(period -> transactionSets.put(period, createTransactionSet(sortedTransactions, period, portfolioDate)));

        return transactionSets;
    }

    private List<ITransaction> createTransactionSet(List<ITransaction> transactions, Period period, LocalDate portfolioDate) {
        if (transactions.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDate periodClose = portfolioDate;
        LocalDate periodStart = getPeriodStart(periodClose, period, transactions.get(0));
        Instrument instrument = transactions.get(0).getInstrument();

        UnifiedTransaction unifiedTransaction = new UnifiedTransaction(periodStart.atStartOfDay(), instrument,
                priceService.getPrice(instrument, periodStart));
        List<ITransaction> transactionHappenedInPeriod = new ArrayList<>();

        for (ITransaction t: transactions) {

            if (t.getDate().toLocalDate().isBefore(periodStart) || t.getDate().toLocalDate().isEqual(periodStart)) {
                // Transactions happened before period
                unifiedTransaction.addTransaction(t);
            } else if (t.getDate().toLocalDate().isBefore(periodClose) || t.getDate().toLocalDate().isEqual(periodClose)) {
                transactionHappenedInPeriod.add(t);
            }
        }

        transactionHappenedInPeriod.add(0, unifiedTransaction);
        return transactionHappenedInPeriod;
    }

    private LocalDate getPeriodStart(LocalDate periodClose, Period period, ITransaction firstTransaction) {
        if (period != Period.ALL) {
            return periodClose.minusDays(period.getDayCount());
        }
        return firstTransaction.getDate().toLocalDate().minusDays(1);
    }
}
