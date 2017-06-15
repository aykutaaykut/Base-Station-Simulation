package domain;

public class BaseStation {

	private int channels;
	private int reservedChannels;
	private int currentReservedChannels;
	
	public int getChannels() {
		return channels;
	}
	
	public void setChannels(int channels) {
		this.channels = channels;
	}
	
	public int getReservedChannels() {
		return reservedChannels;
	}
	
	public void setReservedChannels(int reservedChannels) {
		this.reservedChannels = reservedChannels;
	}
	
	public int getCurrentReservedChannels() {
		return currentReservedChannels;
	}

	public void setCurrentReservedChannels(int currentReservedChannels) {
		this.currentReservedChannels = currentReservedChannels;
	}

	public BaseStation(int reservedChannels){
		if(reservedChannels == 0){
			setChannels(10);
		}else{
			setChannels(10-reservedChannels);
		}
		setReservedChannels(reservedChannels);
		setCurrentReservedChannels(reservedChannels);
	}
	
}
