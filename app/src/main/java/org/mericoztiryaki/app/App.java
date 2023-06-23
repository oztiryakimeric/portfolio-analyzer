package org.mericoztiryaki.app;

import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;
import org.mericoztiryaki.domain.service.impl.ReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) throws IOException, PriceApiException {
        List<TransactionDefinition> transactions = readTransactions();

        ReportParameters parameters = ReportParameters.builder()
                .transactions(transactions)
                .reportDate(LocalDate.now())
                .periods(Set.of(Period.D1, Period.W1, Period.M1, Period.ALL))
                .build();

        new ReportService().generateReport(parameters);

        System.out.println("EDOM");
    }

    private static List<TransactionDefinition> readTransactions() throws IOException {
        List<List<String>> rawCsvFile = Util.readCSVFile(".dev-space/dev-portfolio-1.csv");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        return rawCsvFile.stream()
                .map(row -> new TransactionDefinition(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                        row.get(5), row.get(6), row.get(7)))
                .collect(Collectors.toList());
    }
}
