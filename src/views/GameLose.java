package views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameLose {

	public String CSS = this.getClass().getResource("Lose scene.css").toExternalForm();
	private Scene lose;

	public Scene getLose() {
		return lose;
	}

	public GameLose() {
		Label label = new Label("Unfortunately, you lost the game!");
        label.setAlignment(Pos.CENTER);

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> System.exit(0));
        
        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, exitButton);
        
	}

}
