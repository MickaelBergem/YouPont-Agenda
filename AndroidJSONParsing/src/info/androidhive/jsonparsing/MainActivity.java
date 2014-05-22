package info.androidhive.jsonparsing;

import info.androidhive.jsonparsing.R;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity {

	private ProgressDialog pDialog;

	// URL to get JSON events
	private static String url = "http://dumbo.securem.eu/staticapi-getall.txt";

	// JSON Node names
	private static final String TAG_EVENEMENTS = "evenements";
	private static final String TAG_ID = "id";
	private static final String TAG_LABEL = "label";
	private static final String TAG_DETAILS = "details";
	private static final String TAG_DATE_DEB = "date_deb";
	private static final String TAG_DATE_FIN = "date_fin";
	private static final String TAG_LIEU = "lieu";
	private static final String TAG_COULEUR = "couleur";
	private static final String TAG_NB_PARTICIPANTS = "nb_participants";
	
	// evenements JSONArray
	JSONArray evenements = null;

	// Hashmap for ListView
	ArrayList<HashMap<String, String>> evenementsList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		evenementsList = new ArrayList<HashMap<String, String>>();

		ListView lv = getListView();

		// Listview on item click listener (for single event activity)
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Getting values from selected ListItem
				String label = ((TextView) view.findViewById(R.id.label))
						.getText().toString();
				String date_deb = ((TextView) view.findViewById(R.id.date_deb))
						.getText().toString();
				String lieu = ((TextView) view.findViewById(R.id.lieu))
						.getText().toString();
				String details = ((TextView) view.findViewById(R.id.details))
						.getText().toString();

				// Starting single event activity
				Intent in = new Intent(getApplicationContext(),
						SingleEvenementActivity.class);
				in.putExtra(TAG_LABEL, label);
				in.putExtra(TAG_DATE_DEB, date_deb);
				in.putExtra(TAG_LIEU, lieu);
				in.putExtra(TAG_DETAILS, details);
				startActivity(in);

			}
		});

		// Calling async task to get json
		new GetEvenements().execute();
	}

	/**
	 * Async task class to get json by making HTTP call
	 * */
	private class GetEvenements extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Récupération des événements...");
			pDialog.setCancelable(false);
			pDialog.show();

		}
		
		// Get JSON data
		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			// Making a request to url and getting response
			String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

			Log.d("Response: ", "> " + jsonStr);

			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);
					
					// Getting JSON Array node
					evenements = jsonObj.getJSONArray(TAG_EVENEMENTS);

					// Looping through all Evenements
					for (int i = 0; i < evenements.length(); i++) {
						// Get object "evenements"
						JSONObject v = evenements.getJSONObject(i);
						
						// Get all items for the event
						String id = v.getString(TAG_ID);
						String label = v.getString(TAG_LABEL);
						String details = v.getString(TAG_DETAILS);
						String nb_participants = v.getString(TAG_NB_PARTICIPANTS);
						String lieu = v.getString(TAG_LIEU);
						String couleur = v.getString(TAG_COULEUR);
						
						/** Get and convert dates **/
						// Get the date from Timestamp and convert to Date
						// * 1000 is to convert from s to ms
						long date_deb0 = v.getLong(TAG_DATE_DEB) * 1000;
						java.util.Date date_deb1 = new java.util.Date(date_deb0);
						
						long date_fin0 = v.getLong(TAG_DATE_FIN) * 1000;
						java.util.Date date_fin1 = new java.util.Date(date_fin0);
						
						// Create the date format wanted ("lundi 21 décembre à 15h45")
						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE d MMMM yyyy 'à' HH'h'mm", java.util.Locale.FRENCH);
						
						// Apply this format to the date previously got
						String date_deb = sdf.format(date_deb1);
						String date_fin = sdf.format(date_fin1);
						/** End Dates **/

						// tmp hashmap for single contact
						HashMap<String, String> evenement = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						evenement.put(TAG_ID, id);
						evenement.put(TAG_LABEL, label);
						evenement.put(TAG_DATE_DEB, date_deb);
						evenement.put(TAG_COULEUR, couleur);
						evenement.put(TAG_DATE_FIN, date_fin);
						evenement.put(TAG_DETAILS, details);
						evenement.put(TAG_NB_PARTICIPANTS, nb_participants);
						evenement.put(TAG_LIEU, lieu);

						// adding evenement to evenements list
						evenementsList.add(evenement);
					}
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
			/**
			 * Updating parsed JSON data into ListView
			 * */
			ListAdapter adapter = new SimpleAdapter(
					MainActivity.this, evenementsList,
					R.layout.list_item, new String[] { TAG_LABEL, TAG_DATE_DEB,
							TAG_LIEU, TAG_DETAILS }, new int[] { R.id.label,
							R.id.date_deb, R.id.lieu, R.id.details });

			setListAdapter(adapter);
		}

	}

}