package org.mericoztiryaki.domain.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    USD("$"),
    EUR("€"),
    TRY("₺");

    private final String prefix;

}
