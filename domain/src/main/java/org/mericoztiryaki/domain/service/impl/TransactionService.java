package org.mericoztiryaki.domain.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mericoztiryaki.domain.exception.InvalidTransactionDefinitionException;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.model.transaction.Transaction;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;
import org.mericoztiryaki.domain.model.transaction.UnifiedTransaction;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.service.ITransactionService;
import org.mericoztiryaki.domain.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final IPriceService priceService;

    @Override
    public ITransaction buildTransactionObject(TransactionDefinition definition) throws InvalidTransactionDefinitionException, PriceApiException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            LocalDateTime transactionTime = LocalDateTime.parse(definition.getDate(), formatter);
            Currency transactionCurrency = Currency.valueOf(definition.getCurrency());

            return new Transaction(
                    transactionTime,
                    new Instrument(InstrumentType.valueOf(definition.getInstrumentType()), definition.getSymbol()),
                    TransactionType.valueOf(definition.getTransactionType()),
                    new BigDecimal(definition.getAmount().replace(",", "")),
                    priceService.calculateExchangeRates(transactionTime.toLocalDate(),
                            new BigDecimal(definition.getPurchasePrice().replace(",", "")),
                            transactionCurrency
                    ),
                    priceService.calculateExchangeRates(transactionTime.toLocalDate(),
                            new BigDecimal(definition.getCommissionPrice().replace(",", "")),
                            transactionCurrency
                    ),
                    transactionCurrency);
        } catch (Exception e) {
            log.error("Transaction row is invalid. Index: {}", definition.getIndex(), e);
            throw new InvalidTransactionDefinitionException(e, definition.getIndex());
        }
    }

    @Override
    public Map<Period, List<ITransaction>> createTransactionSetsByPeriods(List<ITransaction> transactions,
                                                                          Set<Period> periods, LocalDate portfolioDate) {
        Map<Period, List<ITransaction>> transactionSets = new HashMap<>();

        periods.forEach(period -> {
            LocalDate periodStart = period != Period.ALL ? portfolioDate.minusDays(period.getDayCount()) :
                    transactions.get(0).getDate().toLocalDate().minusDays(1);

            transactionSets.put(period, createTransactionSetByWindow(transactions, periodStart, portfolioDate));
        });

        return transactionSets;
    }

    @Override
    public List<ITransaction> createTransactionSetByWindow(List<ITransaction> transactions, LocalDate start, LocalDate end) throws PriceApiException {
        Map<Instrument, TransactionSet> transactionSets = new HashMap<>();

        for (ITransaction t: transactions) {
            TransactionSet transactionSet = transactionSets.computeIfAbsent(
                    t.getInstrument(),
                    (i) -> new TransactionSet(start.atStartOfDay(), i)
            );

            if (t.getDate().toLocalDate().isBefore(start) || t.getDate().toLocalDate().isEqual(start)) {
               transactionSet.getUnifiedTransaction().addTransaction(t);
            } else if (t.getDate().toLocalDate().isBefore(end) || t.getDate().toLocalDate().isEqual(end)) {
                transactionSet.getTransactionHappenedInPeriod().add(t);
            }
        }

        return transactionSets.entrySet()
                .stream()
                // Filter unnecessary sets
                .filter(e ->
                        !(BigDecimalUtil.isZero(e.getValue().getUnifiedTransaction().getAmount()) &&
                        e.getValue().getTransactionHappenedInPeriod().size() == 0)
                )
                .map(e -> {
                    if (!BigDecimalUtil.isZero(e.getValue().getUnifiedTransaction().getAmount())) {
                        e.getValue().getUnifiedTransaction().setPurchasePrice(
                                priceService.getPrice(e.getValue().getUnifiedTransaction().getInstrument(), start));
                    }
                    return e.getValue();
                })
                .flatMap(e -> e.getTransactions().stream())
                .sorted(Comparator.comparing(ITransaction::getDate))
                .collect(Collectors.toList());
    }

    @Getter
    private static class TransactionSet {
        private final UnifiedTransaction unifiedTransaction;
        private final List<ITransaction> transactionHappenedInPeriod;

        public TransactionSet(LocalDateTime date, Instrument instrument) {
            this.unifiedTransaction = new UnifiedTransaction(date, instrument);
            this.transactionHappenedInPeriod = new ArrayList<>();
        }

        public List<ITransaction> getTransactions() {
            List<ITransaction> result = new ArrayList<>();
            if (!BigDecimalUtil.isZero(this.unifiedTransaction.getAmount())) {
                result.add(this.unifiedTransaction);
            }
            result.addAll(this.transactionHappenedInPeriod);
            return result;
        }
    }

    @Override
    public Map<Instrument, List<ITransaction>> getOpenPositions(List<ITransaction> transactions) {
        Map<Instrument, InstrumentBucket> buckets = new HashMap<>();

        for (ITransaction transaction: transactions) {
            InstrumentBucket bucket =
                    buckets.computeIfAbsent(transaction.getInstrument(), (key) -> new InstrumentBucket());

            bucket.addTransaction(transaction);

            if (BigDecimalUtil.isZero(bucket.getCumulativeAmount())) {
                buckets.put(transaction.getInstrument(), new InstrumentBucket());
            }
        }

        return buckets.entrySet()
                .stream()
                .filter(e -> !BigDecimalUtil.isZero(e.getValue().getCumulativeAmount()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getTransactions()));
    }

    @Getter
    private static class InstrumentBucket {
        private BigDecimal cumulativeAmount = BigDecimal.ZERO;
        private List<ITransaction> transactions = new ArrayList<>();

        public void addTransaction(ITransaction transaction) {
            this.transactions.add(transaction);
            this.cumulativeAmount = transaction.getTransactionType() == TransactionType.BUY ?
                    this.cumulativeAmount.add(transaction.getAmount()) :
                    this.cumulativeAmount.subtract(transaction.getAmount());
        }
    }

}
