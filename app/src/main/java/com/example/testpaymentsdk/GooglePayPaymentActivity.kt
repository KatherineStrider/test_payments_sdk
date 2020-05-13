package com.example.testpaymentsdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.*
import com.google.android.gms.wallet.Wallet.getPaymentsClient
import com.google.android.gms.wallet.WalletConstants.CARD_NETWORK_VISA
import com.wirecard.ecom.Client
import com.wirecard.ecom.googlepay.model.GooglePayPayment
import com.wirecard.ecom.googlepay.model.googlepay.Transaction
import com.wirecard.ecom.model.TransactionType
import com.wirecard.ecom.model.out.PaymentResponse
import java.math.BigDecimal
import java.util.*

class GooglePayPaymentActivity : AppCompatActivity() {

    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 992

    private lateinit var paymentClient: PaymentsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_form)

        val supportedCardBrands = Arrays.asList(CARD_NETWORK_VISA)

        paymentClient = getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build()
        )
        val transactionInfo = Transaction("1,00", "EUR").getTransactionInfo()

        val paramsBuilder = PaymentMethodTokenizationParameters.newBuilder()
            .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
            .addParameter("gateway", "wirecard")
            .addParameter("gatewayMerchantId", "your_merchant_account_id")

        val paymentDataRequest = PaymentDataRequest.newBuilder()
            .setPhoneNumberRequired(true)
            .setEmailRequired(true)
            .setShippingAddressRequired(true)
            .addAllowedPaymentMethods(
                Arrays.asList(
                    WalletConstants.PAYMENT_METHOD_CARD,
                    WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD
                )
            )
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(supportedCardBrands)
                    .setAllowPrepaidCards(true)
                    .setBillingAddressRequired(true)
                    .build()
            )
            .setTransactionInfo(transactionInfo)
            .setPaymentMethodTokenizationParameters(paramsBuilder.build())
            .setUiRequired(true)
            .build()

        val futurePaymentData: Task<PaymentData> = paymentClient.loadPaymentData(paymentDataRequest)
        AutoResolveHelper.resolveTask(
            futurePaymentData,
            this@GooglePayPaymentActivity,
            LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> {
                val paymentData = PaymentData.getFromIntent(data!!)
                paymentData?.let { startPayment(it) }
                //
            }
            Activity.RESULT_CANCELED -> {
                // handle user canceled the payment process
            }
            AutoResolveHelper.RESULT_ERROR -> {
                // something went wrong - handle here
            }
        }

        val paymentSdkResponse = data?.getSerializableExtra(Client.EXTRA_PAYMENT_SDK_RESPONSE)
        if (paymentSdkResponse is PaymentResponse) {
            // handle response from Elastic Engine
        }
    }

    private fun startPayment(paymentData: PaymentData) {
        val googlePayPayment = GooglePayPayment.Builder()
            .setPaymentData(paymentData)
            .setSignature("signature v2")
            .setRequestId("request id")
            .setMerchantAccountId("merchant account id")
            .setTransactionType(TransactionType.PURCHASE)
            .setAmount(BigDecimal(1.00))
            .setCurrency("EUR")
            .build()
        // by default set to ENVIRONMENT_TEST. Make sure setting this property to ENVIRONMENT_PRODUCTION before going live
        googlePayPayment.environment = WalletConstants.ENVIRONMENT_TEST

        Client(this, "https://api-test.wirecard.com").startPayment(googlePayPayment)
    }

}