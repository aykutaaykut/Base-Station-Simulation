package domain;

public class Event {

	private Call call;
	private String type;
	private double time;
	
	public Call getCall() {
		return call;
	}

	public void setCall(Call call) {
		this.call = call;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public double getTime() {
		return time;
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	
	public Event(Call call, String type, double time){
		setCall(call);
		setType(type);
		setTime(time);
	}
	
}
