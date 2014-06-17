package youPont.mobile;

/**** OFFLINE SYNCHRO ****/
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import youPont.mobile.api.APIUtils;
import youPont.mobile.api.ServiceHandler;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends ListActivity {

	private ProgressDialog pDialog;

	// File used to store JSON data
	File json_InternalFile;
	String jsonStorage_FileName = "jsonStorage_FileName";
	String jsonStorage_FilePath = "jsonStorage_FilePath";

	// JSON Node names
	private static final String TAG_EVENEMENTS = "events";
	private static final String TAG_REPONSES_USER = "reponses_perso";
	private static final String TAG_EVENEMENTS_DATA = "evenements";
	private static final String TAG_ID = "id";
	private static final String TAG_LABEL = "label";
	private static final String TAG_DETAILS = "details";
	private static final String TAG_DATE_DEB = "date_deb";
	private static final String TAG_DATE_FIN = "date_fin";
	private static final String TAG_LIEU = "lieu";
	//	private static final String TAG_COULEUR = "couleur";
	private static final String TAG_NB_PARTICIPANTS = "nb_participants";

	private static final int SINGLE_EVT_INTENT = 1; 

	// evenements JSONArray
	private JSONObject evenements_data = null;
	private JSONObject evenements_reponses = null;	
	private JSONArray evenements = null;

	// Hashmap for ListView
	private List<HashMap<String, String>> evenementsListAll;
	private List<HashMap<String, String>> evenementsList;
	


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (!APIUtils.protectActivity(this)) return;// Checking if a token was stored in memory

		setContentView(R.layout.activity_main);

		evenementsListAll = new ArrayList<HashMap<String, String>>();
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
				String idEvt = ((TextView) view.findViewById(R.id.id))
						.getText().toString();
				String reponse_perso = ((TextView) view.findViewById(R.id.reponse_perso))
						.getText().toString();

				// Starting single event activity
				Intent in = new Intent(getApplicationContext(),
						SingleEvenementActivity.class);
				in.putExtra(TAG_LABEL, label);
				in.putExtra(TAG_DATE_DEB, date_deb);
				in.putExtra(TAG_LIEU, lieu);
				in.putExtra(TAG_DETAILS, details);
				in.putExtra(TAG_ID, idEvt);		
				in.putExtra(TAG_REPONSES_USER, reponse_perso);
				startActivityForResult(in, SINGLE_EVT_INTENT);

			}
		});

		if(((AppModel)getApplication()).isFirstTime()){
			// Calling async task to get json
			new GetEvenements().execute();
			((AppModel)getApplication()).setFirstTime(false);
		}else{
			ListAdapter adapter = null;
			String showAll = "";
			SharedPreferences settings = MainActivity.this.getSharedPreferences("ShowAll", 0);
			if (settings.contains("showAll")) {
				showAll = settings.getString("showAll", null);
			}
			//according to the settings, show show all events or not
			if(showAll.equals("false")){
				adapter = new EventListAdapter(
						MainActivity.this, ((AppModel)getApplication()).getEvenementsList(),
						R.layout.list_item, new String[] { TAG_LABEL, TAG_DATE_DEB,
								TAG_LIEU, TAG_DETAILS, TAG_ID, TAG_REPONSES_USER, TAG_NB_PARTICIPANTS }, new int[] { R.id.label,
								R.id.date_deb, R.id.lieu, R.id.details, R.id.id, R.id.reponse_perso, R.id.participants });
			}else{
				adapter = new EventListAdapter(
						MainActivity.this, ((AppModel)getApplication()).getEvenementsListAll(),
						R.layout.list_item, new String[] { TAG_LABEL, TAG_DATE_DEB,
								TAG_LIEU, TAG_DETAILS, TAG_ID, TAG_REPONSES_USER, TAG_NB_PARTICIPANTS }, new int[] { R.id.label,
								R.id.date_deb, R.id.lieu, R.id.details, R.id.id, R.id.reponse_perso, R.id.participants });
			}

			setListAdapter(adapter);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == SINGLE_EVT_INTENT) {
			if(data!=null){
				if(data.getBooleanExtra("info.YouPont.Mobile.modified", false))//if the user clicked on "Chaud!" or "Cacher" button, update eventsList
					new GetEvenements().execute();// Calling async task to get json
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		String showAll = "";

		SharedPreferences settings = this.getSharedPreferences("ShowAll", 0);// get settings to check the "show all events" option

		if (settings.contains("showAll"))
			showAll = settings.getString("showAll", null);

		//Initialize the checkbox according to settings
		if(showAll.equals("false"))
			menu.getItem(1).setChecked(false);
		else
			menu.getItem(1).setChecked(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_refresh:
			new GetEvenements().execute();
			return true;
		case R.id.action_show_all:
			ListAdapter adapter = null;

			SharedPreferences settings = this.getSharedPreferences("ShowAll", 0);
			SharedPreferences.Editor editor = settings.edit();

			//according to the settings, change the list of events (show all events or not)
			if(item.isChecked()){
				editor.putString("showAll", "false");
				item.setChecked(false);
				adapter = new EventListAdapter(
						MainActivity.this, ((AppModel)getApplication()).getEvenementsList(),
						R.layout.list_item, new String[] { TAG_LABEL, TAG_DATE_DEB,
								TAG_LIEU, TAG_DETAILS, TAG_ID, TAG_REPONSES_USER, TAG_NB_PARTICIPANTS }, new int[] { R.id.label,
								R.id.date_deb, R.id.lieu, R.id.details, R.id.id, R.id.reponse_perso, R.id.participants });
			}else{
				editor.putString("showAll", "true");
				item.setChecked(true);
				adapter = new EventListAdapter(
						MainActivity.this, ((AppModel)getApplication()).getEvenementsListAll(),
						R.layout.list_item, new String[] { TAG_LABEL, TAG_DATE_DEB,
								TAG_LIEU, TAG_DETAILS, TAG_ID, TAG_REPONSES_USER, TAG_NB_PARTICIPANTS }, new int[] { R.id.label,
								R.id.date_deb, R.id.lieu, R.id.details, R.id.id, R.id.reponse_perso, R.id.participants });
			}
			editor.commit();

			setListAdapter(adapter);

			return true;

		case R.id.action_logout:
			//show an alert dialog
			new AlertDialog.Builder(this)
			.setMessage("Voulez-vous vous déconnecter ?")
			.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					APIUtils.logout(MainActivity.this); //delete registered token from memory
					APIUtils.protectActivity(MainActivity.this); //go back to LoginActivity
				}
			})
			.setNegativeButton("Non", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Triggers the Get Evenements request
	 */
	public void getEventsTask(){
		new GetEvenements().execute();
	}

	/*
	 * Async task class to get json by making HTTP call
	 * 
	 */
	private class GetEvenements extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Récupération des événements...");
			pDialog.setCancelable(false);
			pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Annuler", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			    }
			});
			pDialog.show();

		}

		// Get JSON data
		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			NameValuePair getAllEventsVP = new NameValuePair() {

				@Override
				public String getValue() {
					return "event_getall";
				}

				@Override
				public String getName() {
					return "action";
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
			listParams.add(getAllEventsVP);
			listParams.add(TokenVP);
			listParams.add(TokenVP);

			// Making a request to url and getting response
			String jsonString = sh.makeServiceCall(ServiceHandler.GET, listParams);
			Log.d("jsonString_value (event_getall) : ", "> " + jsonString);

			/**** OFFLINE SYCHRO ****/

			ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
			File data_directory = contextWrapper.getDir(jsonStorage_FilePath, 0);
			json_InternalFile = new File(data_directory , jsonStorage_FileName);

			// RECUPERATION DU STRING ET CONVERSION TO FILE
			if (jsonString != null) {
				try {
					FileOutputStream fos = new FileOutputStream(json_InternalFile); // openFileOutput(jsonStorage_FileName, 0); //  
					fos.write(jsonString.getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					Log.e("Response: ", "> " + "File not found");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e("Response: ", "> " + "IO Exception");
					e.printStackTrace();
				}
			}

			// RECUPERATION DU FILE ET CONVERSION TO STRING
			if (jsonString == null) {
				try {
					FileInputStream fis = new FileInputStream(json_InternalFile); 
					DataInputStream dis = new DataInputStream(fis);
					BufferedReader br = new BufferedReader(new InputStreamReader(dis));
					String strLine;
					jsonString = "";
					while ((strLine = br.readLine()) != null) {
						jsonString = jsonString + strLine;
					}
					dis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}


			Log.d("Response: ", "> " + jsonString);

			if (jsonString != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonString);

					Log.d("Parsing evenements_data", "> " + TAG_EVENEMENTS_DATA);
					// Getting JSON Array node
					evenements_data = jsonObj.getJSONObject(TAG_EVENEMENTS_DATA);


					Log.d("Parsing the events data...","evenements_data");
					evenements = evenements_data.getJSONArray(TAG_EVENEMENTS);
					evenements_reponses = evenements_data.getJSONObject(TAG_REPONSES_USER);

					evenementsList.clear();
					evenementsListAll.clear();

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
						String reponse = evenements_reponses.getString(id);
						// 						String couleur = v.getString(TAG_COULEUR);

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
						// 						evenement.put(TAG_COULEUR, couleur);
						evenement.put(TAG_DATE_FIN, date_fin);
						evenement.put(TAG_DETAILS, details);
						evenement.put(TAG_NB_PARTICIPANTS, nb_participants + " participants");
						evenement.put(TAG_LIEU, lieu);
						evenement.put(TAG_REPONSES_USER, reponse);

						// adding evenement to evenements list
						evenementsListAll.add(evenement);
						if(!reponse.equals("rejet"))
							evenementsList.add(evenement);
					}
					
					((AppModel)getApplication()).setEvenementsList(evenementsList);
					((AppModel)getApplication()).setEvenementsListAll(evenementsListAll);
					
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
			ListAdapter adapter = null;
			String showAll = "";
			SharedPreferences settings = MainActivity.this.getSharedPreferences("ShowAll", 0);
			if (settings.contains("showAll")) {
				showAll = settings.getString("showAll", null);
			}
			//according to the settings, show all events or not
			if(showAll.equals("false")){
				adapter = new EventListAdapter(
						MainActivity.this, evenementsList,
						R.layout.list_item, new String[] { TAG_LABEL, TAG_DATE_DEB,
								TAG_LIEU, TAG_DETAILS, TAG_ID, TAG_REPONSES_USER, TAG_NB_PARTICIPANTS }, new int[] { R.id.label,
								R.id.date_deb, R.id.lieu, R.id.details, R.id.id, R.id.reponse_perso, R.id.participants });
			}else{
				adapter = new EventListAdapter(
						MainActivity.this, evenementsListAll,
						R.layout.list_item, new String[] { TAG_LABEL, TAG_DATE_DEB,
								TAG_LIEU, TAG_DETAILS, TAG_ID, TAG_REPONSES_USER, TAG_NB_PARTICIPANTS  }, new int[] { R.id.label,
								R.id.date_deb, R.id.lieu, R.id.details, R.id.id, R.id.reponse_perso, R.id.participants });
			}

			setListAdapter(adapter);
		}

	}

}