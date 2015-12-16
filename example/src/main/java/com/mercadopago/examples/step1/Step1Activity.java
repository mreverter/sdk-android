package com.mercadopago.examples.step1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.mercadopago.ExampleActivity;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.examples.R;
import com.mercadopago.examples.utils.ExamplesUtils;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Token;
import com.mercadopago.mpcardio.CardScannerPreference;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Step1Activity extends ExampleActivity {

    protected List<String> mSupportedPaymentTypes = new ArrayList<String>(){{
        add("credit_card");
        add("debit_card");
        add("prepaid_card");
    }};
    protected Activity mActivity;
    protected CheckBox mCheckBoxEnableCardScanner;
    private CardScannerPreference mCardScannerPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1);
        mActivity = this;
        mCheckBoxEnableCardScanner = (CheckBox) findViewById(R.id.checkBoxEnableCardScanner);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MercadoPago.PAYMENT_METHODS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Set payment method
                PaymentMethod paymentMethod = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);

                // Call new card activity
                ExamplesUtils.startCardActivity(this, ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY, paymentMethod, mCardScannerPreference);
            } else {

                if ((data != null) && (data.getStringExtra("apiException") != null)) {
                    Toast.makeText(getApplicationContext(), data.getStringExtra("apiException"), Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == ExamplesUtils.CARD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Create payment
                ExamplesUtils.createPayment(this, data.getStringExtra("token"),
                        1, null, JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class), null);

            } else {

                if (data != null) {
                    if (data.getStringExtra("apiException") != null) {

                        Toast.makeText(getApplicationContext(), data.getStringExtra("apiException"), Toast.LENGTH_LONG).show();

                    } else if (data.getBooleanExtra("backButtonPressed", false)) {

                        new MercadoPago.StartActivityBuilder()
                                .setActivity(this)
                                .setPublicKey(ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY)
                                .setSupportedPaymentTypes(mSupportedPaymentTypes)
                                .startPaymentMethodsActivity();
                    }
                }
            }
        } else if (requestCode == MercadoPago.CONGRATS_REQUEST_CODE) {

            LayoutUtil.showRegularLayout(this);

        } else if (requestCode == MercadoPago.GUESSING_CARD_REQUEST_CODE){

            if(resultCode == RESULT_OK){
                PaymentMethod paymentMethod = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);
                Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra("issuer"), Issuer.class);
                CardToken cardToken = JsonUtil.getInstance().fromJson(data.getStringExtra("cardToken"), CardToken.class);

                createTokenAsyncAndPay(paymentMethod, issuer, cardToken);

            } else {

                if ((data != null) && (data.getStringExtra("apiException") != null)) {
                    Toast.makeText(getApplicationContext(), data.getStringExtra("apiException"), Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void createTokenAsyncAndPay(final PaymentMethod paymentMethod, final Issuer issuer, CardToken cardToken) {
        LayoutUtil.showProgressLayout(this);
        MercadoPago mercadoPago = new MercadoPago.Builder()
                .setContext(this)
                .setPublicKey(ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY)
                .build();


        mercadoPago.createToken(cardToken, new Callback<Token>() {
            @Override
            public void success(Token token, Response response) {
                LayoutUtil.showRegularLayout(mActivity);
                Long issuerId = null;
                if (issuer != null)
                    issuerId = issuer.getId();

                ExamplesUtils.createPayment(mActivity, token.getId(),
                        1, issuerId, paymentMethod, null);
            }

            @Override
            public void failure(RetrofitError error) {
                ApiUtil.finishWithApiException(mActivity, error);
            }
        });
    }

    public void submitSimpleForm(View view) {
        if(mCheckBoxEnableCardScanner.isChecked()){
            setCardScannerPreference();
        }
        // Call payment methods activity
        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey(ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY)
                .setSupportedPaymentTypes(mSupportedPaymentTypes)
                .startPaymentMethodsActivity();
    }

    public void submitGuessingForm(View view) {
        if(mCheckBoxEnableCardScanner.isChecked()){
            setCardScannerPreference();
        }
        ExamplesUtils.startGuessingCardActivity(this, ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY, true, mCardScannerPreference);
    }

    private void setCardScannerPreference() {
        mCardScannerPreference = new CardScannerPreference(R.color.red, true);
    }
}