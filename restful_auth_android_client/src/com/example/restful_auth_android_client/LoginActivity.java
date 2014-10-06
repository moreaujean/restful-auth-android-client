package com.example.restful_auth_android_client;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	// change the following String value to your Heroku account or other API endpoint
	private final static String LOGIN_ENDPOINT = "http://funny-name-here.herokuapp.com/api/v1/sessions.json";
	
    /**
     * Tag used on log messages.
     */
    static final String TAG = "LoginActivity";	
	
	
	private SharedPreferences mPreferences;
	Button mLoginButton;
	private String mUserEmail;
	private String mUserPassword;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_login);
	    mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
	    
    	ActionBar actionBar = getActionBar();    
        actionBar.setDisplayShowTitleEnabled(true);        
        actionBar.setTitle("Login");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(255, 187, 51)));
                
	    mLoginButton = (Button) findViewById(R.id.loginButton);	    
	    mLoginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {										
				login();
			}
		});        
	}
	
	public void login() {
	    EditText userEmailField = (EditText) findViewById(R.id.userEmail);
	    mUserEmail = userEmailField.getText().toString();
	    EditText userPasswordField = (EditText) findViewById(R.id.userPassword);
	    mUserPassword = userPasswordField.getText().toString();

	    if (mUserEmail.length() == 0 || mUserPassword.length() == 0) {
	        // input fields are empty
	        Toast.makeText(this, "Please complete all the fields", Toast.LENGTH_LONG).show();
	        return;
	    } else {	        
	        login(LoginActivity.this, LOGIN_ENDPOINT);
	    }
	}
	
	void login(final Context context, String url){
		final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setTitle("Please Wait");
        pDialog.setMessage("Logging in...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
		
        JSONObject holder = new JSONObject();       
        JSONObject userObj = new JSONObject();
		try {		    
            userObj.put("email", mUserEmail);
            userObj.put("password", mUserPassword);
            holder.put("user", userObj);		    

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.POST, url, holder,
		   new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					pDialog.dismiss();					
					String token;
					try {
						token = response.getJSONObject("data").getString("auth_token");
		                SharedPreferences.Editor editor = mPreferences.edit();

		                // save the returned data into the SharedPreferences
		                editor.putString("AuthToken", token);	                
		                editor.putString("Email", mUserEmail);
		                editor.commit();

		                // redirect the user to appropriate activity and close this one
		                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		                startActivity(intent);
		                finish();						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
		}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
				    // TODO Auto-generated method stub
				}
		}){     
	        @Override
	        public Map<String, String> getHeaders() throws AuthFailureError { 
	                Map<String, String>  params = new HashMap<String, String>();  
	                params.put("Accept", "application/json");  
	                params.put("Content-Type", "application/json");
	                return params;  
	        }
	    };

		// Access the RequestQueue through your singleton class.
		VolleySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
        pDialog.show();
	}	
}