package org.mericoztiryaki.domain.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Portfolio {

    private LocalDate date;

    private List<Wallet> wallets;

    public Portfolio(LocalDate date) {
        this.date = date;
        this.wallets = new ArrayList<>();
    }

    public Portfolio calculateNextDaysPortfolio() {
        Portfolio copiedPortfolio = new Portfolio(date.plusDays(1));
        copiedPortfolio.setWallets(
                this.wallets.stream().map(w -> w.nextDaysWallet()).collect(Collectors.toList())
        );
        return copiedPortfolio;
    }

}