package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.Product;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.*;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import play.api.Mode;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static services.HttpMethod.*;
import static services.EsIndex.*;
import static services.EsRequestType.*;
import static services.EsRequestQueryParam.*;
import static services.EsJsonBodyParam.*;
import static services.EsJsonBodyField.*;

public class EsService {

    private final RestHighLevelClient esClient;

    @Inject
    public EsService() {
        this.esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
        // TODO: need to investigate when it could be interesting to close the esClient
    }

    public List<Product> searchProducts(String search) {

        // first = try fetching document through ean
        try {
            GetRequest eanRequest = new GetRequest("product", search);
            eanRequest.fetchSourceContext(FetchSourceContext.FETCH_SOURCE);
            GetResponse response = esClient.get(eanRequest, RequestOptions.DEFAULT);
            if(response.isExists()) {
                Product productFetched = Product.createProductFromMap(response.getSourceAsMap());
                List<Product> resultList = new ArrayList<>();
                resultList.add(productFetched);
                return resultList;
            }
        } catch(Exception e) {
            e.printStackTrace();
            // TODO: I never know what's the best practice to deal with Exceptions (how to make this helpful?)
        }

        // if ean failed => search by name & description
        MultiSearchRequest ndMultiSearchRequest = new MultiSearchRequest();
            // name
        SearchRequest nameSearchRequest = new SearchRequest("product");
        SearchSourceBuilder nameSearchSourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder nameMatchQueryBuilder = QueryBuilders.matchQuery("name", search)
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        nameSearchSourceBuilder.query(nameMatchQueryBuilder);
        nameSearchSourceBuilder.size(5);
        nameSearchRequest.source(nameSearchSourceBuilder);
        ndMultiSearchRequest.add(nameSearchRequest);
            // description
        SearchRequest descriptionSearchRequest = new SearchRequest("product");
        SearchSourceBuilder descriptionSearchSourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder descriptionMatchQueryBuilder = QueryBuilders.matchQuery("description", search)
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        descriptionSearchSourceBuilder.query(descriptionMatchQueryBuilder);
        descriptionSearchSourceBuilder.size(3);
        descriptionSearchRequest.source(descriptionSearchSourceBuilder);
        ndMultiSearchRequest.add(descriptionSearchRequest);
            // response
        List<Product> productsFetched = new ArrayList<>();
        try {
            MultiSearchResponse ndResponse = esClient.msearch(ndMultiSearchRequest, RequestOptions.DEFAULT);
                // name
            MultiSearchResponse.Item nameMultiSearchResponse = ndResponse.getResponses()[0];
            SearchResponse nameSearchResponse = nameMultiSearchResponse.getResponse();
            SearchHit[] nameHits = nameSearchResponse.getHits().getHits();
            for(SearchHit hit : nameHits) {
                Product productFetched = Product.createProductFromMap(hit.getSourceAsMap());
                productsFetched.add(productFetched);
            }
                // description
            MultiSearchResponse.Item descriptionMultiSearchResponse = ndResponse.getResponses()[1];
            SearchResponse descriptionSearchResponse = descriptionMultiSearchResponse.getResponse();
            SearchHit[] descriptionHits = nameSearchResponse.getHits().getHits();
            for(SearchHit hit : descriptionHits) {
                Product productFetched = Product.createProductFromMap(hit.getSourceAsMap());
                productsFetched.add(productFetched);
            }
            return productsFetched;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: I never know what's the best practice to deal with Exceptions (how to make this helpful?)
        }
        return new ArrayList<>();
    }



    /*public ElasticsearchJavaRestApi() {
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
