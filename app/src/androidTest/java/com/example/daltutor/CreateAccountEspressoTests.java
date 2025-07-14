package com.example.daltutor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.daltutor.ui.CreateAccountActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class CreateAccountEspressoTests {

    @Rule
    public ActivityTestRule<CreateAccountActivity> activityRule =
            new ActivityTestRule<>(CreateAccountActivity.class);

    @Test
    public void testFieldsAndButtonAreDisplayed() {
        // Check if username field is displayed
        onView(withId(R.id.username)).check(matches(isDisplayed()));

        // Check if email field is displayed
        onView(withId(R.id.email)).check(matches(isDisplayed()));

        // Check if phone number field is displayed
        onView(withId(R.id.phone_number)).check(matches(isDisplayed()));

        // Check if password field is displayed
        onView(withId(R.id.password)).check(matches(isDisplayed()));

        // Check if button is displayed
        onView(withId(R.id.create_account_button)).check(matches(isDisplayed()));
    }

}
