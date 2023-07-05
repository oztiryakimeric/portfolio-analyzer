package org.mericoztiryaki.domain.service.impl;

import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.AggregatedAnalyzeResult;
import org.mericoztiryaki.domain.model.result.InstrumentAnalyzeResult;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.service.*;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService implements IReportService {

    private final IPriceService priceService;
    private final ITransactionService transactionService;

    public ReportService() {
        this.priceService = new PriceService();
        this.transactionService = new TransactionService(this.priceService);
    }

    @Override
    public Report generateReport(ReportParameters reportParameters) {
        List<ITransaction> transactions = reportParameters.getTransactions()
                .stream().map(def -> transactionService.buildTransactionObject(def))
                .filter(t -> reportParameters.getFilteredInstrumentTypes() == null
                        || reportParameters.getFilteredInstrumentTypes().contains(t.getInstrument().getInstrumentType()) )
                .sorted(Comparator.comparing(ITransaction::getDate))
                .collect(Collectors.toList());

        AggregatedAnalyzeResult aggregatedResult = createTotalsTable(transactions, reportParameters);
        List<InstrumentAnalyzeResult> openPositions = createOpenPositionsTable(transactions, reportParameters);

        return new Report(aggregatedResult, openPositions);
    }

    private List<InstrumentAnalyzeResult> createOpenPositionsTable(List<ITransaction> transactions, ReportParameters reportParameters) {
        return transactionService.getOpenPositions(transactions)
                .entrySet()
                .stream()
                .map(e -> {
                    InstrumentAnalyzeResult instrumentAnalyzeResult = new InstrumentAnalyzeResult(e.getKey(), e.getValue());
                    Analyzer walletAnalyzer = new Analyzer(priceService, instrumentAnalyzeResult.getTransactions(), reportParameters.getReportDate());

                    instrumentAnalyzeResult.setTotalAmount(walletAnalyzer.getTotalAmount());
                    instrumentAnalyzeResult.setTotalValue(walletAnalyzer.calculateTotalValue());
                    instrumentAnalyzeResult.setUnitCost(walletAnalyzer.calculateUnitCost());

                    if (!instrumentAnalyzeResult.getTotalAmount().equals(BigDecimal.ZERO)) {
                        instrumentAnalyzeResult.setPrice(priceService.getPrice(instrumentAnalyzeResult.getInstrument(), reportParameters.getReportDate()));
                    }

                    Map<Period, List<ITransaction>> dividedTransactions = transactionService.createTransactionSetsByPeriods(
                            instrumentAnalyzeResult.getTransactions(), reportParameters.getPeriods(), reportParameters.getReportDate());
                    for (Period period: reportParameters.getPeriods()) {
                        List<ITransaction> transactionsOfPeriod = dividedTransactions.get(period);
                        if (transactionsOfPeriod != null) {
                            Analyzer periodAnalyzer = new Analyzer(priceService, transactionsOfPeriod,
                                    reportParameters.getReportDate());

                            instrumentAnalyzeResult.getPnlCalculation().put(period, periodAnalyzer.calculatePNL());
                            instrumentAnalyzeResult.getRoiCalculation().put(period, periodAnalyzer.calculateROI());
                        }
                    }
                    return instrumentAnalyzeResult;
                })
                .collect(Collectors.toList());
    }

    private AggregatedAnalyzeResult createTotalsTable(List<ITransaction> transactions, ReportParameters reportParameters) {
        AggregatedAnalyzeResult rootResult = new AggregatedAnalyzeResult("Total");

        // Group by instrument
        Map<Instrument, List<ITransaction>> groupedTransactions = transactions
                .stream()
                .collect(Collectors.groupingBy(ITransaction::getInstrument));

        for (Instrument instrument: groupedTransactions.keySet()) {
            AggregatedAnalyzeResult instrumentTypeResult = rootResult.getChildren().computeIfAbsent(
                    String.valueOf(instrument.getInstrumentType()),
                    (t) -> new AggregatedAnalyzeResult(String.valueOf(t))
            );

            AggregatedAnalyzeResult symbolResult = instrumentTypeResult.getChildren().computeIfAbsent(
                    instrument.getSymbol(),
                    (s) -> new AggregatedAnalyzeResult(s)
            );

            Map<Period, List<ITransaction>> dividedTransactions = transactionService.createTransactionSetsByPeriods(
                    groupedTransactions.get(instrument), reportParameters.getPeriods(), reportParameters.getReportDate());

            for (Period period: reportParameters.getPeriods()) {
                List<ITransaction> transactionsOfPeriod = dividedTransactions.get(period);
                if (!transactionsOfPeriod.isEmpty()) {
                    Analyzer periodAnalyzer = new Analyzer(priceService, transactionsOfPeriod,
                            reportParameters.getReportDate());

                    appendAnalyzeResult(rootResult, periodAnalyzer, period);
                    appendAnalyzeResult(instrumentTypeResult, periodAnalyzer, period);
                    appendAnalyzeResult(symbolResult, periodAnalyzer, period);
                }
            }

        }

        return rootResult;
    }

    private void appendAnalyzeResult(AggregatedAnalyzeResult targetResult, Analyzer analyzer, Period period) {
        Quotes prevPnl = targetResult.getPnlCalculation().computeIfAbsent(period, (p) -> Quotes.ZERO);
        targetResult.getPnlCalculation().put(period, QuotesUtil.add(prevPnl, analyzer.calculatePNL()));

        if (period == Period.ALL && !analyzer.getTotalAmount().equals(BigDecimal.ZERO)) {
            // If open position
            targetResult.setTotalValue(QuotesUtil.add(targetResult.getTotalValue(), analyzer.calculateTotalValue()));
        }
    }
}
