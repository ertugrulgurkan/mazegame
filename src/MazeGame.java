import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import enigma.console.Console;
import enigma.console.TextAttributes;
import enigma.core.Enigma;

public class MazeGame {
	final int waitTime = 1000; // A game tour duration
	Random random = new Random();
	static Human human = new Human();
	static Computer computer = new Computer();
	static Computer hide = new Computer();
	static Object[][] maze = new Object[21][55];
	static double[][][] cell = new double[21][55][3]; // 0 dont go. 1 go
														// x-x-0 total distance
														// x-x-1computer
														// distance
														// x-x-2 path
	static int seconds = 0; // palying time
	static CircularQueue inputlist = new CircularQueue(20);
	static Stack tStack; // temp stack for pathfinding
	static Stack finalPath = new Stack(999999); // to save the path
	static boolean gameover = false;
	static boolean startaken = false;
	static boolean pathFinded = false;
	static boolean isHumanMoved = false;
	static boolean pause = false;
	static int itemcounter = 0;
	String userName; // login
	Input list = new Input(1);
	static Console cn = Enigma.getConsole("Energy Maze", 160, 30,true);
	static TextAttributes text = new TextAttributes(Color.BLACK, Color.WHITE);
	public KeyListener klis;
	Scanner scanner = new Scanner(System.in);
	// ------ Standard variables for mouse and keyboard ------
	static public int keypr; // key pressed?
	public int rkey; // key (for press/release)

	/**
	 * login function to take user name
	 */
	public void login() {
		cn.getTextWindow().setCursorPosition(16, 14);
		System.out.print("Enter your name: ");
		userName = scanner.next();
	}

	public void Menu() throws FileNotFoundException, Exception {
		cn.getTextWindow().setCursorPosition(0, 4);
		System.out.println("       ______                                         __  __                      \n"
				+ "      |  ____|                                       |  \\/  |                     \n"
				+ "      | |__     _ __     ___   _ __    __ _   _   _  | \\  / |   __ _   ____   ___ \n"
				+ "      |  __|   | '_ \\   / _ \\ | '__|  / _` | | | | | | |\\/| |  / _` | |_  /  / _ \\\n"
				+ "      | |____  | | | | |  __/ | |    | (_| | | |_| | | |  | | | (_| |  / /  |  __/ \n"
				+ "      |______| |_| |_|  \\___| |_|     \\__, |  \\__, | |_|  |_|  \\__,_| /___|  \\___|\n"
				+ "                                       __/ |   __/ |                              \n"
				+ "                                      |___/   |___/                               \n");

		cn.getTextWindow().setCursorPosition(16, 14);
		System.out.println("1 - Start game");
		cn.getTextWindow().setCursorPosition(16, 15);
		System.out.println("2 - Tutorial");
		cn.getTextWindow().setCursorPosition(16, 16);
		System.out.println("3 - Credits");
		cn.getTextWindow().setCursorPosition(16, 17);
		System.out.print("Choice is : ");

		int choice = scanner.nextInt();
		if (choice == 1) {// 1 - Start new game
			consoleClear();
			login();
			consoleClear();
			newGame();
		} else if (choice == 2) {// 2 - Tutorial
			consoleClear();
			tutorial();
			Thread.sleep(10000);// 10 sec
			consoleClear();
			Menu();
		} else if (choice == 3) { // 3 - Credits
			consoleClear();
			credits();
			Thread.sleep(10000);// 10 sec
			consoleClear();
			Menu();
		} else {
			cn.getTextWindow().setCursorPosition(4, 16);
			System.out.println("Wrong Command, Please Enter a valid command");
			Thread.sleep(1000);
			consoleClear();
			Menu();
		}

		consoleClear();
		input();
		firstgeneration();
		refresh();
		play();
		gameOver();
	}

	/**
	 * Game over message
	 */
	public void gameOver() {
		consoleClear();
		cn.getTextWindow().setCursorPosition(55, 12);
		System.out.println("Game Over");
		cn.getTextWindow().setCursorPosition(52, 14);
		System.out.println(userName.toUpperCase() + " score: " + human.getEnergy());
		cn.getTextWindow().setCursorPosition(48, 16);
		System.out.println("You survived "+seconds+" seconds");
	}

