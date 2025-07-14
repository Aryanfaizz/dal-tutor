package com.example.daltutor;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;


@RunWith(AndroidJUnit4.class)
public class RoleDashboardUITest {

    final String launcherPackage = "com.example.daltutor";
    private UiDevice device;

    @Before
    public void setup() throws IOException {
        device = UiDevice.getInstance(getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(launcherPackage);
        assert launchIntent != null;
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(launchIntent);
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5000);
        device.executeShellCommand("pm grant com.example.daltutor android.permission.ACCESS_FINE_LOCATION");
        device.executeShellCommand("pm grant com.example.daltutor android.permission.ACCESS_COARSE_LOCATION");
        device.executeShellCommand("pm grant com.example.daltutor android.permission.POST_NOTIFICATIONS");
    }

    @Test
    public void checkDashboardStudentLogin() throws UiObjectNotFoundException, InterruptedException {
        UiObject emailBox = device.findObject(new UiSelector().text("Enter Your Email Address"));
        emailBox.setText("student@dal.ca");
        UiObject passwordBox = device.findObject(new UiSelector().text("Enter Your Password"));
        passwordBox.setText("abc123");
        UiObject loginButton = device.findObject(new UiSelector().text("LOGIN"));
        loginButton.clickAndWaitForNewWindow(10000);
        UiObject roleLabel = device.findObject(new UiSelector().textContains("Student"));
        Thread.sleep(5000);
        assertTrue(roleLabel.exists());
    }

    @Test
    public void checkDashboardTutorLogin() throws UiObjectNotFoundException {
        UiObject emailBox = device.findObject(new UiSelector().text("Enter Your Email Address"));
        emailBox.setText("tutor@dal.ca");
        UiObject passwordBox = device.findObject(new UiSelector().text("Enter Your Password"));
        passwordBox.setText("abc123");
        UiObject loginButton = device.findObject(new UiSelector().text("LOGIN"));
        loginButton.clickAndWaitForNewWindow();
        UiObject roleLabel = device.findObject(new UiSelector().textContains("Tutor"));
        assertTrue(roleLabel.exists());
    }
}
