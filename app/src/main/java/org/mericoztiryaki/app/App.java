package org.mericoztiryaki.app;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWordMin;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;
import org.mericoztiryaki.domain.service.impl.ReportService;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
        System.out.println("EDOM");
    }

    private static List<TransactionDefinition> readTransactions() throws IOException {
        List<List<String>> rawCsvFile = Util.readTsvFile(".dev-space/dev-portfolio-1.csv");

        return rawCsvFile.stream()
                .map(row -> new TransactionDefinition(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                        row.get(5), row.get(6), row.get(7)))
                .filter(row -> !row.getInstrumentType().equals("FUND"))
                .collect(Collectors.toList());
    }

    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private static final DecimalFormat rateFormat = new DecimalFormat("#0.00");

    private static String renderAggregatedResults(Report report, ReportParameters reportParameters) {
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(20));

        at.addRule();
        at.addRow(
                cellWithCurrency("Total Value", reportParameters.getCurrency()),
                currencyFormat.format(report.getAggregatedResult().getTotalValue().getValue().get(reportParameters.getCurrency()))
        );

        at.addRule();
        at.addRow(
                cellWithCurrency("PNL All", reportParameters.getCurrency()),
                currencyFormat.format(report.getAggregatedResult().getPnlCalculation().get(Period.ALL).getValue().get(reportParameters.getCurrency()))
        );

        at.addRule();
        at.addRow(
                cellWithCurrency("PNL 1M", reportParameters.getCurrency()),
                currencyFormat.format(report.getAggregatedResult().getPnlCalculation().get(Period.M1).getValue().get(reportParameters.getCurrency()))
        );

        at.addRule();
        at.addRow(
                cellWithCurrency("PNL 1W", reportParameters.getCurrency()),
                currencyFormat.format(report.getAggregatedResult().getPnlCalculation().get(Period.W1).getValue().get(reportParameters.getCurrency()))
        );

        at.addRule();
        at.addRow(
                cellWithCurrency("PNL 1D", reportParameters.getCurrency()),
                currencyFormat.format(report.getAggregatedResult().getPnlCalculation().get(Period.D1).getValue().get(reportParameters.getCurrency()))
        );

        at.addRule();

        return at.render();
    }

    private static String renderOpenPositions(Report report, ReportParameters reportParameters) {
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(12));

        at.addRule();
        AT_Row topHeader = at.addRow(null, null, null, null, "", null, null, null, "PNL (" + reportParameters.getCurrency() + ")", null, null, null, "ROI (%)");
        topHeader.getCells().get(7).getContext().setTextAlignment(TextAlignment.CENTER);
        topHeader.getCells().get(11).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        AT_Row header = at.addRow(
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

    private static String cellWithCurrency(String name, Currency currency) {
        return MessageFormat.format("{0} ({1})", name, String.valueOf(currency));
    }
}
