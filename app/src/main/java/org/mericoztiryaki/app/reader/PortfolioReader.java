package org.mericoztiryaki.app.reader;

import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;

import java.io.IOException;
import java.util.List;

public interface PortfolioReader {

    List<TransactionDefinition> read();

}
