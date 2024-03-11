package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Multiplayer Game class handles the main logic, state and properties of the multiplayer version of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class MultiplayerGame extends Game {


    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    protected GameWindow gameWindow;

    /**
     * The communicator
     */
    private final Communicator communicator;

    /**
     * Queue storing game piece values received from the server
     */
    private final LinkedBlockingQueue<Integer> gamePieceQueue = new LinkedBlockingQueue<>();

    /**
     * Number of game piece values received from the server
     */
    private int piecesReceived = 0;

    /**
     * ArrayList used to hold the leaderboard's entries
     */
    private final ArrayList<Pair<String, Integer>> leaderboardEntries = new ArrayList<>();

    /**
     * ObservableArrayList wrapped around the leaderboardEntries ArrayList
     */
    private final ObservableList<Pair<String, Integer>> leaderboardEntriesList = FXCollections.observableArrayList(leaderboardEntries);

    /**
     * SimpleListProperty used to represent the leaderboardEntriesList as a property
     * This allows for binding and adding listeners to the leaderboardEntriesList
     */
    private final ListProperty<Pair<String, Integer>> leaderboardEntriesWrapper = new SimpleListProperty<>(leaderboardEntriesList);

    /**
     * ArrayList used to hold each leaderboard's entry's life status
     */
    private final ArrayList<Pair<String, String>> lifeStatuses = new ArrayList<>();

    /**
     * ObservableArrayList wrapped around the lifeStatuses ArrayList
     */
    private final ObservableList<Pair<String, String>> lifeStatusesList = FXCollections.observableArrayList(lifeStatuses);

    /**
     * SimpleListProperty used to represent the lifeStatusesList as a property
     * This allows for binding and adding listeners to the lifeStatusesList
     */
    private final ListProperty<Pair<String, String>> lifeStatusesWrapper = new SimpleListProperty<>(lifeStatusesList);

    /**
     * Comparator used to sort the leaderboard entries list
     */
    private final Comparator<Pair<String, Integer>> leaderboardSorter = (pair1, pair2) -> {
        if (pair1.getValue() > pair2.getValue()) {
            return -1;
        } else if (pair1.getValue().equals(pair2.getValue())) {
            return 0;
        } else {
            return 1;
        }
    };

    /**
     * ArrayList used to hold the names and game board values of the top 3 players
     */
    private final ArrayList<Pair<String, Grid>> gameBoards = new ArrayList<>();

    /**
     * ObservableArrayList wrapped around the gameBoards ArrayList
     */
    private final ObservableList<Pair<String, Grid>> gameBoardsList = FXCollections.observableArrayList(gameBoards);

    /**
     * SimpleListProperty used to represent the gameBoardsList as a property
     * This allows for binding and adding listeners to the gameBoardsList
     */
    private final ListProperty<Pair<String, Grid>> gameBoardsWrapper = new SimpleListProperty<>(gameBoardsList);

    /**
     * Game's chat
     */
    private final SimpleStringProperty gameChat = new SimpleStringProperty("In-Game Chat Press T to send a chat message");

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, GameWindow gameWindow) {
        super(cols, rows);
        this.gameWindow = gameWindow;
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Initialise a new multiplayer game and set up anything that needs to be done at the start
     */
    @Override
    public void initialiseGame() {
        //Handles receiving a message from the communicator
        communicator.addListener((communication) -> Platform.runLater(() -> {
            var messageSplit = communication.split(" ", 2);
            switch (messageSplit[0]) {
                //If message received is a game piece value, adds it to the game piece queue
                case "PIECE" -> this.addToQueue(Integer.parseInt(messageSplit[1]));
                //If message received is an update on all players, their scores and their number of lives, updates property storing them
                case "SCORES" -> this.updateLeaderboard(messageSplit[1]);
                //If message received is a message to the game's chat, adds message to the game's chat
                case "MSG" -> this.updateChat(messageSplit[1]);
                //If message received is an update on a player's game board, updates the display of the players' game boards
                case "BOARD" -> this.updateGameBoards(messageSplit[1]);
            }
        }));

        //Requests the server for three game piece values using the communicator
        communicator.send("PIECE");
        communicator.send("PIECE");
        communicator.send("PIECE");

        //Requests the server for a status update on all players, their scores and their number of lives using the communicator
        communicator.send("SCORES");

        //Updates the server on the player running out of lives
        lives.addListener((observable, oldValue, newValue) -> {if (newValue.equals(-1)) this.leaveChannel();});

        //Updates the server on the player's initial game board values
        communicator.send("BOARD 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0");
    }

    /**
     * Handles receiving a game piece value from the server
     * @param pieceValue game piece value received
     */
    public void addToQueue(Integer pieceValue) {
        logger.info("Adding game piece value " + pieceValue + " to the game piece queue");

        //Adds game piece value received from the server to the game piece queue
        gamePieceQueue.add(pieceValue);

        //Increments the number of game piece values received from the server
        piecesReceived++;

        //Finishes initialising the game once three game piece values have been received from the server
        if (piecesReceived == 3) {
            super.initialiseGame();
        }
    }

    /**
     * Creates a new random game piece
     * @return game piece created
     */
    @Override
    public GamePiece spawnPiece() {
        logger.info("Spawning new game piece");

        //Creates a new game piece using an integer from the game piece queue
        return GamePiece.createPiece(gamePieceQueue.remove());
    }

    /**
     * Sets the next game piece as the current game piece and sets a new game piece as the next game piece
     */
    @Override
    public void nextPiece() {
        super.nextPiece();

        //Requests the server for another game piece value using the communicator
        communicator.send("PIECE");
    }

    /**
     * Handles what should happen when a game piece is placed
     */
    @Override
    public void afterPiece() {
        super.afterPiece();

        //Updates the server on the player's current game board values
        StringBuilder message = new StringBuilder("BOARD");
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                message.append(" ").append(grid.get(i, j));
            }
        }
        communicator.send(message.toString());
    }

    /**
     * Updates the score based on the number of lines and blocks that were just cleared
     * @param lines number of lines cleared
     * @param blocks number of blocks reset
     */
    @Override
    public void score(int lines, int blocks) {
        super.score(lines, blocks);

        //Sends the server the player's new score value using the communicator
        communicator.send("SCORE " + newScore.get());
    }

    /**
     * Handles what should happen when the timer reaches 0
     */
    @Override
    public void gameLoop() {
        super.gameLoop();

        //Sends the server the player's new number of lives using the communicator
        communicator.send("LIVES " + lives.get());
    }

    /**
     * Gets the leaderboard entries property
     * @return leaderboard entries property
     */
    public ListProperty<Pair<String, Integer>> getLeaderboardEntriesProperty() {
        return leaderboardEntriesWrapper;
    }

    /**
     * Gets the life statuses property
     * @return life statuses property
     */
    public ListProperty<Pair<String, String>> getLifeStatusesProperty() {
        return lifeStatusesWrapper;
    }

    /**
     * Updates the leaderboard entries and life statuses lists using the leaderboard entries received from the server
     * @param entriesReceived the leaderboard entries received
     */
    public void updateLeaderboard(String entriesReceived) {
        logger.info("Updating leaderboard");

        //Clears the leaderboard entries and life statuses lists
        leaderboardEntriesWrapper.clear();
        lifeStatusesWrapper.clear();

        //Creates two array lists to temporarily act as the leaderboard entries and life statuses lists
        var tempLeaderboardEntriesList = new ArrayList<Pair<String, Integer>>();
        var tempLifeStatusesList = new ArrayList<Pair<String, String>>();

        //Stores each leaderboard entry received in a string array
        var leaderboardSplit = entriesReceived.split("\n");


        //Stores each leaderboard's entry's name and score as a pair in the temporary leaderboard entries list
        //Stores each leaderboard's entry's name and life status as a pair in the temporary life statuses list
        for (var entry : leaderboardSplit) {
            var entrySplit = entry.split(":");
            tempLeaderboardEntriesList.add(new Pair<>(entrySplit[0], Integer.parseInt(entrySplit[1])));
            tempLifeStatusesList.add(new Pair<>(entrySplit[0], entrySplit[2]));
        }

        //Sorts the temporary leaderboard entries and life statuses lists
        tempLeaderboardEntriesList.sort(leaderboardSorter);
        tempLifeStatusesList = this.sortLifeStatusList(tempLifeStatusesList, tempLeaderboardEntriesList);

        //Only keeps the top 5 items in the temporary leaderboard entries and life statuses lists
        for (int i = 5; i < tempLeaderboardEntriesList.size(); i++) {
            tempLeaderboardEntriesList.remove(i);
            tempLifeStatusesList.remove(i);
        }

        //Adds all the contents in the temporary leaderboard entries and life statuses lists to the actual lists
        leaderboardEntriesWrapper.addAll(tempLeaderboardEntriesList);
        lifeStatusesWrapper.addAll(tempLifeStatusesList);
    }

    /**
     * Sorts the life statuses list according to the leaderboard entries list using the player names in both lists
     * @param list1 life statuses list
     * @param list2 leaderboard entries list
     * @return the sorted life statuses list
     */
    public ArrayList<Pair<String, String>> sortLifeStatusList(ArrayList<Pair<String, String>> list1, ArrayList<Pair<String, Integer>> list2) {
        logger.info("Sorting life statuses list");

        //Searches for the item in the life statuses list that matches each leaderboard entries list item
        for (int i = 0; i < list2.size(); i++) {
            for (int j = i; j < list1.size(); j++) {
                if (list2.get(i).getKey().equals(list1.get(j).getKey())) {
                    //When found, swaps that item with the item in the same list that has the same index as the leaderboard entries list item
                    var temp = list1.get(i);
                    list1.set(i, list1.get(j));
                    list1.set(j, temp);
                }
            }
        }
        //Returns the sorted life statuses list
        return list1;
    }

    /**
     * Gets the game boards property
     * @return game boards property
     */
    public ListProperty<Pair<String, Grid>> getGameBoardsProperty() {
        return gameBoardsWrapper;
    }

    /**
     * Updates the game boards list using the game board value received from the server
     * @param gameBoard the game board value received
     */
    public void updateGameBoards(String gameBoard) {
        logger.info("Updating game boards list");

        //Creates an array list to temporarily act as the game boards list
        var tempGameBoardsList = new ArrayList<>(gameBoardsWrapper);

        //Clears the game boards list
        gameBoardsWrapper.clear();

        //Sorts the game boards list according to the leaderboard entries list using the player names in both lists
        tempGameBoardsList = this.sortGameBoards(tempGameBoardsList);

        //Stores the player name and game board value received in a string array
        var boardSplit = gameBoard.split(":");

        //Checks if the game board value received belong to one of the top 3 players
        var temp = Math.min(leaderboardEntriesWrapper.getSize(), 3);
        for (int i = 0; i < temp; i++) {
            if (leaderboardEntriesWrapper.get(i).getKey().equals(boardSplit[0])) {

                //If so, updates the temporary game boards list
                var playerName = boardSplit[0];
                boardSplit = boardSplit[1].split(" ");
                var playerGrid = new Grid(5, 5);
                var j = 0;
                for (int x = 0; x < 5; x++) {
                    for (int y = 0; y < 5; y++) {
                        playerGrid.set(x, y, Integer.parseInt(boardSplit[j]));
                        j++;
                    }
                }
                tempGameBoardsList.set(i, new Pair<>(playerName, playerGrid));
            }
        }

        //Adds all the contents in the temporary game boards list to the actual list
        gameBoardsWrapper.addAll(tempGameBoardsList);
    }

    /**
     * Sorts the game boards list according to the leaderboard entries list using the player names in both lists
     * @param list the game boards list
     * @return the sorted game boards
     */
    public ArrayList<Pair<String, Grid>> sortGameBoards(ArrayList<Pair<String, Grid>> list) {
        logger.info("Sorting game boards list");

        //Creates a temporary list to hold the game boards list's contents
        ArrayList<Pair<String, Grid>> tempList = new ArrayList<>(list);

        //Clears the game boards list
        list.clear();

        //Fills the game boards list with empty grids
        list = this.createDefaultGameBoards(list);

        //Adds the original contents of the game boards list back to it in the sorted order
        //Note that the game boards list only contains up to 3 grids so some grids might not be added back
        var loopLimit = Math.min(leaderboardEntriesWrapper.getSize(), 3);
        for (int i = 0; i < loopLimit; i++) {
            for (var entry: tempList) {
                if (leaderboardEntriesWrapper.get(i).getKey().equals(entry.getKey())) {
                    list.set(i, entry);
                }
            }
        }

        //Returns the sorted game boards list
        return list;
    }

    /**
     * Fills the game boards list with empty grids
     */
    public ArrayList<Pair<String, Grid>> createDefaultGameBoards(ArrayList<Pair<String, Grid>> list) {
        logger.info("Filling game boards list with empty grids");

        //Fills the game boards list with up to 3 empty grids depending on the number of players in the game
        var loopLimit = Math.min(leaderboardEntriesWrapper.getSize(), 3);
        for (int i = 0; i < loopLimit; i++) {
            list.add(new Pair<>(leaderboardEntriesWrapper.get(i).getKey(), new Grid(5, 5)));
        }

        //Returns the filled game boards list
        return list;
    }

    /**
     * Gets the game's chat
     * @return the game's chat
     */
    public SimpleStringProperty getGameChat() {
        return gameChat;
    }

    /**
     * Adds a message received from the communicator to the game's chat
     * @param messageReceived the message received
     */
    public void updateChat(String messageReceived) {
        logger.info("Updating game's chat");

        //Splits received message into player name and message
        var messageSplit = messageReceived.split(":");
        var playerName = messageSplit[0];
        String message;
        if (messageSplit.length == 2) {
            message = messageSplit[1];
        } else {
            message = " ";
        }

        //Add the Text to the game's chat
        gameChat.set(playerName + ": " + message);

        //Plays sound effect
        Multimedia.playAudio("sounds/message.wav");
    }

    /**
     * Sends a message to the server
     * @param message the message to send
     */
    public void sendMessage(String message) {
        communicator.send("MSG " + message);
    }

    /**
     * Updates the server on the player "dying"
     */
    public void leaveChannel() {
        communicator.send("DIE");
    }
}
