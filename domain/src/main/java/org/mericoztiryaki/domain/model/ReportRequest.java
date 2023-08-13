package org.mericoztiryaki.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mericoztiryaki.domain.model.constant.*;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Builder
public class ReportRequest {

    private String inputFileLocation;

    private List<TransactionDefinition> transactions;

    private LocalDate reportDate;

    private Set<Period> periods;

    private Set<PnlHistoryUnit> pnlHistoryUnits;

    private Set<Currency> currencies;

    private Set<InstrumentType> filteredInstrumentTypes;

    private ReportOutputType outputType;

    private String outputFileLocation;

    public static ReportRequest getDefaultReportRequest() {
        // Only inputFileLocation field required
        return ReportRequest.builder()
                .reportDate(LocalDate.now())
                .currencies(Set.of(Currency.USD, Currency.TRY))
                .pnlHistoryUnits(Set.of(PnlHistoryUnit.DAY, PnlHistoryUnit.WEEK, PnlHistoryUnit.MONTH, PnlHistoryUnit.YEAR))
                .periods(Set.of(Period.D1, Period.W1, Period.M1, Period.ALL))
                .outputType(ReportOutputType.EXCEL)
                .outputFileLocation(Paths.get("").toAbsolutePath().toString() + "/report.xlsx")
                .build();
    }
}
