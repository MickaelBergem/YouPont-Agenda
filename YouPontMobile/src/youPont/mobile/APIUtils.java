package youPont.mobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * This class contains several tools
 */
public class APIUtils {
	private static String mLastToken = null;
	private static boolean mFirstCall = true;
	
	private static final String TAG_REP = "reponse";
	private static final String TAG_REUSSITE = "reussite";
	private static final String TAG_TOKEN = "token";

	/**
	 * This function protects the activity from non-logged users.
	 * If will finish() the activity, so take care of not calling
	 * anything related to the activity if this function returns false.
	 * 
	 * @param activity Activity instance to protect
	 * @return true if the activity is safe, false otherwise
	 */
	public static boolean protectActivity(final Activity activity) {
		if (mFirstCall == true) { // if it's the first time this function is called ...
			// ... restore from preferences

			SharedPreferences settings = activity.getSharedPreferences("APIAuth", 0);
			if (settings.contains("userToken")) {
				mLastToken = settings.getString("userToken", null);
			}

			mFirstCall = false;
		}

		if (mLastToken == null) {
			Intent i = new Intent(activity, LoginActivity.class);
			activity.startActivity(i);
			activity.finish();

			return false;
		}

		return true;
	}

	/**
	 * Calls APIRequests.createSession() with session management middleware
	 * @param ctx Context instance
	 * @param userLogin User's login
	 * @param userPassword User's password
	 * @return Session token
	 * @throws APIException If API error
	 */
	public static String createSession(Context ctx, final String userLogin, final String userPassword){
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
				return userLogin;
			}
			@Override
			public String getName() {
				return "id";
			}
		};

		NameValuePair passwordVP = new NameValuePair() {
			@Override
			public String getValue() {
				return userPassword;
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
				return "app_ID";
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

				JSONObject reponse = v.getJSONObject(TAG_REP);


				String reussite = reponse.getString(TAG_REUSSITE);

				if(reussite.equals("true")){
					mLastToken = reponse.getString(TAG_TOKEN);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
	

			updateSettings(ctx); // Load the settings in memory
		}
		return jsonString;
	}

	/**
	 * Update settings in memory
	 * @param ctx Context instance
	 * @return AccountSettings instance
	 * @throws APIException If API error
	 */
	public static void updateSettings(Context ctx) {

		// store the token
		// in Android's settings

		SharedPreferences settings = ctx.getSharedPreferences("APIAuth", 0);
		SharedPreferences.Editor editor = settings.edit();

		if (mLastToken != null) {
			// Store the token in Android's settings
			editor.putString("userToken", mLastToken);
		} else {
			// Otherwise, drop it from memory

			if (settings.contains("userToken")) {
				editor.remove("userToken");
			}
		}

		editor.commit();

	}

	/**
	 * Returns the last authentication token fetched from createSession()
	 * @return String instance containing the token if found, null if no token known
	 */
	public static String getLastToken() {
		return mLastToken;
	}

	/**
	 * Removes the current authentication token from memory
	 */
	public static void logout(Context ctx) {
		mLastToken = null;
		//TODO Ajouter un appel à l'API pour supprimer la session côté serveur
		
		// Launch updateSettings() to update shared preferences and remove the session
		updateSettings(ctx);

	}
}
