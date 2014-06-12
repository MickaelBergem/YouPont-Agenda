package info.YouPont.Mobile;

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
	private ConnectionDetector cd;

	// JSON node keys
	private static final String TAG_REP = "reponse";
	private static final String TAG_REUSSITE = "reussite";
	private static final String TAG_PRENOM = "prenom";
	private static final String TAG_NOM = "nom";
	private static final String TAG_UID = "user_id";
	private static final String TAG_RAISON = "raison";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Connexion");
		setContentView(R.layout.activity_login);

		cd = new ConnectionDetector(this);

		/* Bind the button to the proper event function */
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

		if (login.equals("")){ //if the user did not enter a login
			Toast.makeText(this, "Veuillez indiquer votre nom d'utilisateur.", Toast.LENGTH_LONG).show();
			((EditText)findViewById(R.id.login_edittext)).requestFocus();
		}else if (passwd.equals("")){ //if the user did not enter a password
			Toast.makeText(this, "Veuillez indiquer votre mot de passe.", Toast.LENGTH_LONG).show();
			((EditText)findViewById(R.id.password_edittext)).requestFocus();
		}else {
			if(cd.isConnectingToInternet()){
				Login loginCall = new Login(login, passwd);
				loginCall.execute();

			} else {
				// Internet connection is not present
				Toast.makeText(LoginActivity.this, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();;
			}


		}


	}

	/*
	 * Async task class to get json by making HTTP call
	 * 
	 */
	private class Login extends AsyncTask<Void, Void, Void> {
		private String login, password;

		private String reussite, prenom, nom, user_id, raison;//, fin;

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
			String jsonString = APIUtils.createSession(LoginActivity.this, login, password);

			if (jsonString != null) {
				try {
					JSONObject v = new JSONObject(jsonString);

					JSONObject reponse = v.getJSONObject(TAG_REP);

					reussite = reponse.getString(TAG_REUSSITE);

					if(reussite.equals("false"))
						raison = reponse.getString(TAG_RAISON);
					else if (reussite.equals("true")){
						prenom = reponse.getString(TAG_PRENOM);
						nom = reponse.getString(TAG_NOM);
						user_id = reponse.getString(TAG_UID);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(LoginActivity.this, "Echec de la requête.", Toast.LENGTH_SHORT).show();
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

			if(reussite.equals("false"))
				Toast.makeText(LoginActivity.this,"Erreur : " + raison, Toast.LENGTH_LONG).show();
			else if (reussite.equals("true")){
				Toast.makeText(LoginActivity.this, "Connexion réussie.", Toast.LENGTH_SHORT).show();

				Intent i = new Intent(LoginActivity.this, MainActivity.class);
				i.putExtra("prenom", prenom);
				i.putExtra("nom", nom);
				i.putExtra("user_id", user_id);
				LoginActivity.this.startActivity(i);
				LoginActivity.this.finish();
			}
		}

	}
}
