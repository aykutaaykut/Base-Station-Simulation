package model;

import java.util.*;

import org.apache.commons.math3.distribution.*;

import domain.*;

public class Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Simulation of a Cellular Telephony Network");
		System.out.println("#####NEW TRIAL#####");
		System.out.println("Please enter a positive integer for simulation time in terms of sec. OR write \"x\" to exit: ");
		String simulationTimeString = "";
		String reservedCapacityString = "";
		while(!(simulationTimeString = sc.nextLine()).equalsIgnoreCase("x")){
			try {
				double simulationTime = Double.parseDouble(simulationTimeString);
				if(simulationTime > 0){
					System.out.println("Please enter reserved capacity for each cell between 0 and 10: ");
					reservedCapacityString = sc.nextLine();
					int reservedCapacity = Integer.parseInt(reservedCapacityString);
					if(reservedCapacity >= 0 && reservedCapacity <= 10){
						run(simulationTime, reservedCapacity);
					}else{
						System.out.println("You entered a number not between 0 and 10 for reserved capacity, please try another trial!");
					}
				}else{
					System.out.println("You entered a number not positive for simulation time, please try another trial!");
				}
			} catch (Exception e) {
				System.out.println("You entered non-numerical value, please try another trial!");
			} finally {
				System.out.println("#####NEW TRIAL#####");
				System.out.println("Please enter a positive integer for simulation time in terms of sec. OR write \"x\" to exit: ");
			}
		}
		sc.close();
		System.out.println("End of Simulation of a Cellular Telephony Network");
	}
	
	public static void run(double simulationTime, int reservedChannels){
		List<Call> calls = new ArrayList<Call>();
		List<Event> events = new ArrayList<Event>();
		
		int id = 1;
		double time = 0.0;
		while(time < simulationTime){
			double interarrivalTime = new ExponentialDistribution(0.0228).sample() * 60;//exp(0.0228);
			double arrivalTime = time + interarrivalTime;
			int startingBaseStation = (int) Math.ceil(Math.random() / 0.05);//uniform(0.5, 20.5)
			double duration = new GammaDistribution(1.26, 1.46).sample() * 60;//gamma(1.26, 1.46)
			double carVelocity = new NormalDistribution(120, 9.02).sample();//normal(120, 9.02)
			
			Call aCall = new Call(id, arrivalTime, startingBaseStation, duration, carVelocity);
			calls.add(aCall);
			
			id++;
			time += interarrivalTime;
		}
		
		for(Call call : calls){
			List<Event> callEvents = call.generateEvents();
			events.addAll(callEvents);
		}
		
		Collections.sort(events, new Comparator<Event>() {
			@Override
			public int compare(Event e1, Event e2){
				return Double.compare(e1.getTime(),  e2.getTime());
			}
		});
		
		BaseStation[] baseStations = new BaseStation[20];
		for(int i = 0; i < baseStations.length; i++){
			baseStations[i] = new BaseStation(reservedChannels);
		}
		
		double clock = 0.0;
		int totalAttemptedCalls = 0;
		int totalInitializedCalls = 0;
		int totalBlockedCalls = 0;
		int totalDroppedCalls = 0;
		int totalHandovers = 0;
		int totalEndedCalls = 0;
		
		while(!events.isEmpty()){
			Event event = events.get(0);
			clock = event.getTime();
			if(event.getType().equals("init")){
				totalAttemptedCalls++;
				if(baseStations[event.getCall().getStartingBaseStation() - 1].getChannels() > 0){
					totalInitializedCalls++;
					event.getCall().setCurrentChannelType('n');
					baseStations[event.getCall().getStartingBaseStation() - 1].setChannels(
							baseStations[event.getCall().getStartingBaseStation() - 1].getChannels()-1);
					events.remove(0);
				}else{
					totalBlockedCalls++;
					removeCall(events, event.getCall());
				}
			}else{
				if(event.getType().equals("handover")){
					if(baseStations[event.getCall().getCurrentBaseStation()].getChannels() > 0){
						if(event.getCall().getCurrentChannelType() == 'n'){
							baseStations[event.getCall().getCurrentBaseStation() - 1].setChannels(
									baseStations[event.getCall().getCurrentBaseStation() - 1].getChannels() + 1);
						}else{
							baseStations[event.getCall().getCurrentBaseStation() - 1].setCurrentReservedChannels(
									baseStations[event.getCall().getCurrentBaseStation() - 1].getCurrentReservedChannels() + 1);
						}
						event.getCall().setCurrentChannelType('n');
						event.getCall().makeHandover();
						baseStations[event.getCall().getCurrentBaseStation() - 1].setChannels(
								baseStations[event.getCall().getCurrentBaseStation() - 1].getChannels() - 1);
						totalHandovers++;
						events.remove(0);
					}else{
						if(baseStations[event.getCall().getCurrentBaseStation()].getCurrentReservedChannels() > 0){
							if(event.getCall().getCurrentChannelType() == 'n'){
								baseStations[event.getCall().getCurrentBaseStation() - 1].setChannels(
										baseStations[event.getCall().getCurrentBaseStation() - 1].getChannels() + 1);
							}else{
								baseStations[event.getCall().getCurrentBaseStation() - 1].setCurrentReservedChannels(
										baseStations[event.getCall().getCurrentBaseStation() - 1].getCurrentReservedChannels() + 1);
							}
							event.getCall().setCurrentChannelType('r');
							event.getCall().makeHandover();
							baseStations[event.getCall().getCurrentBaseStation() - 1].setCurrentReservedChannels(
									baseStations[event.getCall().getCurrentBaseStation() - 1].getCurrentReservedChannels() - 1);
							totalHandovers++;
							events.remove(0);
						}else{
							baseStations[event.getCall().getCurrentBaseStation() - 1].setChannels(
									baseStations[event.getCall().getCurrentBaseStation() - 1].getChannels() + 1);
							totalDroppedCalls++;
							removeCall(events, event.getCall());
						}
					}
				}else{
					if(event.getType().equals("end")){
						if(event.getCall().getCurrentChannelType() == 'n'){
							baseStations[event.getCall().getCurrentBaseStation() - 1].setChannels(
									baseStations[event.getCall().getCurrentBaseStation() - 1].getChannels() + 1);
						}else{
							baseStations[event.getCall().getCurrentBaseStation() - 1].setCurrentReservedChannels(
									baseStations[event.getCall().getCurrentBaseStation() - 1].getCurrentReservedChannels() + 1);
						}
						totalEndedCalls++;
						events.remove(0);
					}
				}
			}
		}
		
		System.out.println("Total Attempted Calls: " + totalAttemptedCalls);
		System.out.println("Total Initialized Calls: " + totalInitializedCalls);
		System.out.println("Total Blocked Calls: " + totalBlockedCalls);
		System.out.println("Total Dropped Calls: " + totalDroppedCalls);
		System.out.println("Total Handovers: " + totalHandovers);
		System.out.println("Total Ended Calls: " + totalEndedCalls);
		
		double percentageOfBlockedCalls = ((double) totalBlockedCalls / (double) totalAttemptedCalls) * 100;
		double percentageOfDroppedCalls = ((double) totalDroppedCalls / (double) totalAttemptedCalls) * 100;
		
		System.out.println("Percentage of Blocked Calls: " + percentageOfBlockedCalls + " %");
		System.out.println("Percentage of Dropped Calls: " + percentageOfDroppedCalls + " %");
	}
	
	public static void removeCall(List<Event> events, Call call){
		Event event = events.get(0);
		while(event.getTime() <= (call.getArrivalTime() + call.getDuration())){
			int index = events.indexOf(event);
			if(event.getCall().equals(call)){
				events.remove(event);
				if(index < events.size()){
					event = events.get(index);
				}else{
					break;
				}
			}else{
				event = events.get(index + 1);
			}
		}		
	}

}
