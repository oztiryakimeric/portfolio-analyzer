package org.mericoztiryaki.domain.exception;

public class ReportParametersException extends ReportGenerationException {

    private String messsage;

    public ReportParametersException(String message) {
        super(null);
        this.messsage = message;
    }

    @Override
    public String getMessage() {
        return messsage;
    }
}
