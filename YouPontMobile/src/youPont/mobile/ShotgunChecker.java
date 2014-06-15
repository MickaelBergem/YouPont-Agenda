package youPont.mobile;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;

/**
 * Not used for the moment
 *
 */
public class ShotgunChecker {

	private Service service; // Service that will always run in background, and will be the Context of execution of our task
	private long frequency;

	public ShotgunChecker(Service service, long frequency) {
		this.service = service;
		this.frequency = frequency;
	}

	public void runTask() {
		Timer timer = new Timer();
		timer.schedule(new Task(service), 
				10000, //Amount of time before first execution
				frequency);
	}
}

class Task extends TimerTask {

	private Service service;

	public Task(Service service) {
		super();
		this.service = service;
	}


	@Override
	public void run() {
		//TODO Implement here the task to execute regularly
		service.getApplication(); //Example of execution
	}

}