package com.mercadopago.examples.step1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mercadopago.adapters.IdentificationTypesAdapter;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.examples.R;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Token;
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
    private PaymentMethod mPaymentMethod;
    private Issuer mIssuer;

    // Input controls
    private EditText mCardHolderName;
    private EditText mCardNumber;
    private TextView mExpiryError;
    private EditText mExpiryMonth;
    private EditText mExpiryYear;
    private RelativeLayout mIdentificationLayout;
    private EditText mIdentificationNumber;
    private Spinner mIdentificationType;
    private EditText mSecurityCode;

    private ImageView mImagePaymentMethod;
    private Spinner mSpinnerIssuers;

    // Current values
    private CardToken mCardToken;
    private TextView mIssuerLabel;

    // Local vars
    private Activity mActivity;
    private String mExceptionOnMethod;
    private MercadoPago mMercadoPago;
    private String mMerchantPublicKey;


    private String mSavedBin;
    private boolean mCardNumberBlocked;
    private List<String> mSupportedTypes = new ArrayList<String>(){{
        add("credit_card");
        add("debit_card");
    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_guessing_card);

        initialize();
        getIdentificationTypesAsync();
    }

    private void initialize() {

        mActivity = this;
        mSavedBin = "";
        mCardNumberBlocked = false;
        // Get activity parameters
        mMerchantPublicKey = this.getIntent().getStringExtra("merchantPublicKey");

        // Set input controls
        mImagePaymentMethod = (ImageView) findViewById(R.id.pmImage);
        mCardNumber = (EditText) findViewById(R.id.cardNumber);
        mSecurityCode = (EditText) findViewById(R.id.securityCode);
        mCardHolderName = (EditText) findViewById(R.id.cardholderName);
        mIdentificationNumber = (EditText) findViewById(R.id.identificationNumber);
        mIdentificationType = (Spinner) findViewById(R.id.identificationType);
        mIdentificationLayout = (RelativeLayout) findViewById(R.id.identificationLayout);
        mExpiryError = (TextView) findViewById(R.id.expiryError);
        mExpiryMonth = (EditText) findViewById(R.id.expiryMonth);
        mExpiryYear = (EditText) findViewById(R.id.expiryYear);

        mIssuerLabel = (TextView) findViewById(R.id.lblIssuer);
        mIssuerLabel.setVisibility(View.GONE);

        mSpinnerIssuers = (Spinner) findViewById(R.id.spinnerIssuer);
        mSpinnerIssuers.setVisibility(View.GONE);

        // Init MercadoPago object with public key
        mMercadoPago = new MercadoPago.Builder()
                .setContext(mActivity)
                .setPublicKey(mMerchantPublicKey)
                .build();

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

        mCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable cardNumber) {
                if (cardNumber.length() < 6 && mCardNumberBlocked)
                    unBlockCardNumbersInput(mCardNumber);
                guessData(cardNumber);
            }
        });
        mCardNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(mCardNumberBlocked)
                    setInvalidCardNumberError();
                return false;
            }
        });

    }


    private void guessData(Editable cardNumber) {
        if(cardNumber.length() >= MercadoPago.BIN_LENGTH) {
            String newBin = cardNumber.subSequence(0, MercadoPago.BIN_LENGTH).toString();
            if(!newBin.equals(mSavedBin)) {
                mSavedBin = newBin;
                getPaymentMethodsAsync();
            }
        }
        else {
            clearGuessing();
        }
    }

    private void clearGuessing() {
        mSavedBin = "";
        mImagePaymentMethod.setImageResource(android.R.color.transparent);
        mSpinnerIssuers.setVisibility(View.GONE);
        mIssuerLabel.setVisibility(View.GONE);
    }

    private void getPaymentMethodsAsync() {

        mMercadoPago.getPaymentMethods(new Callback<List<PaymentMethod>>() {
            @Override
            public void success(List<PaymentMethod> paymentMethods, Response response) {
                List<PaymentMethod> validPaymentMethodsForBin = mMercadoPago.getPaymentMethodsForBin(mSavedBin, paymentMethods, mSupportedTypes);
                showPaymentMethod(validPaymentMethodsForBin);
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }

    private void showPaymentMethod(List<PaymentMethod> paymentMethods) {
        if(paymentMethods.isEmpty()) {
            blockCardNumbersInput(mCardNumber);
            this.setInvalidCardNumberError();
        }
        else if(paymentMethods.size() == 1){
            mPaymentMethod = paymentMethods.get(0);
            mImagePaymentMethod.setImageResource(MercadoPagoUtil.getPaymentMethodIcon(this, mPaymentMethod.getId()));
            if(mPaymentMethod.isIssuerRequired())
            {
                getIssuersAsync();
            }
        }
        else{
            //TODO: Define what to do.
        }
    }

    private void blockCardNumbersInput(EditText text) {
        int maxLength = MercadoPago.BIN_LENGTH;
        setInputMaxLenght(text, maxLength);
        mCardNumberBlocked = true;
    }

    private void unBlockCardNumbersInput(EditText text){

        int maxLength = 16;
        setInputMaxLenght(text, maxLength);
        mCardNumberBlocked = false;
    }

    private void setInputMaxLenght(EditText text, int maxLength) {
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        text.setFilters(fArray);
    }

    private void getIssuersAsync() {
        if(mPaymentMethod != null)
        {
            mMercadoPago.getIssuers(mPaymentMethod.getId(), new Callback<List<Issuer>>() {
                @Override
                public void success(List<Issuer> issuers, Response response) {
                    populateIssuersSpinner(issuers);
                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }
            });
        }
    }

    private void populateIssuersSpinner(final List<Issuer> issuers) {

        mIssuerLabel.setVisibility(View.VISIBLE);
        mSpinnerIssuers.setVisibility(View.VISIBLE);
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
        issuerNames.add(0, getString(R.string.spinner_default_value));
        for(Issuer issuer : issuers)
            issuerNames.add(issuer.getName());
        issuers.add(0, null);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.layout_spinner_item, issuerNames);
        mSpinnerIssuers.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("backButtonPressed", true);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void refreshLayout(View view) {

        if (mExceptionOnMethod.equals("getIdentificationTypesAsync")) {
            getIdentificationTypesAsync();
        } else if (mExceptionOnMethod.equals("createTokenAsync")) {
            createTokenAsync();
        }
    }

    public void submitForm(View view) {

        LayoutUtil.hideKeyboard(mActivity);

        // Set card token
        mCardToken = new CardToken(getCardNumber(), getMonth(), getYear(), getSecurityCode(), getCardHolderName(),
                getIdentificationTypeId(getIdentificationType()), getIdentificationNumber());

        if (validateForm(mCardToken)) {
            // Create token
            createTokenAsync();
        }
    }

    private boolean validateForm(CardToken cardToken) {

        boolean result = true;
        boolean focusSet = false;

        // Validate card number
        try {
            validateCardNumber(cardToken);
            mCardNumber.setError(null);
        } catch (Exception ex) {
            mCardNumber.setError(ex.getMessage());
            mCardNumber.requestFocus();
            result = false;
            focusSet = true;
        }

        // Validate security code
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
            mCardHolderName.setError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
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
            if (!cardToken.validateIdentificationNumber()) {
                mIdentificationNumber.setError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
                if (!focusSet) {
                    mIdentificationNumber.requestFocus();
                }
                result = false;
            } else {
                mIdentificationNumber.setError(null);
            }
        }

        if(mPaymentMethod != null) {
            if (mPaymentMethod.isIssuerRequired()) {
                if (mIssuer == null) {
                    TextView errorText = (TextView)mSpinnerIssuers.getSelectedView();
                    errorText.setError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
                    errorText.setText("my actual error text");
                    result = false;
                }
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

    private void setInvalidCardNumberError() {
        mCardNumber.setError(getString(com.mercadopago.R.string.mpsdk_invalid_card_luhn));
    }

    private void getIdentificationTypesAsync() {

        LayoutUtil.showProgressLayout(mActivity);

        mMercadoPago.getIdentificationTypes(new Callback<List<IdentificationType>>() {
            @Override
            public void success(List<IdentificationType> identificationTypes, Response response) {

                mIdentificationType.setAdapter(new IdentificationTypesAdapter(mActivity, identificationTypes));

                // Set form "Go" button
                setFormGoButton(mIdentificationNumber);

                LayoutUtil.showRegularLayout(mActivity);
            }

            @Override
            public void failure(RetrofitError error) {

                if ((error.getResponse() != null) && (error.getResponse().getStatus() == 404)) {

                    // No identification type for this country
                    mIdentificationLayout.setVisibility(View.GONE);

                    // Set form "Go" button
                    setFormGoButton(mCardHolderName);

                    LayoutUtil.showRegularLayout(mActivity);

                } else {

                    mExceptionOnMethod = "getIdentificationTypesAsync";
                    ApiUtil.finishWithApiException(mActivity, error);
                }
            }
        });
    }

    private void createTokenAsync() {

        LayoutUtil.showProgressLayout(mActivity);

        mMercadoPago.createToken(mCardToken, new Callback<Token>() {
            @Override
            public void success(Token token, Response response) {

                Intent returnIntent = new Intent();
                returnIntent.putExtra("token", token.getId());
                returnIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
                setResult(RESULT_OK, returnIntent);
                finish();
            }

            @Override
            public void failure(RetrofitError error) {

                mExceptionOnMethod = "createTokenAsync";
                ApiUtil.finishWithApiException(mActivity, error);
            }
        });
    }

    private void setFormGoButton(final EditText editText) {

        editText.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    submitForm(v);
                }
                return false;
            }
        });
    }

    private void setIdentificationNumberKeyboardBehavior() {

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

    private void setErrorTextCleaner(final EditText editText) {

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

    private String getCardNumber() {

        return this.mCardNumber.getText().toString();
    }

    private String getSecurityCode() {

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

    private String getCardHolderName() {

        return this.mCardHolderName.getText().toString();
    }

    private IdentificationType getIdentificationType() {

        return (IdentificationType) mIdentificationType.getSelectedItem();
    }

    private String getIdentificationTypeId(IdentificationType identificationType) {

        if (identificationType != null) {
            return identificationType.getId();
        } else {
            return null;
        }
    }

    private String getIdentificationNumber() {

        if (!this.mIdentificationNumber.getText().toString().equals("")) {
            return this.mIdentificationNumber.getText().toString();
        } else {
            return null;
        }
    }

}
