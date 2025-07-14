package com.example.daltutor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.daltutor.core.Posting;
import com.example.daltutor.firebase.DatabaseHelper;
import com.example.daltutor.ui.SessionDetailsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SessionDetailsEspressoTest {
    @Rule
    public ActivityScenarioRule<SessionDetailsActivity> activityRule =
            new ActivityScenarioRule<>(new Intent()
                    .setClassName("com.example.daltutor", "com.example.daltutor.ui.SessionDetailsActivity")
                    .putExtra("POSTING_ID", "-OKNfhT6Oyi-IHNoZhH8"));

    @Test
    public void checkElementsAppear () {
        onView(withId(R.id.topic_text_hint)).check(matches(isDisplayed()));
        onView(withId(R.id.fee_hint_text)).check(matches(isDisplayed()));
        onView(withId(R.id.duration_text_hint)).check(matches(isDisplayed()));
        onView(withId(R.id.tutor_text_hint)).check(matches(isDisplayed()));
        onView(withId(R.id.datetime_hint_text)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.map_frag)).check(matches(isDisplayed()));
        onView(withId(R.id.address_text)).check(matches(isDisplayed()));
        onView(withId(R.id.tutorial_details_text)).check(matches(isDisplayed()));
    }

    @Test
    public void checkDetailsPopulate() throws InterruptedException {
        final Posting[] posting = new Posting[1];
        DatabaseHelper.getPostingFromID("-OKNfhT6Oyi-IHNoZhH8", new DatabaseHelper.PostingCallback() {
            @Override
            public void onPostingFound(Posting p) {
                posting[0]= p;
            }
        });
        Thread.sleep(10000);
        onView(withId(R.id.topic_text)).check(matches(withText(posting[0].getTopic())));
        onView(withId(R.id.address_text)).check(matches(withText(posting[0].getAddress())));
        onView(withId(R.id.duration_text)).check(matches(withText(posting[0].getDuration()+" minutes")));
        onView(withId(R.id.fee_text)).check(matches(withText("$"+posting[0].getFee())));
        onView(withId(R.id.tutor_text)).check(matches(withText(posting[0].getTutor())));
        onView(withId(R.id.datetime_text)).check(matches(withText(posting[0].getDatetime())));
        onView(withId(R.id.description_text)).check(matches(withText(posting[0].getDescription())));
    }
}
