package com.example.daltutor;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class TutorialPostingUITest {

    final String PACKAGE_NAME = "com.example.daltutor";
    private UiDevice device;

    @Before
    public void setup() throws IOException {
        device = UiDevice.getInstance(getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();
        Intent launchIntent = new Intent();
        launchIntent.setClassName("com.example.daltutor", "com.example.daltutor.ui.TutorialPostingActivity");
        launchIntent.putExtra("USERNAME", "DrProfessor");
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), 5000);
        device.executeShellCommand("pm grant com.example.daltutor android.permission.POST_NOTIFICATIONS");

    }

    @Test
    public void checkSuccessfulPosting() throws UiObjectNotFoundException {
        UiObject duration_box = device.findObject(new UiSelector().textContains("Session Duration"));
        UiObject fee_box = device.findObject(new UiSelector().textContains("Registration Fee"));
        UiObject topic_spinner = device.findObject(new UiSelector().textContains("Topic"));
        UiObject desc_box = device.findObject(new UiSelector().textContains("Description"));
        UiObject post_button = device.findObject(new UiSelector().textContains("Post tutorial"));

        duration_box.setText("300");
        fee_box.setText("2");
        topic_spinner.click();
        device.wait(Until.findObject(By.text("Computer Science")), 3000);
        UiObject csOption = device.findObject(new UiSelector().text("Computer Science"));
        csOption.click();
        device.wait(Until.findObject(By.text("Computer Science")),3000);
        desc_box.setText("This is a description for the tutorial");
        post_button.click();

        device.wait(Until.findObject(By.text("Tutorial session successfully posted")), 3000);
    }

    @Test
    public void checkFailedPosting() throws UiObjectNotFoundException {
        UiObject duration_box = device.findObject(new UiSelector().textContains("Session Duration"));
        UiObject fee_box = device.findObject(new UiSelector().textContains("Registration Fee"));
        UiObject desc_box = device.findObject(new UiSelector().textContains("Description"));
        UiObject post_button = device.findObject(new UiSelector().textContains("Post tutorial"));

        duration_box.setText("");
        fee_box.setText("");
        device.wait(Until.findObject(By.text("Computer Science")),3000);
        desc_box.setText("");
        post_button.click();

        device.wait(Until.findObject(By.text("Posting failed. Make sure all fields are filled.")),3000);
    }
}
