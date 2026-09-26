package old;

import com.xtremealex.toolkit.hosts.models.Fqdn;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Project;

import java.util.List;

public class MainClass  {

	//@Override
	//public void start(Stage primaryStage) throws Exception {
	public static void start() throws Exception{
		// Carica i progetti dal file degli host
		List<Project> projects = HostParser.parseHostsFile("/etc/hosts");
		//System.out.println(projects.toArray().toString());
		for (Project project : projects) {
			System.out.println("Progetto: " + project.getName());
			for (App app : project.getApps()) {
				System.out.println("  App: " + app.getApp());
				System.out.println("  Load Balancer: " + app.getLb());
				System.out.println("  IPs: " + app.getIps());
				System.out.println("  FQDNs:");
				for (Fqdn fqdn : app.getFqdns()) {
					System.out.println("    " + fqdn.getNome() + " (" + (fqdn.isEnabled() ? "ON" : "OFF") + ")");
				}
			}
		}
	}
		/*
		// Crea layout
		VBox layout = new VBox(10);
		layout.getChildren().addAll(createTopButtons());



		// Aggiungi progetti alla UI
		for (Project project : progetti) {
			layout.getChildren().addAll(HostUIFactory.createProgettoUI(project));
		}

		// Imposta la scena
		ScrollPane scrollPane = new ScrollPane(layout);
		Scene scene = new Scene(scrollPane, 400, 300);
		primaryStage.setTitle("Host Manager");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private VBox createTopButtons() {
		Button nuovoHostButton = new Button("Nuovo Host");
		Button salvaModificheButton = new Button("Salva Modifiche");
		VBox topButtons = new VBox(10, nuovoHostButton, salvaModificheButton);
		return topButtons;
	}


		 */
	public static void main(String[] args) throws Exception {
		//launch(args);
		start();
	}
}
