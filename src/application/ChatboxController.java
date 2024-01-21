package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ChatboxController implements Initializable {
	
	@FXML
	private Label connectedAsUsernameLabel;
	
	@FXML
	private AnchorPane chatboxWindow;
	
	@FXML
	private HBox chatboxWindowTopBar;
	
	@FXML
	private ScrollPane scrollpaneChatbox;
	
    @FXML
    private TextFlow chatbox;
	
    @FXML
    private TextField typeMessageTextField;
    
	private String username;
	
    private BufferedWriter outputStream;
    private BufferedReader inputStream;
    
    private Socket socket;
	
	private double x = 0;
	private double y = 0;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		
		typeMessageTextField.addEventHandler(KeyEvent.KEY_PRESSED, hnd); 
		//scrollpaneChatbox.vvalueProperty().bind(chatbox.heightProperty());
		chatbox.heightProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println(newValue);
            scrollToEnd();
        });
		
		
		//TODO: wrap text (possible solution: create new textflows for every message)
		//TODO: space between messages (hbox padding)

	}
	
	EventHandler<KeyEvent> hnd = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent ke) {
			if (ke.getCode().equals(KeyCode.ENTER)) {
	            sendMessage();
	        }
		}
	};
	
	private void scrollToEnd() {
	    // Set value to scroll to the end
		System.out.println(scrollpaneChatbox);
		scrollpaneChatbox.setVvalue(1.0);
	}
	
	@FXML
	public void on_dragged(MouseEvent event)
	{
		Stage stage = (Stage)chatboxWindow.getScene().getWindow();

		stage.setX(event.getScreenX() - x);
		stage.setY(event.getScreenY() - y);
	}
	
	@FXML
	public void on_clicked(MouseEvent event)
	{
		x = event.getSceneX();
		y = event.getSceneY();
	}
	
	@FXML
	public void on_clicked_close_button()
	{
		// TODO
		// tell server you left
		try {
			outputStream.write("4");
			outputStream.newLine();
			outputStream.flush();
			System.exit(0);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Stage stage = (Stage)chatboxWindow.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void on_clicked_minimize_button()
	{
		Stage stage = (Stage)chatboxWindow.getScene().getWindow();
		stage.setIconified(true);
	}
	
	public void initializeChatbox(String username, String serverIP, int serverPort) throws Exception {
			
		this.username = username;
		connectedAsUsernameLabel.setText(username);
		
		
		connectToServer(serverIP, serverPort);
		
		scrollToEnd();
	}
	
	public void connectToServer(String serverIP, int serverPORT) throws Exception {
		
        socket = new Socket(serverIP, serverPORT);
        outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        
        // TODO
        // check if user name is not already in use
        outputStream.write("1>8^(" + username);
        outputStream.newLine();
		outputStream.flush();
        
		
		
		
		
        Thread messageHandling = new Thread(() -> handleServerMessages());
        messageHandling.setName("SERVER MESSAGE HANDLING");
        messageHandling.start();
	}
	
	
	// TODO
	// extend server message handling
	private void handleServerMessages() {
		
		while (!socket.isClosed()) {
		
			try {
				String serverInput = inputStream.readLine();
				Platform.runLater(() -> {
					String[] message = serverInput.split(Pattern.quote(">8^("), 4);
			        
					switch (Integer.parseInt(message[0])) {
					
					// message from user to everyone
					case 0:
						// time, nick, message
						textFlowAppend("[" + message[1] + "] ", 16, true, Color.WHITE);
			    		textFlowAppend("(" + message[2] + ") ", 11, false, Color.GREY);
			    		textFlowAppend();
			    		textFlowAppend(message[3], 16, false, Color.WHITE);
			    		textFlowAppend();
						break;
					
					// TODO
					// whisper from user
					case 1:
						// time, nick, message
						break;
						
					// message from administrator to everyone
					case 2:
						// time, message
						break;
					
					// user changed their nickname
					case 3:
						// time, nick, new nick
						break;
					
					// server is shutting down
					case 4:
						// time
						textFlowAppend("Server shut down ", 16, false, Color.RED);
			    		textFlowAppend("[" + message[1] + "]", 11, false, Color.GREY);
			    		textFlowAppend();
			    		
			    		try {
			    			socket.close();
			    		}
			    		catch (Exception e) {
			    			// ignore
			    		}
			    		
			    		Platform.exit();
						break;
						
					// new user joined
					case 5:
						// time, nick
						textFlowAppend("(" + message[2] + ") ", 16, true, Color.WHITE);
			    		textFlowAppend("has joined the chat!  ", 16, false, Color.WHITE);
			    		textFlowAppend("[" + message[1] + "]", 11, false, Color.GREY);
			    		textFlowAppend();
						break;
						
					// user has quit
					case 6:
						// time, nick
						textFlowAppend("(" + message[2] + ") ", 16, true, Color.WHITE);
			    		textFlowAppend("has left the chat!  ", 16, false, Color.WHITE);
			    		textFlowAppend("[" + message[1] + "]", 11, false, Color.GREY);
			    		textFlowAppend();
						break;
						
					// error
					case 99:
						// error
						break;
						
					// general message from server
					case 100:
						// general message
						break;
						
					default:
						System.out.println("Unknown opcode [" + message[0] + "]");
						break;
					}
						
					scrollToEnd();
				});
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
	
	
	// reads keyboard input field and sends a message to the server
	@FXML
	public void sendMessage() {
		String keyboardInput = typeMessageTextField.getText();
		
		// is empty?
		if (keyboardInput.isBlank()) {
			return;
		}
		
		
		// leave server
		if (keyboardInput.equals("/quit")) {
			try {
				outputStream.write("4");
				outputStream.newLine();
				outputStream.flush();
				
				socket.close();
				Platform.exit();
			}
			catch (Exception e) {
				// ignore
			}
		}
		// broadcast message
		else {
			
			try {
				outputStream.write("1>8^(" + keyboardInput);
				outputStream.newLine();
				outputStream.flush();
			}
			catch (Exception e) {
				// ignore
			}
		}
		
		typeMessageTextField.setText("");
	}
	
	public void textFlowAppend(String text, double fontSize, boolean bold, Color color)
	{
		Text t = new Text(text);
		t.setFill(color);
		if(bold) {
			t.setFont(Font.font("Verdana", FontWeight.BOLD, fontSize));
		}
		else
		{
			t.setFont(Font.font("Verdana", FontWeight.MEDIUM, fontSize));
		}
		//t.setStyle("-fx-padding: 0 0 30 0");
		chatbox.getChildren().add(t);
	}
	
	public void textFlowAppend()
	{
		Text t = new Text(String.valueOf("\n"));
		chatbox.getChildren().add(t);
	}
}
