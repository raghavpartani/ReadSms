package com.example.exp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    String msgData = "";
    ListView lv;

    //ye bhi add krna
    Spinner month;

    ArrayAdapter<String>adapter;
    ArrayList<String> lst1=new ArrayList<>();
    ArrayAdapter<String>adapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=findViewById(R.id.msz);

        month=findViewById(R.id.month);
        Calendar c = Calendar.getInstance();
        int curyear = c.get(Calendar.YEAR);
        int curmonth = c.get(Calendar.MONTH);

        ArrayList<String> months = new ArrayList<>();

        Calendar cc = new GregorianCalendar();
        cc.setTime(new Date());
        for(int i=0;i<24;i++) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM YYYY");
            months.add(sdf.format(cc.getTime()));   // NOW
            cc.add(Calendar.MONTH, -1);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, months);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        month.setAdapter(arrayAdapter);

        month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String monthname = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Selected: " + monthname,          Toast.LENGTH_LONG).show();
                String SelectedDate[]=month.getSelectedItem().toString().split(" ",2);
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
                }
                else {
                    msgData = getAllSms(MainActivity.this,SelectedDate[0],SelectedDate[1]);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
        }
        else {
            String SelectedDate[]=month.getSelectedItem().toString().split(" ",2);
            msgData = getAllSms(this,SelectedDate[0],SelectedDate[1]);
        }


    }
    public String getAllSms(Context context,String month,String year) {
       // Toast.makeText(context, ""+year+month, Toast.LENGTH_SHORT).show();
        ArrayList<String> lst=new ArrayList<>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;
        int count =0;
        String s="";
        String ban="";
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String bodylowercase=body.toLowerCase();
                    Date dateFormat= new Date(Long.valueOf(smsDate));
                    long millisecond = Long.parseLong(smsDate);

                    //new code
                    String dateString = DateFormat.format("dd/MMM/yyyy", new Date(millisecond)).toString();
                    String type;

                    String splitedate[]=dateString.split("/");

                    //yha tk
                    //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();

                 //if(splitedate[1].trim().equals(month)&&splitedate[2].trim().equals(year)){
                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = "inbox";
                            if(number.length() == 10|| number.length() == 13) {
                                s = s + "NULL message";
                            }
                            else{

                                if (body.contains("RS") || body.contains("Rs") || body.contains("rs") || body.contains("INR") || body.contains("Inr")) {

                                    if (body.contains("A/C ") || body.contains("A/c ") || body.contains("Ac ") || body.contains("a/c ") || body.contains("ac ") || body.contains("A/C") || body.contains("A/c") || body.contains(" Ac") || body.contains("a/c") || body.contains(" ac ") || body.contains("curr o/s")) {

                                        if (body.contains("credited ") || body.contains("debited ") || body.contains("Paid ") || body.contains("paid ") || body.contains("credited") || body.contains("debited") || body.contains("Avail") || body.contains("Avail ") || body.contains("spent") || body.contains("spent ")) {
                                       //ye wla if block lgana h tko
                                            if(splitedate[1].trim().equals(month)&&splitedate[2].trim().equals(year)) {
                                                int amount;
                                                String amts="";
                                                if(bodylowercase.contains("rs")){
                                                    String amt[]=bodylowercase.split("rs.",2);
                                                    for(int i=0;i<amt[1].length();i++){
                                                        if(amt[1].charAt(i)>=48&&amt[1].charAt(i)<=57||amt[1].charAt(i)==' '){
                                                           amts=amts+ amt[1].charAt(i);
                                                        }
                                                        else{
                                                            break;
                                                        }
                                                    }
                                                }
                                                lst.add(dateString + number + body+amts);
                                                s = s + dateString + number + body;
                                            }


                                            //bank ka naam print krega
                                            if(number.contains("IDFC")) {
                                                ban = ban + "IDFC Bank";
                                            }
                                            else if(number.contains("SBI")) {
                                                ban = ban + "SBI Bank";
                                            }
                                            else if(number.contains("HDFC")) {
                                                ban = ban + "HDFC Bank" ;
                                            }
                                            else if(number.contains("ICICI")) {
                                                ban = ban + "ICICI Bank";
                                            }
                                            else {
                                                ban = ban + "NULL Bank";
                                            }
                                            // bank ka naam wala khtm

                                            //debit wale transactions
                                            if (body.contains("debited") || body.contains(" debited ")) {
                                                count++;
                                                lst1.add("debited " + count);
                                            }
                                        }
                                        //cred wale ka else
                                        else {
                                            s = s + "NULL message";
                                        }
                                    }
                                    //ac wale ka else
                                    else {
                                        s = s + "NULL message";
                                    }
                                }
                                //rs wale ka else
                                else {
                                    s = s + "NULL message";
                                }
                                // number ki length wale ka else
                            }

                            break;
//                        case Telephony.Sms.MESSAGE_TYPE_SENT:
//                            type = "sent";
//                            break;
//                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
//                            type = "outbox";
//                            break;
                        default:
                            break;
                    }
                    c.moveToNext();
                }
              //}
            }
            Toast.makeText(context, ""+ban, Toast.LENGTH_LONG).show();
            c.close();

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, lst);
        adapter1 = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, lst1);

        lv.setAdapter(adapter);

        return s;
    }



}