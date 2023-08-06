package org.mericoztiryaki.domain.writer.text;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWordMin;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.util.BigDecimalUtil;
import org.mericoztiryaki.domain.writer.ReportWriter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TextReportWriter implements ReportWriter {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat RATE_FORMAT = new DecimalFormat("#0.00");

    @Override
    public byte[] build(Report report, ReportParameters reportParameters) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(renderAggregatedResults(report, reportParameters));

        buffer.append("\n\n");
        buffer.append(renderOpenPositions(report, reportParameters));

        buffer.append("\n\n");
        buffer.append(renderPnlHistory(report.getDailyPnlHistory(), reportParameters, "Daily Pnl History"));

        String result = buffer.toString();
        System.out.println(result);

        return result.getBytes();
    }

    private String renderAggregatedResults(Report report, ReportParameters parameters) {
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(20));

        Currency currency = parameters.getCurrency();

        at.addRule();
        AT_Row tableHeader = at.addRow(null, null, null, null, "Aggregated Results");
        tableHeader.getCells().get(4).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        at.addRow(
                "",
                "ALL",
                String.valueOf(InstrumentType.BIST),
                String.valueOf(InstrumentType.CURRENCY),
                String.valueOf(InstrumentType.FUND)
        );

        at.addRule();
        at.addRow(
                withCurrencyLabel("Value", currency),
                asCurrency(report.getAggregatedResult().getTotalValue(), currency),
                asCurrency(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.BIST)).getTotalValue(), currency),
                asCurrency(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.CURRENCY)).getTotalValue(),currency),
                asCurrency(report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.FUND)).getTotalValue(),currency)
        );

        List<Period> sortedPeriods = parameters.getPeriods().stream()
                .sorted(Comparator.comparing(Period::getDayCount).reversed())
                .collect(Collectors.toList());

        for (Period period: sortedPeriods) {
            at.addRule();
            at.addRow(
                    withCurrencyLabel("Pnl " + period, currency),
                    report.getAggregatedResult().getPnlCalculation()
                            .getOrDefault(period, Optional.empty())
                            .map(pnl -> asCurrency(pnl, currency))
                            .orElse("-"),
                    report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.BIST)).getPnlCalculation()
                            .getOrDefault(period, Optional.empty())                            .map(pnl -> asCurrency(pnl, currency))
                            .orElse("-"),
                    report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.CURRENCY)).getPnlCalculation()
                            .getOrDefault(period, Optional.empty())                            .map(pnl -> asCurrency(pnl, currency))
                            .orElse("-"),
                    report.getAggregatedResult().getChildren().get(String.valueOf(InstrumentType.FUND)).getPnlCalculation()
                            .getOrDefault(period, Optional.empty())
                            .map(pnl -> asCurrency(pnl, currency))
                            .orElse("-")
            );
        }

        at.addRule();

        return at.render();
    }

    private String renderOpenPositions(Report report, ReportParameters parameters) {
        Currency currency = parameters.getCurrency();

        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(15));

        at.addRule();
        AT_Row tableName = at.addRow(null, null, null, null, null, null, null, null,
                null, null, null, null, "Open Positions");
        tableName.getCells().get(12).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        AT_Row header = at.addRow(null, null, null, null, "", null, null, null,
                withCurrencyLabel("PNL", currency), null, null, null, "ROI (%)");
        header.getCells().get(8).getContext().setTextAlignment(TextAlignment.CENTER);
        header.getCells().get(12).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        at.addRow(
                "Instrument",
                withCurrencyLabel("Price", currency),
                "Cost",
                "Amount",
                withCurrencyLabel("Value", currency),
                "ALL", "1M", "1W", "1D",
                "ALL", "1M", "1W", "1D"
        );

        report.getOpenPositions().stream()
                .filter(w -> !BigDecimalUtil.isZero(w.getTotalAmount()))
                .sorted(Comparator.comparing(w -> w.getInstrument().getSymbol()))
                .forEach(w -> {
                    at.addRule();
                    at.addRow(
                            String.valueOf(w.getInstrument().getSymbol()),
                            asCurrency(w.getPrice(), currency),
                            asCurrency(w.getUnitCost(), currency),
                            String.valueOf(w.getTotalAmount()),
                            asCurrency(w.getTotalValue(), currency),
                            asCurrency(w.getPnlCalculation().get(Period.ALL), currency),
                            asCurrency(w.getPnlCalculation().get(Period.M1), currency),
                            asCurrency(w.getPnlCalculation().get(Period.W1), currency),
                            asCurrency(w.getPnlCalculation().get(Period.D1), currency),
                            asRate(w.getRoiCalculation().get(Period.ALL).getValue().get(currency)),
                            asRate(w.getRoiCalculation().get(Period.M1).getValue().get(currency)),
                            asRate(w.getRoiCalculation().get(Period.W1).getValue().get(currency)),
                            asRate(w.getRoiCalculation().get(Period.D1).getValue().get(currency))
                    );
                });
        at.addRule();

        return at.render();
    }

    private String renderPnlHistory(Map<String, Quotes> pnlHistory, ReportParameters reportParameters, String tableName) {
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC(new CWC_LongestWordMin(20));

        at.addRule();
        AT_Row tableHeader= at.addRow(null,tableName);
        tableHeader.getCells().get(1).getContext().setTextAlignment(TextAlignment.CENTER);

        at.addRule();
        at.addRow("Date",withCurrencyLabel("PNL", reportParameters.getCurrency()));

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

    private String asCurrency(Quotes value, Currency currency) {
        return CURRENCY_FORMAT.format(value.getValue().get(currency));
    }

    private String asRate(BigDecimal value) {
        return RATE_FORMAT.format(value);
    }

    private String withCurrencyLabel(String text, Currency currency) {
        return MessageFormat.format("{0} ({1})", text, String.valueOf(currency));
    }
}
