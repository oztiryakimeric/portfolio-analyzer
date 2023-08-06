package org.mericoztiryaki.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class Price {
    private final String day;
    private final Map<String, String> quotes;
}
