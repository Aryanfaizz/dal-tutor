package com.example.daltutor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.uiautomator.UiDevice;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.not;

import com.example.daltutor.ui.SessionDetailsActivity;

import java.io.IOException;

/**
 * Tutorial registration acceptance criteria test:
 * 1. The search UI screen should have a register button for tutorials
 * 2. Once a tutorial is registered for, the student cannot register again
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TutorialRegistrationUITest {
    private UiDevice device;
    @Test
    public void testTutorialRegistrationAcceptanceCriteria() throws IOException {
        // Launch SessionDetailsActivity directly with test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SessionDetailsActivity.class);
        intent.putExtra("POSTING_ID", "test_posting_id");
        intent.putExtra("USERNAME", "test_student");
        intent.putExtra("ROLE", "Student");
        ActivityScenario.launch(intent);
        device = UiDevice.getInstance(getInstrumentation());
        device.executeShellCommand("pm grant com.example.daltutor android.permission.POST_NOTIFICATIONS");

        // Acceptance Criteria 1: Verify register button exists and is usable
        onView(withId(R.id.register_button))
                .check(matches(isDisplayed()))
                .check(matches(withText("Register for Tutorial")))
                .check(matches(isEnabled()));

        // Click register button
        onView(withId(R.id.register_button)).perform(click());

        // Acceptance Criteria 2: Verify student cannot register again
        onView(withId(R.id.register_button))
                .check(matches(withText("Already Registered")))
                .check(matches(not(isEnabled())));
    }
}