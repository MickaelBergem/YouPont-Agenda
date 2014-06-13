package youPont.mobile;

import java.util.HashMap;
import java.util.List;

import android.app.Application;

public class AppModel extends Application{
	
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

	private boolean firstTime = true;
	private List<HashMap<String, String>> evenementsListAll;
	private List<HashMap<String, String>> evenementsList;
}