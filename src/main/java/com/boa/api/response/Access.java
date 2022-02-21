package com.boa.api.response;

import lombok.Data;

@Data
public class Access {

    private Integer suiviPaiements;
    private Integer suiviFacturiers;
    private Integer suiviFactures;
    private Integer listeCommercants;
    private Integer suiviTransferts;
    private Integer listeClients;
    private Integer suiviCashOut;
    private Integer suiviCashIn;
    private Integer inscriptionClients;
    private Integer listeActions;
}
