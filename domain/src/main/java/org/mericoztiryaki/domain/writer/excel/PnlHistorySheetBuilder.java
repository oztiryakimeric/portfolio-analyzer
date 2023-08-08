package org.mericoztiryaki.domain.writer.excel;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.PnlHistoryUnit;
import org.mericoztiryaki.domain.model.result.Report;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PnlHistorySheetBuilder extends AbstractSheetBuilder {


    public PnlHistorySheetBuilder(Report report, ReportParameters parameters, Workbook workbook) {
        super(workbook, report, parameters);
    }

    @Override
    public String getSheetName() {
        return "Pnl History";
    }

    @Override
    public void build() {
        List<PnlHistoryUnit> sortedHistoryUnits = getParameters().getPnlHistoryUnits().stream()
                .sorted(Comparator.comparing(PnlHistorySheetBuilder::getPnlHistoryUnitOrder))
                .collect(Collectors.toList());

        sortedHistoryUnits.forEach(unit -> {
            build(unit);

            getExcelConnector().getRowCursor().reset();
            getExcelConnector().getColCursor().reset(getExcelConnector().getColCursor().current() + 1);
        });

        super.autoSizeAllColumns(getExcelConnector().getColCursor().current() + 3);
    }

    private void build(PnlHistoryUnit unit) {
        renderTableHeader(unit);
        renderHeaders();
        renderHistory(unit);
    }

    private void renderTableHeader(PnlHistoryUnit unit) {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value(MessageFormat.format("{0} Pnl History", getPnlHistoryUnitName(unit)))
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                0,
                0,
                getExcelConnector().getColCursor().getInitialIndex() + 1,
                getExcelConnector().getColCursor().getInitialIndex() + 1 + getSortedCurrencies().size()));
    }

    private void renderHeaders() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value("Period")
                .bold(true)
                .build();

        for (Currency currency: getSortedCurrencies()) {
            getExcelConnector().cellBuilder()
                    .value(currency.toString())
                    .bold(true)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();
        }
    }

    private void renderHistory(PnlHistoryUnit unit) {
        List<String> periods = getReport().getPnlHistory().get(unit).keySet().stream().sorted().collect(Collectors.toList());

        for (int i=0; i<periods.size(); i++) {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder()
                    .value(periods.get(i))
                    .bold(true)
                    .build();

            for (Currency currency: getSortedCurrencies()) {
                getExcelConnector().cellBuilder()
                        .value(getReport().getPnlHistory().get(unit).get(periods.get(i)).getValue().get(currency))
                        .currency(currency)
                        .alignment(HorizontalAlignment.RIGHT)
                        .build();
            }
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
