package com.travels.searchtravels

import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.travels.searchtravels.activity.ChipActivity
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class TravelPricesInstrumentedTest {

    private lateinit var scenario: ActivityScenario<ChipActivity>

    /**
     * Start activity before running tests
     */
    @Before
    fun startUp() {
        scenario = ActivityScenario.launch(ChipActivity::class.java)
    }

    @Test
    fun testUpdatesPriceByBerlin() {
        val priceChangedListener: OnPriceChangedListener = runParametrizedWithCity("Amsterdam")
        runParametrizedWithCity("Berlin")
        assertNotEquals("от 0 ₽", priceChangedListener.price)
    }

    @Test
    fun testUpdatesPriceByAmsterdam() {
        val priceChangedListener: OnPriceChangedListener = runParametrizedWithCity("Amsterdam")
        runParametrizedWithCity("Amsterdam")
        assertNotEquals("от 0 ₽", priceChangedListener.price)
    }

    private fun runParametrizedWithCity(city: String): OnPriceChangedListener {
        val countDownLatch = CountDownLatch(1)
        val priceChangedListener = OnPriceChangedListener(countDownLatch)

        scenario.onActivity { activity ->
            val priceView = activity.findViewById<TextView>(R.id.airticketTV)
            activity.getInfoNomad(city)
            priceView.doOnTextChanged { text, _, _, _ -> priceChangedListener.onPriceChanged(text.toString()) }
        }

        countDownLatch.await()

        return priceChangedListener
    }

    private class OnPriceChangedListener(var countDownLatch: CountDownLatch) {

        var price: String? = null

        fun onPriceChanged(price: String) {
            this.price = price
            countDownLatch.countDown()
        }

    }
}
