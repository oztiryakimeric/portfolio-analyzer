package org.mericoztiryaki.app;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWordMin;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;
import org.mericoztiryaki.domain.service.impl.ReportService;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) throws IOException, PriceApiException {
        List<TransactionDefinition> transactions = readTransactions();

        ReportParameters parameters = ReportParameters.builder()
                .transactions(transactions)
                .reportDate(LocalDate.now())
                .periods(Set.of(Period.D1, Period.W1, Period.M1, Period.ALL))
                .currency(Currency.TRY)
                .build();

        Report report = new ReportService().generateReport(parameters);
        System.out.println();
        System.out.println();

        System.out.println(renderAggregatedResults(report, parameters));

        System.out.println();
        System.out.println();

        System.out.println(renderOpenPositions(report, parameters));

        System.out.println();
        System.out.println();

        System.out.println(renderPnlHistory(report.getDailyPnlHistory(), parameters, "Daily Pnl History"));

        System.out.println();
        System.out.println();

        System.out.println();
        System.out.println();
        System.out.println("EDOM");
    }

    private static List<TransactionDefinition> readTransactions() throws IOException {
        List<List<String>> rawCsvFile = Util.readCsvFile(".dev-space/dev-portfolio-3.csv");

        List<TransactionDefinition> defs = new ArrayList<>();
        for(int i=0; i<rawCsvFile.size(); i++) {
            List<String> row = rawCsvFile.get(i);
            defs.add(new TransactionDefinition(i, row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                    row.get(5), row.get(6), row.get(7)));
        }
        return defs;
    }

    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private static final DecimalFormat rateFormat = new DecimalFormat("#0.00");

    private static String renderAggregatedResults(Report report, ReportParameters reportParameters) {
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(20));

        at.addRule();
        AT_Row tableHeader = at.addRow(null, null, null, null, "Aggregated Results");
        tableHeader.getCells().get(4).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        at.addRow("", "ALL", String.valueOf(InstrumentType.BIST), String.valueOf(InstrumentType.CURRENCY), String.valueOf(InstrumentType.FUND));

        at.addRule();
        at.addRow(
                cellWithCurrency("Value", reportParameters.getCurrency()),
                currencyFormat.format(report.getAggregatedResult().getTotalValue().getValue().get(reportParameters.getCurrency())),
                currencyFormat.format(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.BIST)).getTotalValue().getValue().get(reportParameters.getCurrency())),
                currencyFormat.format(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.CURRENCY)).getTotalValue().getValue().get(reportParameters.getCurrency())),
                currencyFormat.format(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.FUND)).getTotalValue().getValue().get(reportParameters.getCurrency()))
        );

        List<Period> sortedPeriods = reportParameters.getPeriods().stream()
                .sorted(Comparator.comparing(Period::getDayCount).reversed())
                .collect(Collectors.toList());

        for (Period period: sortedPeriods) {
            at.addRule();
            at.addRow(
                    cellWithCurrency("Pnl " + period, reportParameters.getCurrency()),
                    currencyFormat.format(report.getAggregatedResult().getPnlCalculation().get(period).getValue().get(reportParameters.getCurrency())),
                    getSafePnl(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.BIST)).getPnlCalculation(), period)
                            .map(pnl -> pnl.getValue().get(reportParameters.getCurrency()))
                            .map(currency -> currencyFormat.format(currency))
                            .orElse("-"),
                    getSafePnl(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.CURRENCY)).getPnlCalculation(), period)
                            .map(pnl -> pnl.getValue().get(reportParameters.getCurrency()))
                            .map(currency -> currencyFormat.format(currency))
                            .orElse("-"),
                    getSafePnl(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.FUND)).getPnlCalculation(), period)
                            .map(pnl -> pnl.getValue().get(reportParameters.getCurrency()))
                            .map(currency -> currencyFormat.format(currency))
                            .orElse("-")
            );
        }

        at.addRule();

        return at.render();
    }

    private static Optional<Quotes> getSafePnl(Map<Period, Quotes> pnlCalculation, Period p) {
        return Optional.ofNullable(pnlCalculation.get(p));
    }

    private static String renderOpenPositions(Report report, ReportParameters reportParameters) {
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(15));

        at.addRule();
        AT_Row topHeader = at.addRow(null, null, null, null, null, null, null, null, null, null, null, null, "Open Positions");
        topHeader.getCells().get(12).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        AT_Row header = at.addRow(null, null, null, null, "", null, null, null, "PNL (" + reportParameters.getCurrency() + ")", null, null, null, "ROI (%)");
        header.getCells().get(8).getContext().setTextAlignment(TextAlignment.CENTER);
        header.getCells().get(12).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        at.addRow(
                "Instrument",
                "Price(" + reportParameters.getCurrency() + ")",
                "Cost",
                "Amount",
                "Value(" + reportParameters.getCurrency() + ")",
                "ALL", "1M", "1W", "1D",
                "ALL", "1M", "1W", "1D"
        );

        report.getOpenPositions().stream()
                .filter(w -> !w.getTotalAmount().equals(BigDecimal.ZERO))
                .sorted(Comparator.comparing(w -> w.getInstrument().getSymbol()))
                .forEach(w -> {
                    at.addRule();
                    AT_Row row = at.addRow(
                            String.valueOf(w.getInstrument().getSymbol()),
                            currencyFormat.format(w.getPrice().getValue().get(reportParameters.getCurrency())),
                            currencyFormat.format(w.getUnitCost().getValue().get(reportParameters.getCurrency())),
                            String.valueOf(w.getTotalAmount()),
                            currencyFormat.format(w.getTotalValue().getValue().get(reportParameters.getCurrency())),
                            currencyFormat.format(w.getPnlCalculation().get(Period.M1).getValue().get(reportParameters.getCurrency())),
                            currencyFormat.format(w.getPnlCalculation().get(Period.ALL).getValue().get(reportParameters.getCurrency())),
                            currencyFormat.format(w.getPnlCalculation().get(Period.W1).getValue().get(reportParameters.getCurrency())),
                            currencyFormat.format(w.getPnlCalculation().get(Period.D1).getValue().get(reportParameters.getCurrency())),
                            rateFormat.format(w.getRoiCalculation().get(Period.ALL).getValue().get(reportParameters.getCurrency())),
                            rateFormat.format(w.getRoiCalculation().get(Period.M1).getValue().get(reportParameters.getCurrency())),
                            rateFormat.format(w.getRoiCalculation().get(Period.W1).getValue().get(reportParameters.getCurrency())),
                            rateFormat.format(w.getRoiCalculation().get(Period.D1).getValue().get(reportParameters.getCurrency()))
                    );
                });
        at.addRule();

        return at.render();
    }

    private static String renderPnlHistory(Map<String, Quotes> pnlHistory, ReportParameters reportParameters, String tableName) {
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(20));

        at.addRule();
        AT_Row tableHeader= at.addRow(
                null,
                tableName
        );
        tableHeader.getCells().get(1).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        at.addRow(
                "Date",
                "PNL (" + reportParameters.getCurrency() + ")"
        );

        pnlHistory.keySet()
                .stream()
                .sorted()
                .forEach(day -> {
                    at.addRule();
                    at.addRow(day, pnlHistory.get(day).getValue().get(reportParameters.getCurrency()));
                });

        at.addRule();
        return at.render();
    }

    private static String cellWithCurrency(String name, Currency currency) {
        return MessageFormat.format("{0} ({1})", name, String.valueOf(currency));
    }

    private static  String flex(String s1, String s2) {
        String[] l1 = s1.split("/n");
        String[] l2 = s2.split("/n");

        List<String> lines = new ArrayList<>();

        int leftTableSize = l1[0].length();

        for (int rowIndex=0; rowIndex<l1.length; rowIndex++) {
            if (rowIndex < l2.length) {
                lines.add(l1[rowIndex] + " x " + l2[rowIndex]);
            } else {
                lines.add(chTimes('c', leftTableSize) + " x " + l2[rowIndex]);
            }
        }

        return String.join("\n", lines);
    }

    private static String chTimes(char ch, int count) {
        String s = "";
        for(int i=0; i<count; i++) {
            s += ch;
        }
        return s;
    }
}
