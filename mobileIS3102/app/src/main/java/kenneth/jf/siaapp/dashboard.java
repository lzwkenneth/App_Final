package kenneth.jf.siaapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.paypal.android.sdk.payments.PayPalService;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.paypal.android.sdk.payments.PayPalConfiguration;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.security.AccessController.getContext;


public class dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BeaconConsumer {
    //POWER SAVER
    private BackgroundPowerSaver backgroundPowerSaver;
    private static String QRresult;
    private static final String TAG = "SIA APP";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1;
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();
    public static final String PUBLISHABLE_KEY = "pk_test_zeyJXfY34INxorNSshxu83Q7";
    FragmentManager fragmentManager = getFragmentManager();
    String value = ""; // for event Id
    SliderLayout sliderShow;
    SliderLayout mDemoSlider;

    public static final int PAYPAL_REQUEST_CODE = 123;

    boolean test = true;

    //BEACON////////////
    BeaconManager mBeaconManager ;
    private static final String LOG_TAG = "MainActivity";
    private BluetoothManager btManager;
    Set<String> beaconIDs = new HashSet<>();
    private android.bluetooth.BluetoothAdapter btAdapter;
    private Handler scanHandler = new Handler();
    private int scan_interval_ms = 60000;
    String uuid;
    String promoMessage = "";
    private boolean isScanning = false;
    Region region = new Region("myId", null,null,null);
    //

    @Override
    protected void onStop() {
        sliderShow.stopAutoCycle();
        mBeaconManager.unbind(this);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "This is Android Beacon Library version: "+org.altbeacon.beacon.BuildConfig.VERSION_NAME);
        Log.d(TAG, "This is Android version N? :"+(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N));
        verifyBluetooth();
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        // set the duration of the scan to be 1.1 seconds
        mBeaconManager.setBackgroundScanPeriod(5100l);
        // set the time between each scan to be 1 minute (60 seconds)
        mBeaconManager.setBackgroundBetweenScanPeriod(60000l);
        //BEACON///////
        btManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        scanHandler.post(scanRunnable);

        setContentView(R.layout.activity_dashboard);

        sliderShow = (SliderLayout) findViewById(R.id.slider);

        TextSliderView textSliderView2 = new TextSliderView(this);
        textSliderView2.setScaleType(BaseSliderView.ScaleType.Fit)
                .description("Samsung Event")
                .image(R.drawable.samsung);


        TextSliderView textSliderView3 = new TextSliderView(this);
        textSliderView3.setScaleType(BaseSliderView.ScaleType.Fit)
                .description("Christmas Extravaganza")
                .image(R.drawable.xmas);

        TextSliderView textSliderView = new TextSliderView(this);
        textSliderView3.setScaleType(BaseSliderView.ScaleType.Fit)
                .description("Halloween")
                //.image("https://" + ConnectionInformation.getInstance().getUrl() + "/spongebob.jpg");
                .image(R.drawable.halloweensquare);

        TextSliderView textSliderView4 = new TextSliderView(this);
        textSliderView
                .description("Game of Thrones event")
                .image("http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");


        sliderShow.addSlider(textSliderView);
        sliderShow.addSlider(textSliderView2);
        sliderShow.addSlider(textSliderView3);
        // sliderShow.addSlider(textSliderView4);


