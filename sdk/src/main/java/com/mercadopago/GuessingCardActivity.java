package com.mercadopago;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mercadopago.adapters.IdentificationTypesAdapter;
import com.mercadopago.controlers.GuessingCardNumberControler;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GuessingCardActivity extends AppCompatActivity {
    // Activity parameters
    protected String mKey;
    protected String mKeyType;
    protected Boolean mRequireSecurityCode;
    protected Boolean mRequireIssuer;

    // Input controls
    protected EditText mCardHolderName;
    protected TextView mCVVDescriptor;
    protected ImageView mCVVImage;
    protected TextView mExpiryError;
    protected EditText mExpiryMonth;
    protected EditText mExpiryYear;
    protected EditText mIdentificationNumber;
    protected RelativeLayout mIdentificationLayout;
    protected Spinner mIdentificationType;
    protected Spinner mSpinnerIssuers;
    protected RelativeLayout mSecurityCodeLayout;
    protected LinearLayout mIssuerLayout;
    protected EditText mSecurityCode;

    protected GuessingCardNumberControler mGuessingCardNumberControler;

    // Local vars
    protected Activity mActivity;
    private List<String> mSupportedTypes = new ArrayList<String>(){{
        add("credit_card");
        add("debit_card");
    }};
    private PaymentMethod mPaymentMethod;
    private Issuer mIssuer;
    private MercadoPago mMercadoPago;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView();

        mActivity = this;

        // Get activity parameters
        mKeyType = this.getIntent().getStringExtra("keyType");
        mKey = this.getIntent().getStringExtra("key");
        mRequireSecurityCode = this.getIntent().getBooleanExtra("requireSecurityCode", true);
        mRequireIssuer = this.getIntent().getBooleanExtra("requireIssuer", true);

        if ((mKeyType == null) || (mKey == null)) {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            return;
        }

        mMercadoPago = new MercadoPago.Builder()
                .setContext(mActivity)
                .setKey(mKey, mKeyType)
                .build();


        // Set input controls

        mCardHolderName = (EditText) findViewById(R.id.cardholderName);
        mIdentificationNumber = (EditText) findViewById(R.id.identificationNumber);
        mIdentificationType = (Spinner) findViewById(R.id.identificationType);
        mIdentificationLayout = (RelativeLayout) findViewById(R.id.identificationLayout);
        mSecurityCodeLayout = (RelativeLayout) findViewById(R.id.securityCodeLayout);
        mIssuerLayout = (LinearLayout) findViewById(R.id.issuerLayout);
        mCVVImage = (ImageView) findViewById(R.id.cVVImage);
        mCVVDescriptor = (TextView) findViewById(R.id.cVVDescriptor);
        mSecurityCode = (EditText) findViewById(R.id.securityCode);
        mExpiryError = (TextView) findViewById(R.id.expiryError);
        mExpiryMonth = (EditText) findViewById(R.id.expiryMonth);
        mExpiryYear = (EditText) findViewById(R.id.expiryYear);
        mSpinnerIssuers = (Spinner) findViewById(R.id.spinnerIssuer);
        mGuessingCardNumberControler = new GuessingCardNumberControler(this, mMercadoPago, mSupportedTypes,
                new GuessingCardNumberControler.PaymentMethodCallback(){
                    @Override
                    public void onPaymentMethodSet(PaymentMethod paymentMethod) {
                        mPaymentMethod = paymentMethod;
                        setSecurityCodeLayout();
                        setIssuerLayout();
                    }

                    @Override
                    public void onPaymentMethodClearedOrChanged() {
                        mPaymentMethod = null;
                        if(mRequireSecurityCode)
                            setSecurityCodeHelpForPaymentMethod(null);
                        if(mRequireIssuer)
                            setIssuerLayout();
                    }
                });

        setSecurityCodeLayout();
        setIssuerLayout();
        // Set identification type listener to control identification number keyboard
        setIdentificationNumberKeyboardBehavior();
        // Error text cleaning hack
        setErrorTextCleaner(mCardHolderName);

        // Set up expiry edit texts
        mExpiryMonth.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                mExpiryError.setError(null);
                return false;
            }
        });
        mExpiryYear.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                mExpiryError.setError(null);
                return false;
            }
        });

        getIdentificationTypesAsync();
    }

    protected void setContentView() {

        setContentView(R.layout.activity_guessing_card);
    }

    @Override
    public void onBackPressed() {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("backButtonPressed", true);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void refreshLayout(View view) {

        getIdentificationTypesAsync();
    }

    public void submitForm(View view) {

        LayoutUtil.hideKeyboard(mActivity);

        // Set card token
        CardToken cardToken = new CardToken(getCardNumber(), getMonth(), getYear(), getSecurityCode(), getCardHolderName(),
                getIdentificationTypeId(getIdentificationType()), getIdentificationNumber());

        if (validateForm(cardToken)) {
            // Return to parent
            Intent returnIntent = new Intent();
            returnIntent.putExtra("cardToken", JsonUtil.getInstance().toJson(cardToken));
            returnIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
            if(mRequireIssuer)
                returnIntent.putExtra("issuer", JsonUtil.getInstance().toJson(mIssuer));
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    protected boolean validateForm(CardToken cardToken) {

        boolean result = true;
        boolean focusSet = false;

        // Validate card number
        try {
            validateCardNumber(cardToken);
            mGuessingCardNumberControler.setCardNumberError(null);
        }
        catch (Exception ex) {
            mGuessingCardNumberControler.setCardNumberError(ex.getMessage());
            mGuessingCardNumberControler.requestFocusForCardNumber();
            result = false;
            focusSet = true;
        }

        // Validate security code
        if (mRequireSecurityCode) {
            try {
                validateSecurityCode(cardToken);
                mSecurityCode.setError(null);
            } catch (Exception ex) {
                mSecurityCode.setError(ex.getMessage());
                if (!focusSet) {
                    mSecurityCode.requestFocus();
                    focusSet = true;
                }
                result = false;
            }
        }

        // Validate expiry month and year
        if (!cardToken.validateExpiryDate()) {
            mExpiryError.setVisibility(View.VISIBLE);
            mExpiryError.setError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
            if (!focusSet) {
                mExpiryMonth.requestFocus();
                focusSet = true;
            }
            result = false;
        } else {
            mExpiryError.setError(null);
            mExpiryError.setVisibility(View.GONE);
        }

        // Validate card holder name
        if (!cardToken.validateCardholderName()) {
            mCardHolderName.setError(getString(R.string.mpsdk_invalid_field));
            if (!focusSet) {
                mCardHolderName.requestFocus();
                focusSet = true;
            }
            result = false;
        } else {
            mCardHolderName.setError(null);
        }

        // Validate identification number
        if (getIdentificationType() != null) {
            if (!cardToken.validateIdentificationNumber(getIdentificationType())) {
                mIdentificationNumber.setError(getString(R.string.mpsdk_invalid_field));
                if (!focusSet) {
                    mIdentificationNumber.requestFocus();
                    focusSet = true;
                }
                result = false;
            } else {
                mIdentificationNumber.setError(null);
            }
        }

        if(mPaymentMethod == null){
            mGuessingCardNumberControler.setCardNumberError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
            if(!focusSet) {
                mGuessingCardNumberControler.requestFocusForCardNumber();
            }
            result = false;
        }
        else if(mRequireIssuer){
            if(mPaymentMethod.isIssuerRequired() && mIssuer == null){
                TextView errorText = (TextView)mSpinnerIssuers.getSelectedView();
                errorText.setError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
                result = false;
            }
        }

        return result;
    }

    protected void validateCardNumber(CardToken cardToken) throws Exception {

        cardToken.validateCardNumber(this, mPaymentMethod);
    }

    protected void validateSecurityCode(CardToken cardToken) throws Exception {

        cardToken.validateSecurityCode(this, mPaymentMethod);
    }

    protected void getIdentificationTypesAsync() {

        LayoutUtil.showProgressLayout(mActivity);

        mMercadoPago.getIdentificationTypes(new Callback<List<IdentificationType>>() {
            @Override
            public void success(List<IdentificationType> identificationTypes, Response response) {

                mIdentificationType.setAdapter(new IdentificationTypesAdapter(mActivity, identificationTypes));

                // Set form "Go" button
                if (mSecurityCodeLayout.getVisibility() == View.GONE) {
                    setFormGoButton(mIdentificationNumber);
                }

                LayoutUtil.showRegularLayout(mActivity);
            }

            @Override
            public void failure(RetrofitError error) {

                if ((error.getResponse() != null) && (error.getResponse().getStatus() == 404)) {

                    // No identification type for this country
                    mIdentificationLayout.setVisibility(View.GONE);

                    // Set form "Go" button
                    if (mSecurityCodeLayout.getVisibility() == View.GONE) {
                        setFormGoButton(mCardHolderName);
                    }

                    LayoutUtil.showRegularLayout(mActivity);

                } else {

                    ApiUtil.finishWithApiException(mActivity, error);
                }
            }
        });
    }

    protected void setFormGoButton(final EditText editText) {

        editText.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    submitForm(v);
                    return true;
                }
                return false;
            }
        });
    }

    protected void setIdentificationNumberKeyboardBehavior() {

        mIdentificationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                IdentificationType identificationType = getIdentificationType();
                if (identificationType != null) {
                    if (identificationType.getType().equals("number")) {
                        mIdentificationNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                    } else {
                        mIdentificationNumber.setInputType(InputType.TYPE_CLASS_TEXT);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    protected void setErrorTextCleaner(final EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable edt) {
                if (editText.getText().length() > 0) {
                    editText.setError(null);
                }
            }
        });
    }

    protected void setSecurityCodeLayout() {

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

    private void setIssuerLayout() {
        if (!mRequireIssuer || mPaymentMethod == null || !mPaymentMethod.isIssuerRequired())
            mIssuerLayout.setVisibility(View.GONE);
        else {
            getIssuersAsync();
            mIssuerLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getIssuersAsync() {
        if(mPaymentMethod != null)
        {
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

    private void populateIssuerSpinner(final List<Issuer> issuers) {

        //TODO: Adapater
        mSpinnerIssuers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mIssuer = issuers.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mIssuer = null;
            }
        });


        List<String> issuerNames = new ArrayList<>();
        issuerNames.add(0, getString(R.string.mpsdk_select_issuer_label));
        for(Issuer issuer : issuers)
            issuerNames.add(issuer.getName());

        //Add null issuer to match spinner positions
        issuers.add(0, null);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_spinner_item, issuerNames);
        mSpinnerIssuers.setAdapter(adapter);
    }

    private void setSecurityCodeHelpForPaymentMethod(PaymentMethod paymentMethod) {
        if(paymentMethod != null) {
            mCVVDescriptor.setText(MercadoPagoUtil.getCVVDescriptor(this, this.mPaymentMethod));
            mCVVImage.setImageDrawable(ContextCompat.getDrawable(this, MercadoPagoUtil.getCVVImageResource(this, this.mPaymentMethod)));
        }
        else {
            mCVVDescriptor.setText("");
            mCVVImage.setImageDrawable(null);
        }
    }

    protected String getCardNumber() {

        return mGuessingCardNumberControler.getCardNumberText();
    }

    protected String getSecurityCode() {

        return this.mSecurityCode.getText().toString();
    }

    private Integer getMonth() {

        Integer result;
        try {
            result = Integer.parseInt(this.mExpiryMonth.getText().toString());
        } catch (Exception ex) {
            result = null;
        }
        return result;
    }

    private Integer getYear() {

        Integer result;
        try {
            result = Integer.parseInt(this.mExpiryYear.getText().toString());
        } catch (Exception ex) {
            result = null;
        }
        return result;
    }

    protected String getCardHolderName() {

        return this.mCardHolderName.getText().toString();
    }

    protected IdentificationType getIdentificationType() {

        return (IdentificationType) mIdentificationType.getSelectedItem();
    }

    protected String getIdentificationTypeId(IdentificationType identificationType) {

        if (identificationType != null) {
            return identificationType.getId();
        } else {
            return null;
        }
    }

    protected String getIdentificationNumber() {

        if (!this.mIdentificationNumber.getText().toString().equals("")) {
            return this.mIdentificationNumber.getText().toString();
        } else {
            return null;
        }
    }
}
