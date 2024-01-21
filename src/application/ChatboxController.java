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
	
	EventHandler<KeyEvent> hnd = new EventHandler<KeyEvent>()
	{

		@Override
		public void handle(KeyEvent ke) {
			if (ke.getCode().equals(KeyCode.ENTER)) {
	            sendMessage();
	        }
		}
	};
	
	private void scrollToEnd() {
	    // Set vvalue to scroll to the end
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

            new Thread(() -> handleServerMessages(socket)).start();

        } catch (Exception e) {
        	Alert a = new Alert(AlertType.ERROR, "Server not started.", ButtonType.OK);
        	a.show();
        	return;
            
        }
	}
	
	
	public void initializeChatbox(String username, String serverIP, int serverPort)
	{
		
		LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentTime.format(formatter);
        
		this.username = username;
		connectedAsUsernameLabel.setText(username);
		
		connectToServer(serverIP, serverPort);

		textFlowAppend(username + " ", 16, true, Color.WHITE);
		textFlowAppend("has joined the chat!  ", 16, false, Color.WHITE);
		textFlowAppend(formattedTime, 11, false, Color.GREY);
		textFlowAppend();
		
		try {
			outputStream.write(username + " has joined the chat!");
			outputStream.newLine();
			outputStream.flush();
		} catch(IOException e) {
			System.out.println("Server not started.");
		}
		scrollToEnd();
	}
	
	private void handleServerMessages(Socket socket) {
        try {
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                String message = (String) inputStream.readLine();
                Platform.runLater(() -> appendMessage(message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void appendMessage(String message) {
		LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentTime.format(formatter);
        
        String[] arrOfStr = message.split(" ", 2);
        
        if(message.contains(" has joined the chat!"))
        {
        	textFlowAppend(arrOfStr[0] + " ", 16, true, Color.WHITE);
    		textFlowAppend("has joined the chat!  ", 16, false, Color.WHITE);
    		textFlowAppend(formattedTime, 11, false, Color.GREY);
    		textFlowAppend();
        }
        else if (message.contains(" has left the chat!"))
        {
        	textFlowAppend(arrOfStr[0] + " ", 16, true, Color.WHITE);
    		textFlowAppend("has left the chat!  ", 16, false, Color.WHITE);
    		textFlowAppend(formattedTime, 11, false, Color.GREY);
    		textFlowAppend();
        }
        else if (message.contains("SERVER: closed."))
        {
    		textFlowAppend(message, 16, false, Color.RED);
    		textFlowAppend(formattedTime, 11, false, Color.GREY);
    		textFlowAppend();
        }
        else { 
        	textFlowAppend(arrOfStr[0] + " ", 16, true, Color.WHITE);
    		textFlowAppend(formattedTime, 11, false, Color.GREY);
    		textFlowAppend();
    		textFlowAppend(arrOfStr[1], 16, false, Color.WHITE);
    		textFlowAppend();
        }
        
        
		scrollToEnd();
	}

	@FXML
	public void sendMessage()
	{
		String message = typeMessageTextField.getText();
		
		if(message.isBlank())
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
		
		LocalTime currentTime = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		String formattedTime = currentTime.format(formatter);
        
		textFlowAppend(username + " ", 16, true, Color.WHITE);
		textFlowAppend(formattedTime, 11, false, Color.GREY);
		textFlowAppend();
		textFlowAppend(message, 16, false, Color.WHITE);
		textFlowAppend();
//		
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