        sliderShow.setDuration(3600);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //System.out.println("set vivisility gone");
            sliderShow.setVisibility(View.GONE);
            System.out.println("KEY2 VALUE IS:   " + extras.get("key2"));
            if (extras.getString("key2").equals("eventInfo")) {
                System.out.println("EVENT ID:             :" + extras.getString("eventId"));
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new EventShowInfo()).commit();
                Log.d("TAG EVENT INFO", extras.getString("key2"));
            } else if (extras.getString("key2").equals("eventTicketing")) {
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new viewTicketList()).commit();
         /*   } else if (extras.getString("key2").equals("passToCA")) {

                Intent intent = new Intent(this, ConfirmationActivity.class);
                intent.putParcelableArrayListExtra("arraylist",extras.getParcelableArrayList("arraylist"));
                intent.putStringArrayListExtra("arraylist2",extras.getStringArrayList("arraylist2"));
                startActivity(intent);
*/
            } else if (extras.getString("key2").equals("ticketSum")) {
                //from viewTicketList
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new paymentSummary()).commit();
            } else if (extras.getString("key2").equals("locationInfo")) {
                //from viewTicketList
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new locationInfo()).commit();
            } else if (extras.getString("key2").equals("passToPayPal")) {
                //from payment
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new PayPalFrag()).commit();
            } else if (extras.getString("key2").equals("purchasedTix")) {
                //from paymentSummary
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new purchasedTixList()).commit();
            } else if (extras.getString("key2").equals("showQRcode")) {
                //from paymentSummary
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new test()).commit();
            } else if (extras.getString("key2").equals("goToEventList")) {
                //directing to event list
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new eventlisting()).commit();
            } else if (extras.getString("key2").equals("changePass")) {
                //directing to event list
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new changePass()).commit();
            } else {
                //this key is inside confirmationActivity
                String value = extras.getString("key");
                System.out.println("Value of the QR code: " + value);
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new QRcode()).commit();
                setResult(value);
            }
        } else {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new homeLayout()).commit();
            Toast.makeText(this, "HOME", Toast.LENGTH_LONG).show();
        }

        //setContentView(R.layout.login);
        //power saver
        backgroundPowerSaver = new BackgroundPowerSaver(this);


        //paypal

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Nothing here yet!!!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }*/
        doDiscovery();
    }

    private void setResult(String value) {
        QRresult = value;
    }

    public String getResult() {
        return QRresult;
    }

    //Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        sliderShow.setVisibility(View.GONE);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new settingFrag()).commit();
            return true;
        } else if (id == R.id.action_logout) {
            new doLogout().execute();

            Intent intent = new Intent(this, login.class);
            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);


    /*    switch (item.getItemId()) {
            case R.id.home:
                return false; //The fragment will take care of it
            default:
                break;
        }*/
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        sliderShow.setVisibility(View.GONE);

/*        if (id == R.id.first_frag) {
            //this is for the profile setting
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new test()).commit();
            Toast.makeText(this, "Going into making requests", Toast.LENGTH_LONG).show();
            // Handle the camera action
            //can do report feedback using the camera
        } else*//* if (id == R.id.second_frag) {
            //gallery of items to order in flight
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new secondFrag()).commit();
            Toast.makeText(this, "Going into Beacon Tracking", Toast.LENGTH_LONG).show();
        } else if (id == R.id.third_frag) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new thirdFrag()).commit();
            Toast.makeText(this, "Going into Beacon Ranging", Toast.LENGTH_LONG).show();
            //
        } else*/
        if (id == R.id.inFlightOrderingFrag) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new inFlightOrderingFrag()).commit();
            Toast.makeText(this, "Going", Toast.LENGTH_LONG).show();
        } else if (id == R.id.home) {
            sliderShow.setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new homeLayout()).commit();
            Toast.makeText(this, "Back Home", Toast.LENGTH_LONG).show();
        } else if (id == R.id.eventlisting) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new eventlisting()).commit();
            Toast.makeText(this, "Event List", Toast.LENGTH_LONG).show();

/*        } else if (id == R.id.ticketing) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new PayPalFrag()).commit();
            Toast.makeText(this, "Payment with PayPal", Toast.LENGTH_LONG).show();*/
        } else if (id == R.id.ticketingList) {
            //display locations
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new purchasedTixList()).commit();
            Toast.makeText(this, "Ticketing List", Toast.LENGTH_LONG).show();
        } else if (id == R.id.locationlisting) {
            //display locations
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new location()).commit();
            Toast.makeText(this, "Location List", Toast.LENGTH_LONG).show();
