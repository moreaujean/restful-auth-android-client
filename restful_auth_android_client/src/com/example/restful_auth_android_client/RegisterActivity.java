package com.example.restful_auth_android_client;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity {
	
	private static final String SHARED_PREFERENCES_FILE_NAME = "CurrentUser";
	// change the following String value to your Heroku account or other API endpoint
	private final static String REGISTRATION_ENDPOINT = "http://funny-name-here.herokuapp.com/api/v1/registrations";
	private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String EMAIL_ERROR_MSG = "Enter a valid email address";
	private static final String PASSWORD_ERROR_MSG = "Enter at least 8 characters";
	private static final String PASSWORD_CONFIRMATION_ERROR_MSG = "Match the password above";
	private static final String TELEPHONE_ERROR_MSG = "Enter a valid phone number";	
	
    /**
     * Tag used on log messages.
     */
    private static final String TAG = "RegisterActivity";
    
    private Context context = RegisterActivity.this;
	private SharedPreferences mPreferences;
	private EditText mUserPhoneNumberEditText;
	private Button mRegisterButton;
	private String mUserPhoneNumber;
	private String mUserEmail;
	private String mUserPassword;
	private String mUserPasswordConfirmation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_register);
	    mPreferences = getPreferences(context);
	    
	    // Display standard ActionBar
    	ActionBar actionBar = getActionBar();    
        actionBar.setDisplayShowTitleEnabled(true);        
        actionBar.setTitle("Register");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(255, 187, 51)));
	    
        // Capture user telephone and display it to screen 
	    TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	    String mPhoneNumber = tMgr.getLine1Number();	    	    
	    mUserPhoneNumberEditText = (EditText) findViewById(R.id.userPhoneNumber);
	    mUserPhoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
	    mUserPhoneNumberEditText.setText(mPhoneNumber);
	    
	    mRegisterButton = (Button) findViewById(R.id.registerButton);	    
		mRegisterButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {										
				registerNewAccount();
			}
		});
	}
	
    /**
     * Validates registration information. Displays errors if found.
     * Spawns background registration process if no errors found.
     * 
     */    
	public void registerNewAccount() {
		int numErrors = 0;
		
	    EditText userEmailEditText = (EditText) findViewById(R.id.userEmail);
	    mUserEmail = userEmailEditText.getText().toString();
	    
	    EditText userPasswordEditText = (EditText) findViewById(R.id.userPassword);
	    mUserPassword = userPasswordEditText.getText().toString();
	    
	    EditText userPasswordConfirmationEditText = (EditText) findViewById(R.id.userPasswordConfirmation);
	    mUserPasswordConfirmation = userPasswordConfirmationEditText.getText().toString();
	    
	    mUserPhoneNumber = mUserPhoneNumberEditText.getText().toString().replaceAll("[^\\d]", "");;

	    if (!Pattern.matches(EMAIL_REGEX, mUserEmail) || mUserEmail.length() == 0){
	    	userEmailEditText.setHint(EMAIL_ERROR_MSG);
	    	userEmailEditText.setHintTextColor(Color.rgb(255, 0, 0));
	    	numErrors++;
	    }
	    
	    if ( mUserPassword.length() < 8){
	    	userPasswordEditText.setHint(PASSWORD_ERROR_MSG);
	    	userPasswordEditText.setHintTextColor(Color.rgb(255, 0, 0));
	    	numErrors++;
	    }
	    
	    if ( !mUserPassword.equals(mUserPasswordConfirmation)){
	    	userPasswordConfirmationEditText.setHint(PASSWORD_CONFIRMATION_ERROR_MSG);
	    	userPasswordConfirmationEditText.setHintTextColor(Color.rgb(255, 0, 0));
	    	numErrors++;
	    }	    
	    
	    if (!PhoneNumberUtils.isGlobalPhoneNumber(mUserPhoneNumber)){
	    	userPasswordEditText.setHint(TELEPHONE_ERROR_MSG);
	    	userPasswordEditText.setHintTextColor(Color.rgb(255, 0, 0));
	    	numErrors++;
	    }
	    
	    Log.i(TAG, "Errors: "+Integer.toString(numErrors));
	    
	    if(numErrors == 0){
	    	sendRegistrationDataToBackend(context, REGISTRATION_ENDPOINT);	    	
	    }	    
	}	
	    
    /**
     * Posts the registration data to the auth server over HTTP..
     */
	void sendRegistrationDataToBackend(Context context, String url){
		final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setTitle("Please Wait");
        pDialog.setMessage("Creating your account...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        
        JSONObject user_holder = new JSONObject();               
        JSONObject userAttributes = new JSONObject();
                
		try {
			userAttributes.put("email", mUserEmail);
			userAttributes.put("password", mUserPassword);
			userAttributes.put("password_confirmation", mUserPasswordConfirmation);
			userAttributes.put("phone_number", mUserPhoneNumber);
			user_holder.put("user", userAttributes);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.POST, url, user_holder,
		   new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					pDialog.dismiss();
					String token;
					try {
						token = response.getJSONObject("data").getString("auth_token");
    	            	SharedPreferences.Editor editor = mPreferences.edit();
    	                editor.putString("AuthToken", token);
    	                editor.putString("Email", mUserEmail);
    	                editor.commit();
    	                
    	                // Launch the HomeActivity and close this one
    	                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    	                startActivity(intent);
    	                finish();						
					} catch (JSONException e1) {
						e1.printStackTrace();
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

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getPreferences(Context context) {
        return getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }
}