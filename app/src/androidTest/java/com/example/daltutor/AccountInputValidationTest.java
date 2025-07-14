package com.example.daltutor;

import static org.junit.Assert.*;
import org.junit.Test;

import com.example.daltutor.firebase.DatabaseHelper;
import com.google.android.gms.tasks.Task;
import java.util.HashMap;
import java.util.Map;


public class AccountInputValidationTest {

    //Test basic input validation logic
    @Test
    public void testInputValidationAllFields() {
        // Example of a small “input validation” helper:
        assertTrue("Expected valid inputs to pass validation",
                areInputsValid("testUser", "test@user.com", "1234567890", "password", "Student"));

        assertFalse("Missing username should fail validation",
                areInputsValid("", "test@user.com", "1234567890", "password", "Student"));

        assertFalse("Role not selected should fail validation",
                areInputsValid("testUser", "test@user.com", "1234567890", "password", "Choose Your Role"));
    }


//     Test user data map creation

    @Test
    public void testUserDataMap() {
        Map<String, String> userData = buildUserData("testUser", "test@user.com", "1234567890", "password", "Student");
        assertEquals("testUser", userData.get("username"));
        assertEquals("test@user.com", userData.get("email"));
        assertEquals("1234567890", userData.get("phone"));
        assertEquals("password", userData.get("password"));
        assertEquals("Student", userData.get("role"));
    }


    //Test DatabaseHelper returns valid Task
    @Test
    public void testDatabaseHelperCreateUser() {
        Map<String, String> userData = buildUserData("testUser", "test@user.com", "1234567890", "password", "Student");
        Task<Void> task = DatabaseHelper.createUser(userData);
        assertNotNull("DatabaseHelper.createUser should return a non-null Task", task);
    }


    // Helper methods

    private boolean areInputsValid(String username, String email, String phone, String password, String role) {
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            return false;
        }
        if ("Choose Your Role".equals(role)) {
            return false;
        }
        return true;
    }

    private Map<String, String> buildUserData(String username, String email, String phone, String password, String role) {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("password", password);
        userData.put("role", role);
        return userData;
    }
}
