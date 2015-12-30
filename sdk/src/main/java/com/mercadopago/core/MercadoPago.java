package com.mercadopago.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.BankDealsActivity;
import com.mercadopago.CheckoutActivity;
import com.mercadopago.CongratsActivity;
import com.mercadopago.CustomerCardsActivity;
import com.mercadopago.InstallmentsActivity;
import com.mercadopago.IssuersActivity;
import com.mercadopago.PaymentMethodsActivity;
import com.mercadopago.VaultActivity;
import com.mercadopago.model.BankDeal;
import com.mercadopago.model.Card;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.CheckoutPreference;
import com.mercadopago.model.Customer;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.Installment;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentIntent;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodPreference;
import com.mercadopago.model.SavedCardToken;
import com.mercadopago.model.Token;
import com.mercadopago.services.BankDealService;
import com.mercadopago.services.CustomerService;
import com.mercadopago.services.GatewayService;
import com.mercadopago.services.IdentificationService;
import com.mercadopago.services.PaymentService;
import com.mercadopago.util.HttpClientUtil;
import com.mercadopago.util.JsonUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class MercadoPago {

    public static final String KEY_TYPE_PUBLIC = "public_key";
    public static final String KEY_TYPE_PRIVATE = "private_key";

    public static final int CUSTOMER_CARDS_REQUEST_CODE = 0;
    public static final int PAYMENT_METHODS_REQUEST_CODE = 1;
    public static final int INSTALLMENTS_REQUEST_CODE = 2;
    public static final int ISSUERS_REQUEST_CODE = 3;
    public static final int NEW_CARD_REQUEST_CODE = 4;
    public static final int CONGRATS_REQUEST_CODE = 5;
    public static final int VAULT_REQUEST_CODE = 6;
    public static final int BANK_DEALS_REQUEST_CODE = 7;
    public static final int CHECKOUT_REQUEST_CODE = 8;
    public static final int INSTALL_APP_REQUEST_CODE = 9;
    public static final int GUESSING_CARD_REQUEST_CODE = 10;

    public static final int BIN_LENGTH = 6;

    private static final String MP_API_BASE_URL = "https://api.mercadopago.com";
    private String mKey = null;
    private String mKeyType = null;
    private Context mContext = null;
    private RestAdapter mRestAdapterMPApi;

    private MercadoPago(Builder builder) {

        this.mContext = builder.mContext;
        this.mKey = builder.mKey;
        this.mKeyType = builder.mKeyType;

        System.setProperty("http.keepAlive", "false");

        mRestAdapterMPApi = new RestAdapter.Builder()
                .setEndpoint(MP_API_BASE_URL)
                .setLogLevel(Settings.RETROFIT_LOGGING)
                .setConverter(new GsonConverter(JsonUtil.getInstance().getGson()))
                .setClient(HttpClientUtil.getClient(this.mContext))
                .build();
    }

    public void createPayment(PaymentIntent paymentIntent, final Callback<Payment> callback) {

        // TODO: Reemplazar por servicio final
        //PaymentService service = mRestAdapterMPApi.create(PaymentService.class);
        RestAdapter restAdapterBeta = new RestAdapter.Builder()
                .setEndpoint("https://mp-android-sdk.herokuapp.com/")
                .setLogLevel(Settings.RETROFIT_LOGGING)
                .setConverter(new GsonConverter(JsonUtil.getInstance().getGson()))
                .setClient(HttpClientUtil.getClient(this.mContext))
                .build();
        PaymentService service = restAdapterBeta.create(PaymentService.class);
        service.createPayment(paymentIntent, callback);
    }

    public void createToken(final SavedCardToken savedCardToken, final Callback<Token> callback) {

        if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
            savedCardToken.setDevice(mContext);
            GatewayService service = mRestAdapterMPApi.create(GatewayService.class);
            service.getToken(this.mKey, savedCardToken, callback);
        } else {
            throw new RuntimeException("Unsupported key type for this method");
        }
    }

    public void createToken(final CardToken cardToken, final Callback<Token> callback) {

        if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
            cardToken.setDevice(mContext);
            GatewayService service = mRestAdapterMPApi.create(GatewayService.class);
            service.getToken(this.mKey, cardToken, callback);
        } else {
            throw new RuntimeException("Unsupported key type for this method");
        }
    }

    public void getBankDeals(final Callback<List<BankDeal>> callback) {

        if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
            BankDealService service = mRestAdapterMPApi.create(BankDealService.class);
            service.getBankDeals(this.mKey, mContext.getResources().getConfiguration().locale.toString(), callback);
        } else {
            throw new RuntimeException("Unsupported key type for this method");
        }
    }

    public void getCustomer(String preferenceId, final Callback<Customer> callback) {

        // TODO: Reemplazar por servicio final
        //CustomerService service = mRestAdapterMPApi.create(CustomerService.class);
        RestAdapter restAdapterBeta = new RestAdapter.Builder()
                .setEndpoint("https://mp-android-sdk.herokuapp.com/")
                .setLogLevel(Settings.RETROFIT_LOGGING)
                .setConverter(new GsonConverter(JsonUtil.getInstance().getGson()))
                .setClient(HttpClientUtil.getClient(this.mContext))
                .build();
        CustomerService service = restAdapterBeta.create(CustomerService.class);
        service.getCustomer(preferenceId, callback);
    }

    public void getIdentificationTypes(Callback<List<IdentificationType>> callback) {

        IdentificationService service = mRestAdapterMPApi.create(IdentificationService.class);
        if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
            service.getIdentificationTypes(this.mKey, null, callback);
        } else {
            service.getIdentificationTypes(null, this.mKey, callback);
        }
    }

    public void getInstallments(String bin, BigDecimal amount, Long issuerId, String paymentTypeId, Callback<List<Installment>> callback) {

        if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
            PaymentService service = mRestAdapterMPApi.create(PaymentService.class);
            service.getInstallments(this.mKey, bin, amount, issuerId, paymentTypeId,
                    mContext.getResources().getConfiguration().locale.toString(), callback);
        } else {
            throw new RuntimeException("Unsupported key type for this method");
        }
    }

    public void getIssuers(String paymentMethodId, final Callback<List<Issuer>> callback) {

        if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
            PaymentService service = mRestAdapterMPApi.create(PaymentService.class);
            service.getIssuers(this.mKey, paymentMethodId, callback);
        } else {
            throw new RuntimeException("Unsupported key type for this method");
        }
    }

    public void getPaymentMethods(final Callback<List<PaymentMethod>> callback) {

        if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
            PaymentService service = mRestAdapterMPApi.create(PaymentService.class);
            service.getPaymentMethods(this.mKey, callback);
        } else {
            throw new RuntimeException("Unsupported key type for this method");
        }
    }

    public List<PaymentMethod> getPaymentMethodsForBin(String bin, List<PaymentMethod> paymentMethods, List<String> supportedPaymentTypes){
        if(bin.length() == BIN_LENGTH) {
            List<PaymentMethod> validPaymentMethods = new ArrayList<>();
            for (PaymentMethod pm : paymentMethods) {
                if (pm.isValidForBin(bin) && (supportedPaymentTypes == null || supportedPaymentTypes.contains(pm.getPaymentTypeId()))) {
                    validPaymentMethods.add(pm);
                }
            }
            return validPaymentMethods;
        }
        else
            throw new RuntimeException("Invalid bin: " + BIN_LENGTH + " digits needed, " + bin.length() + " found");
    }
