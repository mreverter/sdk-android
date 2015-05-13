package com.mercadopago.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class BankDeal {

    //private Date expirationDate;
    private String id;
    private CardIssuer issuer;
    private String legals;
    private String marketplace;
    private int maxInstallments;
    private List<PaymentMethod> paymentMethods;
    //private Date startDate;
    private BigDecimal totalFinancialCost;

    /*
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
*/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CardIssuer getIssuer() {
        return issuer;
    }

    public void setIssuer(CardIssuer issuer) {
        this.issuer = issuer;
    }

    public String getLegals() {
        return legals;
    }

    public void setLegals(String legals) {
        this.legals = legals;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = marketplace;
    }

    public int getMaxInstallments() {
        return maxInstallments;
    }

    public void setMaxInstallments(int maxInstallments) {
        this.maxInstallments = maxInstallments;
    }

    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
/*
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
*/
    public BigDecimal getTotalFinancialCost() {
        return totalFinancialCost;
    }

    public void setTotalFinancialCost(BigDecimal totalFinancialCost) {
        this.totalFinancialCost = totalFinancialCost;
    }
}
