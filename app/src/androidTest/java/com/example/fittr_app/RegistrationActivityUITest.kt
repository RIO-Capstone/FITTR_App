package com.example.fittr_app

import android.view.View
import android.widget.DatePicker
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.fittr_app.ui.registration.RegistrationActivity
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class RegistrationActivityUITest {
    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(RegistrationActivity::class.java)
    private lateinit var decorView : View
    @Before
    fun setUp() {
        // Initialize Espresso Intents before running tests
        Intents.init()
        activityRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }
    @After
    fun tearDown() {
        // Release Espresso Intents after tests finish
        Intents.release()
    }

    @Test
    fun testNavigationFromPageOneToPageTwo() {
        // Verify we start on PageOne
        onView(withId(R.id.et_first_name)).check(matches(isDisplayed()))
        populateRegistrationPageOneFields(RegistrationPageOneFields())
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        onView(withId(R.id.nxt_btn_page_two)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToPageThree(){
        // Verify we start on PageOne
        onView(withId(R.id.et_first_name)).check(matches(isDisplayed()))
        populateRegistrationPageOneFields(RegistrationPageOneFields())
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        // page two check and click
        onView(withId(R.id.nxt_btn_page_two)).check(matches(isDisplayed()))
        populateRegistrationPageTwoFields(RegistrationPageTwoFields())
        onView(withId(R.id.nxt_btn_page_two)).perform(click())
        // page three check

        onView(withId(R.id.registrationThreeCompleteRegistration)).check(matches(isDisplayed()))
    }

    @Test
    fun invalidEmailInput(){
        populateRegistrationPageOneFields(RegistrationPageOneFields(email = "wrong_email.com"))
        // Attempt to navigate to the next page
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        //Verify that the error is shown
        onView(withId(R.id.et_email)).check(matches(hasErrorText("Please enter a valid email address")))
    }

    @Test
    fun emptyFirstNameField(){
        populateRegistrationPageOneFields(RegistrationPageOneFields(firstName = ""))
        // Attempt to navigate to the next page
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        //Verify that the error is shown
        onView(withId(R.id.et_first_name)).check(matches(hasErrorText("First name is required")))
    }

    @Test
    fun emptyLastNameField(){
        populateRegistrationPageOneFields(RegistrationPageOneFields(lastName = ""))
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        onView(withId(R.id.et_last_name)).check(matches(hasErrorText("Last name is required")))
    }

    @Test
    fun emptyPasswordField(){
        populateRegistrationPageOneFields(RegistrationPageOneFields(password = ""))
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        onView(withId(R.id.et_password)).check(matches(hasErrorText("Password is required")))
    }

    @Test
    fun emptyPhoneNumberField(){
        populateRegistrationPageOneFields(RegistrationPageOneFields(phoneNumber = ""))
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        onView(withId(R.id.et_phone_number)).check(matches(hasErrorText("Phone number is required")))
    }

    @Test
    fun emptyProductIdField(){
        populateRegistrationPageOneFields(RegistrationPageOneFields(productId = ""))
        onView(withId(R.id.nxt_button_page_one)).perform(click())
        onView(withId(R.id.et_product_id)).check(matches(hasErrorText("Product ID is required")))
    }

    @Test
    fun emptyGender(){
        testNavigationFromPageOneToPageTwo()
        populateRegistrationPageTwoFields(RegistrationPageTwoFields(gender = null))
        onView(withId(R.id.nxt_btn_page_two)).perform(click())
        Thread.sleep(500)
        onView(withText("Please select a gender")).inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun emptyDateOfBirth(){
        testNavigationFromPageOneToPageTwo()
        populateRegistrationPageTwoFields(RegistrationPageTwoFields(dateOfBirth = null))
        onView(withId(R.id.nxt_btn_page_two)).perform(click())
        Thread.sleep(500)
        onView(withText("Date of birth is required")).inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun emptyHeight(){
        testNavigationFromPageOneToPageTwo()
        populateRegistrationPageTwoFields(RegistrationPageTwoFields(height = null))
        onView(withId(R.id.nxt_btn_page_two)).perform(click())
        onView(withId(R.id.et_height)).check(matches(hasErrorText("Height is required")))
    }

    private fun populateRegistrationPageOneFields(fields: RegistrationPageOneFields, checkTerms: Boolean = true) {
        fields.firstName?.let {
            onView(withId(R.id.et_first_name)).perform(typeText(it), closeSoftKeyboard())
        }
        fields.lastName?.let {
            onView(withId(R.id.et_last_name)).perform(typeText(it), closeSoftKeyboard())
        }
        fields.phoneNumber?.let {
            onView(withId(R.id.et_phone_number)).perform(typeText(it), closeSoftKeyboard())
        }
        fields.email?.let {
            onView(withId(R.id.et_email)).perform(typeText(it), closeSoftKeyboard())
        }
        fields.productId?.let {
            onView(withId(R.id.et_product_id)).perform(typeText(it), closeSoftKeyboard())
        }
        fields.password?.let {
            onView(withId(R.id.et_password)).perform(typeText(it), closeSoftKeyboard())
        }
        if(checkTerms) onView(withId(R.id.cb_accept_terms)).perform(scrollTo(), click(), click()) // need to double click
    }

    private fun populateRegistrationPageTwoFields(fields:RegistrationPageTwoFields){
        fields.gender?.let {
            onView(withId(R.id.spinner_gender)).perform(click())
            onData(anything()).atPosition(if (it == "Male") 1 else 2).perform(click())
        }
        fields.height?.let {
            onView(withId(R.id.et_height)).perform(typeText(it), closeSoftKeyboard())
        }
        fields.weight?.let {
            onView(withId(R.id.et_weight)).perform(typeText(it), closeSoftKeyboard())
        }
        fields.dateOfBirth?.let {
            setCalendarDate(R.id.et_date_of_birth,2012,5,5)
        }
    }

    private fun setCalendarDate(datePickerLaunchViewId: Int, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(datePickerLaunchViewId)).perform(click())
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
            PickerActions.setDate(
                year,
                monthOfYear,
                dayOfMonth
            )
        )
        onView(withId(android.R.id.button1)).perform(click())
    }

    internal data class RegistrationPageOneFields(
        val firstName: String? = "John",
        val lastName: String? = "Doe",
        val phoneNumber: String? = "12345678",
        val email: String? = "john@example.com",
        val productId: String? = "1",
        val password: String? = "password"
    )

    internal data class RegistrationPageTwoFields(
        val dateOfBirth:String? = "19-08-2000",
        val gender:String? = "Male",
        val height:String? = "170",
        val weight:String? = "70"
    )

}