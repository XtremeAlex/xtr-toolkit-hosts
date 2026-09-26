package com.xtremealex.toolkit.hosts;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class HostParser {

    private static final Pattern IP_PATTERN = Pattern.compile("^(([0-9]{1,3}\\.){3}[0-9]{1,3})|([a-fA-F0-9:]+)$");

    public static List<App> parseHostsFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        List<App> apps = new ArrayList<>();
        App currentApp = null;
        String currentLb = null;  // Load Balancer corrente applicabile agli IP fino a nuovo LB o APP

        boolean startReading = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Ignora le righe vuote
            if (line.isEmpty()) {
                continue;
            }

            // Inizia la lettura solo dopo aver trovato ##start-xtr-toolkit-host
            if (line.equals("##start-xtr-toolkit-host")) {
                startReading = true;
                continue;
            }

            if (!startReading) {
                continue;
            }

            String keyword = "";  // Inizializziamo la keyword in base al contenuto della linea

            if (line.startsWith("#")) {
                String tolgoLoSlash = line.substring(1).trim();

                // Se è un IP commentato
                if (IP_PATTERN.matcher(tolgoLoSlash.split("\\s+")[0]).matches()) {
                    keyword = "IP_COMMENTED";
                } else if (tolgoLoSlash.startsWith("LB:")) {
                    keyword = "LB";
                } else if (tolgoLoSlash.startsWith("APP:")) {
                    keyword = "APP";
                } else {
                    // Se non è un IP, consideralo come APP
                    keyword = "APP_IMPLICIT";
                }
            } else {
                // È un IP non commentato
                keyword = "IP";
            }

            // Gestiamo i casi in base alla keyword identificata
            switch (keyword) {
                case "LB" -> {
                    // Trovato un Load Balancer
                    currentLb = line.substring(4).trim();  // Memorizziamo il LB corrente

                    // Se non esiste ancora un'app corrente, ne creiamo una nuova
                    if (currentApp == null) {
                        currentApp = new App();
                        currentApp.setName("Indefinito");
                        currentApp.setHostType(HostType.BALANCER);
                        currentApp.setLb(currentLb);
                        apps.add(currentApp);
                    } else {
                        currentApp.setHostType(HostType.BALANCER);
                        currentApp.setLb(currentLb);  // Aggiorniamo il LB per l'app corrente
                    }
                }
                case "APP" -> {
                    // Trovata una nuova APP, dobbiamo resettare l'LB precedente
                    currentApp = new App();
                    currentApp.setName(line.substring(5).trim());
                    currentLb = null;  // Annulliamo il LB corrente, poiché questa APP non ha un LB
                    apps.add(currentApp);
                }
                case "APP_IMPLICIT" -> {
                    // Trattiamo la stringa come nome dell'APP, se non è un IP
                    if (currentApp == null || (currentApp.getName() == null || currentApp.getName().isEmpty())) {
                        currentApp = new App();
                        currentApp.setName(line.substring(1).trim());  // Imposta il nome come la stringa trovata
                        currentLb = null;  // Resetta il LB se non applicabile
                        apps.add(currentApp);
                    }
                }
                case "IP_COMMENTED" -> {
                    // IP Commentato (disabilitato)
                    String[] parts = line.split("\\s+");
                    String ip = parts[0].substring(1);  // Rimuovi il `#`

                    if (currentApp == null) {
                        currentApp = new App();
                        currentApp.setLb(currentLb);  // Applichiamo il LB corrente
                        apps.add(currentApp);
                    }

                    // Per ogni FQDN, creiamo un nuovo Host disabilitato
                    for (int i = 1; i < parts.length; i++) {
                        String fqdn = parts[i].replace("#", "");  // Rimuovi il `#` solo dal nome del FQDN
                        boolean enabled = false;  // FQDN disabilitato
                        Host host = new Host(ip, fqdn, enabled);
                        currentApp.getHosts().add(host);
                    }
                }
                case "IP" -> {
                    // IP non commentato (abilitato)
                    String[] parts = line.split("\\s+");
                    String ip = parts[0];

                    if (currentApp == null) {
                        currentApp = new App();
                        currentApp.setLb(currentLb);  // Applichiamo il LB corrente
                        apps.add(currentApp);
                    }

                    // Per ogni FQDN, creiamo un nuovo Host
                    for (int i = 1; i < parts.length; i++) {
                        String fqdn = parts[i].replace("#", "");  // Rimuovi il `#` solo dal nome del FQDN
                        boolean enabled = !parts[i].startsWith("#");  // True se non è commentato
                        Host host = new Host(ip, fqdn, enabled);
                        currentApp.getHosts().add(host);
                    }
                }
                default -> {
                    // Gestione di eventuali casi aggiuntivi
                }
            }
        }
        reader.close();
        return apps;
    }
}