package domain;

import java.util.*;

public class Call {

	private int id;
	private double arrivalTime;
	private int startingBaseStation;
	private double duration;
	private double carVelocity;
	private double startingPosition;
	private int endBaseStation;
	private int currentBaseStation;
	private char currentChannelType;//'n' for normal channels, 'r' for reserved channels
	private List<Integer> handoverList = new ArrayList<Integer>();
	private List<Double> handoverTime = new ArrayList<Double>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getStartingBaseStation() {
		return startingBaseStation;
	}

	public void setStartingBaseStation(int startingBaseStation) {
		this.startingBaseStation = startingBaseStation;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public double getCarVelocity() {
		return carVelocity;
	}

	public void setCarVelocity(double carVelocity) {
		this.carVelocity = carVelocity;
	}

	public double getStartingPosition() {
		return startingPosition;
	}

	public void setStartingPosition(double startingPosition) {
		this.startingPosition = startingPosition;
	}

	public int getEndBaseStation() {
		return endBaseStation;
	}

	public void setEndBaseStation(int endBaseStation) {
		this.endBaseStation = endBaseStation;
	}

	public int getCurrentBaseStation() {
		return currentBaseStation;
	}

	public void setCurrentBaseStation(int currentBaseStation) {
		this.currentBaseStation = currentBaseStation;
	}
	
	public char getCurrentChannelType() {
		return currentChannelType;
	}

	public void setCurrentChannelType(char currentChannelType) {
		this.currentChannelType = currentChannelType;
	}

	public List<Integer> getHandoverList() {
		return handoverList;
	}

	public void setHandoverList(List<Integer> handoverList) {
		this.handoverList = handoverList;
	}

	public List<Double> getHandoverTime() {
		return handoverTime;
	}

	public void setHandoverTime(List<Double> handoverTime) {
		this.handoverTime = handoverTime;
	}

	public Call(int id, double arrivalTime, int startingBaseStation, double duration, double carVelocity){
		setId(id);
		setArrivalTime(arrivalTime);
		setStartingBaseStation(startingBaseStation);
		setDuration(duration);
		setCarVelocity(carVelocity);
		generateStartingPosition();
		findEndBaseStation();
		setCurrentBaseStation(getStartingBaseStation());
		fillHandoverList();
		fillHandoverTime();
	}

	public void generateStartingPosition(){
		double startingPosition = (2 * getStartingBaseStation()) - (2 * Math.random());
		setStartingPosition(startingPosition);
	}

	public double callDurationInKM(){
		return getCarVelocity() * getDuration() / 3600.0;
	}

	public void findEndBaseStation(){
		double callDurationInKM = callDurationInKM();
		double callEndPosition = getStartingPosition() + callDurationInKM;
		int endBaseStation = (int) Math.ceil(callEndPosition / 2);
		if(endBaseStation > 20){
			setEndBaseStation(20);
		}else{
			setEndBaseStation(endBaseStation);
		}
	}

	public void fillHandoverList(){
		for(int i = getStartingBaseStation()+1; i <= getEndBaseStation(); i++){
			addToHandoverList(i);
		}
	}

	public void addToHandoverList(int baseStation){
		getHandoverList().add(baseStation);
	}

	public void removeFromHandoverList(){
		if(!getHandoverList().isEmpty()){
			getHandoverList().remove(0);
		}
	}

	public void fillHandoverTime(){
		if(!getHandoverList().isEmpty()){
			double currentTime = getArrivalTime();
			double currentPosition = getStartingPosition();
			for(int i : getHandoverList()){
				double handoverTime = calculateTimeOfNextHandover(currentTime, currentPosition);
				currentTime = currentTime + handoverTime;
				currentPosition = currentPosition + ((handoverTime * getCarVelocity()) / 3600.0);
				addToHandoverTime(currentTime);
			}
		}
	}

	public void addToHandoverTime(double handoverTime){
		getHandoverTime().add(handoverTime);
	}

	public void removeFromHandoverTime(){
		if(!getHandoverTime().isEmpty()){
			getHandoverTime().remove(0);
		}
	}

	public double calculateTimeOfNextHandover(double currentTime, double position){
		int station = (int) Math.floor((position / 2) + 1);
		double remainingKM = (2 * station) - position;
		return (remainingKM * 3600.0) / getCarVelocity();
	}

	public void makeHandover(){
		if(!getHandoverList().isEmpty()){
			setCurrentBaseStation(getHandoverList().get(0));
			removeFromHandoverList();
			removeFromHandoverTime();
		}
	}

	public List<Event> generateEvents(){
		List<Event> events = new ArrayList<Event>();

		Event initializeCall = new Event(this, "init", getArrivalTime());
		events.add(initializeCall);

		for(int i = 0; i < getHandoverList().size(); i++){
			Event handoverCall = new Event(this, "handover", getHandoverTime().get(i));
			events.add(handoverCall);
		}

		Event endCall = new Event(this, "end", (getArrivalTime() + getDuration()));
		events.add(endCall);

		return events;
	}

	public boolean equals(Call call){
		return getId() == call.getId();
	}

}
