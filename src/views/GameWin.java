package views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameWin {

	public String CSS = this.getClass().getResource("win scene.css").toExternalForm();

	private Scene win;

	public Scene getWin() {
		return win;
	}

	public GameWin() {

		Label label = new Label("Congratulations! You won the game!");
        label.setAlignment(Pos.CENTER);

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> System.exit(0));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, exitButton);

	}

}
