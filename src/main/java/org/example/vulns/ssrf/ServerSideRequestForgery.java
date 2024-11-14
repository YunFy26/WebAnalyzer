package org.example.vulns.ssrf;

import org.example.vulns.AbstractVulnerabilityData;

public class ServerSideRequestForgery extends AbstractVulnerabilityData {

    @Override
    public String getName() {
        return "SSRF-ServerSideRequestForgery";
    }

}