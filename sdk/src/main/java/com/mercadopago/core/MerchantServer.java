package com.mercadopago.core;

import android.content.Context;

import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.MerchantPayment;
import com.mercadopago.model.Payment;
import com.mercadopago.services.MerchantService;
import com.mercadopago.util.HttpClientUtil;
import com.mercadopago.util.JsonUtil;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class MerchantServer {

    public static void getCustomer(Context context, String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, Callback<Customer> callback) {

        MerchantService service = getService(context, merchantBaseUrl);
        service.getCustomer(ripFirstSlash(merchantGetCustomerUri), merchantAccessToken, callback);
    }

    public static void createPayment(Context context, String merchantBaseUrl, String merchantCreatePaymentUri, MerchantPayment payment, final Callback<Payment> callback) {

        MerchantService service = getService(context, merchantBaseUrl);
        service.createPayment(ripFirstSlash(merchantCreatePaymentUri), payment, callback);
    }

    public static void getDiscount(Context context, String merchantBaseUrl, String merchantGetDiscountUri, String merchantAccessToken, String itemId, Integer itemQuantity, final Callback<Discount> callback) {

        MerchantService service = getService(context, merchantBaseUrl);
        service.getDiscount(ripFirstSlash(merchantGetDiscountUri), merchantAccessToken, itemId, itemQuantity, callback);
    }

    private static String ripFirstSlash(String uri) {

        return uri.startsWith("/") ? uri.substring(1, uri.length()) : uri;
    }

    private static RestAdapter getRestAdapter(Context context, String endPoint) {

        return new RestAdapter.Builder()
                .setEndpoint(endPoint)
                .setLogLevel(Settings.RETROFIT_LOGGING)
                .setConverter(new GsonConverter(JsonUtil.getInstance().getGson()))
                .setClient(HttpClientUtil.getClient(context))
                .build();
    }

    private static MerchantService getService(Context context, String endPoint) {

        RestAdapter restAdapter =getRestAdapter(context, endPoint);
        return restAdapter.create(MerchantService.class);
    }
}
