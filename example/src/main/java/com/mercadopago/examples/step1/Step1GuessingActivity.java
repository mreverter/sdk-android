package com.mercadopago.examples.step1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mercadopago.ExampleActivity;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.examples.R;
import com.mercadopago.examples.utils.ExamplesUtils;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class Step1GuessingActivity extends ExampleActivity {

    protected List<String> mSupportedPaymentTypes = new ArrayList<String>(){{
        add("credit_card");
        add("debit_card");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1_guessing);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MercadoPago.GUESSING_CARD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                CardToken cardToken = JsonUtil.getInstance().fromJson(data.getStringExtra("cardToken"), CardToken.class);
                PaymentMethod paymentMethod = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);
                Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra("issuer"), Issuer.class);

                StringBuilder message = new StringBuilder();
                message.append(paymentMethod.getName());
                if(issuer!=null)
                    message.append(" con ").append(issuer.getName());

                Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();

            } else {

                if ((data != null) && (data.getStringExtra("apiException") != null)) {
                    Toast.makeText(getApplicationContext(), data.getStringExtra("apiException"), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void submitForm(View view) {
        ExamplesUtils.startGuessingCardActivity(this, ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY);
    }
}