	/**
	 * refresh maze every 100sec
	 */
	public void refresh() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (int i = 0; i < 21; i++) {
					for (int j = 0; j < 55; j++) {
						if (maze[i][j].equals('*') || maze[i][j].equals('1') || maze[i][j].equals('2')
								|| maze[i][j].equals('3') || maze[i][j].equals('4')) {
							maze[i][j] = ' ';
							addinput();
						}
					}
				}
			}
		}, 100000, 100000);// in every 100 sec with 100sec delay
		/////
		Timer timer2 = new Timer();
		timer2.schedule(new TimerTask() {
			@Override
			public void run() {
				seconds = (seconds + 1) % 100;
			}
		}, 0, 1000); // second counter in every second,no delay

	}

	public void tutorial() throws FileNotFoundException, Exception {
		cn.getTextWindow().setCursorPosition(0, 5);
		System.out.println("-----------------------General Information-----------------------");
		cn.getTextWindow().setCursorPosition(0, 7);
		System.out.println(
				"-	The game is played in a 21*55 game field including walls. There are two competitors: Human (H) and Computer (C).\n"
						+ "-	There are some energy points (*) in the game, which the players collect to increase their energy.\n"
						+ "-	There are also numbers from 1 to 4 in the game, which the human player can push and bring the same numbers together to get extra energy. \n"
						+ "-	Computer (C) always tries to catch Human (H). When it catches the human, the game ends. The aim of the game is having the highest energy point at the end.\n");
		cn.getTextWindow().setCursorPosition(0, 12);
		System.out.println("--------------------------How to Play--------------------------");
		cn.getTextWindow().setCursorPosition(0, 14);
		System.out.println("-	Cursor keys: To move human player\n"
				+ "-	WASD keys: To put an item (related direction) into the backpack\n"
				+ "-	IJKL keys: To remove an item (related direction) from the backpack  \n");
		cn.getTextWindow().setCursorPosition(0, 19);
		System.out.println("--------------------------Energy points--------------------------");
		cn.getTextWindow().setCursorPosition(0, 21);
		System.out.println(" 	Same numbers	Energy\n" + " 	Doubles	     100\n" + " 	Triples         200\n"
				+ " 	Quadruples	  400\n");

		if (keypr == 1)
			Menu();

	}

	public void credits() {
		cn.getTextWindow().setCursorPosition(4, 8);
		System.out.println("University: Dokuz Eylul University");
		cn.getTextWindow().setCursorPosition(4, 9);
		System.out.println("Department: Faculty of Computer Engineering");
		cn.getTextWindow().setCursorPosition(4, 10);
		System.out.println("Course Name: CME1102 - Project Based Learning - II");
		cn.getTextWindow().setCursorPosition(4, 11);
		System.out.println("Coordinator: ****");
		cn.getTextWindow().setCursorPosition(4, 12);
		System.out.println("Project No: 2");
		cn.getTextWindow().setCursorPosition(4, 13);
		System.out.println("Subject: Energy Maze");
		cn.getTextWindow().setCursorPosition(4, 15);
		System.out.println("Details: A simple pacman game.");
		cn.getTextWindow().setCursorPosition(4, 16);
		System.out.println("Version: 1.0");
		cn.getTextWindow().setCursorPosition(4, 17);
		System.out.println("Programmers: 2016510032 Ozgur Gurcan");
		cn.getTextWindow().setCursorPosition(4, 18);
		System.out.println("Programmers: 2015510077 Akif Cakar");
		cn.getTextWindow().setCursorPosition(4, 19);
		System.out.println("Programmers: 2015510032 Ertugrul Ayvaz Gurkan");
		cn.getTextWindow().setCursorPosition(4, 20);
		System.out.println("Date: 14.04.2017 - 00:43");
	}

	/**
	 * Select one of 3 different map and read them from txt and load to maze
	 * array
	 */
	public void newGame() throws Exception {
		cn.getTextWindow().setCursorPosition(4, 10);
		System.out.println("You Can press 1 to select default map or 2 and 3 to select an other map.");
		cn.getTextWindow().setCursorPosition(4, 11);
		System.out.print("Choice is : ");
		int choice = scanner.nextInt();
		if (choice == 1 || choice == 2 || choice == 3) {

			File file = new File("C");
			if (choice == 1) {
				file = new File("D://Eclipse//workshop//Maze4//src//maze.txt");
			} else if (choice == 2) {
				file = new File("D://Eclipse//workshop//Maze4//src//maze2.txt");
			} else if (choice == 3) {
				file = new File("D://Eclipse//workshop//Maze4//src//maze3.txt");
			}
			scanner = new Scanner(file);
			String theString = "";
			theString = scanner.nextLine();
			while (scanner.hasNextLine()) {
				theString = theString + "\n" + scanner.nextLine();
			}
			char[] charArray = theString.toCharArray();
			int column = 0;
			int row = 0;
			for (int i = 0; i < charArray.length; i++) {
				if (column < 55) {
					maze[row][column] = charArray[i];
					column++;
				} else {
					column = 0;
					row++;
				}

			}
		} else {
			cn.getTextWindow().setCursorPosition(4, 11);
			System.out.println("                                            ");
			cn.getTextWindow().setCursorPosition(4, 12);
			System.out.println("Wrong Command, Please Enter a valid command");
			Menu();
		}
	}

	/**
	 * clears all enigma screen
	 */
	public void consoleClear() {
		for (int i = 0; i < 159; i++) {
			for (int j = 0; j < 155; j++) {
				cn.getTextWindow().setCursorPosition(i, j);
				System.out.print(" ");
			}
		}
	}

	/**
	 * creates all inputs like numbers and stars with special suit
	 */
	public void input() {
		int r = 0;
		int counter = 0;
		while (counter < 10) {
			r = random.nextInt(1000);
			if (r >= 500 && r < 1000) {
				list = new Input(5);
				inputlist.enqueue(list.getType());
			}
			if (r >= 375 && r < 500) {
				list = new Input(1);
				inputlist.enqueue(list.getType());
			}
			if (r >= 250 && r < 375) {
				list = new Input(2);
				inputlist.enqueue(list.getType());
			}
			if (r >= 125 && r < 250) {
				list = new Input(3);
				inputlist.enqueue(list.getType());
			}
			if (r >= 0 && r < 125) {
				list = new Input(4);
				inputlist.enqueue(list.getType());
			}
			counter++;
		}

	}

	/**
	 * first defination for inputs,"H","C",cell(distance and suitable)
	 */
	public void firstgeneration() throws FileNotFoundException, Exception {
		int r = 0;
		while (itemcounter < 20) {
			int randomrow = 0;
			int randomcolumn = 0;
			randomrow = random.nextInt(20) + 1;
			randomcolumn = random.nextInt(54) + 1;
			if (maze[randomrow][randomcolumn].equals(' ')) {
				maze[randomrow][randomcolumn] = (char) inputlist.dequeue();
				itemcounter++;
			}
			if (inputlist.size() != 10) {
				r = random.nextInt(1000);
				if (r >= 500 && r < 1000) {
					list = new Input(5);
					inputlist.enqueue(list.getType());
				}
				if (r >= 375 && r < 500) {
					list = new Input(1);
					inputlist.enqueue(list.getType());
				}
				if (r >= 250 && r < 375) {
					list = new Input(2);
					inputlist.enqueue(list.getType());
				}
				if (r >= 125 && r < 250) {
					list = new Input(3);
					inputlist.enqueue(list.getType());
				}
				if (r >= 0 && r < 125) {
					list = new Input(4);
					inputlist.enqueue(list.getType());
				}
			}
		}
		int row = 0;
		int column = 0;
		while (true) {
			row = random.nextInt(20) + 1;
			column = random.nextInt(54) + 1;
			if (maze[row][column].equals(' ')) {
				maze[row][column] = 'H';
				break;
			}
		}
		human.setX(row);
		human.setY(column);

		row = 0;
		column = 0;
		while (true) {
			row = random.nextInt(20) + 1;
			column = random.nextInt(54) + 1;
			if (maze[row][column].equals(' ')) {
				maze[row][column] = 'C';
				break;
			}
		}
		computer.setX(row);
		computer.setY(column);

		for (int i = 0; i < 21; i++) {
			for (int j = 0; j < 55; j++) {
				if ((char) maze[i][j] == '#') {
					cell[i][j][1] = 0;
					cell[i][j][2] = 0;
				} else {
					cell[i][j][1] = 1;
					cell[i][j][2] = 1;
				}

			}
		}
	}

	/**
	 * General play method
	 */
	public void play() throws FileNotFoundException, Exception {
		while (!gameover) {
			clear();
			path();
			screen();
			Thread.sleep(waitTime / 4);
			clear();
			if (!pause) {
				ComputerMovement();
			}
			humanMovement();
			if (!gameover) {
				path();
			}
			screen();
			Thread.sleep(waitTime / 4);
			clear();
			if (human.getEnergy() != 0) {
				humanMovement();
			}
			Thread.sleep(waitTime / 4);
			if (computer.getEnergy() != 0 && !gameover && !pause) {
				ComputerMovement();
			}
			if (!gameover) {
				path();
			}
			screen();
			Thread.sleep(waitTime / 4);
		}
	}

	/**
	 * clears old paths
	 */
	public void clear() {
		for (int i = 0; i < 21; i++) {
			for (int j = 0; j < 55; j++) {
				if (maze[i][j].equals('.')) {
					maze[i][j] = ' ';
					cell[i][j][2] = 1;
				}
			}
		}
	}

	/**
	 * draws path on the game screen
	 */
	public void path() {
		int tempx = computer.getX();
		int tempy = computer.getY();
		int step = 0;
		pathFinded = false;
		while (!pathFinded && step < 200) {
			PathFindingHide();
			step++;
		}
		while (!finalPath.isEmpty()) {
			int pointx = 0;
			int pointy = 0;
			pointy = (int) finalPath.pop();
			pointx = (int) finalPath.pop();
			if (maze[pointx][pointy].equals(' ')) {
				maze[pointx][pointy] = '.';
			}
		}
		computer.setX(tempx);
		computer.setY(tempy);

	}

	/**
	 * pathfinding operation
	 */
	public void PathFindingHide() {

		calculateDistancelofAllMaze();
		double min = 9999.0;
		int neighbourX = computer.getX();

		int neighbourY = computer.getY();

		boolean flag = false;
		if (cell[computer.getX() - 1][computer.getY()][0] < min && cell[computer.getX() - 1][computer.getY()][2] == 1) {
			min = cell[computer.getX() - 1][computer.getY()][0];
			neighbourX = computer.getX() - 1;
			neighbourY = computer.getY();
			flag = true;
		}
		if (cell[computer.getX()][computer.getY() + 1][0] < min && cell[computer.getX()][computer.getY() + 1][2] == 1) {
			min = cell[computer.getX()][computer.getY() + 1][0];
			neighbourX = computer.getX();
			neighbourY = computer.getY() + 1;
			flag = true;
		}
		if (cell[computer.getX() + 1][computer.getY()][0] < min && cell[computer.getX() + 1][computer.getY()][2] == 1) {
			min = cell[computer.getX() + 1][computer.getY()][0];
			neighbourX = computer.getX() + 1;
			neighbourY = computer.getY();
			flag = true;
		}
		if (cell[computer.getX()][computer.getY() - 1][0] < min && cell[computer.getX()][computer.getY() - 1][2] == 1) {
			min = cell[computer.getX()][computer.getY() - 1][0];

			neighbourX = computer.getX();
			neighbourY = computer.getY() - 1;
			flag = true;
		}
		if (!flag) {
			for (int i = 0; i < 21; i++) {
				for (int j = 0; j < 55; j++) {
					if ((char) maze[i][j] == '#') {
						cell[i][j][2] = 0;
					} else {
						cell[i][j][2] = 1;
					}

				}
			}
			// ComputerMovement();
		}

		computer.setX(neighbourX);
		computer.setY(neighbourY);
		// x ve y noktalarını stack'e at
		finalPath.push(neighbourX);
		finalPath.push(neighbourY);

		cell[computer.getX()][computer.getY()][2] = 0;

		if (distance(computer.getX(), computer.getY()) < 2) {
			pathFinded = true;
		}

	}

	/**
	 * draws maze input backpack and energy
	 */
	public static void screen() throws FileNotFoundException, Exception {
		cn.getTextWindow().setCursorPosition(58, 2);
		System.out.println("Input");
		text = new TextAttributes(Color.BLACK, Color.WHITE);
		cn.setTextAttributes(text);
		cn.getTextWindow().setCursorPosition(58, 3);
		System.out.println("<<<<<<<<<<");
		cn.getTextWindow().setCursorPosition(58, 4);
		text = new TextAttributes(Color.WHITE, Color.BLACK);
		cn.setTextAttributes(text);
		printqueue(inputlist);
		text = new TextAttributes(Color.BLACK, Color.WHITE);
		cn.setTextAttributes(text);
		cn.getTextWindow().setCursorPosition(58, 5);
		System.out.println("<<<<<<<<<<");
		cn.getTextWindow().setCursorPosition(58, 7);
		text = new TextAttributes(Color.WHITE, Color.BLACK);
		cn.setTextAttributes(text);
		System.out.println("Backpack");
		cn.getTextWindow().setCursorPosition(58, 8);
		System.out.println("- - - - - +");
		cn.getTextWindow().setCursorPosition(58, 9);
		System.out.print("               ");
		cn.getTextWindow().setCursorPosition(58, 9);
		printstack();
		cn.getTextWindow().setCursorPosition(68, 9);
		System.out.println("|");
		cn.getTextWindow().setCursorPosition(58, 10);
		System.out.println("- - - - - +");
		cn.getTextWindow().setCursorPosition(58, 13);
		System.out.println("Energy");
		cn.getTextWindow().setCursorPosition(58, 14);
		System.out.println("--------");
		cn.getTextWindow().setCursorPosition(58, 15);
		System.out.println("H:               ");
		cn.getTextWindow().setCursorPosition(58, 15);
		System.out.println("H: " + human.getEnergy());
		cn.getTextWindow().setCursorPosition(58, 16);
		System.out.println("C:                 ");
		cn.getTextWindow().setCursorPosition(58, 16);
		System.out.println("C: " + computer.getEnergy());
		cn.getTextWindow().setCursorPosition(58, 18);
		System.out.println("Time :" + seconds);

		cn.getTextWindow().setCursorPosition(0, 0);
		for (int i = 0; i < 21; i++) {

			for (int j = 0; j < 55; j++) {
				if (maze[i][j].equals('C')) {
					text = new TextAttributes(Color.RED, Color.WHITE);
					cn.setTextAttributes(text);
					System.out.print(maze[i][j]);
				} else if (maze[i][j].equals('H')) {
					text = new TextAttributes(Color.BLUE, Color.WHITE);
					cn.setTextAttributes(text);
					System.out.print(maze[i][j]);
				} else if (maze[i][j].equals('#')) {
					text = new TextAttributes(Color.WHITE, Color.BLACK);
					cn.setTextAttributes(text);
					System.out.print(maze[i][j]);
				} else {
					text = new TextAttributes(Color.BLACK, Color.WHITE);
					cn.setTextAttributes(text);
					System.out.print(maze[i][j]);
				}
			}
			System.out.println();
		}

	}

	/**
	 * Takes the key from the user and makes operations according to them
	 */
	public void humanMovement() throws FileNotFoundException, Exception {
		isHumanMoved = false;
		for (int i = 0; i < 21; i++) {
			for (int j = 0; j < 55; j++) {
				if (maze[i][j].equals('o')) {
					maze[i][j] = ' ';
				}
			}
		}
		klis = new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (keypr == 0) {
					keypr = 1;
					rkey = e.getKeyCode();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		};
		cn.getTextWindow().addKeyListener(klis);

		text = new TextAttributes(Color.BLACK, Color.WHITE);
		cn.setTextAttributes(text);
		if (keypr == 1) {
			isHumanMoved = true;
			switch (rkey) {
			case KeyEvent.VK_LEFT:
				if (human.getEnergy() != 0) {
					human.setEnergy(human.getEnergy() - 1);
				}
				if (maze[human.getX()][human.getY() - 1].equals(' ')
						|| maze[human.getX()][human.getY() - 1].equals('.')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setY(human.getY() - 1);
					maze[human.getX()][human.getY()] = 'H';

				} else if (maze[human.getX()][human.getY() - 1].equals('*')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setY(human.getY() - 1);
					maze[human.getX()][human.getY()] = 'H';
					addinput();
					human.setEnergy(human.getEnergy() + 25);
				} else if (human.getY() - 2 != -1 && maze[human.getX()][human.getY() - 2].equals(' ')
						&& (maze[human.getX()][human.getY() - 1].equals('1')
								|| maze[human.getX()][human.getY() - 1].equals('2')
								|| maze[human.getX()][human.getY() - 1].equals('3')
								|| maze[human.getX()][human.getY() - 1].equals('4'))) {
					maze[human.getX()][human.getY()] = ' ';
					human.setY(human.getY() - 1);
					maze[human.getX()][human.getY() - 1] = (char) maze[human.getX()][human.getY()];
					maze[human.getX()][human.getY()] = 'H';

					checknumber(human.getX(), human.getY() - 1);
				}

				break;
			case KeyEvent.VK_RIGHT:
				if (human.getEnergy() != 0) {
					human.setEnergy(human.getEnergy() - 1);
				}
				if (maze[human.getX()][human.getY() + 1].equals(' ')
						|| maze[human.getX()][human.getY() + 1].equals('.')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setY(human.getY() + 1);
					maze[human.getX()][human.getY()] = 'H';

				} else if (maze[human.getX()][human.getY() + 1].equals('*')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setY(human.getY() + 1);
					maze[human.getX()][human.getY()] = 'H';
					addinput();
					human.setEnergy(human.getEnergy() + 25);
				} else if (human.getY() + 2 != 55 && maze[human.getX()][human.getY() + 2].equals(' ')
						&& (maze[human.getX()][human.getY() + 1].equals('1')
								|| maze[human.getX()][human.getY() + 1].equals('2')
								|| maze[human.getX()][human.getY() + 1].equals('3')
								|| maze[human.getX()][human.getY() + 1].equals('4'))) {
					maze[human.getX()][human.getY()] = ' ';
					human.setY(human.getY() + 1);
					maze[human.getX()][human.getY() + 1] = (char) maze[human.getX()][human.getY()];
					maze[human.getX()][human.getY()] = 'H';

					checknumber(human.getX(), human.getY() + 1);
				}

				break;
			case KeyEvent.VK_UP:
				if (human.getEnergy() != 0) {
					human.setEnergy(human.getEnergy() - 1);
				}
				if (maze[human.getX() - 1][human.getY()].equals(' ')
						|| maze[human.getX() - 1][human.getY()].equals('.')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setX(human.getX() - 1);
					maze[human.getX()][human.getY()] = 'H';

				} else if (maze[human.getX() - 1][human.getY()].equals('*')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setX(human.getX() - 1);
					maze[human.getX()][human.getY()] = 'H';
					addinput();
					human.setEnergy(human.getEnergy() + 25);
				} else if (human.getX() - 2 != -1 && maze[human.getX() - 2][human.getY()].equals(' ')
						&& (maze[human.getX() - 1][human.getY()].equals('1')
								|| maze[human.getX() - 1][human.getY()].equals('2')
								|| maze[human.getX() - 1][human.getY()].equals('3')
								|| maze[human.getX() - 1][human.getY()].equals('4'))) {
					maze[human.getX()][human.getY()] = ' ';
					human.setX(human.getX() - 1);
					maze[human.getX() - 1][human.getY()] = (char) maze[human.getX()][human.getY()];
					maze[human.getX()][human.getY()] = 'H';

					checknumber(human.getX() - 1, human.getY());
				}

				break;
			case KeyEvent.VK_DOWN:
				if (human.getEnergy() != 0) {
					human.setEnergy(human.getEnergy() - 1);
				}
				if (maze[human.getX() + 1][human.getY()].equals(' ')
						|| maze[human.getX() + 1][human.getY()].equals('.')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setX(human.getX() + 1);
					maze[human.getX()][human.getY()] = 'H';

				} else if (maze[human.getX() + 1][human.getY()].equals('*')) {
					maze[human.getX()][human.getY()] = ' ';
					human.setX(human.getX() + 1);
					maze[human.getX()][human.getY()] = 'H';
					addinput();
					human.setEnergy(human.getEnergy() + 25);
				} else if (human.getX() + 2 != 21 && maze[human.getX() + 2][human.getY()].equals(' ')
						&& (maze[human.getX() + 1][human.getY()].equals('1')
								|| maze[human.getX() + 1][human.getY()].equals('2')
								|| maze[human.getX() + 1][human.getY()].equals('3')
								|| maze[human.getX() + 1][human.getY()].equals('4'))) {
					maze[human.getX()][human.getY()] = ' ';
					human.setX(human.getX() + 1);
					maze[human.getX() + 1][human.getY()] = (char) maze[human.getX()][human.getY()];
					maze[human.getX()][human.getY()] = 'H';

					checknumber(human.getX() + 1, human.getY());
				}

				break;
			case KeyEvent.VK_A:
				if ((maze[human.getX()][human.getY() - 1].equals('1')
						|| maze[human.getX()][human.getY() - 1].equals('2')
						|| maze[human.getX()][human.getY() - 1].equals('3')
						|| maze[human.getX()][human.getY() - 1].equals('4')) && !human.getBackpack().isFull()
						&& human.getEnergy() >= 100) {

					human.getBackpack().push(maze[human.getX()][human.getY() - 1]);
					maze[human.getX()][human.getY() - 1] = ' ';
					human.setEnergy(human.getEnergy() - 100);
				}
				break;
			case KeyEvent.VK_D:
				if ((maze[human.getX()][human.getY() + 1].equals('1')
						|| maze[human.getX()][human.getY() + 1].equals('2')
						|| maze[human.getX()][human.getY() + 1].equals('3')
						|| maze[human.getX()][human.getY() + 1].equals('4')) && !human.getBackpack().isFull()
						&& human.getEnergy() >= 100) {

					human.getBackpack().push(maze[human.getX()][human.getY() + 1]);
					maze[human.getX()][human.getY() + 1] = ' ';
					human.setEnergy(human.getEnergy() - 100);
				}
				break;
			case KeyEvent.VK_W:
				if ((maze[human.getX() - 1][human.getY()].equals('1')
						|| maze[human.getX() - 1][human.getY()].equals('2')
						|| maze[human.getX() - 1][human.getY()].equals('3')
						|| maze[human.getX() - 1][human.getY()].equals('4')) && !human.getBackpack().isFull()
						&& human.getEnergy() >= 100) {

					human.getBackpack().push(maze[human.getX() - 1][human.getY()]);
					maze[human.getX() - 1][human.getY()] = ' ';
					human.setEnergy(human.getEnergy() - 100);
				}
				break;
			case KeyEvent.VK_S:
				if ((maze[human.getX() + 1][human.getY()].equals('1')
						|| maze[human.getX() + 1][human.getY()].equals('2')
						|| maze[human.getX() + 1][human.getY()].equals('3')
						|| maze[human.getX() + 1][human.getY()].equals('4')) && !human.getBackpack().isFull()
						&& human.getEnergy() >= 100) {

					human.getBackpack().push(maze[human.getX() + 1][human.getY()]);
					maze[human.getX() + 1][human.getY()] = ' ';
					human.setEnergy(human.getEnergy() - 100);
				}
				break;
			case KeyEvent.VK_J:
				if (maze[human.getX()][human.getY() - 1].equals(' ') && !human.getBackpack().isEmpty()) {
					maze[human.getX()][human.getY() - 1] = human.getBackpack().pop();
					checknumber(human.getX(), human.getY() - 1);
				}
				break;
			case KeyEvent.VK_L:
				if (maze[human.getX()][human.getY() + 1].equals(' ') && !human.getBackpack().isEmpty()) {
					maze[human.getX()][human.getY() + 1] = human.getBackpack().pop();
					checknumber(human.getX(), human.getY() + 1);
				}
				break;
			case KeyEvent.VK_I:
				if (maze[human.getX() - 1][human.getY()].equals(' ') && !human.getBackpack().isEmpty()) {
					maze[human.getX() - 1][human.getY()] = human.getBackpack().pop();
					checknumber(human.getX() - 1, human.getY());
				}
				break;
			case KeyEvent.VK_K:
				if (maze[human.getX() + 1][human.getY()].equals(' ') && !human.getBackpack().isEmpty()) {
					maze[human.getX() + 1][human.getY()] = human.getBackpack().pop();
					checknumber(human.getX() + 1, human.getY());
				}
				break;
			case KeyEvent.VK_SPACE: // Gets 100 energy
				human.setEnergy(human.getEnergy() + 100);
				break;
			case KeyEvent.VK_H: // Gets 100 energy
				help();
				break;
			case KeyEvent.VK_P: // Gets 100 energy
				pause = !pause;
				break;
			default:
				break;
			}
		}
		keypr = 0;
	}

	public void help() {
		cn.getTextWindow().setCursorPosition(90, 5);
		System.out.println("-----HELP-----");
		cn.getTextWindow().setCursorPosition(90, 6);
		System.out.println("WASD keys: To put an item (related direction) into the backpack");
		cn.getTextWindow().setCursorPosition(90, 7);
		System.out.println("IJKL keys: To remove an item (related direction) from the backpack");
		// bir s�re bekle ve temizle
		cn.getTextWindow().setCursorPosition(90, 8);
		System.out.println("\"P\" for pause computer movement");
		// cn.getTextWindow().setCursorPosition(90, 9);
		// System.out.println("deneme");
		// cn.getTextWindow().setCursorPosition(90, 10);
		// System.out.println("deneme");
		// cn.getTextWindow().setCursorPosition(90, 11);
		// System.out.println("deneme");
		// cn.getTextWindow().setCursorPosition(90, 12);
		// System.out.println("deneme");

	}

	/**
	 * the computer moves according pathfinding
	 */
	public void ComputerMovement() {

		calculateDistancelofAllMaze();
		double min = 9999.0;
		int neighbourX = computer.getX();
		int neighbourY = computer.getY();
		boolean flag = false;
		if (cell[computer.getX() - 1][computer.getY()][0] < min && cell[computer.getX() - 1][computer.getY()][1] == 1) {
			min = cell[computer.getX() - 1][computer.getY()][0];
			neighbourX = computer.getX() - 1;
			neighbourY = computer.getY();
			flag = true;
		}
		if (cell[computer.getX()][computer.getY() + 1][0] < min && cell[computer.getX()][computer.getY() + 1][1] == 1) {
			min = cell[computer.getX()][computer.getY() + 1][0];
			neighbourX = computer.getX();
			neighbourY = computer.getY() + 1;
			flag = true;
		}
		if (cell[computer.getX() + 1][computer.getY()][0] < min && cell[computer.getX() + 1][computer.getY()][1] == 1) {
			min = cell[computer.getX() + 1][computer.getY()][0];
			neighbourX = computer.getX() + 1;
			neighbourY = computer.getY();
			flag = true;
		}
		if (cell[computer.getX()][computer.getY() - 1][0] < min && cell[computer.getX()][computer.getY() - 1][1] == 1) {
			min = cell[computer.getX()][computer.getY() - 1][0];

			neighbourX = computer.getX();
			neighbourY = computer.getY() - 1;
			flag = true;
		}
		if (!flag) {
			for (int i = 0; i < 21; i++) {
				for (int j = 0; j < 55; j++) {
					if ((char) maze[i][j] == '#') {
						cell[i][j][1] = 0;
					} else {
						cell[i][j][1] = 1;
					}

				}
			}
		}
		// pointPath(min);
		// need to check next cell ' ' or '*' for getting energy

		maze[computer.getX()][computer.getY()] = ' ';
		computer.setX(neighbourX);
		computer.setY(neighbourY);
		if (maze[computer.getX()][computer.getY()].equals('*')) {
			computer.setEnergy(computer.getEnergy() + 50);
		}
		maze[computer.getX()][computer.getY()] = 'C';

		cell[computer.getX()][computer.getY()][1] = 0;
		if (computer.getEnergy() != 0) {
			computer.setEnergy(computer.getEnergy() - 1);
		}
		if (distance(computer.getX(), computer.getY()) == 0) {
			gameover = true;
		}
		if (distance(computer.getX(), computer.getY()) < 2) {
			pathFinded = true;
		}
	}

	/**
	 * calculates double triple or quad awards
	 */
	public void checknumber(int x, int y) {
		int counter = 0;
		Object number = maze[x][y];
		if (maze[x + 1][y] == number) {
			counter++;
			maze[x + 1][y] = ' ';
		}
		if (maze[x][y + 1] == number) {
			counter++;
			maze[x][y + 1] = ' ';
		}
		if (maze[x - 1][y] == number) {
			counter++;
			maze[x - 1][y] = ' ';
		}
		if (maze[x][y + 1] == number) {
			counter++;
			maze[x][y + 1] = ' ';
		}
		if (maze[x][y - 1] == number) {
			counter++;
			maze[x][y - 1] = ' ';
		}
		if (counter != 0) {
			maze[x][y] = ' ';
		}
		if (counter == 3) {
			human.setEnergy(human.getEnergy() + 400);
		} else if (counter == 2) {
			human.setEnergy(human.getEnergy() + 200);
		} else if (counter == 1) {
			human.setEnergy(human.getEnergy() + 100);
		}

	}

	/**
	 * Calculates the distances of the cells in the entire labyrinth according
	 * to human and computer
	 */
	public void calculateDistancelofAllMaze() {
		for (int i = 0; i < 21; i++) {
			for (int j = 0; j < 55; j++) {
				if (maze[i][j].equals('*') || maze[i][j].equals('H')) {
					cell[i][j][0] = distance(i, j) - 5;
				} else if (maze[i][j].equals(' ') || maze[i][j].equals('.')) {
					cell[i][j][0] = distance(i, j);
				} else if (maze[i][j].equals('#') || maze[i][j].equals('1') || maze[i][j].equals('2')
						|| maze[i][j].equals('3') || maze[i][j].equals('4')) {
					cell[i][j][0] = 9999999.0;
					cell[i][j][1] = 0;
				}
			}
		}
	}

	public static void printqueue(CircularQueue q) // print procedure for a
													// queue
	{
		// int size = q.size();
		for (int i = 0; i < 10; i++) {
			System.out.print(q.peek());
			q.enqueue(q.dequeue());
		}
	}

	public static void printstack() throws FileNotFoundException, Exception // this
																			// procedure
																			// is
																			// for
																			// printing
																			// a
	// stack with the right sequence on
	// the screen
	{
		tStack = new Stack(human.getBackpack().size());
		while (!human.getBackpack().isEmpty()) {
			System.out.print(human.getBackpack().peek() + " ");
			tStack.push(human.getBackpack().pop());
		}
		while (!tStack.isEmpty()) {
			human.getBackpack().push(tStack.pop());
		}

	}

	/**
	 * Refresh inputs
	 */
	public void addinput() {
		int r = 0;
		r = random.nextInt(1000);
		if (r >= 500 && r < 1000) {
			list = new Input(5);
			inputlist.enqueue(list.getType());
		}
		if (r >= 375 && r < 500) {
			list = new Input(1);
			inputlist.enqueue(list.getType());
		}
		if (r >= 250 && r < 375) {
			list = new Input(2);
			inputlist.enqueue(list.getType());
		}
		if (r >= 125 && r < 250) {
			list = new Input(3);
			inputlist.enqueue(list.getType());
		}
		if (r >= 0 && r < 125) {
			list = new Input(4);
			inputlist.enqueue(list.getType());
		}
		int randomrow = 0;
		int randomcolumn = 0;
		while (true) {
			randomrow = random.nextInt(20) + 1;
			randomcolumn = random.nextInt(54) + 1;
			if (maze[randomrow][randomcolumn].equals(' ')) {
				maze[randomrow][randomcolumn] = (char) inputlist.dequeue();
				break;
			}
		}

	}

	// Distance to Human H
	public double toH(int x, int y) {
		return Math.sqrt(Math.pow(human.getX() - x, 2) + Math.pow(human.getY() - y, 2));
	}

	// Distance to Computer C
	public double toC(int x, int y) {
		return Math.sqrt(Math.pow(computer.getX() - x, 2) + Math.pow(computer.getY() - y, 2));
	}

	// Total distance
	public double distance(int x, int y) {
		return toH(x, y) + toC(x, y);
	}
}
