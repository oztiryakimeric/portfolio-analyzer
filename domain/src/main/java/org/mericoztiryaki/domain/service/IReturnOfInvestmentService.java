package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.time.LocalDate;
import java.util.List;

public interface IReturnOfInvestmentService {

    Quotes calculateROI(List<ITransaction> transactions, LocalDate portfolioDate);

}
