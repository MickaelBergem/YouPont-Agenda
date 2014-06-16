package youPont.mobile;

import java.util.HashMap;
import java.util.List;

import android.app.Application;

public class AppModel extends Application{
	
	private boolean firstTime = true; //to know if the MainActivity is created for the first time since the opening of the app
	private List<HashMap<String, String>> evenementsListAll;
	private List<HashMap<String, String>> evenementsList;
	
	public AppModel(){
		super();
	}

	public boolean isFirstTime() {
		return firstTime;
	}

	public void setFirstTime(boolean firstTime) {
		this.firstTime = firstTime;
	}

	public List<HashMap<String, String>> getEvenementsListAll() {
		return evenementsListAll;
	}

	public void setEvenementsListAll(List<HashMap<String, String>> evenementsListAll) {
		this.evenementsListAll = evenementsListAll;
	}

	public List<HashMap<String, String>> getEvenementsList() {
		return evenementsList;
	}

	public void setEvenementsList(List<HashMap<String, String>> evenementsList) {
		this.evenementsList = evenementsList;
	}

}