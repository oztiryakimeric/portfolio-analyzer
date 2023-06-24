package org.mericoztiryaki.domain.service.impl;

import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.model.Portfolio;
import org.mericoztiryaki.domain.model.transaction.UnifiedTransaction;
import org.mericoztiryaki.domain.service.*;
import org.mericoztiryaki.domain.util.QuotesUtil;
import org.mericoztiryaki.domain.util.TransactionUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class ReportService implements IReportService {

    private final IPriceService priceService;
    private final IExchangeService exchangeService;
    private final ITransactionService transactionService;
    private final IWalletService walletService;

    public ReportService() {
        this.priceService = new PriceService();
        this.exchangeService = new ExchangeService(priceService);
        this.transactionService = new TransactionService(this.priceService, exchangeService);
        this.walletService = new WalletService();
    }

    @Override
    public Portfolio generateReport(ReportParameters reportParameters) {
        List<ITransaction> transactions = reportParameters.getTransactions()
                .stream().map(def -> transactionService.buildTransactionObject(def))
                .sorted(Comparator.comparing(ITransaction::getDate))
                .collect(Collectors.toList());

        SortedMap<LocalDate, Portfolio> portfolios = this.walletService.calculatePortfolios(transactions);

        Portfolio portfolio = portfolios.get(reportParameters.getReportDate());

        for (Wallet wallet: portfolio.getWallets()) {
            ReportCalculator walletCalculator = new ReportCalculator(wallet.getTransactions(), reportParameters.getReportDate());

            wallet.setTotalAmount(walletCalculator.transactionUtil.getTotalAmount());
            wallet.setTotalValue(walletCalculator.calculateTotalValue());

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
        }

        return portfolio;
    }

    public class ReportCalculator {
        private final List<ITransaction> transactions;
        private final LocalDate portfolioDate;
        private final TransactionUtil transactionUtil;

        public ReportCalculator(List<ITransaction> transactions, LocalDate portfolioDate) {
            this.transactions = transactions;
            this.portfolioDate = portfolioDate;
            this.transactionUtil = new TransactionUtil(transactions);
        }

        public Quotes calculateTotalValue() {
            if (transactionUtil.getTotalAmount().equals(BigDecimal.ZERO)) {
                return Quotes.ZERO;
            }

            Quotes price = priceService.getPrice(transactions.get(0).getInstrument(), portfolioDate);
            return QuotesUtil.multiply(price, transactionUtil.getTotalAmount());
        }

        public Quotes calculatePNL() {
            if (!applicable()) {
                return null;
            }
            return QuotesUtil.subtract(
                    QuotesUtil.add(transactionUtil.getTotalIncome(), calculateTotalValue()),
                    transactionUtil.getTotalCost()
            );
        }

        public Quotes calculateROI() {
            if (!applicable()) {
                return null;
            }
            return QuotesUtil.multiply(
                    QuotesUtil.divide(calculatePNL(), transactionUtil.getTotalCost()),
                    new BigDecimal(100)
            );
        }

        private boolean applicable() {
            boolean isClosedTrade = !(this.transactions.size() == 1
                    && this.transactions.get(0) instanceof UnifiedTransaction
                    && this.transactionUtil.getTotalAmount().equals(BigDecimal.ZERO));

            return isClosedTrade;
        }
    }


}
