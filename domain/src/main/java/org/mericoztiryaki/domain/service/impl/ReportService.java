package org.mericoztiryaki.domain.service.impl;

import lombok.Getter;
import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.TransactionType;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.service.*;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService implements IReportService {

    private final IPriceService priceService;
    private final IExchangeService exchangeService;
    private final ITransactionService transactionService;

    public ReportService() {
        this.priceService = new PriceService();
        this.exchangeService = new ExchangeService(priceService);
        this.transactionService = new TransactionService(this.priceService, exchangeService);
    }

    @Override
    public Report generateReport(ReportParameters reportParameters) {
        List<ITransaction> transactions = reportParameters.getTransactions()
                .stream().map(def -> transactionService.buildTransactionObject(def))
                .sorted(Comparator.comparing(ITransaction::getDate))
                .collect(Collectors.toList());

        List<Wallet> openPositions = transactionService.getOpenPositions(transactions)
                .entrySet()
                .stream()
                .map(e -> {
                    Wallet wallet = new Wallet(e.getKey(), e.getValue());
                    ReportCalculator walletCalculator = new ReportCalculator(wallet.getTransactions(), reportParameters.getReportDate());

                    wallet.setTotalAmount(walletCalculator.getTotalAmount());
                    wallet.setTotalValue(walletCalculator.calculateTotalValue());
                    wallet.setUnitCost(walletCalculator.calculateUnitCost());

                    if (!wallet.getTotalAmount().equals(BigDecimal.ZERO)) {
                        wallet.setPrice(priceService.getPrice(wallet.getInstrument(), reportParameters.getReportDate()));
                    }

                    Map<Period, List<ITransaction>> dividedTransactions = transactionService.createTransactionSetsByPeriods(
                            wallet.getTransactions(), reportParameters.getPeriods(), reportParameters.getReportDate());
                    for (Period period: reportParameters.getPeriods()) {
                        List<ITransaction> transactionsOfPeriod = dividedTransactions.get(period);
                        if (transactionsOfPeriod != null) {
                            ReportCalculator calculator = new ReportCalculator(transactionsOfPeriod,
                                    reportParameters.getReportDate());

                            wallet.getPnlCalculation().put(period, calculator.calculatePNL());
                            wallet.getRoiCalculation().put(period, calculator.calculateROI());
                        }
                    }
                    return wallet;
                })
                .collect(Collectors.toList());

        return new Report(openPositions);
    }

    @Getter
    public class ReportCalculator {
        private final List<ITransaction> transactions;
        private final LocalDate portfolioDate;

        private Quotes totalCost = Quotes.ZERO;
        private Quotes totalIncome = Quotes.ZERO;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        public ReportCalculator(List<ITransaction> transactions, LocalDate portfolioDate) {
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

        public Quotes calculateTotalValue() {
            if (totalAmount.equals(BigDecimal.ZERO)) {
                return Quotes.ZERO;
            }

            Quotes price = priceService.getPrice(transactions.get(0).getInstrument(), portfolioDate);
            return QuotesUtil.multiply(price, totalAmount);
        }

        public Quotes calculatePNL() {
            return QuotesUtil.subtract(
                    QuotesUtil.add(totalIncome, calculateTotalValue()),
                    totalCost
            );
        }

        public Quotes calculateROI() {
            return QuotesUtil.multiply(
                    QuotesUtil.divide(calculatePNL(), totalCost),
                    new BigDecimal(100)
            );
        }

        public Quotes calculateUnitCost() {
            return QuotesUtil.divide(
                    QuotesUtil.subtract(calculateTotalValue(), calculatePNL()),
                    totalAmount
            );
        }
    }


}
