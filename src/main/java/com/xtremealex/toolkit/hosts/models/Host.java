package com.xtremealex.toolkit.hosts.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Host {

    private String ip;
    private String fqdn;
    private boolean enabled;

}
