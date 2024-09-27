package com.xtremealex.toolkit.hosts;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class IOHostParser {

    private static final Pattern IP_PATTERN = Pattern.compile("^(([0-9]{1,3}\\.){3}[0-9]{1,3})|([a-fA-F0-9:]+)$");
    private static final String CUSTOM_SECTION_START = "##start-xtr-toolkit-host";
    //private static final String CUSTOM_SECTION_END = "##end-xtr-toolkit-host"; implementazione in futuro ...

    public static List<App> parseHostsFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        List<App> apps = new ArrayList<>();
        App currentApp = null;
        String currentLb = null;

        boolean startReading = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Ignora le righe vuote
            if (line.isEmpty()) {
                continue;
            }

            // Riconoscimento delle sezioni, in questo caso inizia la lettura solo dopo aver trovato ##start-xtr-toolkit-host
            if (line.equals(CUSTOM_SECTION_START)) {
                startReading = true;
                continue;
            }

            if (!startReading) {
                continue;
            }

            String keyword = "";

            // Tokenizzazione e parsing dei dati tramite il Pattern Matching on Cases
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
                    // Se non è un IP, consideriamolo come APP
                    keyword = "APP_IMPLICIT";
                }
            } else {
                // IP non commentato
                keyword = "IP";
            }

            // Gestisco i singoli casi in base alla keyword
            switch (keyword) {
                case "LB" -> {
                    // Trovato un Load Balancer, e Memorizzo il LB corrente
                    currentLb = line.substring(4).trim();

                    // Se non esiste ancora un'app corrente, ne creo una nuova
                    if (currentApp == null) {
                        currentApp = new App();
                        currentApp.setName("Indefinito");
                        currentApp.setHostType(HostType.BALANCER);
                        currentApp.setLb(currentLb);
                        apps.add(currentApp);
                    } else {
                        currentApp.setHostType(HostType.BALANCER);
                        // Aggiorno il valore dell'LB per l'app corrente
                        currentApp.setLb(currentLb);
                    }
                }
                case "APP" -> {
                    // Trovata una nuova APP, resettare l'LB in memoria
                    currentApp = new App();
                    currentApp.setName(line.substring(5).trim());
                    // Annullo l'LB corrente, perche questa APP non ha un LB
                    currentLb = null;
                    apps.add(currentApp);
                }
                case "APP_IMPLICIT" -> {
                    // Importo la stringa come nome dell'APP, se non è un IP
                    if (currentApp == null || (currentApp.getName() == null || currentApp.getName().isEmpty())) {
                        currentApp = new App();
                        // Imposto il nome come la stringa trovata
                        currentApp.setName(line.substring(1).trim());
                        currentLb = null;
                        apps.add(currentApp);
                    }
                }
                case "IP_COMMENTED" -> {
                    // IP Commentato (disabilitato)
                    String[] parts = line.split("\\s+");
                    // Rimuovo il `#`
                    String ip = parts[0].substring(1);

                    if (currentApp == null) {
                        currentApp = new App();
                        // Aggiungo l'LB corrente
                        currentApp.setLb(currentLb);
                        apps.add(currentApp);
                    }

                    // Per ogni FQDN si crea un nuovo Host disabilitato
                    for (int i = 1; i < parts.length; i++) {
                        String fqdn = parts[i].replace("#", "");
                        // FQDN disabilitato
                        boolean enabled = false;
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
                        currentApp.setLb(currentLb);
                        apps.add(currentApp);
                    }

                    // Per ogni FQDN, si crea un nuovo Host
                    for (int i = 1; i < parts.length; i++) {
                        String fqdn = parts[i].replace("#", "");
                        boolean enabled = !parts[i].startsWith("#");
                        Host host = new Host(ip, fqdn, enabled);
                        currentApp.getHosts().add(host);
                    }
                }
                default -> {
                    // in caso di ulteriori casi ... in progress
                }
            }
        }
        reader.close();
        return apps;
    }

    /**
     * Genera le linee della sezione personalizzata basate sulla lista di App.
     *
     * @param apps La lista di App da includere nella sezione personalizzata.
     * @return Una lista di stringhe rappresentanti le linee da aggiungere al file hosts.
     */
    private static List<String> generateCustomSection(List<App> apps) {
        List<String> sectionLines = new ArrayList<>();

        for (App app : apps) {
            // add la riga dell'APP
            sectionLines.add("#APP: " + app.getName());

            // add la riga del Load Balancer se presente
            if (app.getLb() != null && !app.getLb().trim().isEmpty()) {
                sectionLines.add("#LB: " + app.getLb());
            }

            // add le righe degli Host
            for (Host host : app.getHosts()) {
                StringBuilder hostLine = new StringBuilder();
                if (!host.isEnabled()) {
                    hostLine.append("#");
                }
                hostLine.append(host.getIp()).append(" ").append(host.getFqdn());
                sectionLines.add(hostLine.toString());
            }

            // add una riga vuota per separare le app
            sectionLines.add("");
        }

        return sectionLines;
    }

    public static void writeHostsFile(String filePath, List<App> apps) throws IOException {
        Path path = Paths.get(filePath);
        Path backupPath = Paths.get(filePath + ".bak");

        //Backup del file originale
        try {
            Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup del file hosts creato in: " + backupPath.toString());
        } catch (IOException e) {
            System.err.println("Errore durante la creazione del backup: " + e.getMessage());
            throw e;
        }

        List<String> allLines;
        try {
            allLines = Files.readAllLines(path);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file hosts: " + e.getMessage());
            throw e;
        }

        // Questo serve per trovare l'indice della sezione personalizzata
        int startIndex = -1;
        for (int i = 0; i < allLines.size(); i++) {
            if (allLines.get(i).trim().equals(CUSTOM_SECTION_START)) {
                startIndex = i;
                break;
            }
        }

        // Rimuovere la vecchia sezione personalizzata se esiste
        if (startIndex != -1) {
            allLines = allLines.subList(0, startIndex);
            System.out.println("Rimosse " + (allLines.size() - startIndex) + " linee dalla sezione personalizzata precedente.");
        }

        // Questo srrve a generare la nuova sezione personalizzata ordinata
        List<String> customSection = generateOrderedCustomSection(apps);
        System.out.println("Generata nuova sezione personalizzata con " + customSection.size() + " linee.");

        // Questo serve ad ggiungere la nuova sezione personalizzata
        if (!allLines.isEmpty() && !allLines.get(allLines.size() - 1).trim().isEmpty()) {
            // Aggiungi una riga vuota prima della sezione personalizzata per ordinare il tutto
            allLines.add("");
        }
        allLines.add(CUSTOM_SECTION_START);
        allLines.addAll(customSection);
        System.out.println("Aggiunta nuova sezione personalizzata al file hosts.");

        // Qui agiorno il file di host
        try {
            Files.write(path, allLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File hosts aggiornato con successo.");
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file hosts: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Genera le linee della sezione personalizzata ordinate basate sulla lista di App.
     *
     * @param apps La lista di App da includere nella sezione personalizzata.
     * @return Una lista di stringhe rappresentanti le linee da aggiungere al file hosts.
     */
    private static List<String> generateOrderedCustomSection(List<App> apps) {
        List<String> sectionLines = new ArrayList<>();

        for (App app : apps) {
            // add la riga dell'APP solo se il nome non è null o vuoto
            if (app.getName() != null && !app.getName().trim().isEmpty()) {
                sectionLines.add("#APP: " + app.getName().trim().replace(":", ""));
            }

            // add la riga del Load Balancer se presente e non vuoto
            if (app.getLb() != null && !app.getLb().trim().isEmpty()) {
                sectionLines.add("#LB: " + app.getLb().trim().replace(":", ""));
            }

            // Raggruppo gli Host per IP per evitare duplicazioni
            Map<String, List<Host>> hostsByIp = new LinkedHashMap<>();
            for (Host host : app.getHosts()) {
                hostsByIp.computeIfAbsent(host.getIp(), k -> new ArrayList<>()).add(host);
            }

            // add gli Host raggruppati per IP
            for (Map.Entry<String, List<Host>> entry : hostsByIp.entrySet()) {
                String ip = entry.getKey();
                List<Host> hosts = entry.getValue();

                for (Host host : hosts) {
                    StringBuilder hostLine = new StringBuilder();
                    if (!host.isEnabled()) {
                        // Commento la riga se disabilitato
                        hostLine.append("#");
                    }
                    hostLine.append(ip).append(" ").append(host.getFqdn());
                    sectionLines.add(hostLine.toString());
                }
            }

            // add una riga vuota per separare le app
            sectionLines.add("");
        }

        return sectionLines;
    }
}
