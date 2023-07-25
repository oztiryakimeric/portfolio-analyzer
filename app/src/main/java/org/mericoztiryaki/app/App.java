package org.mericoztiryaki.app;

import org.mericoztiryaki.app.writer.excel.ExcelReportWriter;
import org.mericoztiryaki.app.writer.ReportWriter;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;
import org.mericoztiryaki.domain.service.impl.ReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class App {

    public static void main(String[] args) throws IOException, PriceApiException {
        List<TransactionDefinition> transactions = readTransactions();

        ReportParameters parameters = ReportParameters.builder()
                .transactions(transactions)
                .reportDate(LocalDate.now())
                .periods(Set.of(Period.D1, Period.W1, Period.M1, Period.ALL))
                .currency(Currency.TRY)
                .build();

        Report report = new ReportService().generateReport(parameters);
        ReportWriter writer = new ExcelReportWriter();

        System.out.println(writer.build(report, parameters));

        System.out.println("EDOM");
    }

    private static List<TransactionDefinition> readTransactions() throws IOException {
        List<List<String>> rawCsvFile = Util.readCsvFile(".dev-space/dev-portfolio-3.csv");

        List<TransactionDefinition> defs = new ArrayList<>();
        for(int i=0; i<rawCsvFile.size(); i++) {
            List<String> row = rawCsvFile.get(i);
            defs.add(new TransactionDefinition(i, row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                    row.get(5), row.get(6), row.get(7)));
        }
        return defs;
    }

}
