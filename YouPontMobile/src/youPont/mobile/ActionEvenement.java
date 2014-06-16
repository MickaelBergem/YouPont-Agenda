package youPont.mobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import youPont.mobile.api.APIUtils;
import youPont.mobile.api.ServiceHandler;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Async task class to get json by making HTTP call
 * 
 */
public class ActionEvenement extends AsyncTask<Void, Void, Void> {


	private String eventID, reponseType;
	private String code_erreur, reponse;

	private int contextId = 0;
	private Context context;

	private static final String TAG_ERR_CODE = "code_erreur";
	private static final String TAG_REP = "reponse";
	private static final int ID_MAIN = 0;
	private static final int ID_SINGLE = 1;


	public ActionEvenement(Context context, int contextId, String eventID, String reponseType){
		this.context = context;
		this.contextId = contextId;
		this.eventID = eventID;
		this.reponseType = reponseType;
	}

	// Get JSON data
	@Override
	protected Void doInBackground(Void... arg0) {
		// Creating service handler class instance
		ServiceHandler sh = new ServiceHandler();

		/*
		 * Creating value pairs, parameters for the HTTP request
		 */

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
				return APIUtils.getLastToken();
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

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(context, "Echec de la requête.", Toast.LENGTH_SHORT).show();
			Log.e("ServiceHandler", "Couldn't get any data from the url");
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		if(Integer.parseInt(code_erreur) == 0){//if the request was not wrong
			String msg = "";
			if(reponseType.equals("rejet")){//if the action was "Cacher"
				msg = "Evénement retiré.";
				
				if(contextId == ID_SINGLE){//if the request was called from SingleEvenementActivity
					((Activity)context).finish();
					
				}else if (contextId == ID_MAIN)//if the request was called from MainActivity
					((MainActivity)context).getEventsTask();//update the events list
				
			}else{
				msg = "Inscription enregistrée.";
				
				if(contextId == ID_SINGLE){//if the request was called from SingleEvenementActivit
					TextView lblReponse = (TextView) ((Activity)context).findViewById(R.id.reponse_label);
					lblReponse.setText("Vous êtes inscrit.");
					
					SharedPreferences settings = context.getSharedPreferences("APIAuth", 0);

					TextView lblParticipants = (TextView) ((Activity)context).findViewById(R.id.participants_label);
					lblParticipants.setText(lblParticipants.getText().toString() +
							"- " + settings.getString("prenom", null) + " " + settings.getString("nom", null));
					
				} else if (contextId == ID_MAIN)//if the request was called from MainActivity
					((MainActivity)context).getEventsTask();//update the events list
			}

			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(context, "Erreur : " + reponse , Toast.LENGTH_SHORT).show();
	}

}

