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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.adapters.IdentificationTypesAdapter;
import com.mercadopago.adapters.IssuersSpinnerAdapter;
import com.mercadopago.adapters.PaymentMethodsSpinnerAdapter;
import com.mercadopago.callbacks.PaymentMethodSelectionCallback;
import com.mercadopago.controllers.GuessingCardNumberController;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;

import java.lang.reflect.Type;
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
    private Boolean mShowBankDeals;

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

    protected GuessingCardNumberController mGuessingCardNumberController;

    // Local vars
    protected Activity mActivity;
    private PaymentMethod mPaymentMethod;
    private Issuer mIssuer;
    private MercadoPago mMercadoPago;
    private List<String> mSupportedPaymentTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView();

        mActivity = this;

        // Get activity parameters
        mShowBankDeals = this.getIntent().getBooleanExtra("showBankDeals", true);
        mKeyType = this.getIntent().getStringExtra("keyType");
        mKey = this.getIntent().getStringExtra("key");
        mRequireSecurityCode = this.getIntent().getBooleanExtra("requireSecurityCode", true);
        mRequireIssuer = this.getIntent().getBooleanExtra("requireIssuer", true);

        if (this.getIntent().getStringExtra("supportedPaymentTypes") != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            mSupportedPaymentTypes = gson.fromJson(this.getIntent().getStringExtra("supportedPaymentTypes"), listType);
        }

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

        mGuessingCardNumberController = new GuessingCardNumberController(this, mMercadoPago, mSupportedPaymentTypes,
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

        setFocusOrder();

        getIdentificationTypesAsync();
    }


    private void setFocusOrder() {
        mExpiryMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mExpiryMonth.getText().length() == 2)
                {
                    mExpiryYear.requestFocus();
                }
            }
        });
        mExpiryYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mExpiryYear.getText().length() == 2)
                {
                    mCardHolderName.requestFocus();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mShowBankDeals) {
            getMenuInflater().inflate(R.menu.payment_methods, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_bank_deals) {
            new MercadoPago.StartActivityBuilder()
                    .setActivity(this)
                    .setPublicKey(mKey)
                    .startBankDealsActivity();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
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

        boolean valid = true;
        boolean focusSet = false;

        if(mGuessingCardNumberController.getCardNumberText().equals(""))
        {
            mGuessingCardNumberController.requestFocusForCardNumber();
            mGuessingCardNumberController.setCardNumberError(getString(R.string.mpsdk_invalid_empty_card));
            valid = false;
            focusSet = true;
        }
        else if(mPaymentMethod == null)
        {
            mGuessingCardNumberController.setPaymentMethodError(getString(com.mercadopago.R.string.mpsdk_invalid_field));
            focusSet = true;
            valid = false;

        }
        else{
            if(mRequireIssuer && mPaymentMethod.isIssuerRequired()) {
                if (mIssuer == null) {
                    IssuersSpinnerAdapter adapter = (IssuersSpinnerAdapter) mSpinnerIssuers.getAdapter();
                    View view = mSpinnerIssuers.getSelectedView();
                    adapter.setError(view, getString(com.mercadopago.R.string.mpsdk_invalid_field));
                    mSpinnerIssuers.requestFocus();
                    focusSet = true;
                    valid = false;
                }
            }
            // Validate card number
            try {
                validateCardNumber(cardToken);

            } catch (Exception ex) {
                mGuessingCardNumberController.setCardNumberError(ex.getMessage());
                mGuessingCardNumberController.requestFocusForCardNumber();
                focusSet = true;
                valid = false;
            }
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
                valid = false;
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
            valid = false;
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
            valid = false;
        } else {
            mCardHolderName.setError(null);
        }

        // Validate identification number
        if (getIdentificationType() != null) {
            if (!cardToken.validateIdentificationNumber(getIdentificationType())) {
                mIdentificationNumber.setError(getString(R.string.mpsdk_invalid_field));
                if (!focusSet) {
                    mIdentificationNumber.requestFocus();
                }
                valid = false;
            } else {
                mIdentificationNumber.setError(null);
            }
        }

        return valid;
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

    protected void setLayouts() {
        setSecurityCodeLayout();
        setIssuerLayout();
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

        return mGuessingCardNumberController.getCardNumberText();
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
