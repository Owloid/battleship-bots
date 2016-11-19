/**
 * @ AUTHOR NAME HERE
 * @ Starter Code By Guocheng
 *
 * 2016-01-30
 * For: Purdue Hackers - Battleship
 * Battleship Client
 */

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.lang.Thread;
import java.util.Random;

public class Battleship {
	public static String API_KEY = "626616546"; ///////// PUT YOUR API KEY HERE /////////
	public static String GAME_SERVER = "battleshipgs.purduehackers.com";

	//////////////////////////////////////  PUT YOUR CODE HERE //////////////////////////////////////

	Random generator = new Random();

	char[] letters;
	int[][] grid; //[y][x]
	boolean wasHit = false;

	boolean[][] placementGrid = new boolean[8][8];
	boolean validPlacement(int row, int col, int length, boolean vertical) {
		if (vertical) {
			if (col-1 >= 0) {
				for (int i = 0; i < length; i++) {
					if (row+i >= 8 || placementGrid[row+i][col-1]) {
						return false;
					}
				}
			}
			for (int i = 0; i < length; i++) {
				if (row+i >= 8 || placementGrid[row+i][col]) {
					return false;
				}
			}
			if (col+1 <= 7) {
				for (int i = 0; i < length; i++) {
					if (row+i >= 8 || placementGrid[row+i][col+1]) {
						return false;
					}
				}
			}
		}
		else {
			if (row-1 >= 0) {
				for (int i = 0; i < length; i++) {
					if (col+i >= 8 || placementGrid[row-1][col+i]) {
						return false;
					}
				}
			}
			for (int i = 0; i < length; i++) {
				if (col+i >= 8 || placementGrid[row][col+i]) {
					return false;
				}
			}
			if (row+1 <= 7) {
				for (int i = 0; i < length; i++) {
					if (col+i >= 8 || placementGrid[row+1][col+i]) {
						return false;
					}
				}
			}
		}
		return true;
	}

	String[] makeRandomPlacement(int length, boolean vertical) {
		String[] placement = new String[2];

		int row = (int)(Math.random() * 8);
		int col = (int)(Math.random() * 8);

		while (!validPlacement(row, col, length, vertical)) {
			row = (int)(Math.random() * 8);
			col = (int)(Math.random() * 8);
		}

		placement[0] = this.letters[row] + String.valueOf(col);
		if (vertical) {
			placement[1] = this.letters[row+length-1] + String.valueOf(col);
			for (int i = 0; i < length; i++) {
				placementGrid[row+i][col] = true;
			}
		}
		else {
			placement[1] = this.letters[row] + String.valueOf(col+length-1);
			for (int i = 0; i < length; i++) {
				placementGrid[row][col+i] = true;
			}
		}

		return placement;
	}

