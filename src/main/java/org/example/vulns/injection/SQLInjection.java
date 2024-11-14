package org.example.vulns.injection;

import org.example.vulns.AbstractVulnerabilityData;

public class SQLInjection extends AbstractVulnerabilityData {

    @Override
    public String getName() {
        return "SQLI-SQLInjection";
    }

}