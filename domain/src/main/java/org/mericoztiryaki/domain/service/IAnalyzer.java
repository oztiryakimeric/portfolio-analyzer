package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.model.Quotes;

public interface IAnalyzer {

    public Quotes calculateInitialValue();

    Quotes calculateTotalValue();

    Quotes calculatePNL();

    Quotes calculateROI();

    Quotes calculateUnitCost();

}
