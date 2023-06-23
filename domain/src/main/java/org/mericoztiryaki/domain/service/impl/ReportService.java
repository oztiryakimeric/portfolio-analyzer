package org.mericoztiryaki.domain.service.impl;

import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.model.Portfolio;
import org.mericoztiryaki.domain.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class ReportService implements IReportService {

    private final IPriceService priceService;
    private final IExchangeService exchangeService;
    private final ITransactionService transactionService;
    private final IWalletService walletService;
    private final IProfitAndLossService profitAndLossService;
    private final IReturnOfInvestmentService returnOfInvestmentService;

    public ReportService() {
        this.priceService = new PriceService();
        this.exchangeService = new ExchangeService(priceService);
        this.transactionService = new TransactionService(this.priceService, exchangeService);
        this.walletService = new WalletService();
        this.profitAndLossService = new ProfitAndLossService(this.priceService);
        this.returnOfInvestmentService = new ReturnOfInvestmentService(profitAndLossService);
    }

    @Override
    public void generateReport(ReportParameters reportParameters) {
        List<ITransaction> transactions = reportParameters.getTransactions()
                .stream().map(def -> transactionService.buildTransactionObject(def))
                .collect(Collectors.toList());

        SortedMap<LocalDate, Portfolio> portfolios = this.walletService.calculatePortfolios(transactions);

        Portfolio portfolio = portfolios.get(reportParameters.getReportDate());

        // This can be done in parallel I think
        for (Wallet wallet: portfolio.getWallets()) {
            Map<Period, List<ITransaction>> dividedTransactions = transactionService.createTransactionSetsByPeriods(
                    wallet.getAllTransactions(), reportParameters.getPeriods(), reportParameters.getReportDate());
            wallet.setPeriods(dividedTransactions);

            Map<Period, Quotes> pnlCalculation = dividedTransactions
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> profitAndLossService.calculatePNL(e.getValue(), reportParameters.getReportDate())
                    ));
            wallet.setPnlCalculation(pnlCalculation);

            Map<Period, Quotes> roiCalculation = dividedTransactions
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> returnOfInvestmentService.calculateROI(e.getValue(), reportParameters.getReportDate())
                    ));
            wallet.setRoiCalculation(roiCalculation);
        }

        return;
    }


}
