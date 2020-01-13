package services;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.*;

import java.io.IOException;
import java.util.Map;

import static services.HttpMethod.*;
import static services.EsIndex.*;
import static services.EsRequestType.*;
import static services.EsRequestQueryParam.*;
import static services.EsJsonBodyParam.*;
import static services.EsJsonBodyField.*;

public class EsService {

    /*public ElasticsearchJavaRestApi() {
    }

    public RestClient initializeRestClient() {
        return RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    }


    public JsonNode getProductsFromSearch(String search) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder( new HttpHost("localhost", 9200, "http"))
        );

        //Request esRequest = new Request(GET.getMethod(), PRODUCT.getIndex() + EsRequestType.SEARCH.getType());

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Request buildSyncRequest(HttpMethod httpMethod, Map<String, String> queryParameters) {
        Request esRequest = new Request(httpMethod.getMethod(), "/");
        if (!queryParameters.isEmpty()) esRequest.addParameters(queryParameters);
        return esRequest;
    }

    public JsonNode makeAsyncRequest() {

    }*/
}
