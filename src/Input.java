
public class Input {
	private char type;
	public Input(int typenumber) {
		if(typenumber==1)type='1';
		else if(typenumber==2)type='2';
		else if(typenumber==3)type='3';
		else if(typenumber==4)type='4';
		else if(typenumber==5)type='*';
	}
	public char getType() {
		return type;
	}
	public void setType(char type) {
		this.type = type;
	}
	

}
