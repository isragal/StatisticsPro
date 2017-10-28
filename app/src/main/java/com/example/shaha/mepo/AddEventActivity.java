package com.example.shaha.mepo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class AddEventActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private static final int RC_PICK_IMAGE = 1;
    String[] options;
    private Calendar calendar;
    private int year, month, day;
    private ImageView addEventImg;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private final int EVENT_NAME = 0;
    private final int EVENT_ADDRESS = 1;
    private final int EVENT_TYPE = 2;
    private final int EVENT_START_TIME = 3;
    private final int EVENT_END_TIME = 4;
    private EditText timeEditText;
    private Calendar fromCal, untilCal; //stores the time of the event
    private boolean fromBtnClicked;
    private Date startTime,endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //get current time
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("Events");

        setupListeners();
    }

    private void setupListeners() {
        setUpDateBtnListener();
        setUpClockBtnListener();
        setUpSpinner();
        setUpEventImgListener();
        setUpAddEventBtnListener();
    }

    private void setUpClockBtnListener() {
        final ImageButton timeFromBtn = (ImageButton) findViewById(R.id.add_event_time_from_btn);
        ImageButton timeUntilBtn = (ImageButton) findViewById(R.id.add_event_time_until_btn);
        timeFromBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromBtnClicked = true;
                timeEditText = (EditText) findViewById(R.id.add_event_time_from_edit_text);
                openTimePicker();
            }
        });
        timeUntilBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromBtnClicked = false;
                timeEditText = (EditText) findViewById(R.id.add_event_time_until_edit_text);
                openTimePicker();
            }
        });
    }

    private void openTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(AddEventActivity.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true //24 hours
        );
        tpd.show(getFragmentManager(), "TimePickerDialog");
    }

    private void setUpAddEventBtnListener() {
        FloatingActionButton addEventFab = (FloatingActionButton) findViewById(R.id.add_event_fab_submit_event);
        addEventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Object> eventData = getEventInfo();
                AddEventToDataBase(eventData);
            }
        });
    }

    private void AddEventToDataBase(ArrayList<Object> eventData) {
        MepoEvent event = new MepoEvent(eventData.get(EVENT_NAME).toString(), eventData.get(EVENT_TYPE).toString(), (Date)eventData.get(EVENT_START_TIME), (Date)eventData.get(EVENT_END_TIME), eventData.get(EVENT_ADDRESS).toString());
        mDatabaseReference.push().setValue(event);
        Toast.makeText(AddEventActivity.this, "Event added go have fun", Toast.LENGTH_SHORT).show();
        finish();
    }

    private ArrayList<Object> getEventInfo() {
        ArrayList<Object> eventData = new ArrayList<>();
        //check if all required fields are not empty
        if(isValid()) {
            eventData.add(((EditText) findViewById(R.id.add_event_event_name)).getText().toString());
            eventData.add(((EditText) findViewById(R.id.add_event_edit_text_address)).getText().toString());
            eventData.add(((Spinner) findViewById(R.id.add_event_type_spinner)).getSelectedItem().toString());
            eventData.add(startTime);
            eventData.add(endTime);
            return eventData;
        }else{
            return null;
        }
    }

    private boolean isValid() {
        if(((EditText) findViewById(R.id.add_event_event_name)).getText().toString().equals("")){
            return false;
        }
        if(((EditText) findViewById(R.id.add_event_edit_text_address)).getText().toString().equals("")){
            return false;
        }
        if(startTime==null){
            return false;
        }
        if(endTime==null){
            return false;
        }
        return true;
    }

    private void setUpEventImgListener() {
        addEventImg = (ImageView) findViewById(R.id.add_event_img);
        addEventImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), RC_PICK_IMAGE);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setUpDateBtnListener() {
        Button dateBtn = (Button) findViewById(R.id.add_event_date_btn);
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(1);
            }
        });
    }

    private DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            showDate(year, month + 1, day);
        }
    };

    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            return new DatePickerDialog(this, mDateListener, year, month, day);
        }
        return super.onCreateDialog(id);
    }


    private void showDate(int year, int month, int days) {
        //initiate the calenders
        fromCal = Calendar.getInstance();
        untilCal = Calendar.getInstance();
        //update the calender
        fromCal.set(Calendar.YEAR,year);
        fromCal.set(Calendar.MONTH,month);
        fromCal.set(Calendar.DAY_OF_MONTH,days);
        untilCal.set(Calendar.YEAR,year);
        untilCal.set(Calendar.MONTH,month);
        untilCal.set(Calendar.DAY_OF_MONTH,days);
        //create the date string and update the edit text
        String dateStr = days + "/" + month + "/" + year;
        EditText eventDateEditText = (EditText) findViewById(R.id.event_date_edit_text);
        eventDateEditText.setText(dateStr);
    }

    private void setUpSpinner() {
        options = getResources().getStringArray(R.array.add_event_types);
        Spinner eventTypeSpinner = (Spinner) findViewById(R.id.add_event_type_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options);
        eventTypeSpinner.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == RC_PICK_IMAGE) {
                Uri selectedImgUri = data.getData();
                Bitmap myImg = BitmapFactory.decodeFile(selectedImgUri.getPath());
                addEventImg.setImageBitmap(myImg);
                addEventImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        //update the edit text to show the time
        String timeStr = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
        timeEditText.setText(timeStr);
        Calendar timeCal;

        if(fromBtnClicked){
            //the user clicked on the form clock dialog
            timeCal = fromCal;
        }else{
            timeCal = untilCal;
        }

        timeCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
        timeCal.set(Calendar.MINUTE,minute);
        timeCal.set(Calendar.SECOND,second);

        if(fromBtnClicked) {
            startTime = timeCal.getTime();
        }else{
            endTime = timeCal.getTime();
        }
    }
}
