package youPont.mobile;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class EventListAdapter extends SimpleAdapter{

	private ConnectionDetector cd;
	
	private Context context;

	public EventListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
					int[] to) {
		super(context, data, resource, from, to);
		this.context = context;
		cd = new ConnectionDetector(context);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);

		final String evtId = ((TextView) v.findViewById(R.id.id)).getText().toString();
		final String reponsePerso = ((TextView) v.findViewById(R.id.reponse_perso)).getText().toString();
		
		final ImageButton chaudBtn = (ImageButton) v.findViewById(R.id.chaud_btn);
		final ImageButton cacherBtn = (ImageButton) v.findViewById(R.id.cacher_btn);

		
//		if(reponsePerso.equals("rejet")){
//			cacherBtn.setEnabled(false);
//		} else if (reponsePerso.equals("chaud")){
//			chaudBtn.setEnabled(false);
//		}
		
		//take the Button and set listener. It will be invoked when you click the button.
		chaudBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(cd.isConnectingToInternet()){
					new ActionEvenement(context, evtId, "chaud").execute();
//					cacherBtn.setEnabled(true);
//					chaudBtn.setEnabled(false);
				} else {
					// Internet connection is not present
					Toast.makeText(context, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();;
				}				
			}
		});

		cacherBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(cd.isConnectingToInternet()){
					new ActionEvenement(context, evtId, "rejet").execute();
//					cacherBtn.setEnabled(false);
//					chaudBtn.setEnabled(true);
				} else {
					// Internet connection is not present
					Toast.makeText(context, "Pas de connexion Internet.", Toast.LENGTH_SHORT).show();;
				}
			}
		});
		return v;
	}


}
