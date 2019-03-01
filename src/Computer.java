
public class Computer {
	private Stack backpack = new Stack(5);
	public int energy = 100;
	private int x;
	private int y;

	Computer(int x, int y) {
		this.x = x;
		this.y = y;
	}

	Computer() {

	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Stack getBackpack() {
		return backpack;
	}

	public void setBackpack(Stack backpack) {
		this.backpack = backpack;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}
}
