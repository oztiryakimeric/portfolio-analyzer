package org.mericoztiryaki.domain.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Portfolio {

    private LocalDate date;

    private List<Wallet> wallets;

    public Portfolio(LocalDate date) {
        this.date = date;
        this.wallets = new ArrayList<>();
    }

}