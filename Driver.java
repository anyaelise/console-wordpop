
/** 
 * Name: Anya Marshall
 *
 * Program: WordPop Game
 * This program implements a game where the user removes words from a playing 
 * board and earns points depending on the length of the word.
 *
 * @author Anya Marshall
 * System: NetBeans IDE, jsdk 1.4.2, Windows XP
 */


import java.io.*;
import javax.swing.*;


public class Driver
{

	public static void main(String[] args) throws IOException
	{		
		
		System.out.println("\nAuthor: Anya Marshall");
	 	System.out.println("Program: #3, Graphical WordPop game");
	 	System.out.println("Class: CS 340, Spring 2005\n");  	
	 	
		// check if filename is valid filename
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));	
		if(args[0] == null) {
			System.out.println("No filename entered.");
			System.exit(1);
		}
		
		Board gameBoard = new Board(args[0]);
		gameBoard.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		gameBoard.setSize( 360, 370 ); // set frame size
		gameBoard.setResizable(false);		
		gameBoard.setVisible( true ); // display frame			
	}

}
