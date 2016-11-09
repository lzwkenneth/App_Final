package kenneth.jf.siaapp;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fasterxml.jackson.databind.SerializationFeature;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.R.id.list;

/**
 * Created by User on 5/10/2016.
 */

public class inFlightOrderingFrag extends Fragment {
    View myView;
    private Spinner spinner1, spinner2;
    private Spinner eventListSpinner;
    private Button btnSubmit;
    private EditText feedbackBox;
    Map<String,Long> mapz = new HashMap<>();
    List<String> EventList = new ArrayList<>();
    private String feedbackCategory;
    private String eventSelection;
    private String spin2;
    private String feedback;
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.inflight, container, false);
        addListenerOnButton();
        addListenerOnSpinner1ItemSelection();
        addListenerOnSpinner2ItemSelection();

        addListenerOnEventListSpinner();
        addListenerOnSpinnerItemSelection();
        
        
        return myView;
    }



    private void addListenerOnEventListSpinner() {
        try {
            new viewAllEvents().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, EventList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventListSpinner.setAdapter(dataAdapter);
    }

    private void addListenerOnSpinnerItemSelection() {
        eventListSpinner = (Spinner) myView.findViewById(R.id.eventListSpinner);
        eventListSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }


    public void addListenerOnSpinner1ItemSelection() {
        spinner1 = (Spinner) myView.findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public void addListenerOnSpinner2ItemSelection() {
        //spinner2 = (Spinner) myView.findViewById(R.id.spinner2);
        //spinner2.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    // get the selected dropdown list value
    public void addListenerOnButton() {

        spinner1 = (Spinner) myView.findViewById(R.id.spinner1);
        //spinner2 = (Spinner) myView.findViewById(R.id.spinner2);
        btnSubmit = (Button) myView.findViewById(R.id.btnSubmit);
        feedbackBox = (EditText) myView.findViewById(R.id.seat);
        eventListSpinner = (Spinner) myView.findViewById(R.id.eventListSpinner);


        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                feedback = feedbackBox.getText().toString();
                feedbackCategory = String.valueOf(spinner1.getSelectedItem());
                eventSelection = String.valueOf(eventListSpinner.getSelectedItem());
//                spin2 = String.valueOf(spinner2.getSelectedItem());
                Toast.makeText(getActivity(),
                        "Feedback Sent!",
                        Toast.LENGTH_SHORT).show();

                new HttpRequestTask().execute();

                //Toast.makeText(this,"OnClickListener : ",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private class HttpRequestTask extends AsyncTask<Void, Void,String> {

        protected String doInBackground(Void... params) {
            Log.d("TAG","DO IN BACKGROUND");
            try {
                //POST
                JSONObject requestJ = new JSONObject();
                requestJ.put("category", feedbackCategory);
                requestJ.put("feedback", feedback);
                requestJ.put("eventId", mapz.get(eventSelection));
                Log.d("TAG",requestJ.toString());
                HttpEntity<String> request2 = new HttpEntity<String>(requestJ.toString(),ConnectionInformation.getInstance().getHeaders());
                Log.d("TAGGGGGGGGREQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                String url2 = "https://" + url + "/tixFeedback";
                Log.d("TAG","BEFORE POSTING");
                ResponseEntity<String> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, String.class);

                Log.d("TAGGGGGGGG",responseEntity.toString());

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }


        protected void onPostExecute(String greeting) {
            Log.d("TAG","DO POST EXECUTE");
        }

    }

    private class viewAllEvents extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {

                HttpEntity<String> request2 = new HttpEntity<String>(ConnectionInformation.getInstance().getHeaders());
                Log.d("TAGGGGGGGGREQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                String url2 = "https://" + url + "/tixViewAllEventsFeedback";

                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<EventListObject[]> responseEntity = restTemplate.exchange(url2, HttpMethod.GET, request2, EventListObject[].class);
System.out.println("LENGTH:" + responseEntity.getBody().length);
                for (EventListObject m : responseEntity.getBody()) {
                    EventList.add(m.getEventName());
                    mapz.put(m.getEventName(),m.getId());
                    //return list
                    Log.d("loopforeventlistobject", m.toString());
                }

                Collections.sort(EventList);


            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;

        }

        protected void onPostExecute(String greeting) {
            Log.d("TAG", "DO POST EXECUTE");
            Log.d("EVENT: ", String.valueOf(EventList.size()));
        }
    }


}