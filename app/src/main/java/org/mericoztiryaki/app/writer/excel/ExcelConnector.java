package org.mericoztiryaki.app.writer.excel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.mericoztiryaki.domain.model.constant.Currency;

import java.math.BigDecimal;

@Getter
public class ExcelConnector {

    private final Workbook workbook;
    private final Sheet sheet;

    private final Cursor rowCursor;
    private final Cursor colCursor;

    private Row lastRow;
    private Cell lastCell;

    public ExcelConnector(Workbook workbook, Sheet sheet) {
        this.workbook = workbook;
        this.sheet = sheet;
        this.rowCursor = new Cursor();
        this.colCursor = new Cursor();
    }

    public Row createRow(int index) {
        this.colCursor.reset();
        this.lastRow = sheet.createRow(index);
        return this.lastRow;
    }

    public Row createRow() {
        return this.createRow(rowCursor.next());
    }

    public CellBuilder cellBuilder() {
        return new CellBuilder();
    }

    private Cell createCell(int index) {
        this.lastCell = this.lastRow.createCell(index);
        colCursor.moveTo(index);
        return this.lastCell;
    }

    private Cell createCell() {
        return this.createCell(colCursor.next());
    }

    @Getter
    public static class Cursor {
        private final int initialIndex;

        private int index;
        private int biggest;

        public Cursor() {
            this.index = -1;
            this.initialIndex = -1;
        }

        public int next() {
            index += 1;
            if (index > biggest) {
                biggest = index;
            }

            return index;
        }

        public int current() {
            return index;
        }

        public void reset() {
            index = initialIndex;
        }

        public void moveBy(int amount) {
            index += amount;
        }

        public void moveTo(int index) {
            this.index = index;
        }
    }

    public class CellBuilder {

        private Integer index;

        private String valueStr;

        private BigDecimal valueBigDecimal;

        private boolean isBold;

        private Currency currency;

        private HorizontalAlignment alignment;

        public CellBuilder index(Integer index) {
            this.index = index;
            return this;
        }

        public CellBuilder value(String value) {
            this.valueStr = value;
            return this;
        }

        public CellBuilder value(BigDecimal value) {
            this.valueBigDecimal = value;
            return this;
        }

        public CellBuilder bold(boolean bold) {
            this.isBold = bold;
            return this;
        }

        public CellBuilder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public CellBuilder alignment(HorizontalAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Cell build() {
            Cell cell = index != null ? createCell(index) : createCell();

            if (valueStr != null) {
                cell.setCellValue(this.valueStr);
            } else if (valueBigDecimal != null) {
                cell.setCellValue(this.valueBigDecimal.doubleValue());
            }

            CellStyle cs = workbook.createCellStyle();
            cell.setCellStyle(cs);

            if (isBold) {
                Font font = workbook.createFont();
                font.setBold(true);

                cs.setFont(font);
            }

            if (currency != null) {
                DataFormat df = workbook.createDataFormat();
                cs.setDataFormat(df.getFormat(currency.getPrefix() + "#,##0.0"));
            }

            if (alignment != null) {
                cs.setAlignment(alignment);
            }

            return cell;
        }
    }
}