package org.mericoztiryaki.app.writer.excel;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.Report;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DailyPnlHistorySheetWriter {

    private final Workbook workbook;
    private final Sheet sheet;

    private final Report report;
    private final ReportParameters parameters;

    private final List<Currency> sortedCurrencies;

    public DailyPnlHistorySheetWriter(Report report, ReportParameters parameters, Workbook workbook) {
        this.report = report;
        this.parameters = parameters;
        this.workbook = workbook;
        this.sheet = workbook.createSheet("Daily Pnl History");

        this.sortedCurrencies = Arrays.stream(Currency.values())
                .sorted().collect(Collectors.toList());
    }

    public void build() {
        int tableStartIndex = 0;
        for (Currency c: this.sortedCurrencies) {
            DailyPnlHistoryBuilder builder = new DailyPnlHistoryBuilder(tableStartIndex, c);
            tableStartIndex += builder.build();
            tableStartIndex += 2;
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    @RequiredArgsConstructor
    private class DailyPnlHistoryBuilder {
        private final int tableStartIndex;
        private final Currency currency;

        public int build() {
            int rowCount = 1;

            rowCount += renderTableHeader();
            rowCount += renderDays();

            return rowCount;
        }

        private int renderTableHeader() {
            Row tableHeader = sheet.createRow(tableStartIndex);

            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setFont(font);

            Cell headerCell = tableHeader.createCell(0);
            headerCell.setCellValue(MessageFormat.format("Daily Pnl History {0}", currency));
            headerCell.setCellStyle(cellStyle);

            sheet.addMergedRegion(new CellRangeAddress(tableStartIndex, tableStartIndex, 0, 2));

            return 1;
        }

        private int renderDays() {
            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);

            CellStyle currencyCellStyle = getCurrencyCellStyle(currency);

            List<String> days = report.getDailyPnlHistory().keySet().stream().sorted().collect(Collectors.toList());
            for (int i=0; i<days.size(); i++) {
                Row pnlRow = sheet.createRow(tableStartIndex + 1 + i);

                Cell dayCell = pnlRow.createCell(0);
                dayCell.setCellStyle(cellStyle);
                dayCell.setCellValue(days.get(i));

                Cell pnlCell = pnlRow.createCell(1);
                pnlCell.setCellStyle(currencyCellStyle);
                pnlCell.setCellValue(report.getDailyPnlHistory().get(days.get(i)).getValue().get(currency).doubleValue());
            }

            return days.size();
        }
    }

    public CellStyle getCurrencyCellStyle(Currency currency) {
        CellStyle cs = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        cs.setDataFormat(df.getFormat(currency.getPrefix() + "#,##0.0"));

        return cs;
    }
}
