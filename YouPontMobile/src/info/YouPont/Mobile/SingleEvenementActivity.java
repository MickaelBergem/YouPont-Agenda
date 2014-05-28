package info.YouPont.Mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import youPont.mobile.R;

public class SingleEvenementActivity  extends Activity {
	
	// JSON node keys
	private static final String TAG_LABEL = "label";
	private static final String TAG_DATE_DEB = "date_deb";
	private static final String TAG_LIEU = "lieu";
	private static final String TAG_DETAILS = "details";	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_evenement);
        
        // getting intent data
        Intent in = getIntent();
        
        // Get JSON values from previous intent
        String label = in.getStringExtra(TAG_LABEL);
        String date_deb = in.getStringExtra(TAG_DATE_DEB);
        String lieu = in.getStringExtra(TAG_LIEU);
        String details = in.getStringExtra(TAG_DETAILS);
        
        // Displaying all values on the screen
        TextView lblLabel = (TextView) findViewById(R.id.label_label);
        TextView lblDate_deb = (TextView) findViewById(R.id.date_deb_label);
        TextView lblLieu = (TextView) findViewById(R.id.lieu_label);
        TextView lblDetails = (TextView) findViewById(R.id.details_label);
        
        lblLabel.setText(label);
        lblDate_deb.setText(date_deb);
        lblLieu.setText(lieu);
        lblDetails.setText(details);
    }
}
