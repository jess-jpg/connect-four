/**
 * @author Jessica Lu 
 * @date 01/29/2021 
 * 
 * Connect Four Game
 * This is a program that implements the classic Connect Four game.
 * Players play against an AI opponent.
 * User profiles are created to save all-time player W/L statistics
 */

/*-----------------------------------------------------------------------------------
|                                     CHANGELOG                                     |
| jan. 29th, 2021:                                                                  |
|   - added user opening file thing.                                                |
|   - user is forced to enter something that starts with 'n' or 'r'.                |
|                                                                                   |
| feb. 1st, 2021:                                                                   |
|   - finished all of opening file                                                  |
|       - both new and returning player                                             |
|   - set up board                                                                  |
|   - printing board                                                                |
|                                                                                   |
| feb. 2nd, 2021:                                                                   |
|   - created player turn                                                           |
|   - check if game over (unfinished, none of the 3 work)                           |
|   - created very simple AI (random column 1 to 7)                                 |
|   - outputs the winner (does not work with ties)                                  |
|   - *** things to work on: checking if the game is over!                          |
|                                                                                   |
| feb. 3rd, 2021:                                                                   |
|   - quite literally everything else!                                              |
-----------------------------------------------------------------------------------*/

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ConnectFour {

    // variables
    static Scanner sc = new Scanner(System.in);
    static final char userChip = 'O';
    static final char botChip = 'X';
    static final int totalRows = 6;
    static final int totalCols = 7;

    public static void main(String[] args) {
        // variables and constants
        String fileName;
        char[][] gameBoard;
        String gameResult = "";
        final String userWin = "user wins";
        final String botWin = "bot wins";
        final String gameTie = "tie";
        boolean gameOver;
        boolean playAgain;

        fileName = openPlayerFile(); // ask user if they are new or returning and create/open their text file.

        outputUserStats(fileName, gameResult);

        // output the user and bot chip character.
        System.out.println("Your chip is: " + userChip + ".");
        System.out.println("The computer's chip is: " + botChip + ".");
        System.out.println();

        // play through at least once. loop if the user wants to play again
        do {
            gameBoard = setUpBoard(); // reset/set up the board (the board becomes empty)

            printBoard(gameBoard);

            // loop until game ends
            do {
                playerTurn(gameBoard);

                gameResult = checkIfGameOver(gameBoard, userChip); // check if either the user won or the game tied

                printBoard(gameBoard);

                // only run if the game did not end
                if (!gameResult.equals(userWin) && !gameResult.equals(gameTie)) {
                    // bot's turn
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println("\n\nNow it's the computer's turn!");
                        TimeUnit.SECONDS.sleep(1);
                        botTurn(gameBoard);
                    } catch (InterruptedException ie) {
                        System.out.println("Uh oh! " + ie);
                    }

                    printBoard(gameBoard);

                    gameResult = checkIfGameOver(gameBoard, botChip); // check if either the computer won or the game tied
                }
                gameOver = gameResult.equals(userWin) || gameResult.equals(gameTie) || gameResult.equals(botWin);
            } while (!gameOver);

            outputGameResult(gameResult);

            outputUserStats(fileName, gameResult);

            playAgain = askUserPlayAgain();

        } while (playAgain);
    }

    /*---------------------------------------------------------------------------
    | String openPlayerFile()                                                   |
    |---------------------------------------------------------------------------|
    | returns String: The player's file name                                    |
    |---------------------------------------------------------------------------|
    | This program opens the player's file. If they are a new player, a file is |
    | created for them. If they are returning, their old file is opened. If     |
    | their file cannot be found, the program loops and asks if they are new or |
    | returning.                                                                |
    ---------------------------------------------------------------------------*/
    public static String openPlayerFile() {
        // variables and constants
        final int textFileLines = 3;
        boolean fileExists;
        boolean newPlayer;
        String fileName = "";
        final String fileNameEnd = ".txt";
        String lineIn;

        // ask user to enter if they are new or returning and open their file.
        fileExists = false;
        do {
            try {
                // ask user if they are new or returning
                System.out.println("Are you a new or returning player?");
                newPlayer = forceCorrectInputPlayerType();

                // ask user for their name to open their file
                System.out.print("Enter your name: ");
                fileName = sc.nextLine() + fileNameEnd;

                if (newPlayer) {
                    // make all the wins, losses, and ties = 0.
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
                    for (int i = 1; i <= textFileLines; i++) {
                        writer.write("0\n");
                    }
                    fileExists = true;
                    writer.close();
                } else {
                    // create a writer that writes on that file. only works if file exists!
                    BufferedReader reader = new BufferedReader(new FileReader(fileName));
                    lineIn = reader.readLine();

                    // if the line is not empty (which means the file exists and is not empty), fileExists boolean is true and get the w/l/t
                    if (lineIn != null) {
                        fileExists = true;
                    }
                    reader.close();
                }

                // output confirmation message if file exists
                if (fileExists) {
                    System.out.println("Okay, your file has been opened!");
                    System.out.println();
                }
            } catch (IOException e) {
                System.out.println("Uh oh! Your file can't be found!");
                System.out.println("Let's try again, shall we?");
                System.out.println();
            }
        } while (!fileExists);
        return fileName;
    }

    /*---------------------------------------------------------------------------
    | void outputUserStats()                                                    |
    |---------------------------------------------------------------------------|
    | fileName: The name of the file.                                           |
    | gameResult: The game result (user win, game tie, bot win).                |
    |---------------------------------------------------------------------------|
    | This program outputs the user's game stats.                               |
    ---------------------------------------------------------------------------*/
    public static void outputUserStats(String fileName, String gameResult) {
        // variables
        String lineIn;
        int wins = -1;
        int losses = -1;
        int ties = -1;
        int per = 100;
        double totalGames;
        double winPercent, lossPercent;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            // get the previous wins/losses/ties
            lineIn = reader.readLine();
            wins = Integer.parseInt(lineIn);
            lineIn = reader.readLine();
            losses = Integer.parseInt(lineIn);
            lineIn = reader.readLine();
            ties = Integer.parseInt(lineIn);

            // update the wins/losses/ties
            if (gameResult.equals("user wins")) {
                wins++;
            } else if (gameResult.equals("bot wins")) {
                losses++;
            } else if (gameResult.equals("tie")) {
                ties++;
            }

            // write over the wins/losses/ties
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
            writer.write(wins + "\n");
            writer.write(losses + "\n");
            writer.write(ties + "\n");

            writer.close();
            reader.close();

        } catch (IOException e) {
            System.out.println("Uh oh! " + e);
        }

        // calculate win and loss percent
        try {
            totalGames = wins + losses + ties;
            winPercent = wins / totalGames * per;
            lossPercent = losses / totalGames * per;
        } catch (ArithmeticException ae) {
            winPercent = 0;
            lossPercent = 0;
        }

        // output w/l/t and percent of wins and losses
        System.out.println("----- Your Current Stats -----");
        System.out.printf("Wins: %d (%.1f%%)%n", wins, winPercent);
        System.out.printf("Losses: %d (%.1f%%)%n", losses, lossPercent);
        System.out.println("Ties: " + ties);
        System.out.println();
    }

    /*---------------------------------------------------------------------------
    | char[][] setUpBoard()                                                     |
    |---------------------------------------------------------------------------|
    | returns char[][]: The empty game board (nothing on the board).            |
    |---------------------------------------------------------------------------|
    | This program sets up the Connect Four game board. The game board starts   |
    | off completely empty.                                                     |
    ---------------------------------------------------------------------------*/
    public static char[][] setUpBoard() {
        // variables and constants
        char[][] gameBoard = new char[totalRows][totalCols]; // game board

        // set full game board to false (no pieces are on the board)
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalCols; j++) {
                gameBoard[i][j] = ' ';
            }
        }
        return gameBoard;
    }

    /*---------------------------------------------------------------------------
    | void printBoard()                                                         |
    |---------------------------------------------------------------------------|
    | gameBoard: The Connect Four game board.                                   |
    |---------------------------------------------------------------------------|
    | This program prints the Connect Four board.                               |
    ---------------------------------------------------------------------------*/
    public static void printBoard(char[][] gameBoard) {
        // variables
        int letterInt;

        // print out the board
        letterInt = 65;
        System.out.println("  -----------------------------");
        for (int row = 0; row < totalRows; row++) {
            System.out.print((char) (letterInt + row) + " "); // output letter
            for (int col = 0; col < totalCols; col++) {
                System.out.print("| " + gameBoard[row][col] + " ");
            }
            System.out.println("|");
            System.out.println("  -----------------------------");
        }
        System.out.print("  ");
        for (int i = 1; i <= totalCols; i++) {
            System.out.print("  " + i + " ");
        }
    }

    /*---------------------------------------------------------------------------
    | void playerTurn()                                                         |
    |---------------------------------------------------------------------------|
    | gameBoard: The Connect Four game board.                                   |
    |---------------------------------------------------------------------------|
    | This program is the player's turn.                                        |
    ---------------------------------------------------------------------------*/
    public static void playerTurn(char[][] gameBoard) {
        // variables
        int colNum;

        // method for user to enter a number 1 to 7
        colNum = chooseColumn();

        // check which row the piece goes to
        putChipInBoard(gameBoard, colNum, userChip);
    }

    /*---------------------------------------------------------------------------
    | String checkIfGameOver()                                                  |
    |---------------------------------------------------------------------------|
    | returns String: The game result (a win, a tie, or game not over)          |
    |---------------------------------------------------------------------------|
    | gameBoard: The game board.                                                |
    | chip: Either the user chip or bot chip, which is either a 'O' or 'X'.     |
    |---------------------------------------------------------------------------|
    | This program checks if the game is over.                                  |
     ---------------------------------------------------------------------------*/
    public static String checkIfGameOver(char[][] gameBoard, char chip) {
        // variables
        boolean hasWinner = false;
        int piecesOnBoard = 0;
        String gameResult = "";

        // check horizontal
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols - 3; col++) {
                boolean fourConnected = (gameBoard[row][col] == gameBoard[row][col + 1] && gameBoard[row][col + 1] == gameBoard[row][col + 2] && gameBoard[row][col + 2] == gameBoard[row][col + 3]);
                if (fourConnected && (gameBoard[row][col] == chip)) {
                    hasWinner = true;
                    break;
                }
            }
        }

        // check vertical
        for (int col = 0; col < totalCols; col++) {
            for (int row = 0; row < totalRows - 3; row++) {
                boolean fourConnected = gameBoard[row][col] == gameBoard[row + 1][col] && gameBoard[row + 1][col] == gameBoard[row + 2][col] && gameBoard[row + 2][col] == gameBoard[row + 3][col];
                if (fourConnected && (gameBoard[row][col] == chip)) {
                    hasWinner = true;
                    break;
                }
            }
        }

        // check diagonal #1
        for (int row = totalRows - 1; row > totalRows - 3; row--) {
            for (int col = 0; col < totalCols - 3; col++) {
                boolean fourConnected = gameBoard[row][col] == gameBoard[row - 1][col + 1] && gameBoard[row - 1][col + 1] == gameBoard[row - 2][col + 2] && gameBoard[row - 2][col + 2] == gameBoard[row - 3][col + 3];
                if (fourConnected && (gameBoard[row][col] == chip)) {
                    hasWinner = true;
                    break;
                }
            }
        }

        // check diagonal #2
        for (int row = totalRows - 1; row > totalRows - 3; row--) {
            for (int col = totalCols - 1; col >= 3; col--) {
                boolean fourConnected = gameBoard[row][col] == gameBoard[row - 1][col - 1] && gameBoard[row - 1][col - 1] == gameBoard[row - 2][col - 2] && gameBoard[row - 2][col - 2] == gameBoard[row - 3][col - 3];
                if (fourConnected && (gameBoard[row][col] == chip)) {
                    hasWinner = true;
                    break;
                }
            }
        }

        // check for tie (all 42 pieces on the board are placed)
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalCols; j++) {
                if (gameBoard[i][j] == userChip || gameBoard[i][j] == botChip) {
                    piecesOnBoard++;
                }
            }
        }

        // get game result
        if (hasWinner) {
            if (chip == userChip) {
                gameResult = "user wins";
            } else {
                gameResult = "bot wins";
            }
        } else if (piecesOnBoard == 42) {
            gameResult = "tie";
        } else {
            gameResult = "game not over";
        }
        return gameResult;
    }

    /*---------------------------------------------------------------------------
    | void botTurn()                                                            |
    |---------------------------------------------------------------------------|
    | gameBoard: The game board.                                                |
    |---------------------------------------------------------------------------|
    | This program is the bot turn.                                             |
    ---------------------------------------------------------------------------*/
    public static void botTurn(char[][] gameBoard) {
        // variables and constants
        int randomColumn = (int) (Math.random() * totalCols + 1);

        // update the board with the computer piece (random piece from 1 to 7)
        putChipInBoard(gameBoard, randomColumn, botChip);
    }

    /*---------------------------------------------------------------------------
    | void outputGameResult()                                                   |
    |---------------------------------------------------------------------------|
    | gameResult: The game result (user win, game tie, bot win).                |
    |---------------------------------------------------------------------------|
    | This program outputs the winner.                                          |
    ---------------------------------------------------------------------------*/
    public static void outputGameResult(String gameResult) {
        // output who won
        System.out.println("\n");
        if (gameResult.equals("bot wins")) {
            System.out.println("Aww. The bot won! Better luck next time!");
        } else if (gameResult.equals("user wins")) {
            System.out.println("Yay, you won! GG.");
        } else {
            System.out.println("Woah, a tie happened!");
        }
    }

    /*---------------------------------------------------------------------------
    | boolean askUserPlayAgain()                                                |
    |---------------------------------------------------------------------------|
    | returns boolean: True if the user wants to play again, false if the user  |
    |                  does not.                                                |
    |---------------------------------------------------------------------------|
    | This program outputs the user's game stats.                               |
    ---------------------------------------------------------------------------*/
    public static boolean askUserPlayAgain() {
        // variables
        boolean playAgain;
        String input;
        boolean firstTime = true;
        boolean badInput;

        System.out.println("Do you want to play again?");

        // force user to enter something that starts with yes or no
        do {
            System.out.print("Enter 'yes' or 'no': ");
            input = sc.nextLine();

            if (firstTime) { // gets the "enter" from the integer input from playing the game
                input = sc.nextLine();
                firstTime = false;
            }

            badInput = !(input.toLowerCase().startsWith("y") || input.toLowerCase().startsWith("n"));

            // output error message if input doesn't start with 'y' or 'n'
            if (badInput) {
                System.out.println("That doesn't look like yes or no!");
            }
        } while (badInput);

        // depending on user input, boolean playAgain gets a value
        if (input.toLowerCase().startsWith("y")) {
            System.out.println("Okay! A new game will start right now.");
            playAgain = true;
        } else {
            System.out.println("Aww, well have a good day!");
            playAgain = false;
        }

        System.out.println();
        System.out.println("------------------------------------------");

        return playAgain;
    }

    /*---------------------------------------------------------------------------
    | boolean forceCorrectInputPlayerType()                                     |
    |---------------------------------------------------------------------------|
    | returns boolean: The type of player; new or returning. If new, boolean is |
    |                  true. If returning, boolean is false.                    |
    |---------------------------------------------------------------------------|
    | This program forces the user to enter something that starts with 'n' or   |
    | 'r', signifying if the user is new or returning.                          |
    ---------------------------------------------------------------------------*/
    public static boolean forceCorrectInputPlayerType() {
        // variables
        String input;
        boolean badInput;
        boolean newPlayer; // true if user is new, false if user is returning

        // force user to enter new or returning
        do {
            System.out.print("Enter 'new' or 'returning': ");
            input = sc.nextLine();
            badInput = !(input.toLowerCase().startsWith("n") || input.toLowerCase().startsWith("r"));

            // output error message if input doesn't start with 'n' or 'r'
            if (badInput) {
                System.out.println("That doesn't look like 'new' or returning'!");
            }
        } while (badInput);

        // depending on user input, boolean newPlayer gets a value
        if (input.toLowerCase().startsWith("n")) {
            newPlayer = true;
            System.out.println("Okay! A new file will be opened for you.");
        } else {
            newPlayer = false;
            // **** don't print anything out. print it out when the file is actually opened
            // do this just in case the user says they are returning but their file cannot be found.
        }
        return newPlayer;
    }

    /*---------------------------------------------------------------------------
    | int chooseColumn()                                                        |
    |---------------------------------------------------------------------------|
    | returns int: The column number that the user chose.                       |
    |---------------------------------------------------------------------------|
    | This program forces the user to enter a column integer from 1 to 7.       |
    ---------------------------------------------------------------------------*/
    public static int chooseColumn() {
        // variables
        int colNum = -1;
        String filler;

        // force user to enter a column number (integer between 1 and 7)
        System.out.println("\n\nWhat column do you want to drop your piece in?");
        do {
            try {
                System.out.print("Enter a number from 1 to 7: ");
                colNum = sc.nextInt();
                if (colNum < 1 || colNum > 7) {
                    System.out.println("Uh oh! That isn't an integer between 1 and 7!");
                }
            } catch (InputMismatchException e) {
                System.out.println("Uh oh! That doesn't seem to be an integer!");
                filler = sc.nextLine(); // get the "enter" so it doesn't get saved to colNum (and doesn't loop infinitely)
            }
        } while (colNum < 1 || colNum > 7);
        return colNum;
    }

    /*---------------------------------------------------------------------------
    | void putChipInBoard()                                                     |
    |---------------------------------------------------------------------------|
    | gameBoard: The game board. A new piece will be put in.                    |
    | colNum: The column number the user chose to put their piece in.           |
    | chip: Either the user chip or bot chip, which is either a 'O' or 'X'.     |
    |---------------------------------------------------------------------------|
    | This program puts the user's chip into the game board.                    |
    ---------------------------------------------------------------------------*/
    public static void putChipInBoard(char[][] gameBoard, int colNum, char chip) {
        // variables and constants
        int slotsFull;
        boolean putChipIn;

        // check for what row the chip gets put into (based on what is empty under and gravity) and put the chip in
        putChipIn = false;
        slotsFull = 0;
        for (int i = 0; i < totalRows; i++) {
            if (gameBoard[totalRows - i - 1][colNum - 1] == ' ' && !putChipIn) {
                gameBoard[totalRows - i - 1][colNum - 1] = chip;
                putChipIn = true;
            } else {
                slotsFull++;
            }
        }

        // if that column is full, loop the turn.
        if (slotsFull == totalRows) {
            if (chip == userChip) {
                columnFull(gameBoard, colNum, totalRows);
            } else {
                botTurn(gameBoard);
            }
        }
    }

    /*---------------------------------------------------------------------------
    | void putChipInBoard()                                                     |
    |---------------------------------------------------------------------------|
    | gameBoard: The game board. A new piece will be put in.                    |
    | colNum: The column number the user chose to put their piece in.           |
    | rows: The total amount of rows in the game board.                         |
    |---------------------------------------------------------------------------|
    | This program puts the user's chip into the game board.                    |
    ---------------------------------------------------------------------------*/
    public static void columnFull(char[][] gameBoard, int colNum, int rows) {
        // variables
        int slotsFull = -1;

        do {
            System.out.println("Uh oh! Column # " + colNum + " is all full! Pick another column.");
            playerTurn(gameBoard);

            if (slotsFull != rows) {
                slotsFull = -1;
            }
        } while (slotsFull == rows);
    }
}
