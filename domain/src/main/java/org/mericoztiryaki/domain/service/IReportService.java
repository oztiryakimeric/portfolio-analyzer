package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.exception.ReportGenerationException;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.result.Report;

public interface IReportService {

    Report generateReport(ReportParameters reportParameters) throws ReportGenerationException;

}
