package com.nfcscanner;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;

import java.security.Security;
import java.util.Arrays;

public class NfcReaderModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private static final String TAG_DISCOVERED = "TAG_DISCOVERED";
    private static final String TAG_START_READING = "TAG_START_READING";
    private static final String TAG_READING_FAILED = "TAG_READING_FAILED";
    private static final String TAG_READING_SUCCESS = "TAG_READING_SUCCESS";

    private static final String LOG_TAG = "NfcReaderModule";
    Callback nfc_chip_status_callback;
    Callback nfc_status_callback;

    private TextView result_txt;
    private Button scan_btn;
    public BACKeySpec bacKey;
    public boolean is_scanning = false;

    @Override
    public void onHostResume() {
        if(nfc_status_callback != null) {
            NFCStatus nfc_status = checkForNFCChip();
            if(nfc_status == NFCStatus.NOT_EXIST)
                nfc_status_callback.invoke("NFCStatus.NOT_EXIST");
            else if(nfc_status == NFCStatus.DISABLED)
                nfc_status_callback.invoke("NFCStatus.DISABLED");
            else
                nfc_status_callback.invoke("NFCStatus.READY");
        }
    }

    @Override
    public void onHostPause() {

        stopNFCScan();
    }

    @Override
    public void onHostDestroy() {

    }

    enum NFCStatus {
        ENABLED,
        DISABLED,
        NOT_EXIST
    }

    private ReactApplicationContext reactContext;
    private Context context;

    public NfcReaderModule(ReactApplicationContext reactContext) {
        super(reactContext);

        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        context = reactContext;
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);
        Log.d(LOG_TAG, "NfcManager created");
    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
    }

    @Override
    public void onNewIntent(Intent intent) {

        Log.d(LOG_TAG, "onNewIntent " + intent);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
            if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {

                WritableMap writableMap = new WritableNativeMap();
                writableMap.putString("status", TAG_DISCOVERED);
                sendEvent(writableMap);

                writableMap = new WritableNativeMap();
                writableMap.putString("status", TAG_START_READING);
                sendEvent(writableMap);

                connectToPassport(tag);
            }
        }
    }

    @Override
    public String getName() {
        return "NfcReader";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void goToSettings(Callback callback) {
        nfc_status_callback = callback;
        this.reactContext.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
    }

    @ReactMethod
    public void getNFCStatus(Callback callback)
    {
        NFCStatus nfc_status = checkForNFCChip();

        if(nfc_status == NFCStatus.NOT_EXIST)
            callback.invoke("NFCStatus.NOT_EXIST");
        else if(nfc_status == NFCStatus.DISABLED)
            callback.invoke("NFCStatus.DISABLED");
        else
            callback.invoke("NFCStatus.READY");
    }

    @ReactMethod
    public void startDiscoveringTAG(String pnum, String pdob, String pexp, Callback callback) {
        reactContext.addActivityEventListener(this);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        try {
            bacKey = new BACKey(pnum.toUpperCase(), pdob, pexp);

            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(reactContext);
            if (adapter != null) {
                Intent intent = new Intent(reactContext.getApplicationContext(), this.getClass());
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(reactContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                String[][] filter = new String[][]{new String[]{"android.nfc.tech.IsoDep"}};
                adapter.enableForegroundDispatch(getCurrentActivity(), getPendingIntent(), null, filter);

                callback.invoke("discovering_started");
            }
        }
        catch (Exception e)
        {
            callback.invoke("scan_failed");
        }
    }

    private void sendEvent(@Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(RCTNativeAppEventEmitter.class)
                .emit("NfcReadingStatus", params);
    }

    @ReactMethod
    public void startNFCScan(String pnum, String pdob, String pexp, Callback callback) {
        nfc_chip_status_callback = callback;
        // TODO: Implement some actually useful functionality
        reactContext.addActivityEventListener(this);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        NFCStatus nfc_status = checkForNFCChip();

        if(nfc_status == NFCStatus.NOT_EXIST)
        {
            nfc_chip_status_callback.invoke("NFCStatus.NOT_EXIST");
        }
        else if(nfc_status == NFCStatus.DISABLED)
        {
            nfc_chip_status_callback.invoke("NFCStatus.DISABLED");
        }
        else {

            try {
                bacKey = new BACKey(pnum.toUpperCase(), pdob, pexp);

                NfcAdapter adapter = NfcAdapter.getDefaultAdapter(reactContext);
                if (adapter != null) {
                    Intent intent = new Intent(reactContext.getApplicationContext(), this.getClass());
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(reactContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    String[][] filter = new String[][]{new String[]{"android.nfc.tech.IsoDep"}};
                    adapter.enableForegroundDispatch(getCurrentActivity(), getPendingIntent(), null, filter);
                }
            }
            catch (Exception e)
            {
                nfc_chip_status_callback.invoke("scan_failed");
            }
        }
    }

    private PendingIntent getPendingIntent() {
        Activity activity = getCurrentActivity();
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(activity, 0, intent, 0);
    }

    public NFCStatus checkForNFCChip()
    {
        NfcManager manager = (NfcManager) reactContext.getApplicationContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter == null)
            return NFCStatus.NOT_EXIST;
        else if (!adapter.isEnabled())
            return NFCStatus.DISABLED;
        else
            return NFCStatus.ENABLED;
    }

    public void stopNFCScan()
    {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(reactContext);
        if (adapter != null) {
            adapter.disableForegroundDispatch(getCurrentActivity());
        }
    }

    public void scanFinished(String result)
    {
        if(result.equals("success"))
        {
            nfc_chip_status_callback.invoke("scan_success");
            //result_txt.setText("Passport confirmed");
            WritableMap writableMap = new WritableNativeMap();
            writableMap.putString("status", TAG_READING_SUCCESS);
            sendEvent(writableMap);
        }
        else
        {
            nfc_chip_status_callback.invoke("scan_failed");
            //result_txt.setText("Passport Error");

            WritableMap writableMap = new WritableNativeMap();
            writableMap.putString("status", TAG_READING_FAILED);
            sendEvent(writableMap);
        }
    }

    public void connectToPassport(Tag tag) {

        PassportService ps = null;
        try {
            IsoDep nfc = IsoDep.get(tag);
            CardService cs = CardService.getInstance(nfc);
            ps = new PassportService(cs);
            ps.open();

            ps.sendSelectApplet(false);

            ps.doBAC(bacKey);

            scanFinished("success");

        } catch (CardServiceException e) {
            scanFinished("error");
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
