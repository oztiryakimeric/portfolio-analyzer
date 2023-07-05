package org.mericoztiryaki.domain.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Period {
    D1(1),
    W1(7),
    M1(30),
    ALL(Integer.MAX_VALUE);

    private int dayCount;

}
