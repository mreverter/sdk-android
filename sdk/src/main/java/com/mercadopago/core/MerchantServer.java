package com.mercadopago.core;

import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.MerchantPayment;
import com.mercadopago.model.Payment;
import com.mercadopago.services.MerchantService;
import com.mercadopago.util.JsonUtil;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class MerchantServer {

    public static void getCustomer(String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, Callback<Customer> callback) {

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(merchantBaseUrl).setLogLevel(RestAdapter.LogLevel.FULL).setConverter(new GsonConverter(JsonUtil.getInstance().getGson())).build();
        MerchantService service = restAdapter.create(MerchantService.class);
        service.getCustomer(ripFirstSlash(merchantGetCustomerUri), merchantAccessToken, callback);
    }

    public static void createPayment(String merchantBaseUrl, String merchantCreatePaymentUri, MerchantPayment payment, final Callback<Payment> callback) {

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(merchantBaseUrl).setLogLevel(RestAdapter.LogLevel.FULL).setConverter(new GsonConverter(JsonUtil.getInstance().getGson())).build();
        MerchantService service = restAdapter.create(MerchantService.class);
        service.createPayment(ripFirstSlash(merchantCreatePaymentUri), payment, callback);
    }

    public static void getDiscount(String merchantBaseUrl, String merchantGetDiscountUri, String merchantAccessToken, String itemId, Integer itemQuantity, final Callback<Discount> callback) {

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(merchantBaseUrl).setLogLevel(RestAdapter.LogLevel.FULL).setConverter(new GsonConverter(JsonUtil.getInstance().getGson())).build();
        MerchantService service = restAdapter.create(MerchantService.class);
        service.getDiscount(ripFirstSlash(merchantGetDiscountUri), merchantAccessToken, itemId, itemQuantity, callback);
    }

    private static String ripFirstSlash(String uri) {

        return uri.startsWith("/") ? uri.substring(1, uri.length()) : uri;
    }
}
