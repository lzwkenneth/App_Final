
package kenneth.jf.siaapp;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;

import static kenneth.jf.siaapp.R.layout.transaction_history;

public class transactionHistory extends Fragment {
    View myView;
    TextView dsView;
    TextView eView;
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();
    String toOutput;
    // SweetAlertDialog errorAlert;
    //SweetAlertDialog successAlert;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(transaction_history, container, false);
        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading Transaction History");
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // scanQR(myView);
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }

                , 900);

        // ImageView imageView = (ImageView)myView.findViewById(R.id.ttttt);
        dsView = (TextView) myView.findViewById(R.id.transHistory);
        dsView.setMovementMethod(new ScrollingMovementMethod());
        eView = (TextView) myView.findViewById(R.id.eView);

        try {
            new viewTransactions().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if ( toOutput != null && !toOutput.equals("\"\"") && !toOutput.equals("")){
            eView.setVisibility(View.GONE);
        }
        dsView.setText(toOutput);


       /* Button scanner1 = (Button)myView.findViewById(R.id.scanner2);
        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBar(myView);
            }
        });*/


        return myView;
    }


    private class viewTransactions extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {
                HttpEntity<String> request2 = new HttpEntity<String>(ConnectionInformation.getInstance().getHeaders());
                Log.d("TAGGGGGGGGREQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                String url2 = "https://" + url + "/tixGetTransactionHistory";

                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                System.out.println(request2.toString());
                ResponseEntity<String> responseEntity = restTemplate.exchange(url2, HttpMethod.GET, request2, String.class);

                System.out.println("fuck jinfa" + responseEntity.getBody());
                    toOutput = responseEntity.getBody().replace("\\n", "\n");
                    toOutput = toOutput.substring(1,toOutput.length()-1);


                //toOutput = responseEntity.getBody();



            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
                Toast.makeText(getActivity(), "NO DISCOUNT STORED FOR THIS QR CODE", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        protected void onPostExecute(String greeting) {
            Log.d("TAG", "DO POST EXECUTE");

        }
    }


}