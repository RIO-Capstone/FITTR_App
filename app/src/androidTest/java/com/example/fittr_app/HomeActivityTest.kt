package com.example.fittr_app


import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(HomeActivity::class.java)
    private lateinit var decorView: View

    @Before
    fun setup(){
        Intents.init()
        mActivityScenarioRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @After
    fun tearDown(){
        Intents.release()
    }

    @Test
    fun checkButtonDisplay() {
        val button = onView(
            allOf(
                withId(R.id.home_next_button), withText("GET STARTED"),
                withParent(withParent(withId(android.R.id.content))),
                isDisplayed()
            )
        )
        button.check(matches(isDisplayed()))
    }

    @Test
    fun checkNavigationToAuth(){
        val button = onView(withId(R.id.home_next_button))
        button.perform(click())
        onView(withId(R.id.auth_login_button)).check(matches(isDisplayed()))
    }
}
