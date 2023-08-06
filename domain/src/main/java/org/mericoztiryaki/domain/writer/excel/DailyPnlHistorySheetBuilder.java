package org.mericoztiryaki.domain.writer.excel;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.result.Report;

import java.util.List;
import java.util.stream.Collectors;

public class DailyPnlHistorySheetBuilder extends AbstractSheetBuilder {


    public DailyPnlHistorySheetBuilder(Report report, ReportParameters parameters, Workbook workbook) {
        super(workbook, report, parameters);
    }

    @Override
    public String getSheetName() {
        return "Daily Pnl History";
    }

    @Override
    public void build() {
        renderTableHeader();
        renderHeaders();
        renderDays();

        super.autoSizeAllColumns();
    }

    private void renderTableHeader() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value("Daily Pnl History")
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(0, 0, 0, + getSortedCurrencies().size()));
    }

    private void renderHeaders() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value("Day")
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

    private void renderDays() {
        List<String> days = getReport().getDailyPnlHistory().keySet().stream().sorted().collect(Collectors.toList());

        for (int i=0; i<days.size(); i++) {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder()
                    .value(days.get(i))
                    .bold(true)
                    .build();

            for (Currency currency: getSortedCurrencies()) {
                getExcelConnector().cellBuilder()
                        .value(getReport().getDailyPnlHistory().get(days.get(i)).getValue().get(currency))
                        .currency(currency)
                        .alignment(HorizontalAlignment.RIGHT)
                        .build();
            }
        }
    }

}
