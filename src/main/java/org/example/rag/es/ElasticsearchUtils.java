package org.example.rag.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ElasticsearchUtils {

    private static final Logger logger = LogManager.getLogger(ElasticsearchUtils.class);

    private static final ElasticsearchClient client = ElasticsearchConfig.getClient();

    public static void storeData(MethodData methodData) {
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
            logger.info("Stored data for method ID: {}", response.id());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static String fetchData(String methodSignature) {
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