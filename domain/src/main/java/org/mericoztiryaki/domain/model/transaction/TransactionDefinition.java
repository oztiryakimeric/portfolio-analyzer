package org.mericoztiryaki.domain.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class TransactionDefinition {

    private String date;

    private String instrumentType;

    private String transactionType;

    private String symbol;

    private String amount;

    private String purchasePrice;

    private String commissionPrice;

    private String currency;

}
