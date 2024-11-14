package org.example.vulns.files;

import org.example.vulns.AbstractVulnerabilityData;


public class LocalFileInclusion extends AbstractVulnerabilityData {

    @Override
    public String getName() {
        return "LFI-LocalFileInclusion";
    }

}