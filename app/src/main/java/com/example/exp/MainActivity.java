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

import java.net.CacheRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    String msgData = "";
    ListView lv;
    Spinner month;

    ArrayAdapter<String> adapter;
    ArrayList<String> lst1 = new ArrayList<>();
    static String amts = "";
    String avail = "";
    static String amtsfordisplay = "";
    static boolean isdebited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = findViewById(R.id.msz);
        month = findViewById(R.id.month);
        setmonthspinner();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
        } else {
            String SelectedDate[] = month.getSelectedItem().toString().split(" ", 2);
            msgData = getAllSms(this, SelectedDate[0], SelectedDate[1]);
        }
    }

    private void setmonthspinner() {
        ArrayList<String> months = new ArrayList<>();
        Calendar cc = new GregorianCalendar();
        cc.setTime(new Date());
        for (int i = 0; i < 24; i++) {
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
                Toast.makeText(parent.getContext(), "Selected: " + monthname, Toast.LENGTH_LONG).show();
                String SelectedDate[] = month.getSelectedItem().toString().split(" ", 2);
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
                } else {
                    msgData = getAllSms(MainActivity.this, SelectedDate[0], SelectedDate[1]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    public String getAllSms(Context context, String month, String year) {
        ArrayList<String> lst = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;
        int count = 0;
        String s = "";
        String ban = "";
        Pattern regEx = Pattern.compile("(?=.*[Aa]ccount.*|.*[Aa]/[Cc].*|.*[Aa][Cc][Cc][Tt].*|.*[Cc][Aa][Rr][Dd].*)(?=.*[Cc]redit.*|.*[Dd]ebit.*)(?=.*[Ii][Nn][Rr].*|.*[Rr][Ss].*)");


        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String bodylowercase = body.toLowerCase();
                    Date dateFormat = new Date(Long.valueOf(smsDate));
                    long millisecond = Long.parseLong(smsDate);

                    String dateString = DateFormat.format("dd/MMM/yyyy", new Date(millisecond)).toString();
                    String type;

                    String splitedate[] = dateString.split("/");
                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = "inbox";

                            if (number.length() == 10 || number.length() == 13) {
                                s = s + "NULL message";
                            } else {
                                Matcher m = regEx.matcher(bodylowercase);
                                amts = "";
                                amtsfordisplay = "";


                                if (m.find()) {
                                    Toast.makeText(context, "" + m.group(), Toast.LENGTH_SHORT).show();
                                    if (splitedate[1].trim().equals(month) && splitedate[2].trim().equals(year)) {
                                        if (bodylowercase.contains("debited") || bodylowercase.contains("paid")) {
                                            amts = getamts(bodylowercase, "debited");
                                            avail = getavail(bodylowercase);
                                        }
                                        //abhi credited ka
                                        else if (bodylowercase.contains("credited") || bodylowercase.contains("received")) {
                                            amts = getamts(bodylowercase, "credited");
                                            avail = getavail(bodylowercase);
                                        }
                                        lst.add(dateString + " " + number + " " + body + " " + amts + " " + avail);
                                        s = s + dateString + number + body;
                                    }


                                    //debit wale transactions
                                    if (body.contains("debited") || body.contains(" debited ")) {
                                        count++;
                                        lst1.add("debited " + count);
                                    }
                                }
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

            }
            c.close();

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, lst);
        lv.setAdapter(adapter);

        return s;
    }

    private String getavail(String body) {
        String availAmtfordisplay = "";
        String availAmt = "";
        String[] avail = {};
        String amt[]={};
        boolean toggle=false;
        if (body.contains("inr") && body.contains("bal")) {
          amt = body.split("inr", 2);
            //ye agar balance inr me h to
            if (amt[1].contains("inr")) {
                avail =amt[1].split("inr", 2);
                toggle=true;
            }
            //ye agar balance rs me h to
            else if (amt[1].contains("rs")) {
                 avail=amt[1].split("rs", 2);
                 toggle=true;
            }
        } else if (body.contains("rs") && body.contains("bal")) {
              amt = body.split("rs", 2);
            if (amt[1].contains("inr")) {
                avail = amt[1].split("inr", 2);
                toggle=true;
            } else if (amt[1].contains("rs")) {
                avail = amt[1].split("rs", 2);
                toggle=true;
            }
        }
        if(toggle) {
            for (int i = 0; i < avail[1].length(); i++) {
                if (avail[1].charAt(i) >= 48 && avail[1].charAt(i) <= 57 || avail[1].charAt(i) == ' ' || avail[1].charAt(i) == ',' || avail[1].charAt(i) == '.') {
                    if (avail[1].charAt(i) == ',') {
                        availAmtfordisplay = availAmtfordisplay + avail[1].charAt(i);
                    } else {
                        availAmt = availAmt + avail[1].charAt(i);
                        availAmtfordisplay = availAmtfordisplay + avail[1].charAt(i);
                    }
                } else {
                    break;
                }
            }
            //ye akhri wala dot htane k liye
            availAmt = availAmt.substring(0, availAmt.length() - 2);
        }//ye available amount print krne k liye
        availAmt = "\navailable amount" + availAmt;
        return availAmt;
    }
//
//    private String getamts(String bodylowercase, String creordeb) {
//        ArrayList<Double> ans = extract(bodylowercase);
//        //System.out.println(ans);
//        String ans1="";
//        for (int i = 0; i < ans.size(); i++) {
//            if (i == 0) {
//                ans1=creordeb+" amount is " + ans.get(i);
//            } else if (i == 1) {
//                ans1=ans1+"remaining amount is " + ans.get(i);
//            }
//        }
//        return ans1;
//    }
//
//    public static ArrayList<Double> extract(String s) {
//
//        String[] sarr = s.split(" ");
//
//        isdebited = false;
//
//        ArrayList<Double> results = new ArrayList<>();
//        for(String val:sarr) {
//
//            if(val.equals("debited"))
//                isdebited = true;
//
//            if(val.length()>0)
//                if(val.charAt(0)>=48 && val.charAt(0)<=(48+9))
//                    if(isNumber(val))
//                        results.add(makeNum(val));
//        }
//        return results;
//    }
//
//    public static boolean isNumber(String s) {
//        boolean Num = true;
//        String num = "";
//        double ans = 0.0;
//        for(int i = 0 ; i <s.length();i++)
//        {
//            if(!((s.charAt(i)>=48 && s.charAt(i)<=(48+9)) || s.charAt(i)==',' || s.charAt(i)=='.'))
//            {
//                Num = false;
//                break;
//            }
//        }
//
//        return Num;
//    }
//
//    public static double makeNum(String s) {
//        String num = "";
//        for(int i = 0 ; i < s.length();i++) {
//            if(s.charAt(i)!=',')
//                num = num + s.charAt(i);
//        }
//
//        double ans = 0.0;
//        if(num.charAt(num.length()-1)=='.')
//            num = num.substring(0,num.length()-1);
//
//        ans =  (Double.parseDouble(num));
//        return ans;
//    }
//}

    private String getamts(String bodylowercase, String creordeb) {
        String amt[] = {};
        amtsfordisplay = "";
        if (bodylowercase.contains("inr")) {
            amt = bodylowercase.split("inr", 2);
        } else if (bodylowercase.contains("rs")) {
            amt = bodylowercase.split("rs", 2);
        }
        for (int i = 0; i < amt[1].length(); i++) {
            if (amt[1].charAt(i) >= 48 && amt[1].charAt(i) <= 57 || amt[1].charAt(i) == ' ' || amt[1].charAt(i) == ',' || amt[1].charAt(i) == '.') {
                if (amt[1].charAt(i) == ',') {
                    amtsfordisplay = amtsfordisplay + amt[1].charAt(i);
                } else {
                    amts = amts + amt[1].charAt(i);
                    amtsfordisplay = amtsfordisplay + amt[1].charAt(i);
                }
            } else {
                break;
            }
        }
        amts = "\n" + creordeb + " amount" + amts;
        return amts;
    }


}