package kenneth.jf.siaapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;


/**
 * Created by User on 28/10/2016.
 */



// After payment is confirmed and the tickets are supposed to show the qr codes from the backend
public class purchasedTixList extends Fragment{
    View myView;
    ListView lv;
    int count = 1;
    ArrayList<TicketObject> list = new ArrayList<>();
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.purchased_ticketlist, container, false);
        try {
            new viewPurchasedTix().execute().get();


            lv = (ListView)myView.findViewById(R.id.purchasedTixList);
            TextView txv = (TextView) myView.findViewById(R.id.emptytv);
            lv.setEmptyView(txv);
           // TicketObject[] arr = (TicketObject[]) list.toArray();
            lv.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list));
            lv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TicketObject o = (TicketObject) lv.getItemAtPosition(position);
                    String qr = o.getTicketUUID();
                    Intent intent = new Intent(getActivity(), dashboard.class);
                    intent.putExtra("key2", "showQRcode");
                    intent.putExtra("qrcode", qr);
                    startActivity(intent);
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return myView;
    }

    private class viewPurchasedTix extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {

                HttpEntity<String> request2 = new HttpEntity<String>(ConnectionInformation.getInstance().getHeaders());
                Log.d("TAGGGGGGGGREQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                String url2 = "https://" + url + "/tixGetTix";


                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG",request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<TicketObject[]> responseEntity = restTemplate.exchange(url2, HttpMethod.GET, request2, TicketObject[].class);
                Log.d("TAG",responseEntity.getStatusCode().toString());


                for ( TicketObject o : responseEntity.getBody()){
                    TicketObject ticketObject = o;
                    list.add(ticketObject);

                   // System.err.println("Tickets: " + o.getTicketDetails());
                }
                Collections.sort(list, new Comparator<TicketObject>() {
                    public int compare(TicketObject s1, TicketObject s2) {
                        return (s1.getTicketDetails().compareTo(s2.getTicketDetails()));
                    }
                });

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }
        protected void onPostExecute(String greeting) {
            Log.d("TAG", "DO POST EXECUTE");
        }
    }


}
