package org.mericoztiryaki.domain.exception;

public class InvalidTransactionDefinitionException extends ReportGenerationException {

    private int index;

    public InvalidTransactionDefinitionException(Throwable cause, int index) {
        super(cause);
        this.index = index;
    }

    @Override
    public String getMessage() {
        return "Your csv input has invalid row at index: " + index;
    }

}
