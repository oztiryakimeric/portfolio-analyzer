package org.mericoztiryaki.domain.writer.excel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.xmlbeans.impl.store.Cur;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.result.Report;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Optional;

@Getter
public class AggregatedSheetWriter extends AbstractSheetBuilder {

    public AggregatedSheetWriter(Report report, ReportParameters parameters, Workbook workbook) {
        super(workbook, report, parameters);
    }

    @Override
    public String getSheetName() {
        return "Aggregated Results";
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
        renderTotalValues(currency);
        renderPnls(currency);
        renderRois(currency);
    }

    private void renderTableHeader(Currency currency) {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .value(MessageFormat.format("Aggregated Results {0}", currency))
                .bold(true)
                .alignment(HorizontalAlignment.CENTER)
                .build();

        getExcelConnector().getSheet().addMergedRegion(new CellRangeAddress(
                getExcelConnector().getRowCursor().current(),
                getExcelConnector().getRowCursor().current(),
                0,
                4
        ));
    }

    private void renderInstrumentTypes() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder()
                .index(1)
                .value("ALL")
                .bold(true)
                .alignment(HorizontalAlignment.RIGHT)
                .build();

        getSortedInstrumentTypes().forEach(instrument -> getExcelConnector().cellBuilder()
                .value(instrument)
                .bold(true)
                .alignment(HorizontalAlignment.RIGHT)
                .build());
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
                .alignment(HorizontalAlignment.RIGHT)
                .build();

        getSortedInstrumentTypes().forEach(instrument -> {
            BigDecimal value = getReport().getAggregatedResult().getChildren()
                    .get(instrument).getTotalValue().getValue()
                    .get(currency);

            getExcelConnector().cellBuilder()
                    .value(value)
                    .currency(currency)
                    .alignment(HorizontalAlignment.RIGHT)
                    .build();
        });
    }

    private void renderPnls(Currency currency) {
        getSortedPeriods().forEach(period -> {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder()
                    .value(MessageFormat.format("PNL {0}", period))
                    .bold(true)
                    .build();

            // For all instrument's pnl
            getReport().getAggregatedResult().getPnlCalculation()
                    .getOrDefault(period, Optional.empty())
                    .map(pnl -> pnl.getValue().get(currency))
                    .ifPresentOrElse(
                            v -> getExcelConnector().cellBuilder()
                                        .value(v)
                                        .currency(currency)
                                        .alignment(HorizontalAlignment.RIGHT)
                                        .build(),
                            () -> getExcelConnector().cellBuilder()
                                    .value("-")
                                    .alignment(HorizontalAlignment.RIGHT)
                                    .build()
                    );

            getSortedInstrumentTypes().forEach(instrumentType -> {
                getReport().getAggregatedResult().getChildren().get(instrumentType)
                        .getPnlCalculation()
                        .getOrDefault(period, Optional.empty())
                        .map(pnl -> pnl.getValue().get(currency))
                        .ifPresentOrElse(
                                v -> getExcelConnector().cellBuilder()
                                        .value(v)
                                        .currency(currency)
                                        .alignment(HorizontalAlignment.RIGHT)
                                        .build(),
                                () -> getExcelConnector().cellBuilder()
                                        .value("-")
                                        .alignment(HorizontalAlignment.RIGHT)
                                        .build()
                        );
            });
        });
    }

    private void renderRois(Currency currency) {
        getSortedPeriods().forEach(period -> {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder()
                    .value(MessageFormat.format("ROI {0}", period))
                    .bold(true)
                    .build();

            // For all instrument's pnl
            getReport().getAggregatedResult().getRoiCalculation()
                    .getOrDefault(period, Optional.empty())
                    .map(roi -> roi.getValue().get(currency))
                    .ifPresentOrElse(
                            v -> getExcelConnector().cellBuilder()
                                    .value(v)
                                    .percentage(true)
                                    .alignment(HorizontalAlignment.RIGHT)
                                    .build()
                            ,
                            () -> getExcelConnector().cellBuilder()
                                    .value("-")
                                    .alignment(HorizontalAlignment.RIGHT)
                                    .build()
                    );

            getSortedInstrumentTypes().forEach(instrumentType -> {
                getReport().getAggregatedResult().getChildren().get(instrumentType)
                        .getRoiCalculation()
                        .getOrDefault(period, Optional.empty())
                        .map(roi -> roi.getValue().get(currency))
                        .ifPresentOrElse(
                                v -> getExcelConnector().cellBuilder()
                                        .value(v)
                                        .percentage(true)
                                        .alignment(HorizontalAlignment.RIGHT)
                                        .build(),
                                () -> getExcelConnector().cellBuilder()
                                        .value("-")
                                        .alignment(HorizontalAlignment.RIGHT)
                                        .build()
                        );
            });
        });
    }
}
