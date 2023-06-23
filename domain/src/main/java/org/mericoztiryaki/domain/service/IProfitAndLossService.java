package org.mericoztiryaki.domain.service;


import org.mericoztiryaki.domain.model.Quotes;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.time.LocalDate;
import java.util.List;

public interface IProfitAndLossService {

    Quotes calculatePNL(List<ITransaction> transactions, LocalDate portfolioDate);

}
