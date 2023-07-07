package com.example.selloldassignment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    Button setTime, setRingtone;
    EditText editText;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editText = (EditText) findViewById(R.id.editText);
        setTime = (Button) findViewById(R.id.setTime);
        setRingtone = (Button) findViewById(R.id.setRingtone);

        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarm();
            }
        });

        setRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRingtonePicker();

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isNotificationPermissionGranted()) {
            showNotificationPermissionDialog();
        }


    }

    private boolean isNotificationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            return notificationManager.areNotificationsEnabled();
        }
        return true;
    }

    private void showNotificationPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission")
                .setMessage("Please grant permission to receive notifications.")
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestNotificationPermission();
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());

            startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setAlarm() {
        String numberString = editText.getText().toString();
        String notificationSoundUri = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                .getString("NotificationSound", null);
        if (numberString.isEmpty()) {
            Toast.makeText(this, "Please enter seconds", Toast.LENGTH_SHORT).show();
            return;
        }

        int seconds = Integer.parseInt(numberString);
        if (seconds <= 0) {
            Toast.makeText(this, "Please enter a valid positive number.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (notificationSoundUri != null) {
            Intent alarmIntent = new Intent(this, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            long timeInMillis = System.currentTimeMillis() + (seconds * 1000);
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            Toast.makeText(this, "Notification set", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please select a notification sound", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRingtonePicker() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String savedNotificationSound = preferences.getString("NotificationSound", null);

        Uri notificationUri = null;
        if (savedNotificationSound != null) {
            notificationUri = Uri.parse(savedNotificationSound);
        }

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notificationUri);
        startActivityForResult(intent, 123);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                Uri notificationUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (notificationUri != null) {
                    saveNotificationSound(notificationUri.toString());
                    Ringtone ringtone = RingtoneManager.getRingtone(this, notificationUri);
                    String title = ringtone.getTitle(this);
                    Toast.makeText(this, "Notification sound set to \"" + title + "\"", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    private void saveNotificationSound(String soundUri) {
        getSharedPreferences("AppPreferences", MODE_PRIVATE)
                .edit()
                .putString("NotificationSound", soundUri)
                .apply();
    }

}
