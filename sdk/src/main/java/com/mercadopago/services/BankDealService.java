package com.mercadopago.services;

import com.mercadopago.model.BankDeal;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface BankDealService {

    @GET("/v1/bank_deals")
    void getBankDeals(@Query("public_key") String publicKey, Callback<List<BankDeal>> callback);

    @GET("/mla/credit_card_promos.json")
    void getBankDeals(Callback<List<BankDeal>> callback);
}