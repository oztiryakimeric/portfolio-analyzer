package org.mericoztiryaki.domain.writer.excel;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.result.AggregatedAnalyzeResult;
import org.mericoztiryaki.domain.model.result.Report;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class DetailedAggregatedSheetWriter extends AbstractSheetBuilder {

    private final List<String> symbols;

    public DetailedAggregatedSheetWriter(Report report, ReportParameters parameters, Workbook workbook) {
        super(workbook, report, parameters);

        this.symbols = getSortedInstrumentTypes().stream()
                .map(instrumentType -> getReport().getAggregatedResult().getChildren().get(instrumentType))
                .flatMap(res -> res.getChildren().keySet().stream().sorted())
                .collect(Collectors.toList());
    }

    @Override
    public String getSheetName() {
        return "Detailed Aggregated Results";
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
        renderInstrumentTypes();
        renderSymbols();
        renderTotalValues(currency);
        renderPnls(currency);
    }

    private void renderTableHeader(Currency currency) {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value(MessageFormat.format("Detailed Aggregated Results {0}", currency))
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                getExcelConnector().getRowCursor().current(),
                getExcelConnector().getRowCursor().current(),
                0,
                symbols.size() + 4
        ));
    }

    private void renderInstrumentTypes() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .index(1)
                .value("ALL")
                .bold(true)
                .build();

        getSortedInstrumentTypes().forEach(instrumentType -> {
            int symbolCount = (int) getReport().getAggregatedResult().getChildren().get(instrumentType)
                    .getChildren().keySet().stream().count();

            getExcelConnector().cellBuilder()
                    .value(instrumentType)
                    .bold(true)
                    .build();

            getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                    getExcelConnector().getRowCursor().current(),
                    getExcelConnector().getRowCursor().current(),
                    getExcelConnector().getColCursor().current(),
                    getExcelConnector().getColCursor().current() + symbolCount
            ));

            getExcelConnector().getColCursor().moveBy(symbolCount);
        });
    }

    private void renderSymbols() {
        getExcelConnector().createRow();

        getExcelConnector().getColCursor().moveTo(1);
        getSortedInstrumentTypes().forEach(instrumentType -> {
            AggregatedAnalyzeResult typeLevelResult = getReport().getAggregatedResult().getChildren().get(instrumentType);

            getExcelConnector().cellBuilder()
                    .value("ALL")
                    .bold(true)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            List<AggregatedAnalyzeResult> symbolLevelResults = typeLevelResult.getChildren()
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

            symbolLevelResults.forEach(symbol -> {
                getExcelConnector().cellBuilder()
                        .value(symbol.getId())
                        .bold(true)
                        .alignment(HorizontalAlignment.RIGHT)
                        .build();
            });
        });
    }

    private void renderTotalValues(Currency currency) {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value("Value")
                .bold(true)
                .build();

        getExcelConnector().cellBuilder()
                .value(getReport().getAggregatedResult().getTotalValue().getValue().get(currency))
                .currency(currency)
                .build();

        getSortedInstrumentTypes().forEach(instrumentType -> {
            AggregatedAnalyzeResult typeLevelResult = getReport().getAggregatedResult().getChildren().get(instrumentType);

            getExcelConnector().cellBuilder()
                    .value(typeLevelResult.getTotalValue().getValue().get(currency))
                    .currency(currency)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();

            List<AggregatedAnalyzeResult> symbolLevelResults = typeLevelResult.getChildren()
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

            symbolLevelResults.forEach(symbol -> {
                getExcelConnector().cellBuilder()
                        .value(symbol.getTotalValue().getValue().get(currency))
                        .currency(currency)
                        .alignment(HorizontalAlignment.RIGHT)
                        .build();
            });
        });
    }

    private void renderPnls(Currency currency) {
        getSortedPeriods().forEach(period -> {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder()
                    .value(MessageFormat.format("PNL {0}", period))
                    .bold(true)
                    .build();

            // Total Pnl Cell
            getReport().getAggregatedResult().getPnlCalculation()
                    .getOrDefault(period, Optional.empty())
                    .map(pnl -> pnl.getValue().get(currency))
                    .ifPresentOrElse(
                            v -> getExcelConnector().cellBuilder()
                                    .value(v)
                                    .currency(currency)
                                    .alignment(HorizontalAlignment.RIGHT)
                                    .build()

                            ,
                            () -> getExcelConnector().cellBuilder()
                                    .value("-")
                                    .alignment(HorizontalAlignment.RIGHT)
                                    .build()
                    );

            // Remaining cells for each instrument type
            getSortedInstrumentTypes().forEach(instrumentType -> {
                // Type Level
                AggregatedAnalyzeResult typeLevelResult = getReport().getAggregatedResult().getChildren().get(instrumentType);
                typeLevelResult.getPnlCalculation()
                        .getOrDefault(period, Optional.empty())
                        .map(pnl -> pnl.getValue().get(currency))
                        .ifPresentOrElse(
                                v -> getExcelConnector().cellBuilder()
                                        .value(v)
                                        .currency(currency)
                                        .alignment(HorizontalAlignment.RIGHT)
                                        .build()

                                ,
                                () -> getExcelConnector().cellBuilder()
                                        .value("-")
                                        .alignment(HorizontalAlignment.RIGHT)
                                        .build()
                        );

                List<AggregatedAnalyzeResult> symbolLevelResults = typeLevelResult.getChildren()
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());

                symbolLevelResults.forEach(symbolLevelResult -> {
                    symbolLevelResult.getPnlCalculation()
                            .getOrDefault(period, Optional.empty())
                            .map(pnl -> pnl.getValue().get(currency))
                            .ifPresentOrElse(
                                    v -> getExcelConnector().cellBuilder()
                                            .value(v)
                                            .currency(currency)
                                            .alignment(HorizontalAlignment.RIGHT)
                                            .build()

                                    ,
                                    () -> getExcelConnector().cellBuilder()
                                            .value("-")
                                            .alignment(HorizontalAlignment.RIGHT)
                                            .build()
                            );
                });
            });

        });
    }

}
