package org.mericoztiryaki.app;

import de.vandermeer.asciitable.AsciiTable;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.*;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;
import org.mericoztiryaki.domain.service.impl.ReportService;

import java.io.IOException;
import java.time.LocalDate;
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
                .currency(Currency.TRY)
                .build();

        Portfolio portfolio = new ReportService().generateReport(parameters);
        System.out.println();
        System.out.println();

        System.out.println(renderReport(portfolio, parameters));

        System.out.println();
        System.out.println();
        System.out.println("EDOM");
    }

    private static List<TransactionDefinition> readTransactions() throws IOException {
        List<List<String>> rawCsvFile = Util.readCSVFile(".dev-space/dev-portfolio-1.csv");

        return rawCsvFile.stream()
                .map(row -> new TransactionDefinition(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                        row.get(5), row.get(6), row.get(7)))
                .collect(Collectors.toList());
    }

    private static String renderReport(Portfolio portfolio, ReportParameters reportParameters) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Instrument", "Price", "Total Value", "All PNL", "All ROI");

        for (Wallet w: portfolio.getWallets()) {
            at.addRule();
            at.addRow(
                    String.valueOf(w.getInstrument().getSymbol()),
                    String.valueOf(w.getPrice().getValue().get(reportParameters.getCurrency())),
                    String.valueOf(w.getTotalValue().getValue().get(reportParameters.getCurrency())),
                    String.valueOf(w.getPnlCalculation().get(Period.ALL).getValue().get(reportParameters.getCurrency())),
                    String.valueOf(w.getRoiCalculation().get(Period.ALL).getValue().get(reportParameters.getCurrency()))
            );
        }

        return at.render();
    }
}
