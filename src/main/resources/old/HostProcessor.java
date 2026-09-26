package old;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class HostProcessor {

    public static List<String> readHostsFile() throws IOException {
        File hostsFile = new File("/etc/hosts");
        return Files.readAllLines(hostsFile.toPath());
    }

    public static void saveHostsFile(String project, String ip, List<String> fqdnList) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/etc/hosts", true))) {
            writer.write("# " + project + "\n");
            for (String fqdn : fqdnList) {
                writer.write(ip + " " + fqdn + "\n");
            }
        }
    }
}