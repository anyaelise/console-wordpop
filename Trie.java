
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

public class Trie
{
	//primary data structure
	class TrieNode 
	{
		int letter; //letter which node corresponds to
		boolean isStart = false; //indicates whether node is start of word
		TrieNode leftChild, rightSib; //related nodes
		
		public TrieNode() { }
		
		public TrieNode(int l)
		{
			letter = l;
		}
		
		public TrieNode(int l, TrieNode lc, TrieNode rs)
		{
			letter = l;
			leftChild = lc;
			rightSib = rs;
		}
	}
	
	//global variables
	TrieNode root = new TrieNode(); //root of tree
	TrieNode cursor = root; //position reference
	
	int[] letterCnt = new int[26]; //array containing the number of occurences of each letter in the trie
	double[] letterProb = new double[26]; //array of letter probabilities
	
			
	
	public void buildTree(String fileName) throws FileNotFoundException, IOException
	/* This function constructs the tree using the words in the file supplied
	 * by the user. 
	 * Input parameter: String(filename)
	 * Return type: none*/
	{
		FileReader fileStream = new FileReader(fileName);		
		BufferedReader inputFile = new BufferedReader(fileStream);
		String inputLine;
		int counter = 0;
		initialize(letterCnt);
		
		for( ; ;) {
			inputLine = inputFile.readLine(); //read a word from file
			
			if(inputLine == null){ //end of file reached
				//System.out.println(counter + " words added.");
				return; 
			}
				
			addString(inputLine); //add word to tree
			counter++;			
		}				
	}
	
	
	public boolean findString(String s)
	/* This function finds a string in the tree, and updates the position reference
	 * to the first word ending with that string.
	 * Input parameter: String
	 * Return type: boolean - returns true if found, false if not found*/
	{
		if(s.length() < 3) { // strings should be length 3 or more
			return false;
		}  
	
		TrieNode parent = root;
		cursor = parent.leftChild;
		
		for(int i = 0; i < s.length(); i++) {
			if(cursor == null) {
				return false;
			}
			
			int current = (int)s.charAt(i); //current letter
			
			if(current < 97) {
				current += 32; //folding all characters into lowercase
			}
			
			if(current<97 || current>122) { 
			 	continue;  //filtering out non-alphabetic characters
			 }
				
			if(current < cursor.letter) { //letter is lower than the lowest
				return false;				//current letter
			}
			
			else if(current == cursor.letter) { //one letter found
				parent = cursor;
				cursor = cursor.leftChild; //go to row below
			}				
				
			else if(current > cursor.letter) { //find the letter in current row
				TrieNode leftSib = new TrieNode();
				while((cursor.rightSib != null) && (current > cursor.letter)) {
					leftSib = cursor;
					cursor = cursor.rightSib;
				}
				if(cursor.letter != current) {
					return false;
				}
				else {
					parent = cursor;
					cursor = cursor.leftChild;
				}
			}
		}			
		
		if(parent.isStart == true) {			
			return true;
		}
			
		return false;
	}
		
		
	public void addString(String s)
	/* This function adds a given string in the tree. It is used by the buildTree()
	 * function.	 
	 * Input parameter: String
	 * Return type: none */	
	{
		if(s.length() < 3) {
			return;
		}						
		
		TrieNode parent = root;
		
		for(int i = 0; i < s.length(); i++) {
			
			int newLetter = (int)s.charAt(i);
			
			if(newLetter < 97) {
				newLetter += 32; //folding all characters into lowercase
			}
			
			if(newLetter<97 || newLetter>122) { 
			 	continue;  //filtering out non-alphabetic characters
			 }			 
			
			//adding new letter as first child
			if(parent.leftChild == null) {
				TrieNode extraNode = new TrieNode(0, parent, null); 
				parent.leftChild = new TrieNode(newLetter, null, extraNode);
				parent = parent.leftChild;
				
				addInstance(newLetter);
			}
			
			else {	//adding new letter somewhere in the middle of the tree			
				TrieNode temp = parent.leftChild;
				
				if(newLetter < temp.letter) { //insert at front of list
					parent.leftChild = new TrieNode(newLetter, null, temp); //update leftmost child reference
					parent = parent.leftChild; //reassign parent for next iteration
					
					addInstance(newLetter);					
				}
				
				if(newLetter > temp.letter) {
					TrieNode leftSib = new TrieNode();	
					// if temp.rightSib is null, end of list has been reached				
					while((temp.rightSib != null) && (newLetter > temp.letter)) {
						leftSib = temp;
						temp = temp.rightSib;							
					}
					if(newLetter != temp.letter) {					
						leftSib.rightSib = new TrieNode(newLetter, null, temp); //insert letter in the middle of the list(or end if temp is the dummy/extra node)
						parent = leftSib.rightSib; //reassign parent for next iteration
						
						addInstance(newLetter);
					}
				}
				
				if(newLetter == temp.letter) {
					parent = temp;
				}
			}
		}
		
		parent.isStart = true;
	}
	
	
	public void remString(String s) throws IOException
	/* This function removes a given string from the tree, and resets the 
	 * position reference to the root of the tree.
	 * Input parameter: String
	 * Return type: none */
	{
		//check if string exists
		boolean isFound = findString(s);		
		if(isFound == false) {
			System.out.println("That string does not exist.");
			return;
		}
		
		//get confirmation
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));				
		System.out.println("Warning! This will delete all strings ending in \"" + s + "\".");
		System.out.print("Do you wish to continue? (Y to continue) ");		
		String input = console.readLine();		
		if(!(input.equals("Y") || input.equals("y"))) {
			return;
		}
		
		TrieNode temp = cursor; //cursor is pointing at left child of first node to be deleted
		
		//go to actual node to be deleted

		while(temp.rightSib != null) {
			temp = temp.rightSib;
		}
		
		temp = temp.leftChild; //temp is now node to be deleted
		
		
		//go to parent of node to be deleted so references can be reassigned
		while(temp.rightSib != null) {
			temp = temp.rightSib;
		}
		temp = temp.leftChild; //temp is now parent of node to be deleted
		
		//go through row that node is in and reassign references
		TrieNode parent = temp;
		temp = temp.leftChild; //temp is now first left child in row
		TrieNode leftSib = null;
	
		int letter = (int)s.charAt(0); //first letter of string to be removed
									   //(same as node to be removed)
		if(temp.letter != letter) {
			while(temp.letter != letter) {
				leftSib = temp;
				temp = temp.rightSib;
			}
		}
		
		//now temp == node to be removed	
		if(leftSib == null) { //temp is leftChild of parent
			parent.leftChild = temp.rightSib;
		}
		else { //temp is in middle of row
			leftSib.rightSib = temp.rightSib;
		}			
		
		System.gc(); // garbage collection
		cursor = root;
		System.out.println("String removed.");					
	}	
	
	
	public void initialize(int[] a) {
		
		for(int i=0; i < a.length; i++) {
			a[i] = 0;
		}
	}

	
	public void addInstance(int letter)
	{
		++letterCnt[letter - 97];		
	}
	
	
	public void createProb()
	{
		int sum = 0; //total number of letters
		
		for(int i=0; i < letterCnt.length; i++) {
			sum += letterCnt[i];			
		}		
		
		
		letterProb[0] = (double)letterCnt[0]/sum;
		for(int i=1; i < letterProb.length; i++) {
			letterProb[i] = letterProb[i-1] + (double)letterCnt[i]/sum;
		}			
	}
	
	
	public int getLetter(double number) 
	{
		for(int i=0; i < letterProb.length; i++) {
			if(number < letterProb[i]*100) {
				return i + 97;
			}
		}	
		
		return 25 + 97; //'Z'	
	}
	
	
}
