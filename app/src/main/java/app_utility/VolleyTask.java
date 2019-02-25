package app_utility;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.autochip.vehiclenoscanner.DisplayCardFragment.mAsyncInterface;

public class VolleyTask {

    private Context context;
    private int mStatusCode = 0;
    //private JSONObject jsonObject = new JSONObject();
    private HashMap<String, String> params;
    //private int position;
    String msg;
    String sDescription;

    private int ERROR_CODE = 0;

    ArrayList<String> alProducts;
    ArrayList<String> alSubCategory;
    ArrayList<String> alMainCategory;
    //ArrayList<Integer> alID;
    ArrayList<Integer> alProductName;
    ArrayList<Integer> alProductSubCategory;

    HashMap<Integer, String> hmImageAddressWithDBID = new HashMap<>();

    LinkedHashMap<String, HashMap<String, ArrayList<String>>> lhm = new LinkedHashMap<>();
    int stockFlag;
    String URL;
    JSONObject jsonObject = new JSONObject();

    private CircularProgressBar circularProgressBar;

    public VolleyTask(Context context, HashMap<String, String> params, String sCase, String URL) {
        this.context = context;
        setProgressBar();
        this.params = params;
        this.URL = URL;
        Volley(sCase);
    }

    private void Volley(String sCase) {
        switch (sCase) {
            case "SEND_VEHICLE_NUMBER":
                sendVehicleNo(URL);
                break;
        }
    }

    private void sendVehicleNo(String URL) {

        StringRequest request = new StringRequest(
                Request.Method.POST, URL, //BASE_URL + Endpoint.USER
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Success
                        onPostProductsReceived(mStatusCode, response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        msg = "No response from Server";
                    }
                }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return new JSONObject(params).toString().getBytes();
                //return params.toString().getBytes();
                //return params.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));

        /*request.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(request);

    }


    private void onPostProductsReceived(int mStatusCode, String response) {
        if (mStatusCode == 200) {
            Toast.makeText(context, "Vehicle No Saved", Toast.LENGTH_LONG).show();
            //mAsyncInterface.onAsyncTaskComplete("", 2);

            JSONObject jsonObject;
            int sResponseCode = 0;
            //Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            try {
                jsonObject = new JSONObject(response);
                String sResult = jsonObject.getString("result");
                jsonObject = new JSONObject(sResult);
                sResponseCode = jsonObject.getInt("response_code");
            } catch (Exception e) {
                /*ERROR_CODE = 900;
                msg = "No IDS matched";
                e.printStackTrace();
                sendMsgToActivity();*/
                return;
            }
            if (sResponseCode == 0) {
                msg = "Unable to connect to server, please try again later";
                sendMsgToActivity();
                return;
            }

            switch (sResponseCode) {
                case 201: //success
                    ERROR_CODE = 201;

                    try {
                        //sDescription = jsonObject.getString("description");
                        msg = jsonObject.getString("message");
                        JSONArray jsonArray = new JSONArray(msg);

                        sendMsgToActivity();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ERROR_CODE = 901;
                        msg = "Unable to reach server, please try again";
                        sendMsgToActivity();
                    }
                    break;
            }
        }
        if (circularProgressBar != null && circularProgressBar.isShowing()) {
            circularProgressBar.dismiss();
        }
    }


    private void setProgressBar() {
        circularProgressBar = new CircularProgressBar(context);
        circularProgressBar.setCanceledOnTouchOutside(false);
        circularProgressBar.setCancelable(false);
        circularProgressBar.show();
    }

    private void sendMsgToActivity() {
        try {
            //MainActivity.onFragmentInteractionListener.onFragmentMessage("UPDATE_HOME_BUTTON", 0, "", "");
            //onServiceInterface.onServiceCall("RFID", ERROR_CODE, String.valueOf(this.jsonObject.get("rfids")), msg, alID, alData);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
