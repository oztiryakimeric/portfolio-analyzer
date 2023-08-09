package org.mericoztiryaki.domain.writer;

import org.mericoztiryaki.domain.model.constant.ReportOutputType;
import org.mericoztiryaki.domain.writer.excel.ExcelReportWriter;

public class WriterFactory {

    public static ReportWriter getWriter(ReportOutputType outputType) {
        switch (outputType) {
            case EXCEL:
                return new ExcelReportWriter();
            default:
                throw new RuntimeException("Output Type's writer not implemented.");
        }
    }

}
