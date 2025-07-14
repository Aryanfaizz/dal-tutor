package com.example.daltutor;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import com.example.daltutor.ui.StudentDashboardActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StudentDashboardActivityEspressoTest {

    // Launch the activity with a username extra
    @Rule
    public ActivityScenarioRule<StudentDashboardActivity> activityRule =
            new ActivityScenarioRule<>(new Intent()
                    .setClassName("com.example.daltutor", "com.example.daltutor.ui.StudentDashboardActivity")
                    .putExtra("USERNAME", "spider"));

    @Before
    public void setUp() {
        // Initialize Intents for intent-based testing if needed
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testMapFragmentIsDisplayed() {
        // Verify that the map fragment container is displayed
        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()));
    }

    @Test
    public void testToolbarAndWelcomeMessage() {
        // Check if the toolbar is displayed
        onView(withId(R.id.materialToolbar)).check(matches(isDisplayed()));

        // Check if the roleLabel is displayed and contains the username "spider"
        // Note: The exact text depends on Firebase data, so we check for partial match
        onView(withId(R.id.roleLabel))
                .check(matches(isDisplayed()))
                .check(matches(withText("Welcome, spider (Student)")));
        // Adjust the expected role ("Student") based on your Firebase data
    }

    @Test
    public void testLogoutButtonIsDisplayedAndClickable() {
        // Verify logout button is displayed
        onView(withId(R.id.logout_button))
                .check(matches(isDisplayed()))
                .check(matches(withText("Logout")));

        // Perform click and verify activity finishes (via scenario state)
        onView(withId(R.id.logout_button)).perform(click());
        try (ActivityScenario<StudentDashboardActivity> scenario = activityRule.getScenario()) {
            scenario.onActivity(activity -> {
                assert activity.isFinishing() : "Activity should be finishing after logout";
            });
        }
    }

    @Test
    public void testShowTutorsButtonDisplaysTable() {
        // Verify the "Show List" button for available tutors is displayed
        onView(withId(R.id.show_tutors_button))
                .check(matches(isDisplayed()))
                .check(matches(withText("Show List")));

        // Click the button to load tutors
        onView(withId(R.id.show_tutors_button)).perform(click());

        // Check if the tutor table is displayed
        onView(withId(R.id.tutor_table)).check(matches(isDisplayed()));
        // Note: Verifying table content requires Firebase mocking or pre-populated test data
    }

    @Test
    public void testShowPreferredTutorsButtonDisplaysTable() {
        // Verify the "Show List" button for preferred tutors is displayed
        onView(withId(R.id.show_preferred_tutors_button))
                .check(matches(isDisplayed()))
                .check(matches(withText("Show List")));

        // Click the button to load preferred tutors
        onView(withId(R.id.show_preferred_tutors_button)).perform(click());

        // Check if the preferred tutor table is displayed
        onView(withId(R.id.preferred_tutors_container)).check(matches(isDisplayed()));
        // Note: Verifying table content requires Firebase mocking or pre-populated test data
    }

    @Test
    public void testTutorSectionTitlesAreDisplayed() {
        // Check if "Available Tutors" title is displayed
        onView(withId(R.id.tutor_section_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Available Tutors")));

        // Check if "Preferred Tutors" title is displayed
        onView(withId(R.id.preferred_tutors_label))
                .check(matches(isDisplayed()))
                .check(matches(withText("Preferred Tutors")));
    }
}