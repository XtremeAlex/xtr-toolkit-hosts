package com.xtremealex.toolkit.hosts.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class App {

    private String name;
    private String info;
    private HostType hostType = HostType.IP;
    private String lb;
    private List<Host> hosts = new ArrayList<>();
    private boolean autoload;

}
