package com.xtremealex.toolkit.hosts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremealex.toolkit.hosts.models.App;

import java.io.IOException;
import java.util.List;

public class MainStart {

    public static void main(String[] args) throws IOException {
        try {
            // File path and parser logic
            List<App> hosts = HostParser.parseHostsFile("/etc/hosts");

            // Use Jackson ObjectMapper to print in JSON format
            ObjectMapper mapper = new ObjectMapper();
            String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hosts);

            // Output the parsed projects
            System.out.println(jsonOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}