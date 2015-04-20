package com.mercadopago;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mercadopago.adapters.IdentificationTypesAdapter;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.dialogs.CustomDatePickerDialog;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;

import java.util.Calendar;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NewCardActivity extends ActionBarActivity implements CustomDatePickerDialog.DatePickerDialogListener {

    // Activity parameters
    protected String mKey;
    protected String mKeyType;
    protected PaymentMethod mPaymentMethod;
    protected Boolean mRequireSecurityCode;

    // Input controls
    protected EditText mCardHolderName;
    protected EditText mCardNumber;
    protected TextView mCVVDescriptor;
    protected ImageView mCVVImage;
    protected Button mExpiryDate;
    protected EditText mIdentificationNumber;
    protected RelativeLayout mIdentificationLayout;
    protected Spinner mIdentificationType;
    protected RelativeLayout mSecurityCodeLayout;
    protected EditText mSecurityCode;

    // Current values
    protected int mExpiryMonth;
    protected int mExpiryYear;
    protected Calendar mSelectedExpiryDate;

    // Local vars
    protected Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView();

        mActivity = this;

        // Get activity parameters
        String paymentMethod = this.getIntent().getStringExtra("paymentMethod");
        mKeyType = this.getIntent().getStringExtra("keyType");
        mKey = this.getIntent().getStringExtra("key");
        mRequireSecurityCode = this.getIntent().getBooleanExtra("requireSecurityCode", true);
        if ((paymentMethod == null) || (mKeyType == null) || (mKey == null)) {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            return;
        }
        mPaymentMethod = JsonUtil.getInstance().fromJson(paymentMethod, PaymentMethod.class);

        // Set input controls
        mCardNumber = (EditText) findViewById(R.id.cardNumber);
        mCardHolderName = (EditText) findViewById(R.id.cardholderName);
        mIdentificationNumber = (EditText) findViewById(R.id.identificationNumber);
        mIdentificationType = (Spinner) findViewById(R.id.identificationType);
        mIdentificationLayout = (RelativeLayout) findViewById(R.id.identificationLayout);
        mExpiryDate = (Button) findViewById(R.id.expiryDateButton);
        mSecurityCodeLayout = (RelativeLayout) findViewById(R.id.securityCodeLayout);
        mCVVImage = (ImageView) findViewById(R.id.cVVImage);
        mCVVDescriptor = (TextView) findViewById(R.id.cVVDescriptor);
        mSecurityCode = (EditText) findViewById(R.id.securityCode);

        // Set identification type listener to control identification number keyboard
        setIdentificationNumberKeyboardBehavior();

        // Error text cleaning hack
        setErrorTextCleaner(mCardHolderName);

        // Get identification types
        getIdentificationTypesAsync();

        // Set payment method image
        if (mPaymentMethod.getId() != null) {
            ImageView pmImage = (ImageView) findViewById(R.id.pmImage);
            if (pmImage != null) {
                pmImage.setImageResource(MercadoPagoUtil.getPaymentMethodIcon(this, mPaymentMethod.getId()));
            }
        }

        // Set security code visibility
        setSecurityCodeLayout();
    }

    protected void setContentView() {

        setContentView(R.layout.activity_new_card);
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

    @Override
    public void onDateSet(DialogFragment dialog, int month, int year) {

        // Set local attributes
        mExpiryMonth = month;
        mExpiryYear = year;
        mSelectedExpiryDate = Calendar.getInstance();
        mSelectedExpiryDate.set(year, month - 1, Calendar.DAY_OF_WEEK);

        // Set expiry date label
        String monthString = month < 10 ? "0" + month : Integer.toString(month);
        String yearString = Integer.toString(year).substring(2);
        mExpiryDate.setText(new StringBuilder()
                .append(monthString).append(" / ").append(yearString));

        // Validate new value
        if (CardToken.validateExpiryDate(mExpiryMonth, mExpiryYear)) {
            mExpiryDate.setError(null);
        } else {
            mExpiryDate.setError(getString(R.string.invalid_field));
        }
    }

    public void popExpiryDate(View view) {

        DialogFragment newFragment = new CustomDatePickerDialog();
        Bundle args = new Bundle();
        args.putSerializable(CustomDatePickerDialog.CALENDAR, mSelectedExpiryDate);
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "");
    }

    public void submitForm(View view) {

        LayoutUtil.hideKeyboard(mActivity);

        // Set card token
        CardToken cardToken = new CardToken(getCardNumber(), getMonth(), getYear(), getSecurityCode(), getCardHolderName(),
                getIdentificationTypeId(getIdentificationType()), getIdentificationNumber());

        if (validateForm(cardToken)) {
            // Return to parent
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            returnIntent.putExtra("cardToken", JsonUtil.getInstance().toJson(cardToken));
            finish();
        }
    }

    protected boolean validateForm(CardToken cardToken) {

        boolean result = true;
        boolean focusSet = false;

        // Validate card number
        try {
            validateCardNumber(cardToken);
            mCardNumber.setError(null);
        }
        catch (Exception ex) {
            mCardNumber.setError(ex.getMessage());
            mCardNumber.requestFocus();
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

        // Validate expiry date
        if (!cardToken.validateExpiryDate()) {
            mExpiryDate.setError(getString(R.string.invalid_field));
            result = false;
        } else {
            mExpiryDate.setError(null);
        }

        // Validate card holder name
        if (!cardToken.validateCardholderName()) {
            mCardHolderName.setError(getString(R.string.invalid_field));
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
                mIdentificationNumber.setError(getString(R.string.invalid_field));
                if (!focusSet) {
                    mIdentificationNumber.requestFocus();
                }
                result = false;
            } else {
                mIdentificationNumber.setError(null);
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

        MercadoPago mercadoPago = new MercadoPago.Builder()
                .setContext(mActivity)
                .setKey(mKey, mKeyType)
                .build();

        mercadoPago.getIdentificationTypes(new Callback<List<IdentificationType>>() {
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

        if (mPaymentMethod != null) {

            if (mRequireSecurityCode) {

                // Set CVV descriptor
                mCVVDescriptor.setText(MercadoPagoUtil.getCVVDescriptor(this, mPaymentMethod));

                // Set CVV image
                mCVVImage.setImageDrawable(getResources().getDrawable(MercadoPagoUtil.getCVVImageResource(this, mPaymentMethod)));

                // Set layout visibility
                mSecurityCodeLayout.setVisibility(View.VISIBLE);
                setFormGoButton(mSecurityCode);

                return;
            }
        }
        mSecurityCodeLayout.setVisibility(View.GONE);
    }

    protected String getCardNumber() {

        return this.mCardNumber.getText().toString();
    }

    protected String getSecurityCode() {

        return this.mSecurityCode.getText().toString();
    }

    protected Integer getMonth() {

        return mExpiryMonth;
    }

    protected Integer getYear() {

        return mExpiryYear;
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
