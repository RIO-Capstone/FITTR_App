import android.view.View
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry

class ToastIdlingResource(private val toastText: String) : IdlingResource {
    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName() = ToastIdlingResource::class.java.name

    override fun isIdleNow(): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val view = View(context)

        // Check if the Toast is visible
        val isToastDisplayed = try {
            ViewMatchers.withText(toastText).matches(view)
        } catch (e: Exception) {
            false
        }

        if (isToastDisplayed) {
            callback?.onTransitionToIdle() // Notify Espresso that the app is now idle
            return true
        }

        return false
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }
}