// * Static methods for preference match

    public List<PaymentMethod> getValidPaymentMethods(List<PaymentMethod> paymentMethods, List<String> excludedPaymentMethodIds, List<String> excludedPaymentMethodTypes){
        List<PaymentMethod> validPaymentMethods = new ArrayList<>();
        for(PaymentMethod currentPaymentMethod : paymentMethods){

            if(excludedPaymentMethodIds.contains(currentPaymentMethod.getId())
                    || excludedPaymentMethodTypes.contains(currentPaymentMethod.getPaymentTypeId()))
            {
                validPaymentMethods.add(currentPaymentMethod);
            }
        }
        return validPaymentMethods;
    }

    public static PaymentMethod getDefaultPaymentMethod(List<PaymentMethod> paymentMethods, List<String> defaultPaymentMethodId){
        PaymentMethod defaultPaymentMethod = null;
        for(PaymentMethod currentPaymentMethod : paymentMethods)
        {
            if(currentPaymentMethod.getId().equals(defaultPaymentMethodId)){
                defaultPaymentMethod = currentPaymentMethod;
                break;
            }
        }
        return defaultPaymentMethod;
    }


    // * Static methods for StartActivityBuilder implementation

    private static void startBankDealsActivity(Activity activity, String merchantPublicKey) {

        Intent bankDealsIntent = new Intent(activity, BankDealsActivity.class);
        bankDealsIntent.putExtra("merchantPublicKey", merchantPublicKey);
        activity.startActivityForResult(bankDealsIntent, BANK_DEALS_REQUEST_CODE);
    }

    private static void startCheckoutActivity(Activity activity, String merchantPublicKey, CheckoutPreference checkoutPreference, Boolean showBankDeals) {

        Intent checkoutIntent = new Intent(activity, CheckoutActivity.class);
        checkoutIntent.putExtra("merchantPublicKey", merchantPublicKey);
        checkoutIntent.putExtra("checkoutPreference", checkoutPreference);
        checkoutIntent.putExtra("showBankDeals", showBankDeals);
        activity.startActivityForResult(checkoutIntent, CHECKOUT_REQUEST_CODE);
    }

    private static void startCongratsActivity(Activity activity, Payment payment, PaymentMethod paymentMethod) {

        Intent congratsIntent = new Intent(activity, CongratsActivity.class);
        congratsIntent.putExtra("payment", payment);
        congratsIntent.putExtra("paymentMethod", paymentMethod);
        activity.startActivityForResult(congratsIntent, CONGRATS_REQUEST_CODE);
    }

    private static void startCustomerCardsActivity(Activity activity, List<Card> cards, Boolean supportMPApp) {

        if ((activity == null) || (cards == null)) {
            throw new RuntimeException("Invalid parameters");
        }
        Intent paymentMethodsIntent = new Intent(activity, CustomerCardsActivity.class);
        Gson gson = new Gson();
        paymentMethodsIntent.putExtra("cards", gson.toJson(cards));
        paymentMethodsIntent.putExtra("supportMPApp", supportMPApp);
        activity.startActivityForResult(paymentMethodsIntent, CUSTOMER_CARDS_REQUEST_CODE);
    }

    private static void startInstallmentsActivity(Activity activity, List<PayerCost> payerCosts) {

        Intent installmentsIntent = new Intent(activity, InstallmentsActivity.class);
        Gson gson = new Gson();
        installmentsIntent.putExtra("payerCosts", gson.toJson(payerCosts));
        activity.startActivityForResult(installmentsIntent, INSTALLMENTS_REQUEST_CODE);
    }

    private static void startIssuersActivity(Activity activity, String merchantPublicKey, PaymentMethod paymentMethod) {

        Intent issuersIntent = new Intent(activity, IssuersActivity.class);
        issuersIntent.putExtra("merchantPublicKey", merchantPublicKey);
        issuersIntent.putExtra("paymentMethod", paymentMethod);
        activity.startActivityForResult(issuersIntent, ISSUERS_REQUEST_CODE);
    }

    private static void startNewCardActivity(Activity activity, String keyType, String key, PaymentMethod paymentMethod, Boolean requireSecurityCode) {

        Intent newCardIntent = new Intent(activity, com.mercadopago.NewCardActivity.class);
        newCardIntent.putExtra("keyType", keyType);
        newCardIntent.putExtra("key", key);
        newCardIntent.putExtra("paymentMethod", paymentMethod);
        if (requireSecurityCode != null) {
            newCardIntent.putExtra("requireSecurityCode", requireSecurityCode);
        }
        activity.startActivityForResult(newCardIntent, NEW_CARD_REQUEST_CODE);
    }
    private static void startGuessingCardActivity(Activity activity, String keyType, String key, List<String> supportedPaymentTypes, Boolean requireSecurityCode, Boolean requireIssuer, Boolean showBankDeals) {

        Intent guessingCardIntent = new Intent(activity, com.mercadopago.GuessingNewCardActivity.class);
        guessingCardIntent.putExtra("keyType", keyType);
        guessingCardIntent.putExtra("key", key);
        putListExtra(guessingCardIntent, "supportedPaymentTypes", supportedPaymentTypes);

        if (requireSecurityCode != null) {
            guessingCardIntent.putExtra("requireSecurityCode", requireSecurityCode);
        }
        if (requireIssuer != null) {
            guessingCardIntent.putExtra("requireIssuer", requireIssuer);
        }
        if(showBankDeals != null){
            guessingCardIntent.putExtra("showBankDeals", showBankDeals);
        }
        activity.startActivityForResult(guessingCardIntent, GUESSING_CARD_REQUEST_CODE);
    }


    private static void startPaymentMethodsActivity(Activity activity, String merchantPublicKey, List<String> supportedPaymentTypes, Boolean showBankDeals, Boolean supportMPApp, List<String> excludedPaymentMethodIds, List<String> excludedPaymentTypes, String defaultPaymentMethodId) {

        Intent paymentMethodsIntent = new Intent(activity, PaymentMethodsActivity.class);
        paymentMethodsIntent.putExtra("merchantPublicKey", merchantPublicKey);
        putListExtra(paymentMethodsIntent, "supportedPaymentTypes", supportedPaymentTypes);
        paymentMethodsIntent.putExtra("showBankDeals", showBankDeals);
        paymentMethodsIntent.putExtra("supportMPApp", supportMPApp);
        putListExtra(paymentMethodsIntent, "excludedPaymentMethodIds", excludedPaymentMethodIds);
        putListExtra(paymentMethodsIntent, "excludedPaymentTypes", excludedPaymentTypes);
        paymentMethodsIntent.putExtra("defaultPaymentMethodId", defaultPaymentMethodId);
        activity.startActivityForResult(paymentMethodsIntent, PAYMENT_METHODS_REQUEST_CODE);
    }

    private static void startVaultActivity(Activity activity, String merchantPublicKey, String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, BigDecimal amount, List<String> supportedPaymentTypes, Boolean showBankDeals, Boolean cardGuessingEnabled,
                                           List<String> excludedPaymentMethodIds, List<String> excludedPaymentTypes, String defaultPaymentMethodId,
                                           Integer defaultInstallments, Integer maxInstallments) {

        Intent vaultIntent = new Intent(activity, VaultActivity.class);
        vaultIntent.putExtra("merchantPublicKey", merchantPublicKey);
        vaultIntent.putExtra("merchantBaseUrl", merchantBaseUrl);
        vaultIntent.putExtra("merchantGetCustomerUri", merchantGetCustomerUri);
        vaultIntent.putExtra("merchantAccessToken", merchantAccessToken);
        vaultIntent.putExtra("amount", amount.toString());
        vaultIntent.putExtra("showBankDeals", showBankDeals);

        vaultIntent.putExtra("cardGuessingEnabled", cardGuessingEnabled);
        putListExtra(vaultIntent, "supportedPaymentTypes", supportedPaymentTypes);
        putListExtra(vaultIntent, "excludedPaymentMethodIds", excludedPaymentMethodIds);
        putListExtra(vaultIntent, "excludedPaymentTypes", excludedPaymentTypes);
        vaultIntent.putExtra("defaultPaymentMethodId", defaultPaymentMethodId);

        vaultIntent.putExtra("defaultInstallments", defaultInstallments.toString());
        vaultIntent.putExtra("maxInstallments", maxInstallments.toString());
        activity.startActivityForResult(vaultIntent, VAULT_REQUEST_CODE);
    }

    private static void putListExtra(Intent intent, String listName, List<String> list) {

        if (list != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            intent.putExtra(listName, gson.toJson(list, listType));
        }
    }

    public static class Builder {

        private Context mContext;
        private String mKey;
        private String mKeyType;

        public Builder() {

            mContext = null;
            mKey = null;
        }

        public Builder setContext(Context context) {

            if (context == null) throw new IllegalArgumentException("context is null");
            this.mContext = context;
            return this;
        }

        public Builder setKey(String key, String keyType) {

            this.mKey = key;
            this.mKeyType = keyType;
            return this;
        }

        public Builder setPrivateKey(String key) {

            this.mKey = key;
            this.mKeyType = MercadoPago.KEY_TYPE_PRIVATE;
            return this;
        }

        public Builder setPublicKey(String key) {

            this.mKey = key;
            this.mKeyType = MercadoPago.KEY_TYPE_PUBLIC;
            return this;
        }

        public MercadoPago build() {

            if (this.mContext == null) throw new IllegalStateException("context is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");
            if ((!this.mKeyType.equals(MercadoPago.KEY_TYPE_PRIVATE)) &&
                    (!this.mKeyType.equals(MercadoPago.KEY_TYPE_PUBLIC))) throw new IllegalArgumentException("invalid key type");
            return new MercadoPago(this);
        }
    }

    public static class StartActivityBuilder {

        private Activity mActivity;
        private BigDecimal mAmount;
        private List<Card> mCards;
        private CheckoutPreference mCheckoutPreference;
        private String mKey;
        private String mKeyType;
        private String mMerchantAccessToken;
        private String mMerchantBaseUrl;
        private String mMerchantGetCustomerUri;
        private List<PayerCost> mPayerCosts;
        private Payment mPayment;
        private PaymentMethod mPaymentMethod;
        private Boolean mRequireIssuer;
        private Boolean mRequireSecurityCode;
        private Boolean mShowBankDeals;
        private Boolean mSupportMPApp;
        private List<String> mSupportedPaymentTypes;
        private Boolean mCardGuessingEnabled;
        private Integer mMaxInstallments;
        private Integer mDefaultInstallments;
        private List<String> mExcludedPaymentMethodIds;
        private List<String> mExcludedPaymentTypes;
        private String mDefaultPaymentMethodId;


        public StartActivityBuilder() {

            mActivity = null;
            mKey = null;
            mKeyType = KEY_TYPE_PUBLIC;
        }

        public StartActivityBuilder setActivity(Activity activity) {

            if (activity == null) throw new IllegalArgumentException("context is null");
            this.mActivity = activity;
            return this;
        }

        public StartActivityBuilder setAmount(BigDecimal amount) {

            this.mAmount = amount;
            return this;
        }

        public StartActivityBuilder setCards(List<Card> cards) {

            this.mCards = cards;
            return this;
        }

        public StartActivityBuilder setCheckoutPreference(CheckoutPreference checkoutPreference) {

            this.mCheckoutPreference = checkoutPreference;
            return this;
        }

        public StartActivityBuilder setKey(String key, String keyType) {

            this.mKey = key;
            this.mKeyType = keyType;
            return this;
        }

        public StartActivityBuilder setPrivateKey(String key) {

            this.mKey = key;
            this.mKeyType = MercadoPago.KEY_TYPE_PRIVATE;
            return this;
        }

        public StartActivityBuilder setPublicKey(String key) {

            this.mKey = key;
            this.mKeyType = MercadoPago.KEY_TYPE_PUBLIC;
            return this;
        }

        public StartActivityBuilder setMerchantAccessToken(String merchantAccessToken) {

            this.mMerchantAccessToken = merchantAccessToken;
            return this;
        }

        public StartActivityBuilder setMerchantBaseUrl(String merchantBaseUrl) {

            this.mMerchantBaseUrl = merchantBaseUrl;
            return this;
        }

        public StartActivityBuilder setMerchantGetCustomerUri(String merchantGetCustomerUri) {

            this.mMerchantGetCustomerUri = merchantGetCustomerUri;
            return this;
        }

        public StartActivityBuilder setPayerCosts(List<PayerCost> payerCosts) {

            this.mPayerCosts = payerCosts;
            return this;
        }

        public StartActivityBuilder setPayment(Payment payment) {

            this.mPayment = payment;
            return this;
        }

        public StartActivityBuilder setPaymentMethod(PaymentMethod paymentMethod) {

            this.mPaymentMethod = paymentMethod;
            return this;
        }

        public StartActivityBuilder setRequireSecurityCode(Boolean requireSecurityCode) {

            this.mRequireSecurityCode = requireSecurityCode;
            return this;
        }

        public StartActivityBuilder setRequireIssuer(Boolean requireIssuer) {

            this.mRequireIssuer = requireIssuer;
            return this;
        }

        public StartActivityBuilder setShowBankDeals(boolean showBankDeals) {

            this.mShowBankDeals = showBankDeals;
            return this;
        }

        public StartActivityBuilder setSupportMPApp(boolean supportMPApp) {

            this.mSupportMPApp = supportMPApp;
            return this;
        }

        public StartActivityBuilder setSupportedPaymentTypes(List<String> supportedPaymentTypes) {

            this.mSupportedPaymentTypes = supportedPaymentTypes;
            return this;
        }
        public StartActivityBuilder setExcludedPaymentMethodIds(List<String> paymentMethodIds)
        {
            this.mExcludedPaymentMethodIds = paymentMethodIds;
            return this;
        }


        public StartActivityBuilder setExcludedPaymentTypes(List<String> paymentTypes)
        {
            this.mExcludedPaymentTypes = paymentTypes;
            return this;
        }

        public StartActivityBuilder setDefaultPaymentMethodId(String paymentMethodId)
        {
            this.mDefaultPaymentMethodId = paymentMethodId;
            return this;
        }

        public StartActivityBuilder setDefaultInstallments(Integer defaultInstallments)
        {
            this.mDefaultInstallments = defaultInstallments;
            return this;
        }

        public StartActivityBuilder setMaxInstallments(Integer maxInstallments)
        {
            this.mMaxInstallments = maxInstallments;
            return this;
        }

        public StartActivityBuilder setGuessingCardFormEnabled(boolean cardGuessingEnabled)
        {
            this.mCardGuessingEnabled = cardGuessingEnabled;
            return this;
        }

        public void startBankDealsActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");

            if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
                MercadoPago.startBankDealsActivity(this.mActivity, this.mKey);
            } else {
                throw new RuntimeException("Unsupported key type for this method");
            }
        }

        public void startCheckoutActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mCheckoutPreference == null) throw new IllegalStateException("checkout preference is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");

            PaymentMethodPreference paymentMethodPreference = new PaymentMethodPreference();
            paymentMethodPreference.setDefaultInstallments(this.mDefaultInstallments);
            paymentMethodPreference.setMaxInstallments(this.mMaxInstallments);
            paymentMethodPreference.setDefaultPaymentMethodId(this.mDefaultPaymentMethodId);
            paymentMethodPreference.setExcludedPaymentMethodIds(this.mExcludedPaymentMethodIds);
            paymentMethodPreference.setExcludedPaymentTypes(this.mExcludedPaymentTypes);
            paymentMethodPreference.setSupportedPaymentTypes(this.mSupportedPaymentTypes);

            if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
                MercadoPago.startCheckoutActivity(this.mActivity, this.mKey,
                        this.mCheckoutPreference, this.mShowBankDeals);
            } else {
                throw new RuntimeException("Unsupported key type for this method");
            }
        }

        public void startCongratsActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mPayment == null) throw new IllegalStateException("payment is null");
            if (this.mPaymentMethod == null) throw new IllegalStateException("payment method is null");

            MercadoPago.startCongratsActivity(this.mActivity, this.mPayment, this.mPaymentMethod);
        }

        public void startCustomerCardsActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mCards == null) throw new IllegalStateException("cards is null");

            MercadoPago.startCustomerCardsActivity(this.mActivity, this.mCards, this.mSupportMPApp);
        }

        public void startInstallmentsActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mPayerCosts == null) throw new IllegalStateException("payer costs are null");

            MercadoPago.startInstallmentsActivity(this.mActivity, this.mPayerCosts);
        }

        public void startIssuersActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");
            if (this.mPaymentMethod == null) throw new IllegalStateException("payment method is null");

            if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
                MercadoPago.startIssuersActivity(this.mActivity,
                        this.mKey, this.mPaymentMethod);
            } else {
                throw new RuntimeException("Unsupported key type for this method");
            }
        }

        public void startNewCardActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");
            if (this.mPaymentMethod == null) throw new IllegalStateException("payment method is null");

            MercadoPago.startNewCardActivity(this.mActivity, this.mKeyType, this.mKey,
                    this.mPaymentMethod, this.mRequireSecurityCode);
        }

        public void startGuessingCardActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");
            MercadoPago.startGuessingCardActivity(this.mActivity, this.mKeyType, this.mKey, this.mSupportedPaymentTypes, this.mRequireSecurityCode, this.mRequireIssuer, this.mShowBankDeals);
        }

        public void startPaymentMethodsActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");

            if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
                MercadoPago.startPaymentMethodsActivity(this.mActivity, this.mKey,
                        this.mSupportedPaymentTypes, this.mShowBankDeals, this.mSupportMPApp, this.mExcludedPaymentMethodIds, this.mExcludedPaymentTypes, this.mDefaultPaymentMethodId);
            } else {
                throw new RuntimeException("Unsupported key type for this method");
            }
        }

        public void startVaultActivity() {

            if (this.mActivity == null) throw new IllegalStateException("activity is null");
            if (this.mAmount == null) throw new IllegalStateException("amount is null");
            if (this.mKey == null) throw new IllegalStateException("key is null");
            if (this.mKeyType == null) throw new IllegalStateException("key type is null");
            if (this.mDefaultInstallments != null && this.mDefaultInstallments < 1) throw new RuntimeException("default installments is less than 1");
            if (this.mDefaultInstallments != null && this.mMaxInstallments != null && this.mDefaultInstallments > this.mMaxInstallments) throw new RuntimeException("default installments greater than max");

            if (this.mKeyType.equals(KEY_TYPE_PUBLIC)) {
                MercadoPago.startVaultActivity(this.mActivity, this.mKey, this.mMerchantBaseUrl,
                        this.mMerchantGetCustomerUri, this.mMerchantAccessToken,
                        this.mAmount, this.mSupportedPaymentTypes, this.mShowBankDeals, this.mCardGuessingEnabled,
                        this.mExcludedPaymentMethodIds, this.mExcludedPaymentTypes, this.mDefaultPaymentMethodId,
                        this.mDefaultInstallments, this.mMaxInstallments);
            } else {
                throw new RuntimeException("Unsupported key type for this method");
            }
        }
    }
}