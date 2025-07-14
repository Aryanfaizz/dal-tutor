package com.example.daltutor.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.daltutor.R;
import com.example.daltutor.firebase.DatabaseHelper;
import com.example.daltutor.notifs.NotificationHandler;
import com.example.daltutor.units.DateTime;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TutorialPostingActivity extends AppCompatActivity {

    private String address;
    int year;
    int month;
    int day;
    int hour;
    int minute;

    private String dateAndTime;

    // The result launcher opens the map activity and returns the address as an Intent obj
    private final ActivityResultLauncher<Intent> openMapAndSetAddress =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                address = result.getData().getStringExtra("address");
                TextView address_text = findViewById(R.id.location_text);
                address_text.setText(address);
            });

    private void pickDateAndTime(){
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    hour = selectedHour;
                    minute = selectedMinute;
                },
                hour, minute, true);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    year = selectedYear;
                    month = selectedMonth + 1;
                    day = selectedDay;
                },
                year, month, day);
        datePickerDialog.setOnDismissListener(v -> {
            TextView locationText = findViewById(R.id.date_time_text);
            dateAndTime = DateTime.format_date(new int[]{year, month, day, hour, minute});
            locationText.setText(dateAndTime);
        });

        timePickerDialog.setOnDismissListener(v -> datePickerDialog.show());
        timePickerDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_posting);
        Button location_button = findViewById(R.id.location_button);
        location_button.setOnClickListener(v -> {
            Intent intent = new Intent(TutorialPostingActivity.this, LocationPickerActivity.class);
            openMapAndSetAddress.launch(intent);
        });
        Button dateTimeButton = findViewById(R.id.date_time_button);
        dateTimeButton.setOnClickListener(v -> pickDateAndTime());

        Spinner topicSpinner = findViewById(R.id.topic_spinner);
        LinkedList<String> topics = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.topics)));
        topics.add(0, "Select Topic");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, topics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(adapter);

        EditText durationEditText = findViewById(R.id.duration_text_box);
        EditText feeEditText = findViewById(R.id.fee_text_box);
        EditText descriptionEditText = findViewById(R.id.desc_text_box);

        Button post_button = findViewById(R.id.post_button);
        post_button.setOnClickListener(v -> {
            String duration = durationEditText.getText().toString().trim();
            String fee = feeEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String topic = topicSpinner.getSelectedItem().toString().trim();

            if (duration.isEmpty() || fee.isEmpty() || description.isEmpty() || topic.isEmpty()) {
                Toast.makeText(
                        TutorialPostingActivity.this, "Posting failed. Make sure all fields are filled.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (address == null) {
                address = "";
            }

            if (dateAndTime == null) {
                dateAndTime = "";
            }

            Map<String, String> postingData = new HashMap<>();
            postingData.put("address", address);
            postingData.put("datetime", dateAndTime);
            postingData.put("duration", duration);
            postingData.put("topic", topic);
            postingData.put("fee", fee);
            postingData.put("description", description);
            postingData.put("tutor", getIntent().getStringExtra("USERNAME"));

            DatabaseHelper.createPosting(postingData, getApplicationContext())
                    .addOnSuccessListener(a -> {
                        Toast.makeText(TutorialPostingActivity.this,
                                "Tutorial session successfully posted",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(TutorialPostingActivity.this,
                                "Tutorial sessions posting failed",
                                Toast.LENGTH_LONG).show();
                        finish();
                    });
        });
    }
}
