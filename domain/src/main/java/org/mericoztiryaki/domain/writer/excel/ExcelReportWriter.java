package org.mericoztiryaki.domain.writer.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mericoztiryaki.domain.exception.ReportWriterException;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.writer.ReportWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExcelReportWriter implements ReportWriter {

    @Override
    public byte[] build(Report report, ReportParameters reportParameters) {
        Workbook workbook  = new XSSFWorkbook();

        AggregatedSheetWriter aggregatedSheetWriter = new AggregatedSheetWriter(report, reportParameters, workbook);
        aggregatedSheetWriter.build();

        DetailedAggregatedSheetWriter detailedAggregatedSheetWriter = new DetailedAggregatedSheetWriter(report, reportParameters, workbook);
        detailedAggregatedSheetWriter.build();

        OpenPositionsSheetBuilder openPositionsSheetBuilder = new OpenPositionsSheetBuilder(report, reportParameters, workbook);
        openPositionsSheetBuilder.build();

        DailyPnlHistorySheetBuilder dailyPnlHistorySheetBuilder = new DailyPnlHistorySheetBuilder(report, reportParameters, workbook);
        dailyPnlHistorySheetBuilder.build();

        TransactionSheetBuilder transactionSheetBuilder = new TransactionSheetBuilder(workbook, report, reportParameters);
        transactionSheetBuilder.build();

        byte[] content = convertToByteArray(workbook);

        try {
            Files.write(Paths.get(reportParameters.getOutputFileLocation()), content);
        } catch (IOException e) {
            throw new ReportWriterException(e);
        }

        return content;
    }

    private byte[] convertToByteArray(Workbook workbook) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();

            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
