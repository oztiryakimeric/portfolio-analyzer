package org.mericoztiryaki.domain;

import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.ReportRequest;
import org.mericoztiryaki.domain.model.result.Report;
import org.mericoztiryaki.domain.service.impl.ReportService;
import org.mericoztiryaki.domain.util.ReportParametersUtil;
import org.mericoztiryaki.domain.writer.ReportWriter;
import org.mericoztiryaki.domain.writer.WriterFactory;

@RequiredArgsConstructor
public class ReportManager {

    private final ReportService reportService;

    public void generateReport(ReportRequest reportRequest) {
        ReportParameters parameters = ReportParametersUtil.validateReportRequest
                (reportRequest);

        Report report = reportService.generateReport(parameters);

        ReportWriter writer = WriterFactory.getWriter(parameters.getOutputType());
        writer.build(report, parameters);
    }
}
