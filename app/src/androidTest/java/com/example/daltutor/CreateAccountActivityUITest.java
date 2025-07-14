package com.example.daltutor;

import android.content.Context;
import android.content.Intent;
import android.app.Instrumentation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.daltutor.ui.CreateAccountActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateAccountActivityUITest {

    private static final String PACKAGE_NAME = "com.example.daltutor";
    private UiDevice device;

    @Before
    public void setUp() throws IOException {
        Instrumentation instrumentation = getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        Context context = instrumentation.getTargetContext();
        Intent intent = new Intent(context, CreateAccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), 5000);
        device.executeShellCommand("pm grant com.example.daltutor android.permission.POST_NOTIFICATIONS");

    }

    @Test
    public void testCreateAccount_flow() throws Exception {
        // 1. Find the username field and set text
        UiObject2 usernameField = device.findObject(By.res(PACKAGE_NAME, "username"));
        usernameField.setText("TestUser");

        // 2. Find the email field and set text
        UiObject2 emailField = device.findObject(By.res(PACKAGE_NAME, "email"));
        emailField.setText("testuser@example.com");

        // 3. Find the phone field and set text
        UiObject2 phoneField = device.findObject(By.res(PACKAGE_NAME, "phone_number"));
        phoneField.setText("1234567890");

        // 4. Find the password field and set text
        UiObject2 passwordField = device.findObject(By.res(PACKAGE_NAME, "password"));
        passwordField.setText("Password123");

        // 5. Select a role from the Spinner
        UiObject2 roleSpinner = device.findObject(By.res(PACKAGE_NAME, "role_spinner"));
        roleSpinner.click();

        // Wait for Spinner options to appear (just a short wait)
        device.wait(Until.findObject(By.text("Tutor")), 2000);
        UiObject2 tutorOption = device.findObject(By.text("Tutor"));
        tutorOption.click();

        // 6. Click the Create Account button
        UiObject2 createAccountButton = device.findObject(By.res(PACKAGE_NAME, "create_account_button"));
        createAccountButton.click();

    }
}
