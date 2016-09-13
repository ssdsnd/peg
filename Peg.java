package org.peg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

class Board {
	private static final int NUM_PEGS = 15;
	private static final String BLANKS = "     ";
	private int[] 	_pegs 		= new int[NUM_PEGS];
	private int 	_pegCount 	= NUM_PEGS;
	private long    _totalTries;
	private long    _totalSolutions;
	private List<Integer> 	_deferredMoves = new ArrayList<Integer>();
	private List<Integer>	_currentMoveList = new ArrayList<Integer>();
	
	private static final int[][] JUMP_TABLE = {
		{1,4,2},    {1,6,3},
        {2,7,4},    {2,9,5},
/* {a,b,c}                 */    {3,8,5},    {3,10,6},
/*   a = "from" position   */    {4,6,5}, {4,1,2}, {4,11,7}, {4,13,8},
/*   b = "to" position     */    {5,14,9},   {5,12,8},
/*   c = "jumped" position */    {6,4,5}, {6,13,9}, {6,15,10}, {6,1,3},
        {7,2,4},    {7,9,8},
        {8,3,5},    {8,10,9},
        {9,2,5},    {9,7,8},
        {10,8,9},   {10,3,6},
        {11,13,12}, {11,4,7},
        {12,5,8},   {12,14,13},
        {13,11,12}, {13,15,14}, {13,6,9}, {13,4,8},
        {14,12,13}, {14,5,9},
        {15,13,14}, {15,6,10}
	};

	public Board() {
		for (int i = 0; i < _pegs.length; ++i) {
			_pegs[i] = 1;
		}
	}
	
	private static void assertPeg(int peg) {
		assert(peg > 0 && peg <= NUM_PEGS);
	}

	public void removeInitialPeg(int hole) {
		assertPeg(hole);
		_pegs[hole-1] = 0;
		_pegCount--;
	}

	public int getPegCount() {
		return _pegCount;
	}
	
	public boolean isGameOver() {
		return _pegCount == 1 || this.generateLegalMoves().isEmpty();
	}

	public boolean isPegPresent(int p) {
		assertPeg(p);
		if (_pegs[p-1] == 1) {
			return true;
		}
		return false;
	}

	public List<Integer> generateLegalMoves() {
		List<Integer> moves = new ArrayList<Integer>();
		
		for (int i = 0; i < JUMP_TABLE.length; i++) {
			if (isLegalMove(i)) {
				moves.add(Integer.valueOf(i));
			}
		}
		
		return moves;
	}

	private boolean isLegalMove(int n) {
		int from = JUMP_TABLE[n][0];
		int to = JUMP_TABLE[n][1];
		int jumped = JUMP_TABLE[n][2];
			
		if (isPegPresent(from) && isPegPresent(jumped) && !isPegPresent(to)) {
			return true;
		}

		return false;
	}
	
	public void move(int n) {
		if (isLegalMove(n)) {
			int from = JUMP_TABLE[n][0];
			int to = JUMP_TABLE[n][1];
			int jumped = JUMP_TABLE[n][2];
			_pegs[from-1] = 0;
			_pegs[jumped-1] = 0;
			_pegs[to-1] = 1;
			_pegCount--;
			_currentMoveList.add(Integer.valueOf(n));
		}
	}

	public int pushDeferredMovesToStack(){
		int numPushed = 0;
		for (int i = 0; i < JUMP_TABLE.length; i++) {
			if (isLegalMove(i)) {
				_deferredMoves.add(0, Integer.valueOf(i));
				numPushed++;
			}
		}
			
		return numPushed;
	}

	public void run() {
		int numPushed = pushDeferredMovesToStack();
		for (int i = 0; i < numPushed; i++) {
			depthFirstSearch();
		}
	}

	public void depthFirstSearch() {
		int move = _deferredMoves.remove(0).intValue();
		doMove(move);

		if (getPegCount() == 1) {
			reportSolutions();
			_totalTries++;
			undoMove(move);
			return;
		}
		
		int numPushed = pushDeferredMovesToStack();
		for (int i = 0; i < numPushed; i++) {
			depthFirstSearch();
		}
		
		if (numPushed == 0) {
			_totalTries++;
		}
		undoMove(move);
	}
	
	private void reportSolutions() {
		_totalSolutions++;
		if (_totalSolutions % 1000 == 0) {
			for (int i = 0; i < _currentMoveList.size(); i++) {
				System.out.print( printMove(_currentMoveList.get(i)) + " ");
			}
			System.out.println("#totalTries=" + _totalTries + ", solutions=" + _totalSolutions + ", ratio=" + 
					_totalTries/_totalSolutions);
//			print();
		}
	}
	
	public void doMove(int n) {
		move(n);
	}
	
	public void undoMove(int n) {
		int from = JUMP_TABLE[n][0];
		int to = JUMP_TABLE[n][1];
		int jumped = JUMP_TABLE[n][2];
		_pegs[from-1] = 1;
		_pegs[jumped-1] = 1;
		_pegs[to-1] = 0;
		
		_pegCount++;
		
		_currentMoveList.remove(_currentMoveList.size()-1);
	}
	
	public void print() {
		print("");
	}

	public String printMove(int n) {
		return JUMP_TABLE[n][0] + "-" + JUMP_TABLE[n][1];
	}

	public void print(String note) {
		if (note != null && !note.isEmpty()) {
			System.out.println(note);
		}
		int r = 5;
		int p = 0;
		for (int i = 1; i <= r; i++) {
			System.out.print(BLANKS.substring(i, r));
			for (int j = 1; j <= i; j++) {
				System.out.print(_pegs[p++] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
}

public class Peg {
	
	public static void main(String[] args) {
		
		Random r = new Random();
		Set<String> winningMoves = new TreeSet<String>();
		
		for (int i = 0 ; i < 2000; i++) {
			List<String> gameMoves = new ArrayList<String>();
			Board board = new Board();
			board.removeInitialPeg(1);
			
			while (!board.isGameOver()) {
				List<Integer> moves = board.generateLegalMoves();
				int move = moves.get(r.nextInt(moves.size())).intValue();
				board.move(move);
				gameMoves.add(board.printMove(move));
	//			board.print(move);
			}
			if (board.getPegCount() == 1) {
				winningMoves.add(gameMoves.toString());
			}
		}
		
//		int c = 0;
//		for (String wm : winningMoves) {
//			System.out.println(wm);
//			if (c++==100) {
//				break;
//			}
//		}
//		System.out.println(winningMoves.size());
 
		Board board = new Board();
		board.removeInitialPeg(1);
		board.run();
	}
}
