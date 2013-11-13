package com.quizzingbricks.authentication;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

import com.quizzingbricks.activities.MainScreenActivity;
import com.quizzingbricks.activities.LoginActivity;
import com.quizzingbricks.exceptions.ServerConnectionException;

public class AuthenticationManager {

	private Editor editor;
	private Context context;
	private SharedPreferences sharedPref;
	
	private final String IS_LOGIN = "isLoggedIn";
	private final String KEY_EMAIL = "email";
	private final String KEY_TOKEN = "token";
	
	public AuthenticationManager(Context context)	{
		this.context = context;
		this.sharedPref = context.getSharedPreferences("quizzingbricks-android", 0);
		this.editor = this.sharedPref.edit();
	}
	
	public void login(String email, String password)		{
		AuthenticateTask authenticateTask = new AuthenticateTask();
		authenticateTask.execute(email, password);
	}
	
	public void logout()	{
		editor.clear();
		editor.commit();
	}
	
	/**
	 * Checks if the users if authenticated (i.e. has a token) and changes activity to LoginActivity if necessary
	 */
	public void checkAuthentication()	{
		if(!isLoggedIn())	{
			Intent intent = new Intent(this.context, LoginActivity.class);
			// Closing all the Activities
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.context.startActivity(intent);
		}
	}
	
	public boolean isLoggedIn()	{
		return sharedPref.getBoolean(IS_LOGIN, false);
	}
	
	private class AuthenticateTask extends AsyncTask<String, Void, AsyncTaskResult<String>> {

		private String email;
		private String password;
		private ProgressDialog progressDialog;
		
		private HttpClient client = new DefaultHttpClient();
		private HttpPost httppost = new HttpPost("http://130.240.94.184:5000/login");
		
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("Processing...");
			progressDialog.setMessage("Please wait.");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}
		
		
		@Override
		protected AsyncTaskResult<String> doInBackground(String... params) {
			
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				this.email = params[0];
				this.password = params[1];
				nameValuePairs.add(new BasicNameValuePair("email", this.email));
				nameValuePairs.add(new BasicNameValuePair("password", this.password));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpEntity entity = client.execute(httppost).getEntity();
				
				if(entity != null)	{
					String response = EntityUtils.toString(entity); 
			        entity.consumeContent();
			        client.getConnectionManager().shutdown();
			        JSONObject object = new JSONObject(response.trim());
			        try {
			        	return new AsyncTaskResult<String>(object.getString("token"));
			        }
			        catch (JSONException je){
			        	return new AsyncTaskResult<String>(object.getString("error"));
			        }
				}
				else	{
					return new AsyncTaskResult<String>(new ServerConnectionException("No return message from server"));
				}
				
			} 
			catch (ClientProtocolException e) {
				return new AsyncTaskResult<String>(new ServerConnectionException("Could not send HTTP Post to server"));
			}
			catch (Exception e)	{
				return new AsyncTaskResult<String>(e);
			}
		}
		
		protected void onPostExecute(AsyncTaskResult<String> result)	{
			if(result.getException() != null)	{
				result.getException().printStackTrace();
			}
			else if(isCancelled())	{
				//Remove the pop up
			}
			else	{
				editor.putBoolean(IS_LOGIN, true);
				editor.putString(KEY_EMAIL, this.email);
				editor.putString(KEY_TOKEN, result.getResult());
				editor.commit();
				progressDialog.dismiss();
				Intent intent = new Intent(context, MainScreenActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		}

	}
	/**
	 *	Inspiration from http://stackoverflow.com/a/6312491 
	 */
	private class AsyncTaskResult<ResultType>	{
		private ResultType result;
		private Exception exception;
		
		public AsyncTaskResult(ResultType result){
			this.result = result;
		}
		public AsyncTaskResult(Exception exception)	{
			this.exception = exception;
		}
		
		public ResultType getResult()	{
			return this.result;
		}
		
		public Exception getException()	{
			return this.exception;
		}
	}
}