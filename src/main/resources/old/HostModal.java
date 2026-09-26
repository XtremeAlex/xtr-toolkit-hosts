package old;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HostModal {

    public static void show() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Nuovo Host");

        TextField projectName = new TextField();
        projectName.setPromptText("Nome Progetto");

        TextField tag = new TextField();
        tag.setPromptText("TAG");

        TextField ipField = new TextField();
        ipField.setPromptText("IP o Load Balancer");

        TextField fqdnField = new TextField();
        fqdnField.setPromptText("FQDN");

        Button saveButton = new Button("Salva");

        VBox layout = new VBox(10);
        layout.getChildren().addAll(projectName, tag, ipField, fqdnField, saveButton);

        Scene scene = new Scene(layout, 300, 200);
        modalStage.setScene(scene);
        modalStage.show();
    }
}
