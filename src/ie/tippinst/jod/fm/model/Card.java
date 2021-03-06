package ie.tippinst.jod.fm.model;

import java.io.Serializable;

public class Card implements Serializable{
	
	private static final long serialVersionUID = 3858707336758865936L;
	private Player player;
	private int colour;
	private int minute;
	
	public Card(){
		
	}
	
	public Card(Player player, int minute, int colour){
		this.player = player;
		this.minute = minute;
		this.colour = colour;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setMinute(int minute) {
		this.minute = minute;
	}
	
	public int getMinute() {
		return minute;
	}
	
	public void setColour(int colour) {
		this.colour = colour;
	}
	
	public int getColour() {
		return colour;
	}
}
