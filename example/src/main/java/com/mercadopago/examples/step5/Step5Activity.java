package com.mercadopago.examples.step5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.mercadopago.ExampleActivity;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.examples.R;
import com.mercadopago.examples.utils.ExamplesUtils;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.mpcardio.CardScannerPreference;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;

import java.util.ArrayList;
import java.util.List;

public class Step5Activity extends ExampleActivity {

    protected List<String> mSupportedPaymentTypes = new ArrayList<String>(){{
        add("credit_card");
        add("debit_card");
        add("prepaid_card");
        add("atm");
        add("ticket");
    }};

    protected CardScannerPreference mCardScannerPreference;
    protected CheckBox mCheckBoxEnableCardScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step5);
        mCheckBoxEnableCardScanner = (CheckBox) findViewById(R.id.checkBoxEnableCardScanner);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MercadoPago.VAULT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Set issuer id
                Long issuerId = (data.getStringExtra("issuerId") != null)
                        ? Long.parseLong(data.getStringExtra("issuerId")) : null;

                // Set installments
                Integer installments = (data.getStringExtra("installments") != null)
                        ? Integer.parseInt(data.getStringExtra("installments")) : null;

                // Create payment
                ExamplesUtils.createPayment(this, data.getStringExtra("token"),
                        installments, issuerId,
                        JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class), null);

            } else {

                if ((data != null) && (data.getStringExtra("apiException") != null)) {
                    Toast.makeText(getApplicationContext(), data.getStringExtra("apiException"), Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == MercadoPago.CONGRATS_REQUEST_CODE) {

            LayoutUtil.showRegularLayout(this);
        }
    }

    public void submitSimpleForm(View view) {
        // Call final vault activity
        if(mCheckBoxEnableCardScanner.isChecked()) {
            setCardScannerPreference();
        }
        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey(ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY)
                .setMerchantBaseUrl(ExamplesUtils.DUMMY_MERCHANT_BASE_URL)
                .setMerchantGetCustomerUri(ExamplesUtils.DUMMY_MERCHANT_GET_CUSTOMER_URI)
                .setMerchantAccessToken(ExamplesUtils.DUMMY_MERCHANT_ACCESS_TOKEN)
                .setAmount(ExamplesUtils.DUMMY_ITEM_UNIT_PRICE)
                .setSupportedPaymentTypes(mSupportedPaymentTypes)
                .setShowBankDeals(true)
                .setCardScannerPreference(mCardScannerPreference)
                .startVaultActivity();
    }

    public void submitGuessingForm(View view){

        if(mCheckBoxEnableCardScanner.isChecked()) {
            setCardScannerPreference();
        }
        // Call final vault activity
        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey(ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY)
                .setMerchantBaseUrl(ExamplesUtils.DUMMY_MERCHANT_BASE_URL)
                .setMerchantGetCustomerUri(ExamplesUtils.DUMMY_MERCHANT_GET_CUSTOMER_URI)
                .setMerchantAccessToken(ExamplesUtils.DUMMY_MERCHANT_ACCESS_TOKEN)
                .setAmount(ExamplesUtils.DUMMY_ITEM_UNIT_PRICE)
                .setSupportedPaymentTypes(mSupportedPaymentTypes)
                .setShowBankDeals(true)
                .setCardScannerPreference(mCardScannerPreference)
                .setGuessingCardFormEnabled(true)
                .startVaultActivity();
    }

    private void setCardScannerPreference() {
        mCardScannerPreference = new CardScannerPreference(R.color.red, true);
    }

}
