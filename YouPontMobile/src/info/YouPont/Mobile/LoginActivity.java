package info.YouPont.Mobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import youPont.mobile.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity{
	private ProgressDialog pDialog;

	private static final String TAG_ERR_CODE = "code_erreur";
	private static final String TAG_REP = "reponse";
	//private static final String TAG_FIN = "fin";
	
	private ConnectionDetector cd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Connexion");
		setContentView(R.layout.activity_login);

		cd = new ConnectionDetector(this);
		
		/* Bind the button to the proper event functions */
		((Button)findViewById(R.id.login_button)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginClicked();
			}
		});
	}

	private void loginClicked() {
		String login = ((EditText)findViewById(R.id.login_edittext)).getText().toString();
		String passwd = ((EditText)findViewById(R.id.password_edittext)).getText().toString();

		if (login.equals("")){
			Toast.makeText(this, "Veuillez indiquer votre nom d'utilisateur.", Toast.LENGTH_LONG).show();
			((EditText)findViewById(R.id.login_edittext)).requestFocus();
		}else if (passwd.equals("")){
			Toast.makeText(this, "Veuillez indiquer votre mot de passe.", Toast.LENGTH_LONG).show();
			((EditText)findViewById(R.id.password_edittext)).requestFocus();
		}else {
			if(cd.isConnectingToInternet()){
				Login loginCall = new Login(login, passwd);
//				loginCall.execute();
				Intent in = new Intent(getApplicationContext(),
						MainActivity.class);
				startActivity(in);
			} else {
				// Internet connection is not present
				Toast.makeText(LoginActivity.this, "Pas de connexion Internet", Toast.LENGTH_SHORT).show();;
			}


		}


	}

	/*
	 * Async task class to get json by making HTTP call
	 * 
	 */
	private class Login extends AsyncTask<Void, Void, Void> {
		private String login, password;

		private String code_erreur, reponse;//, fin;

		public Login(String login, String password){
			this.login = login;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Connexion...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		// Get JSON data
		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			NameValuePair actionLoginVP = new NameValuePair() {
				@Override
				public String getValue() {
					return "connexion";
				}
				@Override
				public String getName() {
					return "action";
				}
			};

			NameValuePair loginVP = new NameValuePair() {
				@Override
				public String getValue() {
					return login;
				}
				@Override
				public String getName() {
					return "id";
				}
			};

			NameValuePair passwordVP = new NameValuePair() {
				@Override
				public String getValue() {
					return password;
				}
				@Override
				public String getName() {
					return "pass";
				}
			};

			NameValuePair appIdVP = new NameValuePair() {
				@Override
				public String getValue() {
					return "1";
				}
				@Override
				public String getName() {
					return "appID";
				}
			};

			List<NameValuePair> listParams = new ArrayList<NameValuePair>();
			listParams.add(actionLoginVP);
			listParams.add(loginVP);
			listParams.add(passwordVP);
			listParams.add(appIdVP);

			// Making a request to url and getting response
			String jsonString = sh.makeServiceCall(ServiceHandler.GET, listParams);
			Log.d("jsonString_value: ", "> " + jsonString);

			if (jsonString != null) {
				try {
					JSONObject v = new JSONObject(jsonString);


					// Get all items for the event
					code_erreur = v.getString(TAG_ERR_CODE);
					reponse = v.getString(TAG_REP);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
                // TODO : afficher un message d'erreur à l'écran
				Log.e("ServiceHandler", "Couldn't get any data from the url");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();

			if(Integer.parseInt(code_erreur) == 0)
				Toast.makeText(LoginActivity.this, "Effectué.", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(LoginActivity.this, "Erreur : " + reponse , Toast.LENGTH_SHORT).show();
		}

	}
}
