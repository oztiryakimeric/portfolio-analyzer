package org.mericoztiryaki.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.mericoztiryaki.domain.model.constant.*;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class ReportParameters {

    private List<TransactionDefinition> transactions;

    private LocalDate reportDate;

    private Set<Period> periods;

    private Set<PnlHistoryUnit> pnlHistoryUnits;

    private Set<Currency> currencies;

    private Set<InstrumentType> filteredInstrumentTypes;

    private ReportOutputType outputType;

    private String outputFileLocation;


}
