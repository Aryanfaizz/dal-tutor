package com.example.daltutor.firebase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.daltutor.core.Posting;
import com.example.daltutor.notifs.NotificationHandler;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {

    private static final DatabaseReference usersRef =
            FirebaseDatabase.getInstance().getReference("Users");

    private static final DatabaseReference postingsRef =
            FirebaseDatabase.getInstance().getReference("Postings");

    private static final DatabaseReference studentPreferredTutorsRef =
            FirebaseDatabase.getInstance().getReference("TutorPreferredList");

    private static final DatabaseReference tutorPreferredStudentsRef =
            FirebaseDatabase.getInstance().getReference("StudentPreferredList");

    private static ArrayList<String> postingIDs = new ArrayList<>();
    private static ArrayList<String> tutorUsernames = new ArrayList<>();

    private static final DatabaseReference tutorialRegistrationsRef =
            FirebaseDatabase.getInstance().getReference("TutorialRegistrations");

    /**
     * Adds a tutor to a student's preferred list.
     *
     * @param studentUsername The student's username.
     * @param tutorUsername   The tutor's username.
     * @return A Task object to handle success/failure callbacks.
     */
    public static Task<Void> addPreferredTutor(String studentUsername, String tutorUsername) {
        return studentPreferredTutorsRef.child(studentUsername).push().setValue(tutorUsername);
    }

    /**
     * Removes a tutor from a student's preferred list.
     *
     * @param studentUsername The student's username.
     * @param tutorKey        The Firebase-generated key for the preferred tutor entry.
     * @return A Task object to handle success/failure callbacks.
     */
    public static Task<Void> removePreferredTutor(String studentUsername, String tutorKey) {
        return studentPreferredTutorsRef.child(studentUsername).child(tutorKey).removeValue();
    }

    // Private constructor to prevent instantiation
    private DatabaseHelper() {
        throw new UnsupportedOperationException("");
    }

    /**
     * Gets reference for Tutor's Preferred Students list.
     * @return DatabaseReference for preferred students.
     * Gets reference for Student's Preferred Tutors list.
     *
     * @return DatabaseReference for preferred tutors.
     */
    public static DatabaseReference getStudentPreferredTutorsRef() {
        return studentPreferredTutorsRef;
    }

    /**
     * Adds a student to a tutor's preferred list.
     *
     * @param tutorUsername   The tutor's username.
     * @param studentUsername The student's username.
     * @return A Task object to handle success/failure callbacks.
     */
    public static Task<Void> addPreferredStudent(String tutorUsername, String studentUsername) {
        return tutorPreferredStudentsRef.child(tutorUsername).push().setValue(studentUsername);
    }

    /**
     * Removes a student from a tutor's preferred list.
     *
     * @param tutorUsername The tutor's username.
     * @param studentKey    The Firebase-generated key for the preferred student entry.
     * @return A Task object to handle success/failure callbacks.
     */
    public static Task<Void> removePreferredStudent(String tutorUsername, String studentKey) {
        return tutorPreferredStudentsRef.child(tutorUsername).child(studentKey).removeValue();
    }

    /**
     * Gets reference for Tutor's Preferred Students list.
     * @return DatabaseReference for preferred students.
     */
    public static DatabaseReference getPreferredStudentsRef() {
        return tutorPreferredStudentsRef;
    }

    /**
     * Creates a new user in the Firebase Realtime Database.
     * @param userData Map containing user data (username, email, etc.).
     * @return A Task object to handle success/failure callbacks.
     */
    public static Task<Void> createUser(Map<String, String> userData) {
        return usersRef.push().setValue(userData);
    }

    /**
     * Creates a new tutorial posting in the Firebase Realtime Database.
     * @param postingData Map containing tutorial details.
     * @return A Task object to handle success/failure callbacks.
     */
    public static Task<Void> createPosting(Map<String, String> postingData, Context context) {
        String postingId = postingsRef.push().getKey();
        NotificationHandler handler = new NotificationHandler(context);
        handler.sendNotification(postingId, postingData.get("tutor"));
        assert postingId != null;
        return postingsRef.child(postingId).setValue(postingData);
    }

    /**
     * return a Posting object from a db id
     *
     * @param id A unique ID generated by firebase
     * @return A Posting object
     */
    public static Posting getPostingFromID(String id, PostingCallback callback) {
        final String[] posting_fields = new String[7];
        final Posting[] posting = new Posting[1];
        postingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.getKey().equals(id)) {
                        posting_fields[0] = snap.child("topic").getValue().toString();
                        posting_fields[1] = snap.child("address").getValue().toString();
                        posting_fields[2] = snap.child("description").getValue().toString();
                        posting_fields[3] = snap.child("duration").getValue().toString();
                        posting_fields[4] = snap.child("fee").getValue().toString();
                        posting_fields[5] = snap.child("tutor").getValue().toString();
                        posting_fields[6] = snap.child("datetime").getValue().toString();

                        posting[0] = new Posting(posting_fields[0], posting_fields[1],
                                posting_fields[2], posting_fields[3], posting_fields[4],
                                posting_fields[5], posting_fields[6]);
                        callback.onPostingFound(posting[0]);
                        break;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Database error", String.valueOf(error));
            }

        });
        return posting[0];
    }

    public interface PostingCallback {
        void onPostingFound(Posting p);
    }

    public static ArrayList<String> getPostingIDs () {
        postingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postingIDs.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    postingIDs.add(snap.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ERROR", "Failed to load posting IDs");
            }
        });
        return postingIDs;
    }

    // TUTORIAL REGISTRATION: DatabaseHelper methods
    // registerForTutorial      adds student to tutorialRegistrations
    // isStudentRegistered      checks if studentUsername is in tutorialRegistrations
    // getRegistrationCount     returns number of students in a given tutorial posting
    public static Task<Void> registerForTutorial(String studentUsername, String postingId) {
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("timestamp", ServerValue.TIMESTAMP);
        return tutorialRegistrationsRef.child(postingId).child(studentUsername).setValue(registrationData);
    }

    public static void isStudentRegistered(String studentUsername, String postingId, RegistrationCheckCallback callback) {
        tutorialRegistrationsRef.child(postingId).child(studentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public interface RegistrationCheckCallback {
        void onResult(boolean isRegistered);
        void onError(String errorMessage);
    }

    public static void getRegistrationCount(String postingId, RegistrationCountCallback callback) {
        tutorialRegistrationsRef.child(postingId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onCountReceived((int) snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public interface RegistrationCountCallback {
        void onCountReceived(int count);
        void onError(String errorMessage);
    }

    /**
     * get all preferred tutors for a given student username
     * @return A task with the result of an array list of tutor usernames
     */
    public static Task<ArrayList<String>> getPreferredTutors(String studentUsername) {
        tutorUsernames.clear();
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();
        studentPreferredTutorsRef.child(studentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()){
                    tutorUsernames.add(snap.getValue().toString());
                }
                tcs.setResult(tutorUsernames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ERROR", "Tutor usernames");
            }
        });
        return tcs.getTask();
    }
}
