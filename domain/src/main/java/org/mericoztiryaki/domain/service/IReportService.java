package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.model.Portfolio;
import org.mericoztiryaki.domain.model.ReportParameters;

public interface IReportService {

    Portfolio generateReport(ReportParameters reportParameters);

}
