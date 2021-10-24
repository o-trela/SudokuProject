package pl.first.firstjava;

import java.lang.Math;
import java.lang.System;
import java.util.Random;

public class SudokuBoard {
    private final int[][] board;
    private final int boardSize;
    private final int boxSize; // square root of N
    private final Random random;
    private final SudokuSolver sudokuSolver;

    SudokuBoard() {
        this.boardSize = 9;
        this.boxSize = 3;
        this.random = new Random();
        this.board = new int[boardSize][boardSize];
        sudokuSolver = new BacktrackingSudokuSolver();
        this.fillDiagonal();
    }

    SudokuBoard(int[][] givenBoard) {
        this.boardSize = 9;
        this.boxSize = 3;
        this.random = new Random();
        this.board = new int[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                this.board[row][column] = givenBoard[row][column];
            }
        }
        sudokuSolver = new BacktrackingSudokuSolver();
    }

    public void solveGame() {
        sudokuSolver.solve(this);
    }

    public int get(int x, int y) {
        return board[x][y];
    }

    public void set(int x, int y, int value) {
        board[x][y] = value;
    }

    public int getBoardSize() {
        return this.boardSize;
    }

    public int getBoxSize() {
        return this.boxSize;
    }

    public int[][] getBoard() {
        int[][] boardCopy;
        boardCopy = new int[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            System.arraycopy(this.board[row], 0, boardCopy[row], 0, boardSize);
        }
        return boardCopy;
    }

    // Board Generator
    // Fill the diagonal boxSize number of boxSize x boxSize matrices.
    private void fillDiagonal() {
        for (int i = 0; i < boardSize; i += boxSize) {
            // Fill only diagonals (row==column).
            fillBox(i, i);
        }
    }

    // Fill indicated matrix.
    private void fillBox(int row, int column) {
        for (int i = 0; i < boxSize; i++) {
            for (int j = 0; j < boxSize; j++) {
                int num;
                do {
                    num = getRandomInt();
                } while (!unUsedInBox(row, column, num));

                board[row + i][column + j] = num;
            }
        }
    }

    // Random generator
    private int getRandomInt() {
        return getRandomInt(boardSize);
    }

    private int getRandomInt(int max) {
        return getRandomInt(max, 1);
    }

    private int getRandomInt(int max, int min) {
        return Math.abs(random.nextInt() % max + min);
    }

    // Checking whole board
    public boolean isBoardValid() {
        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                if (!(isValid(row, column, board[row][column]))) {
                    return false;
                }
            }
        }
        return true;
    }

    // Check if safe to put in cell
    private boolean isValid(int i, int j, int num) {
        int temp = board[i][j];
        board[i][j] = -1;
        boolean isUnused = (unUsedInRow(i, num)
                && unUsedInColumn(j, num)
                && unUsedInBox(i - i % boxSize, j - j % boxSize, num));
        board[i][j] = temp;
        return isUnused;
    }

    // by doing [i - i % SRN][j - j % SRN] we are always getting first cell of matrix

    // check in the row for existence
    private boolean unUsedInRow(int row, int num) {
        for (int column = 0; column < boardSize; column++) {
            if (board[row][column] == num) {
                return false;
            }
        }
        return true;
    }

    // check in the row for existence
    private boolean unUsedInColumn(int column, int num) {
        for (int row = 0; row < boardSize; row++) {
            if (board[row][column] == num) {
                return false;
            }
        }
        return true;
    }

    // Returns false if given 3 x 3 block contains num.
    private boolean unUsedInBox(int rowStart, int columnStart, int num) {
        for (int row = 0; row < boxSize; row++) {
            for (int column = 0; column < boxSize; column++) {
                if (board[rowStart + row][columnStart + column] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    // Print board
    public void printBoard() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                System.out.print(board[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}
