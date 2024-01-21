package application;

import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ServerLoginController implements Initializable {
	@FXML
	private AnchorPane serverLoginWindow;
	
	@FXML
	private HBox serverLoginWindowTopBar;

	@FXML
	private TextField serverIPTextField;
	
	@FXML
	private TextField serverPortTextField;
	
	@FXML
	private TextField usernameTextField;
	
	
	public String serverIP;
	
	public int serverPort;
	
	public String username;
	
	private double x = 0;
	private double y = 0;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
	
	@FXML
	public void on_dragged(MouseEvent event)
	{
		Stage stage = (Stage)serverLoginWindow.getScene().getWindow();
//		System.out.println(x);
//		System.out.println(y);
		stage.setX(event.getScreenX() - x);
		stage.setY(event.getScreenY() - y);
	}
	
	@FXML
	public void on_clicked(MouseEvent event)
	{

//		System.out.println(x);
//		System.out.println(y);
		x = event.getSceneX();
		y = event.getSceneY();
	}
	
	@FXML
	public void on_clicked_close_button()
	{
		Stage stage = (Stage)serverLoginWindow.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void on_clicked_minimize_button()
	{
		Stage stage = (Stage)serverLoginWindow.getScene().getWindow();
		stage.setIconified(true);
	}
	
	@FXML
	public void on_clicked_start_button(ActionEvent event) throws IOException
	{
		serverIP = serverIPTextField.getText();
		serverPort = Integer.valueOf(serverPortTextField.getText());
		username = usernameTextField.getText();
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Chatbox.fxml"));
		
		Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

		Parent root = loader.load();
		
		ChatboxController cc = loader.getController();
		cc.initializeChatbox(username, serverIP, serverPort);
		
		Scene scene = new Scene(root);
		scene.setFill(null);


		stage.setScene(scene);
		
		stage.setResizable(false);
		stage.show();
		
	}
	
	public String getUsername()
	{
		return username;
	}
}
