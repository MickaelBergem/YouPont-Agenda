package youPont.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import youPont.mobile.api.APIUtils;
import youPont.mobile.api.ServiceHandler;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SingleEvenementActivity  extends Activity {

	private ConnectionDetector cd;

	// JSON node keys
	private static final String TAG_LABEL = "label";
	private static final String TAG_DATE_DEB = "date_deb";
	private static final String TAG_LIEU = "lieu";
	private static final String TAG_DETAILS = "details";	
	private static final String TAG_ID = "id";
	private static final String TAG_REP = "reponse";
	private static final String TAG_NB_PART = "nb_participants";
	private static final String TAG_LISTE = "liste";
	private static final String TAG_PSEUDO = "pseudo";
	private static final String TAG_PRENOM = "prenom";
	private static final String TAG_NOM = "nom";
	private static final String TAG_PROMO = "promo";
	private static final String TAG_DPT = "departement";
	private static final String TAG_TEL = "telephone";
	private static final String TAG_REPONSES_USER = "reponses_perso";

	// participants list
	private ArrayList<HashMap<String, String>> participantsList;

	private Intent in = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_evenement);

		cd = new ConnectionDetector(this);

		// getting intent data
		in = getIntent();

		// Get JSON values from previous intent
		final String label = in.getStringExtra(TAG_LABEL);
		final String date_deb = in.getStringExtra(TAG_DATE_DEB);
		final String lieu = in.getStringExtra(TAG_LIEU);
		final String details = in.getStringExtra(TAG_DETAILS);
		final String id = in.getStringExtra(TAG_ID);
		final String reponsePerso = in.getStringExtra(TAG_REPONSES_USER);

		// Displaying all values on the screen
		TextView lblLabel = (TextView) findViewById(R.id.label_label);
		TextView lblDate_deb = (TextView) findViewById(R.id.date_deb_label);
		TextView lblLieu = (TextView) findViewById(R.id.lieu_label);
		TextView lblDetails = (TextView) findViewById(R.id.details_label);
		TextView lblId = (TextView) findViewById(R.id.id_label);
		TextView lblReponse = (TextView) findViewById(R.id.reponse_label);

		lblLabel.setText(label);
		lblDate_deb.setText(date_deb);
		lblLieu.setText(lieu);
		lblDetails.setText(details);
		lblId.setText(id);
		if(reponsePerso.equals("chaud")){
			lblReponse.setText("Vous êtes inscrit.");
		} else {
			lblReponse.setText("Vous n'êtes pas inscrit.");
		}
		
		//initialize participants list
		participantsList = new ArrayList<HashMap<String, String>>();

		if(cd.isConnectingToInternet()){
			//get participants
			new GetChauds(id).execute();
		}else{
			Toast.makeText(this, "Impossible de récupérer la liste des participants. Vérifier votre connexion Internet.", Toast.LENGTH_SHORT).show();;
		}
		//view buttons
		final ImageButton chaudBtn = (ImageButton)findViewById(R.id.chaud_btn);
		final ImageButton cacherBtn = (ImageButton)findViewById(R.id.cacher_btn);
		
		chaudBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(cd.isConnectingToInternet()){
					in.putExtra("info.YouPont.Mobile.modified", true);
					setResult(RESULT_OK, in);
					new ActionEvenement(SingleEvenementActivity.this, 1, id, "chaud").execute();	
				} else {
					// Internet connection is not present
					Toast.makeText(SingleEvenementActivity.this, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();;
				}

			}
		});

		cacherBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(cd.isConnectingToInternet()){
					in.putExtra("info.YouPont.Mobile.modified", true);
					setResult(RESULT_OK, in);
					new ActionEvenement(SingleEvenementActivity.this, 1, id, "rejet").execute();
				} else {
					// Internet connection is not present
					Toast.makeText(SingleEvenementActivity.this, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();;
				}
			}
		});
	}

	
	/*
	 * Async task class to get json by making HTTP call
	 * 
	 */
	private class GetChauds extends AsyncTask<Void, Void, Void> {

		private String eventID;
		private int nb_participants;

		private JSONArray participants = null;

		public GetChauds(String eventID){
			this.eventID = eventID;
		}

		// Get JSON data
		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			/*
			 * Creating value pairs, parameters for the HTTP request
			 */

			NameValuePair eventGetChaudsVP = new NameValuePair() {
				@Override
				public String getValue() {
					return "event_getchauds";
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
					return "id_event";
				}
			};

			NameValuePair TokenVP = new NameValuePair() {

				@Override
				public String getValue() {
					return APIUtils.getLastToken();
				}

				@Override
				public String getName() {
					return "token";
				}
			};

			List<NameValuePair> listParams = new ArrayList<NameValuePair>();
			listParams.add(eventGetChaudsVP);
			listParams.add(eventIdVP);
			listParams.add(TokenVP);

			// Making a request to url and getting response
			String jsonString = sh.makeServiceCall(ServiceHandler.GET, listParams);
			Log.d("jsonString_value(reponse): ", "> " + jsonString);

			if (jsonString != null) {
				try {
					
					JSONObject v = new JSONObject(jsonString);
					JSONObject reponse = v.getJSONObject(TAG_REP);

					participants = reponse.getJSONArray(TAG_LISTE);
					nb_participants = reponse.getInt(TAG_NB_PART);

					// Looping through all Participants
					for (int i = 0; i < participants.length(); i++) {
						// Get object "participant"
						JSONObject participant = participants.getJSONObject(i);

						// Get all items for the participant
						String id = participant.getString(TAG_ID);
						String pseudo = participant.getString(TAG_PSEUDO);
						String prenom = participant.getString(TAG_PRENOM);
						String nom = participant.getString(TAG_NOM);
						String promo = participant.getString(TAG_PROMO);
						String departement = participant.getString(TAG_DPT);
						String telephone = participant.getString(TAG_TEL);

						// tmp hashmap for single participant
						HashMap<String, String> infosParticipant = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						infosParticipant.put(TAG_ID, id);
						infosParticipant.put(TAG_PSEUDO, pseudo);
						infosParticipant.put(TAG_PRENOM, prenom);
						infosParticipant.put(TAG_NOM, nom);
						infosParticipant.put(TAG_PROMO, promo);
						infosParticipant.put(TAG_DPT, departement);
						infosParticipant.put(TAG_TEL, telephone);

						// adding participant to participants list
						participantsList.add(infosParticipant);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(SingleEvenementActivity.this, "Echec de la requête.", Toast.LENGTH_SHORT).show();
				Log.e("ServiceHandler", "Couldn't get any data from the url");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			/*
			 * Updating the View showing the participants list
			 */
			
			TextView participants = (TextView)findViewById(R.id.participants_label);

			String participantsText = nb_participants + " participants. \n";
			for(HashMap<String, String> hm : participantsList){
				participantsText += "- " + hm.get(TAG_PRENOM) + " " + hm.get(TAG_NOM) + "\n";
			}
			participants.setText(participantsText);

		}

	}
}
