package org.mericoztiryaki.domain.writer;

import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.result.Report;

public interface ReportWriter {

    byte[] build(Report report, ReportParameters reportParameters);

}
