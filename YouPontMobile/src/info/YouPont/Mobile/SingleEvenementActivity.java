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
import android.widget.TextView;
import android.widget.Toast;

public class SingleEvenementActivity  extends Activity {

	private ProgressDialog pDialog;
	private ConnectionDetector cd;

	// JSON node keys
	private static final String TAG_LABEL = "label";
	private static final String TAG_DATE_DEB = "date_deb";
	private static final String TAG_LIEU = "lieu";
	private static final String TAG_DETAILS = "details";	
	private static final String TAG_ID = "id";
	
	private static final String TAG_ERR_CODE = "code_erreur";
	private static final String TAG_REP = "reponse";
	//private static final String TAG_FIN = "fin";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_evenement);
		
		cd = new ConnectionDetector(this);

		// getting intent data
		Intent in = getIntent();

		// Get JSON values from previous intent
		final String label = in.getStringExtra(TAG_LABEL);
		final String date_deb = in.getStringExtra(TAG_DATE_DEB);
		final String lieu = in.getStringExtra(TAG_LIEU);
		final String details = in.getStringExtra(TAG_DETAILS);
		final String id = in.getStringExtra(TAG_ID);

		// Displaying all values on the screen
		TextView lblLabel = (TextView) findViewById(R.id.label_label);
		TextView lblDate_deb = (TextView) findViewById(R.id.date_deb_label);
		TextView lblLieu = (TextView) findViewById(R.id.lieu_label);
		TextView lblDetails = (TextView) findViewById(R.id.details_label);
		TextView lblId = (TextView) findViewById(R.id.id_label);

		lblLabel.setText(label);
		lblDate_deb.setText(date_deb);
		lblLieu.setText(lieu);
		lblDetails.setText(details);
		lblId.setText(id);

		Button chaudBtn = (Button)findViewById(R.id.chaud_btn);
		Button cacherBtn = (Button)findViewById(R.id.cacher_btn);

		chaudBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(cd.isConnectingToInternet()){
					new ActionEvenement(id, "chaud").execute();
				} else {
					// Internet connection is not present
					Toast.makeText(SingleEvenementActivity.this, "Pas de connexion Internet", Toast.LENGTH_SHORT).show();;
				}
				
			}
		});

		cacherBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(cd.isConnectingToInternet()){
					new ActionEvenement(id, "rejet").execute();
				} else {
					// Internet connection is not present
					Toast.makeText(SingleEvenementActivity.this, "Pas de connexion Internet", Toast.LENGTH_SHORT).show();;
				}
			}
		});
	}

	/*
	 * Async task class to get json by making HTTP call
	 * 
	 */
	private class ActionEvenement extends AsyncTask<Void, Void, Void> {
		private String eventID, reponseType;
		
		private String code_erreur, reponse;//, fin;

		public ActionEvenement(String eventID, String reponseType){
			this.eventID = eventID;
			this.reponseType = reponseType;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(SingleEvenementActivity.this);
			pDialog.setMessage("Chargement...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		// Get JSON data
		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			NameValuePair eventReponseVP = new NameValuePair() {
				@Override
				public String getValue() {
					return "event_reponse";
				}
				@Override
				public String getName() {
					return "action";
				}
			};

			NameValuePair eventIdVP = new NameValuePair() {
				@Override
				public String getValue() {
					return eventID;
				}
				@Override
				public String getName() {
					return "id";
				}
			};

			NameValuePair reponseVP = new NameValuePair() {
				@Override
				public String getValue() {
					return reponseType;
				}
				@Override
				public String getName() {
					return "reponse";
				}
			};
			
            NameValuePair TokenVP = new NameValuePair() {

                @Override
                public String getValue() {
                    // TODO: token hardcodé, à récupérer dynamiquement à la connexion !
                    return "3ef6d2274e3f53d761ef9626b8ca54c10cf191257f524810c8dfdbf620ee7b77170a1bb7226de060";
                }

                @Override
                public String getName() {
                    return "token";
                }
            };
            
			List<NameValuePair> listParams = new ArrayList<NameValuePair>();
			listParams.add(eventReponseVP);
			listParams.add(eventIdVP);
            listParams.add(reponseVP);
            listParams.add(TokenVP);

			// Making a request to url and getting response
			String jsonString = sh.makeServiceCall(ServiceHandler.GET, listParams);
			Log.d("jsonString_value(reponse): ", "> " + jsonString);

			if (jsonString != null) {
				try {
					JSONObject v = new JSONObject(jsonString);

					
						// Get all items for the event
						code_erreur = v.getString(TAG_ERR_CODE);
						reponse = v.getString(TAG_REP);
						//fin = v.getString(TAG_FIN);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
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
				Toast.makeText(SingleEvenementActivity.this, "Effectué.", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(SingleEvenementActivity.this, "Erreur : " + reponse , Toast.LENGTH_SHORT).show();
		}

	}
}
