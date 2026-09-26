package old;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class MainClassTris extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Simula il caricamento dei dati (App con Hosts e FQDN)
        List<App> apps = HostParser.parseHostsFile("/etc/hosts");

        // Layout principale
        VBox mainLayout = new VBox(10);
        mainLayout.setStyle("-fx-padding: 10px;");

        // Scorri le applicazioni (APP)
        for (App app : apps) {
            VBox appBox = new VBox(10);
            Label appLabel = new Label(app.getName());
            appLabel.setFont(new Font("Arial", 20));
            appBox.getChildren().add(appLabel);

            // Scorri gli hosts per ogni applicazione
            for (Host host : app.getHosts()) {
                HBox hostBox = new HBox(10);
                Button statusButton = new Button(host.isEnabled() ? "ON" : "OFF");
                statusButton.setStyle(host.isEnabled()
                        ? "-fx-background-color: lightgreen;"
                        : "-fx-background-color: lightcoral;");
                Label ipLabel = new Label(host.getIp());
                Label fqdnLabel = new Label(host.getFqdn());

                // Configura stili
                ipLabel.setFont(new Font("Arial", 16));
                fqdnLabel.setFont(new Font("Arial", 16));

                // Aggiungi i componenti all'interfaccia
                hostBox.getChildren().addAll(statusButton, ipLabel, fqdnLabel);
                appBox.getChildren().add(hostBox);
            }

            mainLayout.getChildren().add(appBox);
        }

        // Imposta lo ScrollPane per gestire molte App e Host
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);

        // Crea e mostra la scena
        Scene scene = new Scene(scrollPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Host Manager");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
