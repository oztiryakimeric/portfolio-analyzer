package org.mericoztiryaki.domain.util;

import org.mericoztiryaki.domain.exception.ReportParametersException;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.ReportRequest;
import org.mericoztiryaki.domain.model.constant.ReportOutputType;

import java.time.LocalDate;
import java.util.HashSet;

public class ReportParametersUtil {

    public static ReportParameters validateReportRequest(ReportRequest reportRequest) throws ReportParametersException {
        if (reportRequest.getTransactions() == null || reportRequest.getTransactions().isEmpty()) {
            throw new ReportParametersException("Transactions must not be empty");
        }

        if (LocalDate.now().isBefore(reportRequest.getReportDate())) {
            throw new ReportParametersException("Report date must not be future");
        }

        if (reportRequest.getPeriods() == null || reportRequest.getPeriods().isEmpty()) {
            throw new ReportParametersException("Periods must not be empty");
        }

        if (reportRequest.getPnlHistoryUnits() == null || reportRequest.getPnlHistoryUnits().isEmpty()) {
            reportRequest.setPnlHistoryUnits(new HashSet<>());
        }

        if (reportRequest.getCurrencies() == null || reportRequest.getCurrencies().isEmpty()) {
            throw new ReportParametersException("Currencies must not be empty");
        }

        if (reportRequest.getOutputType() == null) {
            throw new ReportParametersException("Output type must not be empty");
        }

        if (reportRequest.getOutputType() == ReportOutputType.EXCEL
                && reportRequest.getOutputFileLocation() == null) {
            throw new ReportParametersException("Output file location must not be empty for excel reports");
        }

        return ReportParameters.builder()
                .transactions(reportRequest.getTransactions())
                .reportDate(reportRequest.getReportDate())
                .periods(reportRequest.getPeriods())
                .pnlHistoryUnits(reportRequest.getPnlHistoryUnits())
                .currencies(reportRequest.getCurrencies())
                .filteredInstrumentTypes(reportRequest.getFilteredInstrumentTypes())
                .filteredSymbols(reportRequest.getFilteredSymbols())
                .outputType(reportRequest.getOutputType())
                .outputFileLocation(reportRequest.getOutputFileLocation())
                .build();
    }

}
