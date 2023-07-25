package org.mericoztiryaki.app.writer.excel;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.xmlbeans.impl.store.Cur;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.AggregatedAnalyzeResult;
import org.mericoztiryaki.domain.model.result.Report;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;


public class DetailedAggregatedSheetWriter extends AggregatedSheetWriter {

    private final List<String> symbols;

    public DetailedAggregatedSheetWriter(Report report, ReportParameters parameters, Workbook workbook) {
        super(report, parameters, workbook);
        this.symbols = getSortedInstrumentTypes().stream()
                .map(instrumentType -> getReport().getAggregatedResult().getChildren().get(instrumentType))
                .flatMap(res -> res.getChildren().keySet().stream())
                .collect(Collectors.toList());
    }

    public void build() {
        int tableStartIndex = 0;
        for (Currency c: super.getSortedCurrencies()) {
            AggregatedTableBuilder builder = new AggregatedTableBuilder(tableStartIndex, c);
            tableStartIndex += builder.build();
            tableStartIndex += 2;
        }

        getSheet().autoSizeColumn(1);
        getSheet().autoSizeColumn(2);
        getSheet().autoSizeColumn(3);
        getSheet().autoSizeColumn(4);
        getSheet().autoSizeColumn(5);
    }

    @Override
    public String getSheetName() {
        return "Detailed Aggregated Results";
    }

    @RequiredArgsConstructor
    private class AggregatedTableBuilder {
        private final int tableStartIndex;
        private final Currency currency;

        public int build() {
            int totalRowCount = 1;

            totalRowCount += renderTableHeader();
            totalRowCount += renderInstrumentTypes();
            totalRowCount += renderSymbols();
            totalRowCount += renderTotalValues();
            totalRowCount += renderPnls();

            return totalRowCount;
        }

        public int renderTableHeader() {
            // Table header
            Row tableHeader = getSheet().createRow(tableStartIndex);

            Font font = getWorkbook().createFont();
            font.setBold(true);

            CellStyle cellStyle = getWorkbook().createCellStyle();
            cellStyle.setFont(font);

            Cell headerCell = tableHeader.createCell(0);
            headerCell.setCellValue(MessageFormat.format("Aggregated Results {0}", currency));
            headerCell.setCellStyle(cellStyle);

            getSheet().addMergedRegion(new CellRangeAddress(tableStartIndex, tableStartIndex, 0, symbols.size() + 1));

            return 1;
        }

        public int renderInstrumentTypes() {
            Font font = getWorkbook().createFont();
            font.setBold(true);

            CellStyle cellStyleBold = getWorkbook().createCellStyle();
            cellStyleBold.setFont(font);

            Row instrumentTypeRow = getSheet().createRow(tableStartIndex + 1);
            Cell allCell = instrumentTypeRow.createCell(1);
            allCell.setCellValue("All");
            allCell.setCellStyle(cellStyleBold);

            int lastColIndex = 2;
            for (int i=0; i<getSortedInstrumentTypes().size(); i++) {
                long symbolCount = getReport().getAggregatedResult().getChildren().get(getSortedInstrumentTypes()
                        .get(i)).getChildren().keySet().stream().count();

                getSheet().addMergedRegion(new CellRangeAddress(tableStartIndex + 1, tableStartIndex + 1, lastColIndex, lastColIndex + (int) symbolCount));

                Cell cell = instrumentTypeRow.createCell(lastColIndex);
                cell.setCellValue(getSortedInstrumentTypes().get(i));
                cell.setCellStyle(cellStyleBold);

                lastColIndex += symbolCount + 1;
            }

            return 1;
        }

        public int renderSymbols() {
            Font font = getWorkbook().createFont();
            font.setBold(true);

            CellStyle cellStyleBold = getWorkbook().createCellStyle();
            cellStyleBold.setAlignment(HorizontalAlignment.CENTER);
            cellStyleBold.setFont(font);

            Row symbolRow = getSheet().createRow(tableStartIndex + 2);

            int columnIndex = 2;
            for (String instrumentType: getSortedInstrumentTypes()) {
                AggregatedAnalyzeResult typeLevelResult = getReport().getAggregatedResult().getChildren().get(instrumentType);

                Cell allCell = symbolRow.createCell(columnIndex++);
                allCell.setCellValue("All");
                allCell.setCellStyle(cellStyleBold);

                List<AggregatedAnalyzeResult> symbolLevelResults = typeLevelResult.getChildren()
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());

                for (AggregatedAnalyzeResult symbolLevelResult: symbolLevelResults) {
                    Cell cell = symbolRow.createCell(columnIndex++);
                    cell.setCellValue(symbolLevelResult.getId());
                    cell.setCellStyle(cellStyleBold);
                }
            }

            return 1;
        }

