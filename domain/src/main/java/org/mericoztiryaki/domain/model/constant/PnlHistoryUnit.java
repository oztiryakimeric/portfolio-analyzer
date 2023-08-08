package org.mericoztiryaki.domain.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PnlHistoryUnit {
    DAY(30),
    WEEK(8),
    MONTH(24),
    YEAR(2);

    private int size;
}
