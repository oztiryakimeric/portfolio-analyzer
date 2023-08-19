package org.mericoztiryaki.domain.exception;

import java.text.MessageFormat;

public class ReaderFailedException extends ReportGenerationException {

    private String path;

    public ReaderFailedException(Throwable cause, String path) {
        super(cause);
        this.path = path;
    }

    @Override
    public String getMessage() {
        return "Could not open the given csv file. Is csv file path correct? " + path;
    }
}
