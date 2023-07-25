package org.mericoztiryaki.app.writer.excel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.xmlbeans.impl.store.Cur;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.Report;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class AggregatedSheetWriter {

    private final Workbook workbook;
    private final Sheet sheet;

    private final Report report;
    private final ReportParameters parameters;

    private final List<String> sortedInstrumentTypes;
    private final List<Currency> sortedCurrencies;
    private final List<Period> sortedPeriods;

    public AggregatedSheetWriter(Report report, ReportParameters parameters, Workbook workbook) {
        this.report = report;
        this.parameters = parameters;
        this.workbook = workbook;
        this.sheet = workbook.createSheet(getSheetName());

        this.sortedInstrumentTypes = Arrays.stream(InstrumentType.values())
                .map(Objects::toString)
                .sorted().collect(Collectors.toList());

        this.sortedCurrencies = Arrays.stream(Currency.values())
                .sorted().collect(Collectors.toList());

        this.sortedPeriods = parameters.getPeriods().stream()
                .sorted(Comparator.comparing(Period::getDayCount).reversed())
                .collect(Collectors.toList());
    }

    public String getSheetName() {
        return "Aggregated Results";
    }

    public void build() {
        int tableStartIndex = 0;
        for (Currency c: sortedCurrencies) {
            AggregatedTableBuilder builder = new AggregatedTableBuilder(tableStartIndex, c);
            tableStartIndex += builder.build();
            tableStartIndex += 2;
        }

        for (int i=1; i<6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @RequiredArgsConstructor
    private class AggregatedTableBuilder {
        private final int tableStartIndex;
        private final Currency currency;

        public int build() {
            int totalRowCount = 1;

            totalRowCount += renderTableHeader();
            totalRowCount += renderInstrumentTypes();
            totalRowCount += renderTotalValues();
            totalRowCount += renderPnls();

            return totalRowCount;
        }

        public int renderTableHeader() {
            // Table header
            Row tableHeader = sheet.createRow(tableStartIndex);

            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setFont(font);

            Cell headerCell = tableHeader.createCell(0);
            headerCell.setCellValue(MessageFormat.format("Aggregated Results {0}", currency));
            headerCell.setCellStyle(cellStyle);

            sheet.addMergedRegion(new CellRangeAddress(tableStartIndex, tableStartIndex, 0, 4));

            return 1;
        }

        public int renderInstrumentTypes() {
            Row instrumentTypeRow = sheet.createRow(tableStartIndex + 1);
            instrumentTypeRow.createCell(1).setCellValue("ALL");

            for (int i=0; i<sortedInstrumentTypes.size(); i++) {
                instrumentTypeRow.createCell(i + 2).setCellValue(sortedInstrumentTypes.get(i));
            }

            return 1;
        }

        public int renderTotalValues() {
            CellStyle currencyCellStyle = getCurrencyCellStyle(currency);

            Row totalValuesRow = sheet.createRow(tableStartIndex + 2);
            totalValuesRow.createCell(0).setCellValue("Value");

            BigDecimal value = report.getAggregatedResult().getTotalValue().getValue().get(currency);
            Cell cell = totalValuesRow.createCell(1);

            cell.setCellStyle(currencyCellStyle);
            cell.setCellValue(value.doubleValue());

            for (int i=0; i<sortedInstrumentTypes.size(); i++) {
                cell = totalValuesRow.createCell( i + 2);
                value = report.getAggregatedResult().getChildren()
                        .get(sortedInstrumentTypes.get(i)).getTotalValue().getValue()
                        .get(currency);

                cell.setCellValue(value.doubleValue());
                cell.setCellStyle(currencyCellStyle);
            }

            return 1;
        }

        public int renderPnls() {
            CellStyle currencyCellStyle = getCurrencyCellStyle(currency);

            for (int i=0; i<sortedPeriods.size(); i++) {
                Period period = sortedPeriods.get(i);
                Row periodRow = sheet.createRow(tableStartIndex + 3 + i);

                periodRow.createCell(0).setCellValue(MessageFormat.format("PNL {0}", period));
                Cell cell = periodRow.createCell(1);

                // For all instrument's pnl
                report.getAggregatedResult().getPnlCalculation()
                        .getOrDefault(period, Optional.empty())
                        .map(pnl -> pnl.getValue().get(currency))
                        .ifPresentOrElse(
                                v -> {
                                    cell.setCellValue(v.doubleValue());
                                    cell.setCellStyle(currencyCellStyle);
                                },
                                () -> cell.setCellValue("-")
                        );

                for (int j=0; j<sortedInstrumentTypes.size(); j++) {
                    String instrumentType = sortedInstrumentTypes.get(j);
                    Cell finalCell = periodRow.createCell(2 + j);

                    report.getAggregatedResult().getChildren().get(instrumentType)
                            .getPnlCalculation()
                            .getOrDefault(period, Optional.empty())
                            .map(pnl -> pnl.getValue().get(currency))
                            .ifPresentOrElse(
                                    v -> {
                                        finalCell.setCellValue(v.doubleValue());
                                        finalCell.setCellStyle(currencyCellStyle);
                                        },
                                    () -> finalCell.setCellValue("-")
                            );
                }

            }

            return parameters.getPeriods().size();
        }
    }

    public CellStyle getCurrencyCellStyle(Currency currency) {
        CellStyle cs = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        cs.setDataFormat(df.getFormat(currency.getPrefix() + "#,##0.0"));

        return cs;
    }
}
