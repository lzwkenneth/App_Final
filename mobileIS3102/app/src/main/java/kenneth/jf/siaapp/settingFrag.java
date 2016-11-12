package kenneth.jf.siaapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Created by User on 10/11/2016.
 */

public class settingFrag extends Fragment{
    View myView;
    BeaconManager mBeaconManager ;
    ListView mainListView;
   //default true
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.setting_frag, container, false);
        setBeaconState();
        /*Button toChangePass = (Button) myView.findViewById(R.id.changePassword);
        toChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),dashboard.class);
                intent.putExtra("key2", "changePass");
                startActivity(intent);
            }
        });*/
        return myView;
    }
    public void setBeaconState(){
        Switch simpleSwitch = (Switch) myView.findViewById(R.id.turnBeaconOff_switch);
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getActivity());
        // check current state of a Switch (true or false).
        final dashboard activty = (dashboard)getActivity();
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked == true){
                    activty.startBeaconScanning();
                    //activty.onBeaconServiceConnect();
                    Toast.makeText(getActivity(), "Start Beacons Tracking...", Toast.LENGTH_LONG).show();
                 /*   try {
                        //mBeaconManager.startMonitoringBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));

                        Toast.makeText(getActivity(), "Start Beacons Tracking...", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                else {
                    activty.stopBeaconScanning();
                    Toast.makeText(getActivity(), "Stopped Beacons Tracking...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
