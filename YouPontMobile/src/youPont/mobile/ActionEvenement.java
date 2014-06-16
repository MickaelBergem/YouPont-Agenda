package youPont.mobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/*
 * Async task class to get json by making HTTP call
 * 
 */
public class ActionEvenement extends AsyncTask<Void, Void, Void> {
	
	private ProgressDialog pDialog;

	private String eventID, reponseType;
	private String code_erreur, reponse;
	
	private Context context;
	
	private static final String TAG_ERR_CODE = "code_erreur";
	private static final String TAG_REP = "reponse";

	public ActionEvenement(Context context, String eventID, String reponseType){
		this.context = context;
		this.eventID = eventID;
		this.reponseType = reponseType;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// Showing progress dialog
		pDialog = new ProgressDialog(context);
		pDialog.setMessage("Chargement...");
		pDialog.setCancelable(false);
		pDialog.show();

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
		// Dismiss the progress dialog
		
		if (pDialog.isShowing())
			pDialog.dismiss();

		if(Integer.parseInt(code_erreur) == 0){//if the request was not wrong
			String msg = "";
			if(reponseType.equals("rejet")){//if the action was "Cacher"
				msg = "Evénement retiré.";
				//mActivity.finish();//destroy the activity to return to events list
			}else
				msg = "Inscription enregistrée.";

			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(context, "Erreur : " + reponse , Toast.LENGTH_SHORT).show();
	}

}

