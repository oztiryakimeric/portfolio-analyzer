package org.mericoztiryaki.app.writer.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mericoztiryaki.app.writer.ReportWriter;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.result.Report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelReportWriter implements ReportWriter {

    private final Workbook workbook;

    public ExcelReportWriter() {
        this.workbook = new XSSFWorkbook();
    }

    @Override
    public String build(Report report, ReportParameters reportParameters) {
        AggregatedSheetWriter aggregatedSheetWriter = new AggregatedSheetWriter(report, reportParameters, workbook);
        aggregatedSheetWriter.build();

        DetailedAggregatedSheetWriter detailedAggregatedSheetWriter = new DetailedAggregatedSheetWriter(report, reportParameters, workbook);
        detailedAggregatedSheetWriter.build();

        OpenPositionsSheetWriter openPositionsSheetWriter = new OpenPositionsSheetWriter(report, reportParameters, workbook);
        openPositionsSheetWriter.build();

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";

        try {
            FileOutputStream outputStream = new FileOutputStream(fileLocation);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

}
