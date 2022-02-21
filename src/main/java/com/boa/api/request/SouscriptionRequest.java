package com.boa.api.request;

import lombok.Data;

@Data
public class SouscriptionRequest {

    private String msisdn;
    private String typeIdentite;
    private String numeroIdentite;
    private String cardType;
    private String numCard;
    private String dateValidite;
    private String nom;
    private String prenom;
    private String adresse;
    private String pays;
    private String codeBanque;
    private String codeAgence;
    private String userName;
    private String password;
    private String idSession;
}
