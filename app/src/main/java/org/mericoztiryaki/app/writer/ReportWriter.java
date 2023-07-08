package org.mericoztiryaki.app.writer;

import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.result.Report;

public interface ReportWriter {

    String build(Report report, ReportParameters reportParameters);

}
