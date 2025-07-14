package com.example.daltutor;
import com.paypal.android.sdk.payments.PayPalConfiguration;

public class PaypalConfig {

    private PayPalConfiguration payPalConfig;

    private void configPayPal() {
        String PAYPAL_CLIENT_ID = "AdnRx4d8lGoIKWHbM1BD8Z1CAytLNnvOwE4IOsyA6OHZyi8H0sjNaFvZ1nd9jdOM7l5WziaSv7mjVADO";
        payPalConfig = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(PAYPAL_CLIENT_ID);
    }

}
