package org.mericoztiryaki.app.reader;

import lombok.RequiredArgsConstructor;
import org.mericoztiryaki.app.util.CsvUtil;
import org.mericoztiryaki.domain.model.transaction.TransactionDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CsvReader implements PortfolioReader {

    private final String csvFilePath;

    @Override
    public List<TransactionDefinition> read() throws IOException {
        List<List<String>> rawCsvFile = CsvUtil.readCsvFile(csvFilePath);

        List<TransactionDefinition> defs = new ArrayList<>();

        for(int i=0; i<rawCsvFile.size(); i++) {
            List<String> row = rawCsvFile.get(i);
            defs.add(new TransactionDefinition(i, row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                    row.get(5), row.get(6), row.get(7)));
        }

        return defs;
    }

}
