package org.mericoztiryaki.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.PnlHistoryUnit;
import org.mericoztiryaki.domain.model.result.AggregatedAnalyzeResult;
import org.mericoztiryaki.domain.model.result.InstrumentAnalyzeResult;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.service.IPriceService;
import org.mericoztiryaki.domain.service.IReportService;
import org.mericoztiryaki.domain.service.ITransactionService;
import org.mericoztiryaki.domain.util.BigDecimalUtil;
import org.mericoztiryaki.domain.util.QuotesUtil;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final IPriceService priceService;
    private final ITransactionService transactionService;

    @Override
    public Report generateReport(ReportParameters reportParameters) {
        List<ITransaction> transactions = reportParameters.getTransactions()
                .stream().map(def -> transactionService.buildTransactionObject(def))
                .filter(t -> reportParameters.getFilteredInstrumentTypes() == null
                        || reportParameters.getFilteredInstrumentTypes().contains(t.getInstrument().getInstrumentType()) )
                .sorted(Comparator.comparing(ITransaction::getDate))
                .collect(Collectors.toList());

        AggregatedAnalyzeResult aggregatedResult = createAggregatedResult(transactions, reportParameters);
        List<InstrumentAnalyzeResult> openPositions = createOpenPositionsTable(transactions, reportParameters);

        Map<PnlHistoryUnit, Map<String, Quotes>> pnlHistory = reportParameters.getPnlHistoryUnits()
                .stream()
                .collect(
                        Collectors.toMap(
                                (unit) -> unit,
                                (unit) -> createPnlHistory(transactions, reportParameters, unit, unit.getSize())
                        )
                );

        return new Report(transactions, aggregatedResult, openPositions, pnlHistory);
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

                    if (!BigDecimalUtil.isZero(instrumentAnalyzeResult.getTotalAmount())) {
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

    private AggregatedAnalyzeResult createAggregatedResult(List<ITransaction> transactions, ReportParameters reportParameters) {
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
        Optional<Quotes> prevPnl = targetResult.getPnlCalculation().computeIfAbsent(period, (p) -> Optional.of(Quotes.ZERO));
        targetResult.getPnlCalculation().put(period, Optional.of(QuotesUtil.add(prevPnl.get(), analyzer.calculatePNL())));

        if (period == Period.ALL && !BigDecimalUtil.isZero(analyzer.getTotalAmount())) {
            // If open position
            targetResult.setTotalValue(QuotesUtil.add(targetResult.getTotalValue(), analyzer.calculateTotalValue()));
        }

        if (!QuotesUtil.isZero(targetResult.getTotalValue())) {
            targetResult.getRoiCalculation().put(period, Optional.of(
                    QuotesUtil.divide(
                            targetResult.getPnlCalculation().get(period).orElse(Quotes.ZERO),
                            QuotesUtil.subtract(targetResult.getTotalValue(), targetResult.getPnlCalculation().get(period).orElse(Quotes.ZERO))
                    )
            ));
        }
    }

    private Map<String, Quotes> createPnlHistory(List<ITransaction> transactions, ReportParameters reportParameters, PnlHistoryUnit unit, int count) {
        // Group by instrument
        Map<Instrument, List<ITransaction>> groupedTransactions = transactions
                .stream()
                .collect(Collectors.groupingBy(ITransaction::getInstrument));

        Map<String, Quotes> pnlSums = new LinkedHashMap<>();

        for (Instrument instrument: groupedTransactions.keySet()) {
            List<Pair<LocalDate, LocalDate>> priceWindows = createPriceWindows(unit, count);

            priceWindows.forEach(window -> {
                String windowId = MessageFormat.format("{0} -> {1}", window.getLeft(), window.getRight());
                List<ITransaction> transactionWindow = transactionService.createTransactionSetByWindow(
                        groupedTransactions.get(instrument), window.getLeft(), window.getRight());

                Analyzer periodAnalyzer = new Analyzer(priceService, transactionWindow, window.getRight());
                Quotes prevSum = pnlSums.computeIfAbsent(windowId, (p) -> Quotes.ZERO);

                pnlSums.put(windowId, QuotesUtil.add(prevSum, periodAnalyzer.calculatePNL()));
            });
        }

        return pnlSums;
    }

    private List<Pair<LocalDate, LocalDate>> createPriceWindows(PnlHistoryUnit unit, int count) {
        List<Pair<LocalDate, LocalDate>> windows = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            LocalDate start = null;
            LocalDate end = null;
            if (unit == PnlHistoryUnit.DAY) {
                start = LocalDate.now().minusDays(i + 1);
                end = LocalDate.now().minusDays(i);
            } else if (unit == PnlHistoryUnit.WEEK) {
                start = LocalDate.now().minusWeeks(i).with(DayOfWeek.MONDAY).minusDays(1);
                end = LocalDate.now().minusWeeks(i).with(DayOfWeek.SUNDAY);
            } else if (unit == PnlHistoryUnit.MONTH) {
                start = LocalDate.now().minusMonths(i).with(TemporalAdjusters.firstDayOfMonth()).minusDays(1);
                end = LocalDate.now().minusMonths(i).with(TemporalAdjusters.lastDayOfMonth());
            } else if (unit == PnlHistoryUnit.YEAR) {
                start = LocalDate.now().minusYears(i).with(TemporalAdjusters.firstDayOfYear()).minusDays(1);
                end = LocalDate.now().minusYears(i).with(TemporalAdjusters.lastDayOfYear());
            } else {
                throw new RuntimeException("Unit not implemented: " + unit);
            }

            if (end.isAfter(LocalDate.now())) {
                end = LocalDate.now();
            }

            windows.add(Pair.of(start, end));
        }

        return windows;
    }

}
