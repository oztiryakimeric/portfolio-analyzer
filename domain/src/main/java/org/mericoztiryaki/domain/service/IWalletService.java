package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.model.Portfolio;
import org.mericoztiryaki.domain.model.transaction.ITransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

public interface IWalletService {

    SortedMap<LocalDate, Portfolio> calculatePortfolios(List<ITransaction> transactions);

}
