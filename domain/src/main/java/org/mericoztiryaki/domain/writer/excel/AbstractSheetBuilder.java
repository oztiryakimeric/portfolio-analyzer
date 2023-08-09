package org.mericoztiryaki.domain.writer.excel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.Workbook;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.Report;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractSheetBuilder {

    private final ExcelConnector excelConnector;

    private final Report report;
    private final ReportParameters parameters;

    private final List<String> sortedInstrumentTypes;
    private final List<Currency> sortedCurrencies;
    private final List<Period> sortedPeriods;

    public AbstractSheetBuilder(Workbook workbook, Report report, ReportParameters parameters) {
        this.excelConnector = new ExcelConnector(workbook, workbook.createSheet(getSheetName()));

        this.report = report;
        this.parameters = parameters;

        this.sortedInstrumentTypes = Arrays.stream(InstrumentType.values())
                .map(Objects::toString)
                .sorted().collect(Collectors.toList());

        this.sortedCurrencies = parameters.getCurrencies().stream()
                .sorted().collect(Collectors.toList());

        this.sortedPeriods = parameters.getPeriods().stream()
                .sorted(Comparator.comparing(Period::getDayCount).reversed())
                .collect(Collectors.toList());
    }

    public abstract String getSheetName();

    public abstract void build();

    public void autoSizeAllColumns() {
        for (int i=0; i<excelConnector.getColCursor().getBiggest(); i++) {
            excelConnector.getSheet().autoSizeColumn(i);
        }
    }

    public void autoSizeAllColumns(int count) {
        for (int i=0; i<count; i++) {
            excelConnector.getSheet().autoSizeColumn(i);
        }
    }
}
