package org.mericoztiryaki.domain.service.impl;

import lombok.Getter;
import lombok.Setter;
import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.model.Portfolio;
import org.mericoztiryaki.domain.service.IWalletService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class WalletService implements IWalletService {

    @Override
    public SortedMap<LocalDate, Portfolio> calculatePortfolios(List<ITransaction> transactions) {
        List<ITransaction> sortedTransactions = transactions
                .stream()
                .sorted(Comparator.comparing(ITransaction::getDate))
                .collect(Collectors.toList());

        SortedMap<LocalDate, Map<Instrument, InstrumentBucket>> days = new TreeMap<>();

        for (ITransaction transaction: sortedTransactions) {
            // Create empty portfolio if date not exists
            Map<Instrument, InstrumentBucket> portfolioAtDay = days.
                    computeIfAbsent(transaction.getDate().toLocalDate(), (key) -> new HashMap<>());

            // Create empty buckets if not exists
            InstrumentBucket bucket = portfolioAtDay
                    .computeIfAbsent(transaction.getInstrument(), (key) -> new InstrumentBucket());

            // Add in day transactions
            bucket.addInDayTransaction(transaction);
        }

        if (days.isEmpty()) {
            return new TreeMap<>();
        }

        // Apply in day transactions to next day
        LocalDate day = days.firstKey();
        while (day.isBefore(LocalDate.now())) {
            Map<Instrument, InstrumentBucket> portfolioToday = days.get(day);
            Map<Instrument, InstrumentBucket> portfolioTomorrow =
                    days.computeIfAbsent(day.plusDays(1), (k) -> new HashMap<>());

            for (Instrument instrument: portfolioToday.keySet()) {
                InstrumentBucket instrumentToday = portfolioToday.get(instrument);
                InstrumentBucket instrumentTomorrow =
                        portfolioTomorrow.computeIfAbsent(instrument, (k) -> new InstrumentBucket());

                instrumentTomorrow.setCumulativeAmount(instrumentToday.calculateNextDayCumulativeAmount());
                instrumentTomorrow.setTransactions(instrumentToday.getNextDayTransactions());
            }

            day = day.plusDays(1);
        }

        // Map to portfolio object
        SortedMap<LocalDate, Portfolio> portfolios = new TreeMap<>();
        for (LocalDate d: days.keySet()) {
            Map<Instrument, InstrumentBucket> walletMap = days.get(d);
            Portfolio portfolio = new Portfolio(d);
            List<Wallet> wallets = walletMap
                    .keySet()
                    .stream()
                    .map(instrument -> {
                        InstrumentBucket bucket = walletMap.get(instrument);
                        return new Wallet(d, instrument, bucket.getCumulativeAmount(), bucket.getTransactions());
                    })
                    .collect(Collectors.toList());
            portfolio.setWallets(wallets);
            portfolios.put(d, portfolio);
        }

        return portfolios;
    }

    @Setter
    @Getter
    private static class InstrumentBucket {
        private BigDecimal cumulativeAmount = BigDecimal.ZERO;
        private List<ITransaction> transactions = new ArrayList<>();
        private List<ITransaction> inDayTransactions = new ArrayList<>();

        public void addInDayTransaction(ITransaction transaction) {
            this.inDayTransactions.add(transaction);
        }

        public BigDecimal calculateNextDayCumulativeAmount() {
            BigDecimal inDayTransactionsCumulativeAmount = inDayTransactions.stream()
                    .map(trn -> {
                        BigDecimal sign = trn.getTransactionType() == TransactionType.BUY ? BigDecimal.ONE : BigDecimal.ONE.negate();
                        return trn.getAmount().multiply(sign);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return cumulativeAmount.add(inDayTransactionsCumulativeAmount);
        }

        public List<ITransaction> getNextDayTransactions() {
            List<ITransaction> transactions = new ArrayList<>(this.transactions);
            transactions.addAll(inDayTransactions);
            return transactions;
        }
    }
}