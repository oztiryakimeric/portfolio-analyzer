package org.mericoztiryaki.app.util;

import org.apache.commons.cli.*;
import org.mericoztiryaki.domain.exception.ReportParametersException;
import org.mericoztiryaki.domain.model.ReportRequest;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class CliParser {

    public static ReportRequest buildReportRequest(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder()
                .option("i")
                .longOpt("input-file")
                .hasArg()
                .required()
                .desc("Csv file path which transactions defined in")
                .build());

        options.addOption(Option.builder()
                .option("d")
                .longOpt("date")
                .hasArg()
                .desc("Date the report will be generated")
                .build());

        options.addOption(Option.builder()
                .option("c")
                .longOpt("currency")
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .valueSeparator(',')
                .desc("Currencies in which the reporter will be created")
                .build());

        options.addOption(Option.builder()
                .option("fi")
                .longOpt("filtered-instrument-types")
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .valueSeparator(',')
                .desc("Instrument types that will not be included in the calculation when calculating the report")
                .build());

        options.addOption(Option.builder()
                .option("fs")
                .longOpt("filtered-symbols")
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .valueSeparator(',')
                .desc("Symbols that will not be included in the calculation when calculating the report")
                .build());

        options.addOption(Option.builder()
                .option("o")
                .longOpt("output-file")
                .hasArg()
                .desc("Output file path")
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

            if (line.hasOption("output-file")) {
                reportRequest.setOutputFileLocation(line.getOptionValue("output-file"));
            }

            if (line.hasOption("currency")) {
                Set<Currency> currencies = new HashSet<>();
                for (String s: line.getOptionValues("currency")) {
                    if (s.trim().length() != 0) {
                        try {
                            currencies.add(Currency.parse(s));
                        } catch (IllegalArgumentException e) {
                            throw new ReportParametersException(e.getMessage());
                        }
                    }
                }
                reportRequest.setCurrencies(currencies);
            }

            if (line.hasOption("filtered-instrument-types")) {
                Set<InstrumentType> filteredInstrumentTypes = new HashSet<>();
                for (String s: line.getOptionValues("filtered-instrument-types")) {
                    if (s.trim().length() != 0) {
                        try {
                            filteredInstrumentTypes.add(InstrumentType.parse(s));
                        } catch (IllegalArgumentException e) {
                            throw new ReportParametersException(e.getMessage());
                        }
                    }
                }
                reportRequest.setFilteredInstrumentTypes(filteredInstrumentTypes);
            }

            if (line.hasOption("filtered-symbols")) {
                Set<String> filteredSymbols = new HashSet<>();
                for (String s: line.getOptionValues("filtered-symbols")) {
                    if (s.trim().length() != 0) {
                        filteredSymbols.add(s);
                    }
                }
                reportRequest.setFilteredSymbols(filteredSymbols);
            }

            return reportRequest;
        } catch (ParseException e) {
            throw new ReportParametersException(e.getMessage());
        }

    }

}
