package org.mericoztiryaki.domain.writer.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.result.Report;

import java.text.MessageFormat;

public class TransactionSheetBuilder extends AbstractSheetBuilder {

    public TransactionSheetBuilder(Workbook workbook, Report report, ReportParameters parameters) {
        super(workbook, report, parameters);
    }

    @Override
    public String getSheetName() {
        return "Transactions";
    }

    @Override
    public void build() {
        getExcelConnector().createRow();

        getExcelConnector().cellBuilder().value("Date").bold(true).build();
        getExcelConnector().cellBuilder().value("Instrument Type").bold(true).build();
        getExcelConnector().cellBuilder().value("Symbol").bold(true).build();
        getExcelConnector().cellBuilder().value("Transaction Type").bold(true).build();
        getExcelConnector().cellBuilder().value("Amount").bold(true).build();

        getSortedCurrencies().forEach(currency -> {
            getExcelConnector().cellBuilder().value(MessageFormat.format("Price ({0})", currency)).build();
        });

        getReport().getTransactions().forEach(transaction -> {
            getExcelConnector().createRow();

            getExcelConnector().cellBuilder().value(transaction.getDate()).build();
            getExcelConnector().cellBuilder().value(transaction.getInstrument().getInstrumentType().toString()).build();
            getExcelConnector().cellBuilder().value(transaction.getInstrument().getSymbol()).build();
            getExcelConnector().cellBuilder().value(transaction.getTransactionType().toString()).build();
            getExcelConnector().cellBuilder().value(transaction.getAmount()).build();

            getSortedCurrencies().forEach(currency -> {
                getExcelConnector().cellBuilder()
                        .value(transaction.getPurchasePrice().getValue().get(currency))
                        .currency(currency)
                        .colorFormat(null)
                        .build();
            });
        });
    }

}