        public int renderTotalValues() {
            Font font = getWorkbook().createFont();
            font.setBold(true);

            CellStyle cellStyleBold = getWorkbook().createCellStyle();
            cellStyleBold.setAlignment(HorizontalAlignment.CENTER);
            cellStyleBold.setFont(font);

            Row valuesRow = getSheet().createRow(tableStartIndex + 3);

            Cell labelCell = valuesRow.createCell(0);
            labelCell.setCellValue("Value");
            labelCell.setCellStyle(cellStyleBold);

            Cell totalCell = valuesRow.createCell(1);
            totalCell.setCellValue(getReport().getAggregatedResult().getTotalValue()
                    .getValue().get(currency).doubleValue());
            totalCell.setCellStyle(getCurrencyCellStyle(currency));

            int columnIndex = 2;
            for (String instrumentType: getSortedInstrumentTypes()) {
                AggregatedAnalyzeResult typeLevelResult = getReport().getAggregatedResult().getChildren().get(instrumentType);

                valuesRow.createCell(columnIndex++).setCellValue(typeLevelResult.getTotalValue()
                        .getValue().get(currency).doubleValue());

                List<AggregatedAnalyzeResult> symbolLevelResults = typeLevelResult.getChildren()
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());

                for (AggregatedAnalyzeResult symbolLevelResult: symbolLevelResults) {
                    Cell cell = valuesRow.createCell(columnIndex++);
                    cell.setCellValue(symbolLevelResult.getTotalValue().getValue().get(currency).doubleValue());
                    cell.setCellStyle(getCurrencyCellStyle(currency));
                }
            }

            return 1;
        }

        public int renderPnls() {
            CellStyle currencyCellStyle = getCurrencyCellStyle(currency);

            Font font = getWorkbook().createFont();
            font.setBold(true);

            CellStyle cellStyleBold = getWorkbook().createCellStyle();
            cellStyleBold.setAlignment(HorizontalAlignment.CENTER);
            cellStyleBold.setFont(font);

            for (int i=0; i<getSortedPeriods().size(); i++) {
                Period period = getSortedPeriods().get(i);
                Row periodRow = getSheet().createRow(tableStartIndex + 4 + i);

                Cell labelCell = periodRow.createCell(0);
                labelCell.setCellValue(MessageFormat.format("PNL {0}", period));
                labelCell.setCellStyle(cellStyleBold);

                Cell totalCell = periodRow.createCell(1);
                getReport().getAggregatedResult().getPnlCalculation()
                        .getOrDefault(period, Optional.empty())
                        .map(pnl -> pnl.getValue().get(currency))
                        .ifPresentOrElse(
                                v -> {
                                    totalCell.setCellValue(v.doubleValue());
                                    totalCell.setCellStyle(currencyCellStyle);
                                },
                                () -> totalCell.setCellValue("-")
                        );

                int columnIndex = 2;
                for (String instrumentType: getSortedInstrumentTypes()) {
                    AggregatedAnalyzeResult typeLevelResult = getReport().getAggregatedResult().getChildren().get(instrumentType);

                    Cell totalPnlCell = periodRow.createCell(columnIndex++);
                    totalPnlCell.setCellStyle(currencyCellStyle);
                    typeLevelResult.getPnlCalculation()
                            .getOrDefault(period, Optional.empty())
                            .map(pnl -> pnl.getValue().get(currency))
                            .ifPresentOrElse(
                                    v -> {
                                        totalPnlCell.setCellValue(v.doubleValue());
                                        totalPnlCell.setCellStyle(currencyCellStyle);
                                    },
                                    () -> totalPnlCell.setCellValue("-")
                            );

                    List<AggregatedAnalyzeResult> symbolLevelResults = getSortedResults(typeLevelResult);

                    for (AggregatedAnalyzeResult symbolLevelResult: symbolLevelResults) {
                        Cell pnlCell = periodRow.createCell(columnIndex++);
                        symbolLevelResult.getPnlCalculation()
                                .getOrDefault(period, Optional.empty())
                                .map(pnl -> pnl.getValue().get(currency))
                                .ifPresentOrElse(
                                        v -> {
                                            pnlCell.setCellValue(v.doubleValue());
                                            pnlCell.setCellStyle(currencyCellStyle);
                                        },
                                        () -> pnlCell.setCellValue("-")
                                );

                    }
                }
            }

            return getParameters().getPeriods().size();
        }

        private List<AggregatedAnalyzeResult> getSortedResults(AggregatedAnalyzeResult res) {
            return res.getChildren()
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        }
    }

    public CellStyle getCurrencyCellStyle(Currency currency) {
        CellStyle cs = getWorkbook().createCellStyle();
        DataFormat df = getWorkbook().createDataFormat();
        cs.setDataFormat(df.getFormat(currency.getPrefix() + "#,##0.0"));

        return cs;
    }
}
