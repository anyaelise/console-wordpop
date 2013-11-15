
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
import java.util.Random;
import java.util.ArrayList;

import javax.swing.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.*;


public class Board extends JFrame
{
	/** Each letter on the game board is represented by a Letter button. Each Letter
	 *  is its own event handler. */
	class Letter extends JButton implements MouseListener	
	{
		private int name; // the integer value for the character displayed
		private int iPosition;
		private int jPosition;
		private boolean isMarked = false; // variable used during recursive calls	
		private boolean isPressed = false;		
		
		public Letter()		{		}
		
		/** Initializes the name variable. 
		 *  @param name char associated with this Letter
		 *  @param picture Icon associated with this Letter*/
		public Letter(int name, Icon picture)		
		{
			super(picture);
			this.name = name;		
		}
		
		/** Marks the current letter as visited. */
		public void mark()		
		{
			isMarked = true;
		}
		
		/** Clears the mark on the current letter. */
		public void unmark()
		{
			isMarked = false;
		}

		/** Performs appropriate actions when a letter is selected. */
		public void mouseClicked(MouseEvent e) {
			Letter letter = (Letter)e.getSource();			
			
			if(removedWord.isEmpty()) {
				currentWord.setText("");	// clear display panel		
			}				
			
			/* When a letter is selected that is not connected to the previous letter
			 * selected, all previous selections are cleared and the current selected
			 * letter starts a new word. */ 
			else if(removedWord.isEmpty()== false) {
				Letter l = (Letter)removedWord.get(removedWord.size()-1);  
				int i = l.iPosition;
				int j = l.jPosition;	
				if(isNeighbour(i,j,letter) == false && letter.isPressed == false) {
					currentWord.setText("");
					unpressAll();
					removedWord.clear();
				}
			}
			
			/* When a letter is selected, its Icon is changed and it is added to the
			 * removedWord ArrayList. */
			if(letter.isPressed == false) {
				letter.isPressed = true; // change the pressed state
				Icon downImage = new ImageIcon(getClass().getResource(downImageArray[letter.name-97]));
				letter.setIcon(downImage);
				removedWord.add(letter);	
				/* The display panel is updated to show the string of selected words. */
				currentWord.setText(currentWord.getText() + (char)(letter.name-32));
			}
			
			/* When a letter is unselected, its Icon is returned to the default. Also, 
			 * all letters following it in the current string are also unselected. */
			else { 
				int index = removedWord.indexOf(letter);
				int size = removedWord.size();
				for(int i=index; i<size; i++){
					letter = (Letter)removedWord.get(index); 
					letter.isPressed = false; // change the pressed state
					Icon upImage = new ImageIcon(getClass().getResource(upImageArray[letter.name-97]));
					letter.setIcon(upImage);
					removedWord.remove(index);
				}
				/* update display panel */
				String word = currentWord.getText();
				currentWord.setText(word.substring(0,index));
			}			
		}

	
		// unused methods associated with MouseListener interface
		public void mouseEntered(MouseEvent arg0) 	{		}
		public void mouseExited(MouseEvent arg0) 	{		}		
		public void mousePressed(MouseEvent arg0) 	{		}		
		public void mouseReleased(MouseEvent arg0) 	{		}
	}
	
	/** Handles the undo, check and rotate buttons */
	class ButtonHandler extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {
			if(e.getSource().equals(undo)) { // undo button was clicked
				handleUndo();
			}
			
			else if(e.getSource().equals(check)) { // check button was clicked
				handleCheck();				
			}
			
			else if(e.getSource().equals(rotate)){ // rotate button was clicked
				rotate();
				drawBoard();
			}
			
