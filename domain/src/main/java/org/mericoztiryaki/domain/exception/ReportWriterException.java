package org.mericoztiryaki.domain.exception;

public class ReportWriterException extends ReportGenerationException {

    public ReportWriterException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "Error while creating an Excel file. Detail: " + getCause().getMessage();
    }
}
