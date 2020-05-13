package com.example.testpaymentsdk

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wirecard.ecom.Client
import com.wirecard.ecom.card.core.CardPaymentAction
import com.wirecard.ecom.card.model.CardPayment
import com.wirecard.ecom.core.PaymentAction
import com.wirecard.ecom.model.CardToken
import com.wirecard.ecom.model.TransactionType
import com.wirecard.ecom.model.out.PaymentResponse
import com.wirecard.ecom.util.Observer
import de.wirecard.paymentsdk.ui.widgets.cardform.singleline.CardForm
import kotlinx.android.synthetic.main.activity_card_form.*
import java.math.BigDecimal
import java.util.*

class KotlinCardFieldActivity : AppCompatActivity(), Observer<PaymentResponse> {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_form)

        val merchantID = "merchant_id"
        val secretKey = "secret_key"
        val requestID = UUID.randomUUID().toString()
        val transactionType = TransactionType.PURCHASE
        val amount = BigDecimal(5)
        val currency = "EUR"
        val signature = ""

        submitBtn.setOnClickListener {
            val cardPayment = CardPayment(
                signature,
                requestID,
                merchantID,
                transactionType,
                amount,
                currency
            )
            cardPayment.attempt3d = true
            cardPayment.animatedCardPayment = true

            CardFieldFragmentImplFragment
            Client(context = this, url = "https://api-test.wirecard.com").startPayment(cardPayment)
        }
    }

    override fun onObserve(eventMessage: PaymentResponse) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                eventMessage.errorMessage ?: eventMessage.payment.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}