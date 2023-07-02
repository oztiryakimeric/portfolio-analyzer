package org.mericoztiryaki.domain.service;

import org.mericoztiryaki.domain.model.Quotes;

public interface IAnalyzer {

    Quotes calculateTotalValue();

    Quotes calculatePNL();

    Quotes calculateROI();

    Quotes calculateUnitCost();

}
