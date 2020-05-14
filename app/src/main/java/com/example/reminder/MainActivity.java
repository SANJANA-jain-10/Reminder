package com.example.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.reminder.ui.main.SectionsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String TAG = "MainActivity";
    EditText et;
    ListView lv;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    static int stage_variable=0;
    static String values = "";
    public TextToSpeech texttoSpeech;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //microphone permission
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        //text to speech
        texttoSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });

        //VoiceInput
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values = "";
                startVoiceInput();
            }
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Do you want to add a reminder?");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("Do you want to add a reminder?", TextToSpeech.QUEUE_FLUSH,null,null);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stage_variable=1;

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
        }

    }


    private void add_reminder() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What's the reminder?");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("What's the reminder?", TextToSpeech.QUEUE_FLUSH, null, null);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stage_variable=2;
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    private void add_date() {

       DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("Please select a date.", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        datePickerDialog.show();


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // add date to database
        String reminder_date = dayOfMonth + "/" + month + "/" + year;
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/reminder_id/reminder_date");
        myRef.setValue(reminder_date);
    }

    private void add_time() {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("Please select the time.", TextToSpeech.QUEUE_FLUSH, null, null);
        }

        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // add this to database
        String reminder_time = hourOfDay + ":" + minute;
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/reminder_id/reminder_time");
        myRef.setValue(reminder_time);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                                            if (resultCode == RESULT_OK && null != data) {
                                                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                                                if(stage_variable == 1){
                                                    String choice = result.get(0);
                                                    if(choice.equals("yes"))
                                                        add_reminder();
                                                    else
                                                    {
                                                        //if "no" , then some other action
                                                    }

                                                }
                                                else if(stage_variable == 2){
                                                    String reminder_desc = result.get(0);

                                                    database = FirebaseDatabase.getInstance();
                                                    DatabaseReference myRef = database.getReference("/reminder_id/reminder_desc");
                                                    myRef.setValue(reminder_desc);

                                                    add_date();
                                                    add_time();
                                                    stage_variable = 3;
                                                }

                                                if(stage_variable == 3){
                                                    // retrieve the collected data n repeat to the user
                                                    // Read from the database
                                                    database = FirebaseDatabase.getInstance();
                                                    DatabaseReference myRef = database.getReference("/reminder_id");

                                                    myRef.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                                                // TODO: handle the post
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            // Getting Post failed, log a message
                                                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                                            // ...
                                                        }
                                                    });

                                                    /*    public void onDataChange(DataSnapshot dataSnapshot) {
                                                            // This method is called once with the initial value and again
                                                            // whenever data at this location is updated.
                                                            String value = dataSnapshot.getValue(String.class);
                                                            value = "You have set a reminder for "+ value;
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                                texttoSpeech.speak(value , TextToSpeech.QUEUE_FLUSH, null, null);
                                                            }

                                                        }*/

                                                }

                                            }
                                            break;
                                        }

        }
    }



}