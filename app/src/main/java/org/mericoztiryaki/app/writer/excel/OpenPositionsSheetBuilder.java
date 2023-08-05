package org.mericoztiryaki.app.writer.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.InstrumentAnalyzeResult;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.util.BigDecimalUtil;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class OpenPositionsSheetBuilder extends AbstractSheetBuilder {

    public OpenPositionsSheetBuilder(Report report, ReportParameters parameters, Workbook workbook) {
        super(workbook, report, parameters);
    }

    @Override
    public String getSheetName() {
        return "Open Positions";
    }

    @Override
    public void build() {
        for (Currency currency: getSortedCurrencies()) {
            build(currency);
            getExcelConnector().getRowCursor().moveBy(2);
        }

        super.autoSizeAllColumns();
    }

    private void build(Currency currency) {
        renderTableHeader(currency);
        renderPnlAndRoiHeaders();
        renderHeaders();
        renderPositions(currency);
    }

    private void renderTableHeader(Currency currency) {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value(MessageFormat.format("Open Positions {0}", currency))
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                getExcelConnector().getRowCursor().current(),
                getExcelConnector().getRowCursor().current(),
                0,
                4 + getSortedPeriods().size() * 2)
        );
    }

    private void renderPnlAndRoiHeaders() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .index(5)
                .value("PNL")
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                getExcelConnector().getRowCursor().current(),
                getExcelConnector().getRowCursor().current(),
                5,
                8
        ));

        getExcelConnector().cellBuilder()
                .index(9)
                .value("ROI (%)")
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                getExcelConnector().getRowCursor().current(),
                getExcelConnector().getRowCursor().current(),
                9,
                12
        ));
    }

    private void renderHeaders() {
        getExcelConnector().createRow();

        List<String> headers = new ArrayList<>(List.of("Instrument", "Price", "Cost", "Amount", "Value"));
        headers.addAll(getSortedPeriods().stream().map(Objects::toString).collect(Collectors.toList()));
        headers.addAll(getSortedPeriods().stream().map(Objects::toString).collect(Collectors.toList()));

        for (int i=0; i<headers.size(); i++) {
            getExcelConnector().cellBuilder()
                    .value(headers.get(i))
                    .bold(true)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();
        }
    }


    private void renderPositions(Currency currency) {
        List<InstrumentAnalyzeResult> openPositions = getReport().getOpenPositions().stream()
                .filter(w -> !BigDecimalUtil.isZero(w.getTotalAmount()))
                .sorted(Comparator.comparing(w -> w.getInstrument().getSymbol()))
                .collect(Collectors.toList());

        for (InstrumentAnalyzeResult position: openPositions) {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder()
                    .value(position.getInstrument().getSymbol())
                    .bold(true)
                    .build();

            getExcelConnector().cellBuilder()
                    .value(position.getPrice().getValue().get(currency))
                    .currency(currency)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            getExcelConnector().cellBuilder()
                    .value(position.getUnitCost().getValue().get(currency))
                    .currency(currency)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            getExcelConnector().cellBuilder()
                    .value(position.getTotalAmount())
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            getExcelConnector().cellBuilder()
                    .value(position.getTotalValue().getValue().get(currency))
                    .currency(currency)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            for (Period period: getSortedPeriods()) {
                getExcelConnector().cellBuilder()
                        .value(position.getPnlCalculation().get(period).getValue().get(currency))
                        .currency(currency)
                        .alignment(HorizontalAlignment.RIGHT)
                        .build();
            }

            for (Period period: getSortedPeriods()) {
                getExcelConnector().cellBuilder()
                        .value(position.getRoiCalculation().get(period).getValue().get(currency))
                        .currency(currency)
                        .alignment(HorizontalAlignment.RIGHT)
                        .build();
            }
        }
    }

}