			else { // suggest button was clicked
			    String suggestion = suggest();
			    
			    currentWord.setText(suggestion.toUpperCase());
			}
		}
		
		
		/** Restores the playing board to the state it was in before the last word
		 *  was removed. */
		public void handleUndo()
		{
			if(undoBuffer.isEmpty()) // nothing to undo
				return;
			
			/* restore letter buttons */
			display = (Letter[][]) undoBuffer.get(undoBuffer.size()-1);
			undoBuffer.remove(undoBuffer.size()-1);
			currentWord.setText("");
			
			/* restore score */
			Integer someInt = (Integer)scoreBuffer.get(scoreBuffer.size()-1);
			scoreBuffer.remove(scoreBuffer.size()-1);
			score = someInt.intValue();
			totalScore.setText(Integer.toString(score));
			
			/* restore word count */
			someInt = (Integer)numWordsBuffer.get(numWordsBuffer.size()-1);
			numWordsBuffer.remove(numWordsBuffer.size()-1);
			wordCount = someInt.intValue();
			numWords.setText(Integer.toString(wordCount));
			
			unpressAll();		
			drawBoard();
			removedWord.clear();
		}
		
		public void handleCheck()
		{
			if(currentWord.getText().equals("")){ // no word to check
				currentWord.setText("NO LETTERS SELECTED");
				return;
			}
			
			// assemble word from letters in removedWord ArrayList
			String word = new String("");
			for(int x=0; x<removedWord.size(); x++) {			
				Letter l = (Letter)removedWord.get(x);
				word = new String(word + (char)l.name);
			}
			
			/* check if string is word */
			if(isWord(word)==false) {
				currentWord.setText("THAT IS NOT A WORD");
				unmarkAll();
				removedWord.clear();
				unpressAll();
				return;
			}	
			
			else {  // remove word
				remove(word);
				
				/* check if there are any words left on the board */
				wordList.clear();
				fillWordList();
				if(wordList.isEmpty()) {
					currentWord.setText("NO WORDS REMAINING");
				}
			}						
		}
	}
	
	static final int ARRAY_SIZE = 6; // number of rows/columns on the board
	Trie wordTrie; // Trie used to store all words in the dictionary
	Letter[][] display = new Letter[ARRAY_SIZE][ARRAY_SIZE]; // array of Letters
	int score = 0; // keeps track of the user's score
	int wordCount = 0; // keeps track of the number of words that have been found
	ArrayList wordList = new ArrayList(); // holds all the possible words to play 
	ArrayList removedWord = new ArrayList(); // contains all the Letters to be removed from the board
	ArrayList undoBuffer = new ArrayList(); // holds copies of the board after each move
	ArrayList scoreBuffer = new ArrayList(); // holds copies of each new score
	ArrayList numWordsBuffer = new ArrayList(); // holds copies of the number of words found
	ArrayList suggestedWord = new ArrayList(); // holds the individual Letter objects in the suggested word
	String[] upImageArray = {"a.gif","b.gif","c.gif","d.gif","e.gif", "f.gif", "g.gif", "h.gif", "i.gif", "j.gif", "k.gif", "l.gif", "m.gif", "n.gif", "o.gif", "p.gif", "q.gif", "r.gif", "s.gif", "t.gif", "u.gif", "v.gif", "w.gif", "x.gif", "y.gif", "z.gif"};
	String[] downImageArray = {"a_down.GIF","b_down.GIF","c_down.GIF","d_down.GIF","e_down.GIF", "f_down.GIF", "g_down.GIF", "h_down.GIF", "i_down.GIF", "j_down.GIF", "k_down.GIF", "l_down.GIF", "m_down.GIF", "n_down.GIF", "o_down.GIF", "p_down.GIF", "q_down.GIF", "r_down.GIF", "s_down.GIF", "t_down.GIF", "u_down.GIF", "v_down.GIF", "w_down.GIF", "x_down.GIF", "y_down.GIF", "z_down.GIF"};
	JPanel letterPanel = new JPanel(new GridLayout(ARRAY_SIZE,ARRAY_SIZE)); // holds all the letters
	JPanel statusPanel = new JPanel(); // shows wordCount and score
	JPanel wordDisplayPanel = new JPanel();  // shows currently selected letters
	JPanel buttonPanel = new JPanel(new GridLayout(4,1));  // holds undo, check, rotate and suggest	
	JPanel topPanel = new JPanel(new GridLayout(2,1));
	JButton undo = new JButton("UNDO");
	JButton check = new JButton("CHECK");
	JButton rotate = new JButton("ROTATE");
	JButton suggest = new JButton("SUGGEST");
	JLabel numWords = new JLabel("0", SwingConstants.RIGHT);
	JLabel numWordsTitle = new JLabel("WORDS FOUND", SwingConstants.LEFT);
	JLabel totalScore = new JLabel("0", SwingConstants.RIGHT);
	JLabel totalScoreTitle = new JLabel("TOTAL SCORE", SwingConstants.LEFT);
	JLabel currentWord = new JLabel("", SwingConstants.CENTER);
	JSeparator separator = new JSeparator();
	
	
	/** Creates a new Board from a 'dictionary' file with a list of words.
	 *  @param filename name of the dictionary file */
	public Board(String filename)
	{
		super("WordPop");
		
		wordTrie = new Trie();
		
		try {			
			wordTrie.buildTree(filename); // fill the Trie with words
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
		} catch (IOException e) {
			System.out.println("IOException.");
		}		
		
		this.getContentPane().setLayout( new FlowLayout() ); // set frame layout
		this.getContentPane().setBackground(Color.BLACK);
		
		// initialize all the panels on the board with their respective objects
		createLetterPanel();
		createStatusPanel();
		createWordDisplayPanel();
		createButtonPanel();	
		createTopPanel();
		
		// add all panels to the board frame
		this.getContentPane().add(topPanel);
		this.getContentPane().add(letterPanel);
		this.getContentPane().add(buttonPanel);
		
		
		ButtonHandler handler = new ButtonHandler();  // create a handler
		/* register buttons with the event handler */
		undo.addMouseListener(handler);
		check.addMouseListener(handler);
		rotate.addMouseListener(handler);
		suggest.addMouseListener(handler);
	}
	
	
	/** Create the panel which holds the undo, check, rotate and suggest buttons. */
	private void createButtonPanel() {
		undo.setPreferredSize(new Dimension(100,50));	
		
		undo.setFont(new Font("SanSerif", Font.BOLD, 14));
		undo.setForeground(Color.WHITE);
		undo.setBackground(Color.GRAY);
		check.setFont(new Font("SanSerif", Font.BOLD, 14));
		check.setForeground(Color.DARK_GRAY);
		check.setBackground(Color.WHITE);
		rotate.setFont(new Font("SanSerif", Font.BOLD, 14));
		rotate.setForeground(Color.WHITE);
		rotate.setBackground(Color.GRAY);
		suggest.setFont(new Font("SanSerif", Font.BOLD, 14));
		suggest.setForeground(Color.DARK_GRAY);
		suggest.setBackground(Color.WHITE);
		
		buttonPanel.add(undo);
		buttonPanel.add(check);
		buttonPanel.add(rotate);
		buttonPanel.add(suggest);
		
		buttonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
	}


	/** Create the panel which displays the currently selected letters. */
	private void createWordDisplayPanel() {
		currentWord.setPreferredSize(new Dimension(250, 30));
		currentWord.setFont(new Font("SanSerif", Font.BOLD, 18));
		currentWord.setForeground(Color.DARK_GRAY);
		
		wordDisplayPanel.add(currentWord);		
		wordDisplayPanel.setBackground(Color.WHITE);
	}


	/** Create the panel which shows the current score and the number of words found. */
	private void createStatusPanel() 
	{		
		totalScoreTitle.setPreferredSize(new Dimension(120,40));
		numWordsTitle.setPreferredSize(new Dimension(120,40));

		statusPanel.setBackground(Color.GRAY);
		
		totalScoreTitle.setFont(new Font("SanSerif", Font.BOLD, 14));
		totalScoreTitle.setForeground(Color.WHITE);
		totalScore.setFont(new Font("SanSerif", Font.BOLD, 14));
		totalScore.setForeground(Color.LIGHT_GRAY);
		numWordsTitle.setFont(new Font("SanSerif", Font.BOLD, 14));
		numWordsTitle.setForeground(Color.WHITE);
		numWords.setFont(new Font("SanSerif", Font.BOLD, 14));
		numWords.setForeground(Color.LIGHT_GRAY);
		
		statusPanel.add(totalScoreTitle);		
		statusPanel.add(totalScore);
		statusPanel.add(separator);
		statusPanel.add(numWordsTitle);
		statusPanel.add(numWords);
	}
	
	
	private void createTopPanel()
	{
		topPanel.setPreferredSize(new Dimension(344, 80));
		topPanel.add(statusPanel);
		topPanel.add(wordDisplayPanel);
		topPanel.setBackground(Color.WHITE);
		topPanel.setBorder(BorderFactory.createEtchedBorder());
	}


	/** Fills the Letter array with Letter buttons and adds them to the letter
	 *  panel. 
	 *  The probability of picking a particular letter is based on the number of 
	 *  occurrences of that letter in the dictionary.*/
	public void createLetterPanel()
	{	
		letterPanel.setPreferredSize(new Dimension(240,240));
		letterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		wordTrie.createProb();		
		Random generator = new Random();
			
		for (int i=0; i<ARRAY_SIZE; i++) {
			for(int j=0; j<ARRAY_SIZE; j++) {
				int index = wordTrie.getLetter(generator.nextInt(100));
				/* create icon */
				Icon upImage = new ImageIcon(getClass().getResource(upImageArray[index-97]));				
				display[i][j] = new Letter(index, upImage);
				display[i][j].iPosition = i; // set row position
				display[i][j].jPosition = j; // set column position
				display[i][j].addMouseListener(display[i][j]); // register with event handler
				display[i][j].setBackground(Color.WHITE);
				letterPanel.add(display[i][j]); // add to panel
			}
		}
	}
		
	
	/** Redraws the letter panel.*/
	public void drawBoard() 
	{
		letterPanel.removeAll();
		
		for(int i=0; i < ARRAY_SIZE; i++) {
			for(int j=0; j<ARRAY_SIZE; j++) {
				letterPanel.add(display[i][j]);
				/* Reregister event handlers for non-empty spaces. */
				if (display[i][j].name != 32) {
					display[i][j].removeMouseListener(display[i][j]);
					display[i][j].addMouseListener(display[i][j]);
				}
			}
		}		
	}
	
	
	/** Checks to see if the given word is in the dictionary. 
	 *  @param word word to be looked up
	 *  @return     true if the word exists, false otherwise */
	public boolean isWord(String word)
	{
		return wordTrie.findString(word);
	}
	
	
	/** Rotates the board 1/4 turn clockwise. */
	/*  All the letters are copied to a new array according to a transition 
	 *  function: xprime = y; yprime = arraysize - 1 - x */
	public void rotate()
	{		
		Letter[][] tempArray = new Letter[ARRAY_SIZE][ARRAY_SIZE];
		int someLetter;		
		
		for(int i=0; i<ARRAY_SIZE; i++) {
			for(int j=0; j<ARRAY_SIZE; j++) {				
				tempArray[j][ARRAY_SIZE-1-i] = display[i][j];
				tempArray[j][ARRAY_SIZE-1-i].iPosition = j;
				tempArray[j][ARRAY_SIZE-1-i].jPosition = ARRAY_SIZE-1-i;
			}
		}
		
		display = tempArray; // assign the temp array to the old one
		dropLetters(); // drop any dangling letters;
		unpressAll(); 
		
		/* see if there are any more moves after rotation */
		currentWord.setText("");
		wordList.clear();
		fillWordList();
		if(wordList.size() == 0) {
			currentWord.setText("NO WORDS REMAINING");
		}
	}
		
	
	/** Suggests a word based on the current contents of the board.
	 *  @ return    the word suggested or a message if no words exist */
	public String suggest()
	{
		wordList.clear();  // empty the ArrayList of words
		fillWordList(); // fill the ArrayList with words from current board
		
		Random generator = new Random();
		int number;
		
		if(wordList.isEmpty()) 
			return ("There are no more words.");
		
		number = generator.nextInt(wordList.size());
		return (String)wordList.get(number); // return a random word		
	}
	
	
	/** Recursive function for creating words to be added to the ArrayList of
	 *  possible words.  Due to performance considerations, only words of length 7 or
	 *  less are generated.
	 *  @param i current row position
	 *	@param j current column position
	 *  @param s string to be concatenated */
	/* The string parameter is concatenated with the surrounding letters of the
	 * current i,j position and if the result is a word, it is added to the ArrayList
	 * of possible words. */
	void fillWordList(String s, int i, int j)
	{
		if(s.length() > 7) // return if max length of word is exceeded
			return;
		
		Letter l;
		String newString;
		
		if(j-1 >= 0) { 
			l = display[i][j-1]; // west neighbour
			
			if(l.name == 32) return; // skip over spaces
			
			if(l.isMarked == false) { // letter hasn't already been visited
				l.mark(); // mark letter as visited
				/* create a new string with west neighbour */
				newString = new String(s + (char)l.name);
				addWord(newString); // check if string is a word, then add it
				/* call the function again with the new string and the neighbours
				 * of the current position */
				fillWordList(newString,i,j-1); 
				l.unmark();
			}
		}
		
		/* The rest of the function is just a repetition of the above block with 
		 * different neighbours. */
		
		if((i-1)>=0 && (j-1)>=0) { 
			l = display[i-1][j-1]; // northwest neighbour
			
			if(l.name == 32) return;
			
			if(l.isMarked == false) {
				l.mark();
				newString = new String(s + (char)l.name);
				addWord(newString);
				fillWordList(newString,i-1,j-1);
				l.unmark();
			}
		}
		
		if(i-1 >= 0) {
			l = display[i-1][j]; // north neighbour
			
			if(l.name == 32) return;
			
			if(l.isMarked == false) {
				l.mark();
				newString = new String(s + (char)l.name);
				addWord(newString);
				fillWordList(newString,i-1,j);
				l.unmark();
			}
		}
		
		if((i-1)>=0 && (j+1)<=ARRAY_SIZE-1) {
			l = display[i-1][j+1]; // northeast neighbour
			
			if(l.name == 32) return;
			
			if(l.isMarked == false) {
				l.mark();
				newString = new String(s + (char)l.name);
				addWord(newString);
				fillWordList(newString,i-1,j+1);
				l.unmark();
			}
		}
		
		if(j+1 <= ARRAY_SIZE-1) {
			l = display[i][j+1]; // east neighbour
			
			if(l.name == 32) return;
			
			if(l.isMarked == false) {
				l.mark();
				newString = new String(s + (char)l.name);
				addWord(newString);
				fillWordList(newString,i,j+1);
				l.unmark();
			}
		}
		
		if((i+1)<=ARRAY_SIZE-1 && (j+1)<=ARRAY_SIZE-1) {			
			l = display[i+1][j+1]; // southeast neighbour
			
			if(l.name == 32) return;
				
			if(l.isMarked == false) {
				l.mark();
				newString = new String(s + (char)l.name);
				addWord(newString);
				fillWordList(newString,i+1,j+1);
				l.unmark();
			}
		}
		
		if(i+1 <= ARRAY_SIZE-1) {
			l = display[i+1][j]; // south neighbour
			
			if(l.name == 32) return;
			
			if(l.isMarked == false) {
				l.mark();
				newString = new String(s + (char)l.name);
				addWord(newString);
				fillWordList(newString,i+1,j);	
				l.unmark();				
			}
		}
		
		if((i+1)<=ARRAY_SIZE-1 && (j-1)>=0) {
			l = display[i+1][j-1]; // southwest neighbour
			
			if(l.name == 32) return;
			
			if(l.isMarked == false) {
				l.mark();
				newString = new String(s + (char)l.name);
				addWord(newString);
				fillWordList(newString,i+1,j-1);
				l.unmark();					
			}
		}
		
	}
	
	
	/** Driver for the primary fillWordList function. */
	/* Finds all possible words that start with each letter of the board */
	public void fillWordList()
	{
		char aChar;
		String aString;
		
		// For each letter on the board...
		for(int i=0; i<ARRAY_SIZE; i++) {
			for(int j=0; j<ARRAY_SIZE; j++) {
				(display[i][j]).mark(); // mark the starting point
				aChar = (char)(display[i][j]).name;
				aString = new String("" + aChar);			
				/* start the word hunt with the current character */
				fillWordList(aString, i, j); 
				unmarkAll(); // unmark all the letters
			}
		}
	}
	
	
	/** Checks if the string parameter is a word, and if so, adds it to the 
	 *  ArrayList of possible words on the current board. 
	 *  @param s  word to be checked */
	public void addWord(String s)
	{
		if(s.length() < 3) // only add words longer than 2 characters
			return;
		
		if(wordTrie.findString(s) == true) {
			wordList.add(s);
		}
	}
	
	
	/** Unmark all the Letters on the board.*/
	void unmarkAll() 
	{
		for(int i=0; i<ARRAY_SIZE; i++) {
			for(int j=0; j<ARRAY_SIZE; j++) {				
				(display[i][j]).unmark();
			}
		}		
	}
	
	
	/** Check the neighbours of a given position to see if any of them have
	 *  the given Letter.
	 *  @param i  row location
	 *  @param j  column location
	 *  @param l  Letter 
	 *  @return true if Lteer was found in a neighbour of position i,j; false otherwise */
	public boolean isNeighbour(int i, int j, Letter l) 
	{		
		if(j-1 >= 0) { // west neighbour			
			if(display[i][j-1].equals(l)) {
				return true;
			}
		}
		
		/* The rest of the function is just a repetition of the above block with 
		 * different neighbours. */
		
		if((i-1)>=0 && (j-1)>=0) { // northwest neighbour
			if(display[i-1][j-1].equals(l)) {
				return true;				
			}			
		}
		
		if(i-1 >= 0) { // north neighbour
			if(display[i-1][j].equals(l)) {
				return true;												
			}		
		}
		
		if((i-1)>=0 && (j+1)<=ARRAY_SIZE-1) { // northeast neighbour
			if(display[i-1][j+1].equals(l)) {
				return true;				
			}			
		}
		
		if(j+1 <= ARRAY_SIZE-1) { // east neighbour
			if(display[i][j+1].equals(l)) {
				return true;					
			}
		}
		
		if((i+1)<=ARRAY_SIZE-1 && (j+1)<=ARRAY_SIZE-1) { // southeast neighbour
			if(display[i+1][j+1].equals(l)) {
				return true;
			}			
		}		
		
		if(i+1 <= ARRAY_SIZE-1) { // south neighbour
			if(display[i+1][j].equals(l)) {
				return true;					
			}					
		}
			
		if((i+1)<=ARRAY_SIZE-1 && (j-1)>=0) { // southwest neighbour
			if(display[i+1][j-1].equals(l)) {
				return true;					
			}		
		}
		
		// no neighbours were pressed
		return false;					
	}
	
	
	/** Makes all dangling letters "fall" down.*/
	public void dropLetters() 
	{
		for (int i=ARRAY_SIZE-1; i>0; i--) {
			for(int j=ARRAY_SIZE-1; j>=0; j--) {
				if(display[i][j].name == 32) {
					while(display[i][j].name == 32) { // remove multiple spaces
					 /* ignore spaces at the top of a column */						
						if(isTop(i,j)==false) {
							drop(i,j);
						}
						
						else
							break; // break when at a position at the top of a column
					}
				}					
			}
		}
	}
	
	/** "Pull down" all the letters above position i,j by 1
	 * @param i row postion
	 * @param j column position */
	void drop(int i, int j) 
	{
		for(; i>0; i=i-1) {
			/* letter is replaced by letter above it */
			copy(display[i][j], display[i-1][j]);					
		}
		
		/* the letter at the top of the column is replaced by a space */
		display[0][j].name = 32;
		
	}
	
	
	/** Check if there are any non-spaces above position i,j 
	 *  @param i row position
	 *  @param j column position
	 *  @return true if there are no non-spaces above position i,j*/
	boolean isTop(int i, int j)
	{
		boolean top = true;
		
		for(; i>=0; i=i-1) {			
			if(display[i][j].name != 32) {
				top = false; // one of the above letters is not a space
			}
		}
		
		return top;			
	}
		
	
	/** Update the score according to a point system based on length of word */
	public void updateScore()
	{
		int points;
		int size = removedWord.size();
		if(size < 3)
			return;  // only words of length > 3 are considered			
		
		switch(size)
		{
			case 3: points = 1; break;
			case 4: points = 2; break;
			case 5: points = 4; break;
			case 6: points = 7; break;
			default: points = 12; break;
		}
		
		currentWord.setText(Integer.toString(points) + " POINTS ADDED");
		score += points;
		
		totalScore.setText(Integer.toString(score)); // update score panel
	}	
	
	
	/** Unselects all the letter buttons on the board.  Also redraws empty spaces 
	 *  to have no Icon. */
	public void unpressAll()
	{
		for(int i=0; i<ARRAY_SIZE; i++) {
			for(int j=0; j<ARRAY_SIZE; j++) {
				if(display[i][j].name != 32) {  // not empty space
					display[i][j].isPressed = false;
					Icon upImage = new ImageIcon(getClass().getResource(upImageArray[display[i][j].name-97]));
					display[i][j].setIcon(upImage);
				}
				else { // empty space
					delete(display[i][j]);
				}
			}
		}
	}
	
	/** Redraws the given Letter to have no Icon and also unregisters it with its
	 *  event handler so nothing happens when it is clicked.
	 *  @param l  Letter to be deleted
	 */
	public void delete(Letter l)
	{
		int i = l.iPosition;
		int j = l.jPosition;
		display[i][j].setIcon(null);
		display[i][j].removeMouseListener(display[i][j]);		
	}
	
	
	/** Copies the letter value from one letter to another while still maintaining
	 *  its original array position. 
	 *  @param x Letter doing the copying 
	 *  @param y Letter being copied */
	public void copy (Letter x, Letter y)
	{		
		x.setIcon(y.getIcon());
		x.name = y.name;
	}
		
	
	/** Removes the given word from the playing board. It is assumed that the word has
	 *  already been checked to be valid.
	 *  @param word word to be removed */
	public void remove(String word)
	{
		Letter[][] newDisplay = new Letter[ARRAY_SIZE][ARRAY_SIZE];
		for(int i=0; i<ARRAY_SIZE; i++) {
			for(int j=0; j<ARRAY_SIZE; j++) {
				newDisplay[i][j] = new Letter(display[i][j].name, display[i][j].getIcon());
				newDisplay[i][j].addMouseListener(newDisplay[i][j]);
				newDisplay[i][j].iPosition = i;
				newDisplay[i][j].jPosition = j;
				display[i][j].setBackground(Color.WHITE);
			}
		}			
		
		/* save the current state of the board (to be used when undo is pressed) */
		undoBuffer.add(newDisplay);
		scoreBuffer.add(new Integer(score));
		numWordsBuffer.add(new Integer(wordCount));
		
		for(int x=0; x<removedWord.size(); x++){
			Letter l = (Letter)removedWord.get(x);
			l.name = 32;
		}
		
		dropLetters();	
		unpressAll();
		updateScore();			
		removedWord.clear();
		wordCount++; // update word count	
		numWords.setText(Integer.toString(wordCount)); // display new word count
	}
		
}
