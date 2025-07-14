package com.example.daltutor;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.espresso.intent.Intents;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import com.example.daltutor.ui.LoginActivity;
import com.example.daltutor.ui.TutorialPostingActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TutorDashboardActivityEspressoTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private void login() throws InterruptedException {
        // Enter email
        onView(withId(R.id.emailforlogin)).perform(typeText("aryan@daltutor.com"), closeSoftKeyboard());
        // Enter password
        onView(withId(R.id.passwordforlogin)).perform(typeText("abc123"), closeSoftKeyboard());
        // Click login button
        onView(withId(R.id.loginbutton)).perform(click());
        // Wait for transition to dashboard
        Thread.sleep(3000); // Adjust based on the network response time
    }

    @Test
    public void testMapFragmentIsDisplayed() throws InterruptedException {
        login();
        // Check that the map fragment container (with id: map_fragment) is displayed.
        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreatePostingButtonLaunchesPostingActivity() throws InterruptedException {
        login();
        // Initialize Espresso Intents to capture and verify outgoing intents.
        Intents.init();
        try {
            // Click the create posting button.
            onView(withId(R.id.create_posting_button)).perform(click());
            // Verify that an intent is sent to launch TutorialPostingActivity.
            intended(hasComponent(TutorialPostingActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }

    @Test
    public void testShowStudentsButtonDisplaysStudentTable() throws InterruptedException {
        login();
        // Wait for UI to be ready before clicking.
        Thread.sleep(2000); // Delay for UI to load
        // Click the "Show Available Students" button.
        onView(withId(R.id.show_students_button)).perform(click());
        // Wait for student table to be updated.
        Thread.sleep(2000); // Delay for UI to update
        // Verify that the student table is displayed.
        onView(withId(R.id.student_table)).check(matches(isDisplayed()));
    }

    @Test
    public void testShowPreferredButtonDisplaysPreferredStudentsTable() throws InterruptedException {
        login();
        // Wait for UI to be ready before clicking.
        Thread.sleep(2000); // Delay for UI to load
        // Click the "Show Preferred Students" button.
        onView(withId(R.id.show_preferred_button)).perform(click());
        // Wait for preferred students table to be updated.
        Thread.sleep(2000); // Delay for UI to update
        // Verify that the preferred students table is displayed.
        onView(withId(R.id.preferred_students_container)).check(matches(isDisplayed()));
    }
}
