package org.example.rag.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.vulns.VulnerabilityData;

import javax.xml.transform.Source;
import java.io.IOException;
import java.util.*;

public class ElasticsearchUtils {

    private static final Logger logger = LogManager.getLogger(ElasticsearchUtils.class);

    private static final ElasticsearchClient client = ElasticsearchConfig.getClient();

    public static void storeVulnData(VulnerabilityData vulnerabilityData){
        String vulnName = vulnerabilityData.getName();
        Set<String> sinkPoints = vulnerabilityData.getSinkPoints();
        Set<String> vulnSamples = vulnerabilityData.getSamples();
        Set<String> vulnPocSamples = vulnerabilityData.getPocSamples();
        Map<String, Object> document = new HashMap<>();
        document.put("vuln_name", vulnName);
        document.put("sink_points", sinkPoints);
        document.put("vuln_samples", vulnSamples);
        document.put("vuln_poc_samples", vulnPocSamples);
//        System.out.println(document.get("vuln_name"));
//        logger.info(document.get("vuln_name"));

        try {
            IndexRequest<Map<String, Object>> request = new IndexRequest.Builder<Map<String, Object>>()
                    .index("vulnerabilities")
                    .id(vulnName)
                    .document(document)
                    .build();

            IndexResponse response = client.index(request);
//            logger.info("Stored vulnerability data with ID: {}", response.id());
        } catch (IOException e) {
            logger.error("Error storing vulnerability data: {}", e.getMessage());
        }
    }

    public static void fetchSinkPoints(String vulnName) {
        try {

            GetRequest getRequest = new GetRequest.Builder().index("vulnerabilities").id(vulnName).build();
            GetResponse<Map> response = client.get(getRequest, Map.class);

            if (response.found()) {
                Map<String, Object> source = response.source();
                logger.info("vuln_name: {}", source.get("vuln_name"));
                logger.info("sink_points: {}", source.get("sink_points"));
                logger.info("vuln_samples: {}", source.get("vuln_samples"));
                logger.info("vuln_poc_samples: {}", source.get("vuln_poc_samples"));
            } else {
                logger.error("Document with ID {} not found.", vulnName);
            }
        } catch (IOException e) {
            logger.error("Error fetching sink points: {}", e.getMessage());
        }
    }

    public static void storeMethodData(MethodData methodData) {
        String methodSignature = methodData.getMethodSignature();
        String vars = methodData.getVars();
        String stmts = methodData.getStmts();

        Map<String, String> document = new HashMap<>();
        document.put("vars", vars);
        document.put("stmts", stmts);

        try {
            IndexRequest<Map<String, String>> request = new IndexRequest.Builder<Map<String, String>>()
                    .index("method_ir")
                    .id(methodSignature)
                    .document(document)
                    .build();

            IndexResponse response = client.index(request);
//            logger.info("Stored data for method ID: {}", response.id());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static String fetchMethodData(String methodSignature) {
        try {
            GetRequest getRequest = new GetRequest.Builder().index("method_ir").id(methodSignature).build();
            GetResponse<Object> response = client.get(getRequest, Object.class);

            if (response.found()) {
                if (response.source() != null) {
                    return response.source().toString();
                }
                return "response.source() is null!";
            } else {
                return "The IR of %s not found.".formatted(methodSignature);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeClient() {
        try {
            ((RestClientTransport) client._transport()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}