package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.model.Report;
import org.mericoztiryaki.domain.model.ReportParameters;

public interface IReportService {

    Report generateReport(ReportParameters reportParameters);

}