/*        } else if (id == R.id.payment) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new SupportWalletFragment()).commit();
            Toast.makeText(this, "Payment", Toast.LENGTH_LONG).show();*/
        } else if (id == R.id.qr_scanner) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new QR_Scanner()).commit();
            Toast.makeText(this, "Scan For Discount Using QR Scanner", Toast.LENGTH_LONG).show();
        } else if (id == R.id.transactionHistory) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new transactionHistory()).commit();
            Toast.makeText(this, "TransactionHistory", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    //checking for permission
    public void doDiscovery() {
        int hasPermission = ActivityCompat.checkSelfPermission(dashboard.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            //continueDoDiscovery();
            return;
        }

        ActivityCompat.requestPermissions(dashboard.this,
                new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_COARSE_LOCATION_PERMISSIONS);
    }



    public void onRangingClicked(View view) {
        Intent myIntent = new Intent(this, RangingActivity.class);
        this.startActivity(myIntent);
    }

    /*@Override
    public void onResume() {
        super.onResume();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }
*/

    //verify bluetooth is on
    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }
    }


    Bundle result = null;

    public void saveData(int id, Bundle data) {
        // based on the id you'll know which fragment is trying to save data(see below)
        // the Bundle will hold the data
        result = new Bundle(data);

    }

    public Bundle getSavedData() {
        // here you'll save the data previously retrieved from the fragments and
        // return it in a Bundle
        return result;
    }

    //paypal
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, PayPalService.class));
        mBeaconManager.unbind(this);

    }


    private class doLogout extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {

                HttpEntity<String> request2 = new HttpEntity<String>(ConnectionInformation.getInstance().getHeaders());
                String url2 = "https://" + url + "/logout";
                Log.d("TAGTOSTRING ", request2.toString());
                ResponseEntity<Object> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, Object.class);
                Log.d("TAGGGGGGGGREQUEST", responseEntity.getStatusCode().toString());
                if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                    ConnectionInformation.getInstance().setIsAuthenticated(false);
                    Log.d("TAG", "Logged out inside async");
                }

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }


        protected void onPostExecute(String greeting) {

            Log.d("TAG", "DO POST EXECUTE");
            if (ConnectionInformation.getInstance().getAuthenticated()) {
                Log.d("TAG", "SERVER LOG OUT DID NOT WORK");
            } else {
                Log.d("TAG", "LOG OUT ON SERVER OK");
            }
        }

    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {

            if (isScanning) {
                if (btAdapter != null) {



                    btAdapter.stopLeScan(leScanCallback);
                }
            } else {


                    if (btAdapter != null) {
                        btAdapter.startLeScan(leScanCallback);
                    }

            }

            isScanning = !isScanning;

            scanHandler.postDelayed(this, scan_interval_ms);
        }
    };

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    private android.bluetooth.BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            int startByte = 2;
            boolean patternFound = false;
                if(test) {
                    while (startByte <= 5) {
                        if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                                ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                            patternFound = true;
                            break;
                        }
                        startByte++;
                    }
                }



            if (patternFound) {
                //Convert to hex String
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);

                //UUID detection
                uuid = hexString.substring(0, 8) + "-" +
                        hexString.substring(8, 12) + "-" +
                        hexString.substring(12, 16) + "-" +
                        hexString.substring(16, 20) + "-" +
                        hexString.substring(20, 32);

                // major
                final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

                // minor
                final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

                //Record in front
                Log.i(LOG_TAG, "UUID: " + uuid + "\\nmajor: " + major + "\\nminor" + minor);

                try {
                    new getMessage().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                /*if(String.valueOf(minor).equals("36128")){
                    setText1("Welcome to IFMS");

                    Toast.makeText(getActivity(), "WELCOME TO IFMS!!!", Toast.LENGTH_LONG).show();
                    setMessage("WELCOME TO IFMS");
                }
                else if(String.valueOf(minor).equals("1219")){
                    setText1("There is a whopping 50% off for the second top purchased at H&M!");
                    Toast.makeText(getActivity(), "There is a whopping 50% off for the second top purchased at H&M!", Toast.LENGTH_LONG).show();
                    setMessage("There is a whopping 50% off for the second top purchased at H&M!");
                }
                else{
                    setText1("Hurry Up! The 1 for 1 drink at Starbucks ends at 4pm!");
                    Toast.makeText(getActivity(), "Hurry Up! The 1 for 1 drink at Starbucks ends at 4pm!", Toast.LENGTH_LONG).show();
                    setMessage("Hurry Up! The 1 for 1 drink at Starbucks ends at 4pm!");
                }
*/
                System.out.println("beaconidsize: " + beaconIDs.size());
                if (!beaconIDs.contains(promoMessage) && !promoMessage.equals("")) {

                    System.out.println("beaconidsize: " + beaconIDs.size());

                    beaconIDs.add(promoMessage);

                    NotificationManager notificationManager = (NotificationManager)
                            getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

                    // Intent intent = new Intent(getApplicationContext(), dashboard.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
                    //PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);

                        Notification n = new Notification.Builder(getApplicationContext())
                                .setContentTitle("Message from IFMS App")
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentText(promoMessage)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSmallIcon(R.drawable.ic_feedback_black_24dp)
                                //.setContentIntent(pIntent)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setStyle((new Notification.BigTextStyle().bigText(promoMessage)))
                                .setAutoCancel(true).build();
                        System.out.println("issue notification");
                        notificationManager.notify(0, n);
                        System.out.println("issued notification");
                }
                //Toast.makeText(getActivity(), "Your Beacon Is Found!", Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * bytesToHex method
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    String textResult = "Scanning started";

    @Override
    public void onResume() {
        super.onResume();

     /* mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=fed8,m:2-2=00,p:3-3:-41,i:4-21v"));*/
        mBeaconManager.bind(this);

        // Detect the main Eddystone-UID frame:

    }

    private static final String BEACON_UUID = "5C3F2F21-20D1-11E6";
    private static final int BEACON_MAJOR = 1000;


    public void onBeaconServiceConnect() {

        //text.setText("on Beacon Service Connect");
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                try {
                    if (beacons.size() > 0) {
                        Log.i("ranging", "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                        //textResult = "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.";
                        //text.setText("THE DISTANCE IS: " + beacons.iterator().next().getDistance());
                    }

                    if (beacons.iterator().next().getDistance() < 1) {
                        // setText("Very Near!");
                        new AlertDialog.Builder(getApplicationContext())
                                .setTitle("Your luggage is near!")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    if (beacons.iterator().next().getDistance() > 1) {
                    }
                /*for (Beacon beacon: beacons) {
                    Log.i(TAG, "This beacon has identifiers:"+beacon.getId1()+", "+beacon.getId2()+", "+beacon.getId3());
                    TextView text2 = (TextView) myView.findViewById(R.id.thirdResult);
                    text2.setText("This beacon has identifiers:"+beacon.getId1()+", "+beacon.getId2()+", "+beacon.getId3());
                }*/
                } catch (Exception e) {
                    // Log.d("TAG",e.getMessage());
                }

            }


        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }


/*    @Override
    public Context getApplicationContext() {
        return this.getApplicationContext();
    }*/


    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }


    private class getMessage extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {
                JSONObject obj = new JSONObject();
                obj.put("beaconUUID", uuid);
                HttpEntity<String> request2 = new HttpEntity<String>(obj.toString(), ConnectionInformation.getInstance().getHeaders());
                String url2 = "https://" + url + "/getBeaconMessage";
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<String> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, String.class);
                if (!responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    System.out.println("RESPONSE BODY @@@@ " + responseEntity.getBody());
                    promoMessage = responseEntity.getBody();
                }


            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }

        protected void onPostExecute(String greeting) {
        }
    }


    public void stopBeaconScanning(){
        test = false;
    }
    public void startBeaconScanning(){
        test = true;
    }
}



