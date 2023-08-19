package org.mericoztiryaki.app.util;

import org.apache.commons.cli.*;
import org.mericoztiryaki.domain.exception.ReportGenerationException;
import org.mericoztiryaki.domain.exception.ReportParametersException;
import org.mericoztiryaki.domain.model.ReportRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CliParser {

    public static ReportRequest buildReportRequest(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder()
                .option("d")
                .longOpt("date")
                .hasArg()
                .desc("Date the report will be generated")
                .build());

        options.addOption(Option.builder()
                .option("i")
                .longOpt("input-file")
                .hasArg()
                .required()
                .desc("Csv file path which transactions defined in")
                .build());

        try {
            ReportRequest reportRequest = ReportRequest.getDefaultReportRequest();
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("date")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("DD-MM-YYYY");
                LocalDate date = LocalDate.parse(line.getOptionValue("date"), formatter);

                reportRequest.setReportDate(date);
            }

            if (line.hasOption("input-file")) {
                reportRequest.setInputFileLocation(line.getOptionValue("input-file"));
            }

            return reportRequest;
        } catch (ParseException e) {
            throw new ReportParametersException(e.getMessage());
        }

    }

}
