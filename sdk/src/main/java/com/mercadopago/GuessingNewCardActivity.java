package com.mercadopago;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.adapters.IssuersSpinnerAdapter;
import com.mercadopago.callbacks.PaymentMethodSelectionCallback;
import com.mercadopago.controllers.GuessingCardNumberController;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GuessingNewCardActivity extends NewCardActivity {

    //Activity parameters
    protected Boolean mRequireIssuer;
    protected Boolean mShowBankDeals;

    //Input controls
    protected Spinner mSpinnerIssuers;
    protected LinearLayout mIssuerLayout;
    protected GuessingCardNumberController mGuessingCardNumberController;


    //Local vars
    protected Issuer mIssuer;
    protected MercadoPago mMercadoPago;
    protected List<String> mSupportedPaymentTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get activity parameters
        if (this.getIntent().getStringExtra("supportedPaymentTypes") != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            mSupportedPaymentTypes = gson.fromJson(this.getIntent().getStringExtra("supportedPaymentTypes"), listType);
        }
        mShowBankDeals = this.getIntent().getBooleanExtra("showBankDeals", true);
        mRequireIssuer = this.getIntent().getBooleanExtra("requireIssuer", true);

        //Set input controls
        mIssuerLayout = (LinearLayout) findViewById(R.id.issuerSelectionLayout);
        mSpinnerIssuers = (Spinner) findViewById(R.id.spinnerIssuer);

        mMercadoPago = new MercadoPago.Builder()
                .setContext(mActivity)
                .setKey(mKey, mKeyType)
                .build();

        LayoutUtil.showProgressLayout(this);
        getPaymentMethodsAsync();

    }

    protected void getPaymentMethodsAsync() {
        mMercadoPago.getPaymentMethods(new Callback<List<PaymentMethod>>() {
            @Override
            public void success(List<PaymentMethod> paymentMethods, Response response) {
                initializeGuessingCardNumberController(paymentMethods);
                LayoutUtil.showRegularLayout(mActivity);
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }

    protected void initializeGuessingCardNumberController(List<PaymentMethod> paymentMethods) {

        List<PaymentMethod> supportedPaymentMethods = getSupportedPaymentMethods(paymentMethods);

        mGuessingCardNumberController = new GuessingCardNumberController(this, supportedPaymentMethods,
                new PaymentMethodSelectionCallback(){
                    @Override
                    public void onPaymentMethodSet(PaymentMethod paymentMethod) {
                        mPaymentMethod = paymentMethod;
                        setLayouts();
                    }

                    @Override
                    public void onPaymentMethodCleared() {
                        mPaymentMethod = null;
                        setLayouts();
                    }
                });
    }

    protected List<PaymentMethod> getSupportedPaymentMethods(List<PaymentMethod> paymentMethods) {

        if(mSupportedPaymentTypes != null) {
            List<PaymentMethod> supportedPaymentMethods = new ArrayList<>();
            for (PaymentMethod pm : paymentMethods) {
                if (mSupportedPaymentTypes.contains(pm.getPaymentTypeId()))
                    supportedPaymentMethods.add(pm);
            }
            return supportedPaymentMethods;
        }
        else return paymentMethods;

    }

    @Override
    protected void verifyValidCreate(){
        if ((mKeyType == null) || (mKey == null)) {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    @Override
    public void setPaymentMethodImage(){
        ImageView pmImage = (ImageView) findViewById(R.id.pmImage);
        if (pmImage != null) {
            pmImage.setImageDrawable(null);
        }
    }

    @Override
    public void setContentView()
    {
        setContentView(R.layout.activity_guessing_new_card);
    }

    @Override
    public boolean validateForm(CardToken cardToken)
    {
        boolean result = true;
        boolean requestFocus = true;


        if(!validateCardNumber(cardToken, requestFocus)) {
            result = false;
            requestFocus = false;
        }

        if(!validatePaymentMethod()){
            result = false;
        }

        if(!validateIssuer()) {
            result = false;
        }

        if (!validateSecurityCode(cardToken, requestFocus)) {
            result = false;
            requestFocus = false;
        }

        if (!validateExpiryDate(cardToken, requestFocus)) {
            result = false;
            requestFocus = false;
        }

        if (!validateCardHolderName(cardToken, requestFocus)) {
            result = false;
            requestFocus = false;
        }

        if(!validateIdentificationNumber(cardToken, requestFocus)) {
            result = false;
        }

        return result;
    }

    protected boolean validateIssuer() {
        if(mPaymentMethod != null && mRequireIssuer && mPaymentMethod.isIssuerRequired() && mIssuer == null)
        {
            IssuersSpinnerAdapter adapter = (IssuersSpinnerAdapter) mSpinnerIssuers.getAdapter();
            View view = mSpinnerIssuers.getSelectedView();
            adapter.setError(view, getString(com.mercadopago.R.string.mpsdk_invalid_field));
            mSpinnerIssuers.requestFocus();
            return false;
        }
        return true;
    }

    protected boolean validatePaymentMethod() {
        if(mGuessingCardNumberController.getCardNumberText().length() >= MercadoPago.BIN_LENGTH && mPaymentMethod == null)
        {
            mGuessingCardNumberController.setPaymentMethodError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
            return false;
        }
        return true;
    }

    @Override
    public boolean validateCardNumber(CardToken cardToken, boolean requestFocus)
    {
        boolean valid = true;
        if(mGuessingCardNumberController.getCardNumberText().equals(""))
        {
            mGuessingCardNumberController.setCardNumberError(getString(R.string.mpsdk_invalid_empty_card));

            if(requestFocus)
                mGuessingCardNumberController.requestFocusForCardNumber();
            valid = false;
        }
        else {
            try {
                validateCardNumber(cardToken);
            } catch (Exception ex) {
                mGuessingCardNumberController.setCardNumberError(ex.getMessage());
                if (requestFocus)
                    mGuessingCardNumberController.requestFocusForCardNumber();
                valid = false;
            }
        }
        return valid;
    }

    @Override
    public void setSecurityCodeLayout()
    {
        if (!mRequireSecurityCode)
            mSecurityCodeLayout.setVisibility(View.GONE);
        else if(mPaymentMethod == null)
            setSecurityCodeHelpForPaymentMethod(null);
        else {
            setSecurityCodeHelpForPaymentMethod(mPaymentMethod);
            // Set layout visibility
            mSecurityCodeLayout.setVisibility(View.VISIBLE);
            setFormGoButton(mSecurityCode);
        }
    }

    protected void setLayouts() {
        setSecurityCodeLayout();
        setIssuerLayout();
    }

    protected void setSecurityCodeHelpForPaymentMethod(PaymentMethod paymentMethod) {
        if(paymentMethod != null) {
            mCVVDescriptor.setText(MercadoPagoUtil.getCVVDescriptor(this, this.mPaymentMethod));
            mCVVImage.setImageDrawable(ContextCompat.getDrawable(this, MercadoPagoUtil.getCVVImageResource(this, this.mPaymentMethod)));
        }
        else {
            mCVVDescriptor.setText("");
            mCVVImage.setImageDrawable(null);
        }
    }

    protected void setIssuerLayout() {
        if (!mRequireIssuer || mPaymentMethod == null || !mPaymentMethod.isIssuerRequired())
            mIssuerLayout.setVisibility(View.GONE);
        else {
            getIssuersAsync();
            mIssuerLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void getIssuersAsync() {
        if (mPaymentMethod != null) {
            mMercadoPago.getIssuers(mPaymentMethod.getId(), new Callback<List<Issuer>>() {
                @Override
                public void success(List<Issuer> issuers, Response response) {
                    populateIssuerSpinner(issuers);
                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }
            });
        }
    }

    protected void populateIssuerSpinner(final List<Issuer> issuers) {

        mSpinnerIssuers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mIssuer = (Issuer) mSpinnerIssuers.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mIssuer = null;
            }
        });

        mSpinnerIssuers.setAdapter(new IssuersSpinnerAdapter(mActivity, issuers));
    }

    @Override
    public void submitForm(View view) {

        LayoutUtil.hideKeyboard(mActivity);

        // Set card token
        CardToken cardToken = new CardToken(getCardNumber(), getMonth(), getYear(), getSecurityCode(), getCardHolderName(),
                getIdentificationTypeId(getIdentificationType()), getIdentificationNumber());

        if (validateForm(cardToken)) {
            // Return to parent
            Intent returnIntent = new Intent();
            returnIntent.putExtra("cardToken", cardToken);
            returnIntent.putExtra("paymentMethod", mPaymentMethod);
            if(mRequireIssuer)
                returnIntent.putExtra("issuer", mIssuer);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

}
