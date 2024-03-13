package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Lobby scene.
 * Provides a display for the multiplayer game lobbies.
 */
public class LobbyScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    /**
     * The communicator
     */
    private final Communicator communicator = gameWindow.getCommunicator();

    /**
     * Timer used to request current channels from the server using the communicator
     */
    private final ScheduledExecutorService channelTimer = Executors.newSingleThreadScheduledExecutor();

    /**
     * VBox used to display the current channels available to join
     */
    private VBox channelsList;

    /**
     * The current channels available to join
     */
    private final ArrayList<Text> channels = new ArrayList<>();

    /**
     * The channel the player is currently in if there is one
     */
    private Text joinedChannel;

    /**
     * VBox used to display the lobby of the channel that the player is currently in
     */
    private VBox channelLobby;

    /**
     * Player's name in the channel that they are currently in
     */
    private final SimpleStringProperty nickname = new SimpleStringProperty();

    /**
     * FlowPane used to display the names of the players in the channel that the player is currently in
     */
    private FlowPane playerNames;

    /**
     * TextFlow used to display the messages in the chat of the lobby of the channel that the player is currently in
     */
    private TextFlow messages;

    /**
     * ScrollPane used to scroll up and down the chat of the lobby of the channel that the player is currently in
     */
    private ScrollPane scroller;

    /**
     * Boolean used to indicate whether the scroller should scroll to the bottom or not
     */
    private Boolean scrollToBottom = false;

    /**
     * Text field where messages to send to the chat of the lobby of the channel that the player is currently in are entered
     */
    private TextField messageToSend;

    /**
     * BorderPane used to display the buttons in the lobby of the channel that the player is currently in
     */
    private BorderPane buttons;

    /**
     * VBox used to display the host game button and its text field if pressed
     */
    private VBox hostGame;

    /**
     * Host game button
     */
    private Text hostGameButton;

    /**
     * Host game button's text field
     */
    private TextField hostGameTextField;

    /**
     * Boolean used to indicate whether the host game button's text field is being shown or not
     */
    private Boolean textFieldActive = false;


    /**
     * Creates a new lobby scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Lobby Scene");
    }

    /**
     * Initialises the lobby and sets up anything that needs to be done at the start
     */
    @Override
    public void initialise() {
        logger.info("Initialising Lobby");

        //Adds keyboard support to this scene
        scene.setOnKeyPressed(this::keyboardSupport);

        //Handles receiving a message from the communicator
        communicator.addListener((communication) -> Platform.runLater(() -> {
            var messageSplit = communication.split(" ", 2);
            switch (messageSplit[0]) {
                //If message received are the current channels, populates the channels list display with them
                case "CHANNELS" -> {if (messageSplit.length == 2) this.addChannels(messageSplit[1]);}
                //If message received is confirmation of having joined a channel, joins that channel's lobby
                case "JOIN" -> this.joinChannel(messageSplit[1]);
                //If message received is the player's name in the lobby of the channel that they are currently in, updates String storing it
                case "NICK" -> this.updateNick(messageSplit[1]);
                //If message received is the names of the players in the lobby of the channel that the player is currently in, updates their display
                case "USERS" -> this.updatePlayers(messageSplit[1]);
                //If message received is a message sent to the chat of the lobby of the channel that the player is currently in, adds message to the chat
                case "MSG" -> this.updateChat(messageSplit[1]);
                //If message received is confirmation of having left a channel, leaves that channel's lobby
                case "PARTED" -> this.leaveChannel();
                //If message received is confirmation of the player being the host of the channel that they are currently in, creates and adds a start game button to the channel's lobby
                case "HOST" -> this.makeHost();
                //If message received is confirmation of the game starting, loads the multiplayer scene
                case "START" -> this.startGame();
                //If message received is an error, displays error alert
                case "ERROR" -> this.sendErrorAlert(messageSplit[1]);
            }
        }));

        //Makes channel timer request current channels from the server using the communicator
        channelTimer.scheduleAtFixedRate(() -> Platform.runLater(() -> communicator.send("LIST")), 0, 4, TimeUnit.SECONDS);

        //Handles updating the channel's lobby's chat's scroller
        scene.addPostLayoutPulseListener(this::jumpToBottom);
    }

    /**
     * Builds the lobby layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);

        var mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10, 10, 10, 10));
        lobbyPane.getChildren().add(mainPane);

        //Creates the title
        var title = new Text("Multiplayer Lobby");
        title.getStyleClass().add("title");
        mainPane.setTop(title);
        mainPane.setAlignment(title, Pos.TOP_CENTER);

        //Creates a HBox to hold the main contents of this scene
        var contents = new HBox();
        contents.setSpacing(30);
        mainPane.setCenter(contents);

        //Creates a UI component structure to display the current channels available to join
        var channels = new VBox();
        channels.setAlignment(Pos.CENTER);
        channels.setSpacing(5);
        var channelsHeading = new Text("Current Games");
        channelsHeading.getStyleClass().add("heading");
        channelsList = new VBox();
        channelsList.getStyleClass().add("channel-list");
        channelsList.setPrefHeight(400);
        channelsList.setPrefWidth(250);
        channels.getChildren().addAll(channelsHeading, channelsList);
        contents.getChildren().add(channels);

        //Creates a VBox to hold some of the contents of the contents HBox
        //I.e. the host game button and the lobby that the player is currently if there is one
        var subContents = new VBox();
        subContents.setSpacing(5);
        subContents.setTranslateY(100);
        contents.getChildren().add(subContents);

        //Creates a UI component structure to display the host game button and its text field if pressed
        hostGame = new VBox();
        hostGame.setPrefHeight(55);
        hostGameButton = new Text("Host New Game");
        hostGameButton.getStyleClass().add("host-game-button");
        hostGame.getChildren().add(hostGameButton);
        subContents.getChildren().add(hostGame);

        //Handles the host game button being pressed
        hostGameButton.setOnMouseClicked((event) -> this.setOnHostGameButton());

        //Creates a VBox to hold the channel lobby that the player is currently in, if there is one, and its heading
        channelLobby = new VBox();
        channelLobby.setTranslateY(24);
        subContents.getChildren().add(channelLobby);
    }

    /**
     * Handles the host game button being pressed
     */
    public void setOnHostGameButton() {
        logger.info("Showing host game button's text field");

        //Checks if the host game button's text field is not being shown
        if (!textFieldActive) {
            //If so, displays the host game button's text field
            textFieldActive = true;
            hostGameButton.setSelectionStart(0);
            hostGameButton.setSelectionEnd(hostGameButton.getText().length());
            hostGameButton.setSelectionFill(Color.YELLOW);
            hostGameTextField = new TextField();
            hostGameTextField.setPrefWidth(400);
            hostGameTextField.setPromptText("Enter new game name here");
            hostGame.getChildren().add(hostGameTextField);
            hostGameTextField.requestFocus();

            //Handles the player requesting to create a new channel
            hostGameTextField.setOnKeyPressed((event) -> {if (event.getCode() == KeyCode.ENTER && !hostGameTextField.getText().isBlank()) this.hostGame();});

            //Plays sound effect
            Multimedia.playAudio("sounds/rotate.wav");
        } //Otherwise, stops displaying the host game button's text field
        else {
            textFieldActive = false;
            hostGameButton.setSelectionStart(-1);
            hostGame.getChildren().remove(hostGameTextField);

            //Plays sound effect
            Multimedia.playAudio("sounds/rotate.wav");
        }
    }

    /**
     * Handles the player requesting to create a new channel
     */
    public void hostGame() {
        logger.info("Requesting to create new channel " + hostGameTextField.getText());

        //Stops displaying the host game button's text field
        textFieldActive = false;
        hostGameButton.setSelectionStart(-1);
        hostGame.getChildren().remove(hostGameTextField);

        //Sends request to server to create a new channel
        communicator.send("CREATE " + hostGameTextField.getText());
    }

    /**
     * Populates the channels list display with the channels received from the server
     * @param receivedChannels the channels received
     */
    public void addChannels(String receivedChannels) {
        logger.info("Displaying Channels");

        //Clears the channels list display
        channelsList.getChildren().clear();

        //Stores each channel in a string array
        var filteredChannels = receivedChannels.split("\n");

        //Clears the channels list
        channels.clear();

        //Adds each channel to the channels list display and the channels list
        for (var channel : filteredChannels) {
            //Checks if each channel is the channel that the player is currently in
            if (joinedChannel != null && joinedChannel.getText().equals(channel)) {
                //If so, adds the already created channel instead of re-creating it
                channelsList.getChildren().add(joinedChannel);
                channels.add(joinedChannel);
            } //Otherwise, creates each channel and adds it
            else {
                var newChannel = new Text(channel);
                newChannel.getStyleClass().add("channel-item");
                channelsList.getChildren().add(newChannel);
                channels.add(newChannel);

                //Handles each channel being asked to join
                newChannel.setOnMouseClicked((event) -> communicator.send("JOIN " + newChannel.getText()));
            }
        }
    }

    /**
     * Handles having joined a channel
     * @param channelName name of channel joined
     */
    public void joinChannel(String channelName) {
        logger.info("Joining " + channelName + " channel");

        //Sets the channel as joined
        for (var channel: channels) {
            if (channel.getText().equals(channelName)) joinedChannel = channel;
        }

        //If channel was just created, creates it, adds it to the channels list display and the channels list and sets it as joined
        if (joinedChannel == null) {
            var newChannel = new Text(channelName);
            newChannel.getStyleClass().add("channel-item");
            channelsList.getChildren().add(newChannel);
            channels.add(newChannel);
            joinedChannel = newChannel;
        }

        //Sets the text colour of the channel as yellow to indicate being in it
        joinedChannel.setSelectionStart(0);
        joinedChannel.setSelectionEnd(joinedChannel.getText().length());
        joinedChannel.setSelectionFill(Color.YELLOW);

        //Disables channel's button so that the player can't try to join the channel while already being in it
        joinedChannel.setOnMouseClicked(null);

        //Builds the channel's lobby
        this.buildChannelLobby();
    }

    /**
     * Builds the lobby of the channel that the player is currently in
     */
    public void buildChannelLobby() {
        logger.info("Building " + joinedChannel.getText() + " channel's lobby");

        //Creates channel's lobby's heading
        var channelLobbyHeading = new Text(joinedChannel.getText() + " Game Lobby");
        channelLobbyHeading.getStyleClass().add("heading");

        //Creates VBox to display the channel's lobby
        var channelLobbyDisplay = new VBox();
        channelLobbyDisplay.setSpacing(10);
        channelLobbyDisplay.getStyleClass().add("channel-lobby");
        channelLobbyDisplay.setPrefHeight(275);
        channelLobbyDisplay.setPrefWidth(450);

        //Creates an HBox to display a list of the names of the players in the channel's lobby
        var playerList = new HBox();
        playerList.setSpacing(15);
        playerList.getStyleClass().add("player-list");
        var playerListHeading = new Text("Players:");
        playerListHeading.getStyleClass().addAll("player-list-heading");
        playerNames = new FlowPane();
        playerNames.setHgap(15);
        playerList.getChildren().addAll(playerListHeading, playerNames);

        //Creates a TextFlow to display the channel's lobby's chat
        messages = new TextFlow();
        messages.getStyleClass().add("messages");
        messages.setPrefHeight(150);
        messages.getChildren().add(new Text("Welcome to the lobby \nType /nick NewName to change your name\n"));

        //Adds a scroller to the channel's lobby's chat
        scroller = new ScrollPane();
        scroller.getStyleClass().add("scroller");
        scroller.setFitToWidth(true);
        scroller.setContent(messages);

        //Creates a text field where messages to send to the channel's lobby's chat are entered
        messageToSend = new TextField();
        messageToSend.setPromptText("Send a message");

        //Handles the player sending a message to the channel's lobby's chat
        messageToSend.setOnKeyPressed((event)-> {if (event.getCode() == KeyCode.ENTER) this.sendMessage(messageToSend.getText());});

        //Creates a borderPane to display the start and leave buttons in the channel's lobby
        buttons = new BorderPane();
        var leaveButton = new Button("Leave game");
        buttons.setRight(leaveButton);

        //Handles leave button being pressed
        leaveButton.setOnAction((event) -> communicator.send("PART"));

        //Puts all the UI components of the channel's lobby together
        channelLobbyDisplay.getChildren().addAll(playerList, messages, scroller, messageToSend, buttons);
        channelLobby.getChildren().addAll(channelLobbyHeading, channelLobbyDisplay);
        messageToSend.requestFocus();
    }

    /**
     * Updates the String that stores the player's name in the lobby of the channel that they are currently in
     * @param newNick the updated name of the player in the lobby
     */
    public void updateNick(String newNick) {
        logger.info("Updating nickname");

        //Checks if nickname is received is the player's name, if so stores it in the nickname string
        var nickSplit = newNick.split(":");
        if (nickSplit.length == 1) {
            nickname.set(nickSplit[0]);;
        } else if (nickSplit.length == 2 && nickname.get().equals(nickSplit[0])) {
            nickname.set(nickSplit[1]);
        }
    }

    /**
     * Updates the display of the names of the players in the lobby of the channel that the player is currently in
     * @param namesOfPlayers the updated names of the players in the lobby
     */
    public void updatePlayers(String namesOfPlayers) {
        logger.info("Updating display of the names of the players in the lobby");

        //Clears the display
        playerNames.getChildren().clear();

        //Stores each player's name in a String array
        var playerSplit = namesOfPlayers.split("\n");

        //Adds each player's name to the display
        for (var playerName: playerSplit) {
            var newPlayerName = new Text(playerName);
            newPlayerName.getStyleClass().add("player-list-text");
            playerNames.getChildren().add(newPlayerName);

            //Highlights the name of the player in the display of the names of the players
            if (playerName.equals(nickname.get())) newPlayerName.getStyleClass().add("my-name");
        }

        //Plays sound effect
        Multimedia.playAudio("sounds/message.wav");
    }

    /**
     * Adds a message received from the communicator to the channel's lobby's chat
     * @param messageReceived the message received
     */
    public void updateChat(String messageReceived) {
        logger.info("Adding " + messageReceived + " to channel lobby chat");

        //Splits received message into player name and message
        var messageSplit = messageReceived.split(":");
        var playerName = messageSplit[0];
        String message;
        if (messageSplit.length == 2) {
            message = messageSplit[1];
        } else {
            message = " ";
        }

        //Create a Text to display the message
        var date = new Text("\n[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "]");
        var player = new Text("  " + playerName + ": ");
        player.getStyleClass().add("messages-name");
        var receivedMessage = new Text(message);

        //Add the Text to the channel's lobby's chat
        messages.getChildren().addAll(date, player, receivedMessage);

        //Plays sound effect
        Multimedia.playAudio("sounds/message.wav");

        //Scrolls to the bottom of the channel's lobby's chat
        if(scroller.getVvalue() == 0.0f || scroller.getVvalue() > 0.9f) scrollToBottom = true;
    }

    /**
     * Handles updating the scroller of the chat of the lobby of the channel that the player is currently in
     */
    public void jumpToBottom() {
        //Checks if scroller shouldn't scroll to the bottom of the chat
        if (!scrollToBottom) {
            //If so ends method call
            return;
        }
        //Otherwise, scrolls to the bottom of the chat
        logger.info("Scrolling to bottom");
        scroller.setVvalue(1.0f);
        scrollToBottom = false;
    }

    /**
     * Sends messages and nickname change requests to the server
     * @param message message/request to send
     */
    public void sendMessage(String message) {
        logger.info("Checking message: " + message);

        //Checks if message to send is a request to change nickname
        var msgSplit = message.split(" ");
        if (msgSplit[0].equals("/nick") && msgSplit.length > 1) {
            //If so, sends request to change nickname to server using the communicator
            communicator.send("NICK " + msgSplit[1]);
            messageToSend.clear();
        } //Otherwise, sends message to server using the communicator
        else {
            communicator.send("MSG " + message);
            messageToSend.clear();

        }
    }

    /**
     * Handles having left a channel
     */
    public void leaveChannel() {
        logger.info("Leaving " + joinedChannel.getText() + " channel");

        //Re-enables the channel's button
        var channel = joinedChannel;
        channel.setOnMouseClicked((mouseEvent) -> communicator.send("JOIN " + channel.getText()));

        //Clears and resets the channel's lobby display
        joinedChannel.setSelectionStart(-1);
        joinedChannel = null;
        channelLobby.getChildren().clear();

        //Plays sound effect
        Multimedia.playAudio("sounds/rotate.wav");
    }

    /**
     * Updates the display of the lobby of the channel that the player is currently in once they become the host of it
     */
    public void makeHost() {
        logger.info("Making player host of channel " + joinedChannel.getText());

        //Creates a start button and adds it to the channel's lobby display
        var startButton = new Button("Start game");
        buttons.setLeft(startButton);

        //Handles the start game button being pressed
        startButton.setOnAction((event) -> communicator.send("START"));
    }

    /**
     * Starts the multiplayer challenge
     */
    public void startGame() {
        logger.info("Starting multiplayer challenge");

        //Plays a sound effect
        Multimedia.playAudio("sounds/rotate.wav");

        //Starts the multiplayer challenge
        channelTimer.shutdownNow();
        var multiplayerScene = new MultiplayerScene(gameWindow);
        multiplayerScene.setOnNameRequested(() -> nickname);
        gameWindow.loadScene(multiplayerScene);
    }

    /**
     * Displays an error alert when an error message is received from the server
     * @param errorMessage the error message received
     */
    public void sendErrorAlert(String errorMessage) {
        logger.info("Displaying error alert: " + errorMessage);

        //Plays a sound effect
        Multimedia.playAudio("sounds/fail.wav");

        //Creates and displays the error alert
        var alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Handles a key being pressed
     * @param event the event
     */
    public void keyboardSupport(KeyEvent event) {
        logger.info("Handling a key being pressed");

        //Checks if the ESC key was pressed
        if (event.getCode() == KeyCode.ESCAPE) {
            //If so, checks if host game button's text field is being shown
            if (hostGameTextField != null && hostGameTextField.isFocused()) {
                //If so, stops displaying the host game button's text field
                textFieldActive = false;
                hostGameButton.setSelectionStart(-1);
                hostGame.getChildren().remove(hostGameTextField);

                //Plays sound effect
                Multimedia.playAudio("sounds/rotate.wav");
            } //Otherwise, checks if player is currently in a channel
            else if (messageToSend != null && messageToSend.isFocused()) {
                //If so, requests to leave the channel
                communicator.send("PART");
            } //Otherwise, leaves the scene
            else {
                channelTimer.shutdownNow();
                Multimedia.getMusicPlayer().stop();
                Multimedia.playAudio("sounds/rotate.wav");
                gameWindow.startMenu();
            }
        }
    }
}