package org.mericoztiryaki.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.InstrumentType;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.ReportOutputType;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Builder
@Getter
public class ReportRequest {

    private List<TransactionDefinition> transactions;

    private LocalDate reportDate;

    private Set<Period> periods;

    private Currency currency;

    private Set<InstrumentType> filteredInstrumentTypes;

    private ReportOutputType outputType;

    private String outputFileLocation;

}
