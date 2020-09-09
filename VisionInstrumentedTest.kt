package com.travels.searchtravels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.api.services.vision.v1.model.LatLng
import com.travels.searchtravels.activity.MainActivity
import com.travels.searchtravels.api.OnVisionApiListener
import com.travels.searchtravels.api.VisionApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch


@RunWith(AndroidJUnit4::class)
open class VisionInstrumentedTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    /**
     * Run application for instrumented tests
     */
    @Before
    fun launchApplication() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testFoundRealLocation() {
        val realLocationPhoto = extractExternalBitmap(REAL_LOCATION_PHOTO)

        val visionListener = runParametrizedByBitmap(realLocationPhoto)

        assertEquals(visionListener.latLng!!.latitude, 50.0960828, 0.0000002) // prevent not accurate lat & lon
        assertEquals(visionListener.latLng!!.longitude, 14.4195728, 0.0000002)
    }

    @Test
    fun testRecognizedSnowLandmark() {
        val snowPhoto = extractExternalBitmap(SNOW_PHOTO)

        val visionListener = runParametrizedByBitmap(snowPhoto)

        assertEquals("snow", visionListener.category)
    }

    @Test
    fun testRecognizedMountainsLandmark() {
        val mountainsPhoto = extractExternalBitmap(MOUNTAINS_PHOTO)

        val visionListener = runParametrizedByBitmap(mountainsPhoto)

        assertEquals("mountain", visionListener.category)
    }

    @Test
    fun testRecognizedBeachLandmark() {
        val beachPhoto = extractExternalBitmap(BEACH_PHOTO)

        val visionListener = runParametrizedByBitmap(beachPhoto)

        assertEquals("beach", visionListener.category)
    }

    @Test
    fun testRecognizedOceanLandmark() {
        val oceanPhoto = extractExternalBitmap(OCEAN_PHOTO)

        val visionListener = runParametrizedByBitmap(oceanPhoto)

        assertEquals("ocean", visionListener.category)
    }

    @Test
    fun testRecognizedSeaLandmark() {
        val seaPhoto = extractExternalBitmap(SEA_PHOTO)

        val visionListener = runParametrizedByBitmap(seaPhoto)

        assertEquals("sea", visionListener.category)
    }


    /**
     * Grabs image from URL and converts to Bitmap
     */
    private fun extractExternalBitmap(url: String): Bitmap {

        val externalConnection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        externalConnection.doInput = true
        externalConnection.connect()

        return BitmapFactory.decodeStream(externalConnection.getInputStream())
    }

    /**
     * Callback mock with access to the received values
     */
    private class TestVisionListener(var countDownLatch: CountDownLatch) : OnVisionApiListener {

        var latLng: LatLng? = null;
        var category: String? = null;
        var failed: Boolean = false;

        override fun onSuccess(latLng: LatLng?) {
            this.latLng = latLng;
            this.countDownLatch.countDown()
        }

        override fun onErrorPlace(category: String?) {
            this.category = category
            this.countDownLatch.countDown()
        }

        override fun onError() {
            this.failed = true
            this.countDownLatch.countDown()
        }
    }

    /**
     *  Runs test parametrized by different bitmaps
     */
    private fun runParametrizedByBitmap(bitmap: Bitmap): TestVisionListener {
        val countDownLatch = CountDownLatch(1)
        val visionListener = TestVisionListener(countDownLatch)

        scenario.onActivity {
            VisionApi.findLocation(bitmap, VISION_API_TOKEN, visionListener)
        }
        countDownLatch.await()

        return visionListener
    }

    companion object {
        private const val VISION_API_TOKEN =
            "ya29.a0AfH6SMAQiD1h4-xHQrm76_-J3WR-p68FQ2qTxlQYfABGWDj7XMrxSgJWyBfbLgz9mpEHPwUL2hMX91QvVGjFzw4Vy8BfZoVGu76hQkRC1yFXfx-0yyj9D3UUKu3YJbPnBg00w0bbF9FUJ2TBV3ZZL4uD2nPMs5Ez_arH"

        private const val REAL_LOCATION_PHOTO =
            "https://www.arthotel.cz/files-sbbasic/sr_arthotel_cz/art-hotel-praha-257859398.jpg?w=1900"

        private const val BEACH_PHOTO =
            "https://i.ytimg.com/vi/u07hdz5B0oU/maxresdefault.jpg"

        private const val MOUNTAINS_PHOTO =
            "https://pre00.deviantart.net/ca29/th/pre/i/2014/232/1/8/mountain_background_by_burtn-d7vyhp9.jpg"

        private const val OCEAN_PHOTO =
            "https://cdn.theatlantic.com/assets/media/img/photo/2019/01/2018-ocean-art/u03_Mirrorless_Wide-Angle_Fabrice/main_900.jpg?1548277383"

        private const val SEA_PHOTO =
            "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fupload.wikimedia.org%2Fwikipedia%2Fcommons%2Fthumb%2F7%2F70%2FLabrador-sea-paamiut.jpg%2F1200px-Labrador-sea-paamiut.jpg&f=1&nofb=1"

        private const val SNOW_PHOTO =
            "https://thumbs.dreamstime.com/b/skiing-trail-beautiful-winter-landscape-big-trees-covered-snow-ski-resort-finland-lapland-81326022.jpg"
    }
}

