package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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
			outputStream.write(username + " has left the chat!");
			outputStream.newLine();
			outputStream.flush();
		} catch(IOException e) {
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
	
	public void connectToServer(String serverIP, int serverPORT) {
		
		try {
            socket = new Socket(serverIP, serverPORT);
            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            scrollToEnd();
            new Thread(() -> handleServerMessages(socket)).start();

        } catch (Exception e) {
        	Alert a = new Alert(AlertType.ERROR, "Server not started.", ButtonType.OK);
        	a.show();
        	return;
        }
	}
	
	
	public void initializeChatbox(String username, String serverIP, int serverPort) {
		
		this.username = username;
		connectedAsUsernameLabel.setText(username);
		
		connectToServer(serverIP, serverPort);
		
		scrollToEnd();
	}
	
	// TODO
	// extend server message handling
	private void handleServerMessages(Socket socket) {
				
		Platform.runLater(() -> {
			
			String serverInput;
			
			try {
				serverInput = inputStream.readLine();
				
				String[] message = serverInput.split(" ", 2);
                
                if (serverInput.contains(" has joined the chat!"))
                {
                	textFlowAppend(message[0] + " ", 16, true, Color.WHITE);
            		textFlowAppend("has joined the chat!  ", 16, false, Color.WHITE);
            		textFlowAppend("TIME", 11, false, Color.GREY);
            		textFlowAppend();
                }
                else if (serverInput.contains(" has left the chat!"))
                {
                	textFlowAppend(message[0] + " ", 16, true, Color.WHITE);
            		textFlowAppend("has left the chat!  ", 16, false, Color.WHITE);
            		textFlowAppend("TIME", 11, false, Color.GREY);
            		textFlowAppend();
                }
                else if (serverInput.contains("SERVER: closed."))
                {
            		textFlowAppend(serverInput, 16, false, Color.RED);
            		textFlowAppend("TIME", 11, false, Color.GREY);
            		textFlowAppend();
            		return;
                }
                else {
                	textFlowAppend(message[0] + " ", 16, true, Color.WHITE);
            		textFlowAppend("TIME", 11, false, Color.GREY);
            		textFlowAppend();
            		textFlowAppend(message[1], 16, false, Color.WHITE);
            		textFlowAppend();
                }
                
        		scrollToEnd();
        		handleServerMessages(socket);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
		});
    }
	
	// sends messages to the server
	@FXML
	public void sendMessage()
	{
		String message = typeMessageTextField.getText();
		
		if (message.isBlank())
		{
			return;
		}
		
		try {
			outputStream.write(username + " " + message);
			outputStream.newLine();
			outputStream.flush();
		} catch(IOException e) {
			e.printStackTrace();
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
