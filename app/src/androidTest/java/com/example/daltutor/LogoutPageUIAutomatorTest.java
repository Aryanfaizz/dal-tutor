package com.example.daltutor;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.provider.Contacts;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
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
public class LogoutPageUIAutomatorTest {

    final String launcherPackage = "com.example.daltutor";
    private UiDevice device;

    @Before
    public void setup() throws RemoteException, RuntimeException, IOException {
        // Emulated device setup :: Attribution "LoginPageUIAutomatorTest.java"
        // BUT I ADDED ".wakeUP()" CHECK BECAUSE I DIDN'T KNOW YOU HAD TO PRESS THE FAKE POWER BUTTON FOR THE TEST TO ACTUALLY RUN >:(
        device = UiDevice.getInstance(getInstrumentation());
        if (!device.isScreenOn()) {
            device.wakeUp();
        }
        Context context = ApplicationProvider.getApplicationContext();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(launcherPackage);
        assert launchIntent != null;
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(launchIntent);

        try {
            // Test login info attribution "LoginPageUIAutomatorTest.java"
            // We need to login before we can test logging out
            // These elements have been specified by UI id available at the layout login_activity.xml
            UiObject emailBox = device.findObject(new UiSelector().resourceId("com.example.daltutor:id/emailforlogin"));
            device.wait(Until.hasObject(By.res("com.example.daltutor:id/emailforlogin")), 5000);
            emailBox.setText("tutor@dal.ca");

            UiObject passwordBox = device.findObject(new UiSelector().resourceId("com.example.daltutor:id/passwordforlogin"));
            device.wait(Until.hasObject(By.res("com.example.daltutor:id/passwordforlogin")), 5000);
            passwordBox.setText("abc123");

            UiObject loginButton = device.findObject(new UiSelector().resourceId("com.example.daltutor:id/loginbutton"));
            device.wait(Until.hasObject(By.res("com.example.daltutor:id/passwordforlogin")), 5000);
            loginButton.click();

        } catch (UiObjectNotFoundException e) {
            throw new RuntimeException("Failed setup - could not complete login process", e);
        }
        device.executeShellCommand("pm grant com.example.daltutor android.permission.ACCESS_FINE_LOCATION");
        device.executeShellCommand("pm grant com.example.daltutor android.permission.ACCESS_COARSE_LOCATION");
        device.executeShellCommand("pm grant com.example.daltutor android.permission.POST_NOTIFICATIONS");

    }

    @Test
    public void checkIfLogoutButtonExists() {
        // Test to look for logout button
        // if pass :: logout button found
        UiObject logoutButton = device.findObject(new UiSelector().textContains("logout"));
        assertTrue(logoutButton.exists());
    }

    @Test
    public void checkIfLogoutSuccess() throws UiObjectNotFoundException {
        // Click logout
        UiObject logoutButton = device.findObject(new UiSelector().textContains("logout"));
        logoutButton.clickAndWaitForNewWindow();

        // Wait for the device to be idle after the activity switch
        device.waitForIdle();

        // Now, wait explicitly for the Login button to appear
        UiObject loginButton = device.findObject(new UiSelector().text("Login"));
        device.wait(Until.hasObject(By.text("Login")), 5000);  // Wait for up to 5 seconds for the login button

        assertTrue("Login button should be visible after logout", loginButton.exists());
    }
}