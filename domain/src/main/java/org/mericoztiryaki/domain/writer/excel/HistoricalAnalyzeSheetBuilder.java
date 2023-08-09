package org.mericoztiryaki.domain.writer.excel;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.PnlHistoryUnit;
import org.mericoztiryaki.domain.model.result.HistoricalAnalyzeResult;
import org.mericoztiryaki.domain.model.result.Report;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HistoricalAnalyzeSheetBuilder extends AbstractSheetBuilder {

    public HistoricalAnalyzeSheetBuilder(Report report, ReportParameters parameters, Workbook workbook) {
        super(workbook, report, parameters);
    }

    @Override
    public String getSheetName() {
        return "Historical Analyze";
    }

    @Override
    public void build() {
        List<PnlHistoryUnit> sortedHistoryUnits = getParameters().getPnlHistoryUnits().stream()
                .sorted(Comparator.comparing(HistoricalAnalyzeSheetBuilder::getPnlHistoryUnitOrder))
                .collect(Collectors.toList());

        sortedHistoryUnits.forEach(unit -> {
            build(unit);

            getExcelConnector().getRowCursor().reset();
            getExcelConnector().getColCursor().reset(getExcelConnector().getColCursor().current() + 1);
        });

        super.autoSizeAllColumns(getExcelConnector().getColCursor().current() + 3);
    }

    private void build(PnlHistoryUnit unit) {
        int longestTableRowCount = getReport().getHistoricalAnalyzeResult().values().stream()
                .mapToInt(r -> r.size()).max().getAsInt() + 2;  // 2 for headers

        for (Currency currency: getSortedCurrencies()) {
            getExcelConnector().getRowCursor().moveTo((longestTableRowCount + 5) * getSortedCurrencies().indexOf(currency));

            renderTableHeader(unit, currency);
            renderHeaders();
            renderHistory(unit, currency);
        }
    }

    private void renderTableHeader(PnlHistoryUnit unit, Currency currency) {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value(MessageFormat.format("{0} Analyze ({1})", getPnlHistoryUnitName(unit), currency))
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                getExcelConnector().getRowCursor().current(),
                getExcelConnector().getRowCursor().current(),
                getExcelConnector().getColCursor().getInitialIndex() + 1,
                getExcelConnector().getColCursor().getInitialIndex() + 4));
    }

    private void renderHeaders() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value("Period")
                .bold(true)
                .build();

        getExcelConnector().cellBuilder()
                .value("Value")
                .bold(true)
                .build();

        getExcelConnector().cellBuilder()
                .value("PNL")
                .bold(true)
                .build();

        getExcelConnector().cellBuilder()
                .value("Change")
                .bold(true)
                .build();
    }

    private void renderHistory(PnlHistoryUnit unit, Currency currency) {
        for (HistoricalAnalyzeResult result: getReport().getHistoricalAnalyzeResult().get(unit)) {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder()
                    .value(MessageFormat.format("{0} -> {1}", result.getStart(), result.getEnd()))
                    .bold(true)
                    .build();

            getExcelConnector().cellBuilder()
                    .value(result.getTotalValue().getValue().get(currency))
                    .currency(currency)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            getExcelConnector().cellBuilder()
                    .value(result.getPnl().getValue().get(currency))
                    .currency(currency)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            getExcelConnector().cellBuilder()
                    .value(result.getChange() == null ? BigDecimal.ZERO : result.getChange().getValue().get(currency))
                    .percentage(true)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();
        }
    }

    private static String getPnlHistoryUnitName(PnlHistoryUnit unit) {
        switch (unit) {
            case DAY: return "Daily";
            case WEEK: return "Weekly";
            case MONTH: return "Monthly";
            case YEAR: return "Yearly";
            default: throw new RuntimeException("PNl unit name not specified");
        }
    }

    private static int getPnlHistoryUnitOrder(PnlHistoryUnit unit) {
        switch (unit) {
            case DAY: return 0;
            case WEEK: return 1;
            case MONTH: return 2;
            case YEAR: return 3;
            default: throw new RuntimeException("PNl unit order not specified");
        }
    }
}
