package kenneth.jf.siaapp;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconManager;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Created by User on 10/11/2016.
 */

public class changePass extends Fragment {
    View myView;

    //default true
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.change_pass, container, false);

        return myView;
    }


    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();
    public String oldPass = myView.findViewById(R.id.oldPass).toString();
    public String newPass = myView.findViewById(R.id.newPass).toString();
    String returnObj;
    private class changePassword extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {
                JSONObject obj = new JSONObject();
                obj.put("password", newPass);
                obj.put("oldpassword",oldPass);
                //obj.put("discount", "12345");
                HttpEntity<String> request2 = new HttpEntity<String>(obj.toString(), ConnectionInformation.getInstance().getHeaders());
                Log.d("DISCOUNT REQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                String url2 = "https://" + url + "/user/changePassword";

                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<String> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, String.class);
                returnObj = responseEntity.getBody();
                System.out.println("RESPONSE ENTITY: " + responseEntity.getBody());


            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
                Toast.makeText(getActivity(), "Something wrong with changing password", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        protected void onPostExecute(String greeting) {
            Log.d("TAG", "DO POST EXECUTE");

        }
    }
}
