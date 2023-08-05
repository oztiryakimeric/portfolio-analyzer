package org.mericoztiryaki.app.writer.excel;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.InstrumentAnalyzeResult;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.util.BigDecimalUtil;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class OpenPositionsSheetWriter {
    private final Workbook workbook;
    private final Sheet sheet;

    private final Report report;
    private final ReportParameters parameters;

    private final List<Currency> sortedCurrencies;
    private final List<Period> sortedPeriods;

    public OpenPositionsSheetWriter(Report report, ReportParameters parameters, Workbook workbook) {
        this.report = report;
        this.parameters = parameters;
        this.workbook = workbook;
        this.sheet = workbook.createSheet("Open Positions");

        this.sortedCurrencies = Arrays.stream(Currency.values())
                .sorted().collect(Collectors.toList());

        this.sortedPeriods = parameters.getPeriods().stream()
                .sorted(Comparator.comparing(Period::getDayCount).reversed())
                .collect(Collectors.toList());
    }

    public void build() {
        int tableStartIndex = 0;
        for (Currency c: sortedCurrencies) {
            OpenPositionsTableBuilder builder = new OpenPositionsTableBuilder(tableStartIndex, c);
            tableStartIndex += builder.build();
            tableStartIndex += 2;
        }

        for (int i=1; i<12; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @RequiredArgsConstructor
    private class OpenPositionsTableBuilder {
        private final int tableStartIndex;
        private final Currency currency;

        public int build() {
            int totalRowCount = 1;

            totalRowCount += renderTableHeader();
            totalRowCount += renderPnlAndRoiHeaders();
            totalRowCount += renderHeaders();
            totalRowCount += renderPositions();

            return totalRowCount;
        }

        public int renderTableHeader() {
            Row tableHeader = sheet.createRow(tableStartIndex);

            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setFont(font);

            Cell headerCell = tableHeader.createCell(0);
            headerCell.setCellValue(MessageFormat.format("Open Positions {0}", currency));
            headerCell.setCellStyle(cellStyle);

            sheet.addMergedRegion(new CellRangeAddress(tableStartIndex, tableStartIndex, 0, 4 + sortedPeriods.size() * 2));

            return 1;
        }

        public int renderPnlAndRoiHeaders() {
            Row pnlAndRoiHeadersRow = sheet.createRow(tableStartIndex + 1);

            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setFont(font);

            Cell pnlCell = pnlAndRoiHeadersRow.createCell(5);
            pnlCell.setCellValue("PNL");
            pnlCell.setCellStyle(cellStyle);

            sheet.addMergedRegion(new CellRangeAddress(tableStartIndex + 1, tableStartIndex + 1, 5, 8));

            Cell roiCell = pnlAndRoiHeadersRow.createCell(9);
            roiCell.setCellValue("ROI (%)");
            roiCell.setCellStyle(cellStyle);

            sheet.addMergedRegion(new CellRangeAddress(tableStartIndex + 1, tableStartIndex + 1, 9, 12));

            return 1;
        }

        public int renderHeaders() {
            Row headerRow = sheet.createRow(tableStartIndex + 2);

            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setFont(font);


            List<String> headers = new ArrayList<>(List.of("Instrument", "Price", "Cost", "Amount", "Value"));
            headers.addAll(sortedPeriods.stream().map(Objects::toString).collect(Collectors.toList()));
            headers.addAll(sortedPeriods.stream().map(Objects::toString).collect(Collectors.toList()));

            for (int i=0; i<headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(cellStyle);
            }

            return 1;
        }

        public int renderPositions() {
            int lastRowIndex = tableStartIndex + 3;

            CellStyle currencyCellStyle = getCurrencyCellStyle(currency);

            List<InstrumentAnalyzeResult> openPositions = report.getOpenPositions().stream()
                    .filter(w -> !BigDecimalUtil.isZero(w.getTotalAmount()))
                    .sorted(Comparator.comparing(w -> w.getInstrument().getSymbol()))
                    .collect(Collectors.toList());

            for (InstrumentAnalyzeResult position: openPositions) {
                Row positionRow = sheet.createRow(lastRowIndex++);

                positionRow.createCell(0).setCellValue(position.getInstrument().getSymbol());

                Cell priceCell = positionRow.createCell(1);
                priceCell.setCellStyle(currencyCellStyle);
                priceCell.setCellValue(position.getPrice().getValue().get(currency).doubleValue());

                Cell costCell = positionRow.createCell(2);
                costCell.setCellStyle(currencyCellStyle);
                costCell.setCellValue(position.getUnitCost().getValue().get(currency).doubleValue());

                positionRow.createCell(3).setCellValue(position.getTotalAmount().doubleValue());

                Cell valueCell = positionRow.createCell(4);
                valueCell.setCellStyle(currencyCellStyle);
                valueCell.setCellValue(position.getTotalValue().getValue().get(currency).doubleValue());

                for (int i=0; i<sortedPeriods.size(); i++) {
                    Period period = sortedPeriods.get(i);

                    Cell pnlCell = positionRow.createCell(5 + i);
                    pnlCell.setCellStyle(currencyCellStyle);
                    pnlCell.setCellValue(position.getPnlCalculation().get(period).getValue().get(currency).doubleValue());
                }

                for (int i=0; i<sortedPeriods.size(); i++) {
                    Period period = sortedPeriods.get(i);

                    Cell roiCell = positionRow.createCell(5 + sortedPeriods.size() + i);
                    roiCell.setCellStyle(currencyCellStyle);
                    roiCell.setCellValue(position.getRoiCalculation().get(period).getValue().get(currency).doubleValue());
                }
            }

            return openPositions.size();
        }
    }

    public CellStyle getCurrencyCellStyle(Currency currency) {
        CellStyle cs = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        cs.setDataFormat(df.getFormat(currency.getPrefix() + "#,##0.0"));

        return cs;
    }
}