	void placeShips(String opponentID) {
		// Fill Grid With -1s
		for(int i = 0; i < grid.length; i++) { for(int j = 0; j < grid[i].length; j++) grid[i][j] = -1; }

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				placementGrid[i][j] = false;
			}
		}

		/* Placement order */
		int[] shipOrder = {0, 1, 2, 3, 4};
		for (int i = 4; i >= 1; i--) {
			int k = (int)(Math.random() * (i+1));
			int temp = shipOrder[k];
			shipOrder[k] = shipOrder[i];
			shipOrder[i] = temp;
		}


		boolean[] vertical = new boolean[5];
		vertical[0] = Math.random() > 0.5 ? true : false;
		vertical[1] = Math.random() > 0.5 ? true : false;
		vertical[2] = Math.random() > 0.5 ? true : false;
		vertical[3] = Math.random() > 0.5 ? true : false;
		vertical[4] = Math.random() > 0.5 ? true : false;

		// Place Ships
		for (int i = 0; i < 5; i++) {
			int shipNum = shipOrder[i];

			String[] placement = null;

			switch (shipNum) {
				case 0:
					placement = makeRandomPlacement(2, vertical[0]);
					placeDestroyer(placement[0], placement[1]);
					break;
				case 1:
					placement = makeRandomPlacement(3, vertical[1]);
					placeSubmarine(placement[0], placement[1]);
					break;
				case 2:
					placement = makeRandomPlacement(3, vertical[2]);
					placeCruiser(placement[0], placement[1]);
					break;
				case 3:
					placement = makeRandomPlacement(4, vertical[3]);
					placeBattleship(placement[0], placement[1]);
					break;
				case 4:
					placement = makeRandomPlacement(5, vertical[4]);
					placeCarrier(placement[0], placement[1]);
					break;
			}
		}

	}

	boolean moveIsReasonable(int x, int y) {
		// Check locations on X's for fired areas.
		// _ X _
		// X X X
		// _ X _
		if (this.grid[y][x] != -1)
			return false;
		if ((y - 1 > 0) && this.grid[y-1][x] != -1)
			return false;
		if ((y + 1 < 7) && this.grid[y+1][x] != -1)
			return false;
		if ((x - 1 > 0) && this.grid[y][x-1] != -1)
			return false;
		if ((x + 1 < 7) && this.grid[y][x+1] != -1)
			return false;
		return true;
	}

	void makeMove() {
		/*for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if (this.grid[i][j] == -1) {
					String wasHitSunkOrMiss = placeMove(this.letters[i] + String.valueOf(j));

					if (wasHitSunkOrMiss.equals("Hit") || wasHitSunkOrMiss.equals("Sunk")) {
						this.grid[i][j] = 1;
					} else {
						this.grid[i][j] = 0;
					}
					return;
				}
			}
		}*/

		wasHit = false;
		if (wasHit) {
			//intelligentSearch();
		} else {
			String hitSunkMiss;
			int x;
			int y;
			while (true) { // Each slot has a 4.55% chance of being selected each roll.
				x = generator.nextInt(7 + 1); // Select column 0-7 randomly.

				int vertOffset = x % 3;

				if ((vertOffset + 6) < 8) { // Check if the third slot is available.
					y = generator.nextInt(2 + 1); // Select row slot 0-2 randomly.
				} else {
					y = generator.nextInt(1 + 1); // Select row slot 0-1 randomly.
				}

				y = y * 3 + vertOffset; // Scale y position to board, add vertical offset.

				System.out.println(x + ", " + y);

				if (moveIsReasonable(x, y)) {
					// Fire!
					hitSunkMiss = placeMove(this.letters[y] + String.valueOf(x));
					break; // Exit infinite loop. We had a reasonable move and took it.
				}
				// Otherwise, move sucks. So reroll.
			}

			if (hitSunkMiss.equals("Hit")) {
				this.grid[y][x] = 1;
				wasHit = true;
			} else if (hitSunkMiss.equals("Sunk")) {
				this.grid[y][x] = 1;
			} else {
				this.grid[y][x] = 0;
			}
		}
	}

	////////////////////////////////////// ^^^^^ PUT YOUR CODE ABOVE HERE ^^^^^ //////////////////////////////////////

	Socket socket;
	String[] destroyer, submarine, cruiser, battleship, carrier;

	String dataPassthrough;
	String data;
	BufferedReader br;
	PrintWriter out;
	Boolean moveMade = false;

	public Battleship() {
		this.grid = new int[8][8];
		for(int i = 0; i < grid.length; i++) { for(int j = 0; j < grid[i].length; j++) grid[i][j] = -1; }
		this.letters = new char[] {'A','B','C','D','E','F','G','H'};

		destroyer = new String[] {"A0", "A0"};
		submarine = new String[] {"A0", "A0"};
		cruiser = new String[] {"A0", "A0"};
		battleship = new String[] {"A0", "A0"};
		carrier = new String[] {"A0", "A0"};
	}

	void connectToServer() {
		try {
			InetAddress addr = InetAddress.getByName(GAME_SERVER);
			socket = new Socket(addr, 23345);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.print(API_KEY);
			out.flush();
			data = br.readLine();
		} catch (Exception e) {
			System.out.println("Error: when connecting to the server...");
			socket = null;
		}

		if (data == null || data.contains("False")) {
			socket = null;
			System.out.println("Invalid API_KEY");
			System.exit(1); // Close Client
		}
	}



	public void gameMain() {
		while(true) {
			try {
				if (this.dataPassthrough == null) {
					this.data = this.br.readLine();
				}
				else {
					this.data = this.dataPassthrough;
					this.dataPassthrough = null;
				}
			} catch (IOException ioe) {
				System.out.println("IOException: in gameMain");
				ioe.printStackTrace();
			}
			if (this.data == null) {
				try { this.socket.close(); }
				catch (IOException e) { System.out.println("Socket Close Error"); }
				return;
			}

			if (data.contains("Welcome")) {
				String[] welcomeMsg = this.data.split(":");
				placeShips(welcomeMsg[1]);
				if (data.contains("Destroyer")) { // Only Place Can Receive Double Message, Pass Through
					this.dataPassthrough = "Destroyer(2):";
				}
			} else if (data.contains("Destroyer")) {
				this.out.print(destroyer[0]);
				this.out.print(destroyer[1]);
				out.flush();
			} else if (data.contains("Submarine")) {
				this.out.print(submarine[0]);
				this.out.print(submarine[1]);
				out.flush();
			} else if (data.contains("Cruiser")) {
				this.out.print(cruiser[0]);
				this.out.print(cruiser[1]);
				out.flush();
			} else if (data.contains("Battleship")) {
				this.out.print(battleship[0]);
				this.out.print(battleship[1]);
				out.flush();
			} else if (data.contains("Carrier")) {
				this.out.print(carrier[0]);
				this.out.print(carrier[1]);
				out.flush();
			} else if (data.contains( "Enter")) {
				this.moveMade = false;
				this.makeMove();
			} else if (data.contains("Error" )) {
				System.out.println("Error: " + data);
				System.exit(1); // Exit sys when there is an error
			} else if (data.contains("Die" )) {
				System.out.println("Error: Your client was disconnected using the Game Viewer.");
				System.exit(1); // Close Client
			} else {
				System.out.println("Received Unknown Response:" + data);
				System.exit(1); // Exit sys when there is an unknown response
			}
		}
	}

	void placeDestroyer(String startPos, String endPos) {
		destroyer = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
	}

	void placeSubmarine(String startPos, String endPos) {
		submarine = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
	}

	void placeCruiser(String startPos, String endPos) {
		cruiser = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
	}

	void placeBattleship(String startPos, String endPos) {
		battleship = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
	}

	void placeCarrier(String startPos, String endPos) {
		carrier = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
	}

	String placeMove(String pos) {
		if(this.moveMade) { // Check if already made move this turn
			System.out.println("Error: Please Make Only 1 Move Per Turn.");
			System.exit(1); // Close Client
		}
		this.moveMade = true;

		this.out.print(pos);
		out.flush();
		try { data = this.br.readLine(); }
		catch(Exception e) { System.out.println("No response after from the server after place the move"); }

		if (data.contains("Hit")) return "Hit";
		else if (data.contains("Sunk")) return "Sunk";
		else if (data.contains("Miss")) return "Miss";
		else {
			this.dataPassthrough = data;
			return "Miss";
		}
	}

	public static void main(String[] args) {
		Battleship bs = new Battleship();
		while(true) {
			bs.connectToServer();
			if (bs.socket != null) bs.gameMain();
		}
	}
}
