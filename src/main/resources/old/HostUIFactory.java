package old;

public class HostUIFactory {
/*
    public static VBox createProgettoUI(Project project) {
        VBox progettoLayout = new VBox(10);
        Label progettoLabel = new Label(project.getName());
        progettoLabel.setFont(new Font("Arial", 18));
        progettoLayout.getChildren().add(progettoLabel);

        for (Tag tag : project.getTags()) {
            progettoLayout.getChildren().addAll(createTagUI(tag));
        }

        return progettoLayout;
    }

    private static VBox createTagUI(Tag tag) {
        VBox tagLayout = new VBox(5);
        Label tagLabel = new Label(tag.getName());
        tagLabel.setFont(new Font("Arial", 14));
        tagLayout.getChildren().add(tagLabel);

        for (Host host : tag.getHosts()) {
            tagLayout.getChildren().addAll(createHostUI(host));
        }

        return tagLayout;
    }

    private static HBox createHostUI(Host host) {
        HBox hostBox = new HBox(10);
        for (Fqdn fqdn : host.getFqdns()) {
            ToggleButton toggleButton = new ToggleButton(fqdn.isEnabled() ? "ON" : "OFF");
            toggleButton.setStyle(fqdn.isEnabled() ? "-fx-background-color: lightgreen;" : "-fx-background-color: lightcoral;");
            Label fqdnLabel = new Label(host.getIp() + " " + fqdn.getNome());
            hostBox.getChildren().addAll(toggleButton, fqdnLabel);
        }
        return hostBox;
    }


 */
}