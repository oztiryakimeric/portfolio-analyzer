package org.mericoztiryaki.domain.model;

import lombok.Builder;
import lombok.Data;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class ReportParameters {

    private List<TransactionDefinition> transactions;

    private LocalDate reportDate;

    private Set<Period> periods;

}
