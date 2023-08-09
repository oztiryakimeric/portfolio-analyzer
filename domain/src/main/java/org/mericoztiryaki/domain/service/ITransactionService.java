package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.model.Instrument;
import org.mericoztiryaki.domain.model.ReportParameters;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.ITransaction;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITransactionService {

    ITransaction buildTransactionObject(TransactionDefinition definition);

    Map<Instrument, List<ITransaction>> getOpenPositions(List<ITransaction> transactions);

    Map<Period, List<ITransaction>> createTransactionSetsByPeriods(List<ITransaction> transactions,
                                                                   Set<Period> periods,
                                                                   LocalDate portfolioDate);

    List<ITransaction> createTransactionSetByWindow(List<ITransaction> transactions, LocalDate start, LocalDate end);
}
