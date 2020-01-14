package services;

import models.Product;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EsService {

    private final RestHighLevelClient esClient;

    @Inject
    public EsService() {
        this.esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
        // TODO: need to investigate when it could be interesting to close the esClient
    }

    public List<Product> searchProducts(String search) {
        // request
        SearchRequest searchRequest = new SearchRequest("product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
                .should(
                       QueryBuilders.matchQuery("ean", search)
                               .fuzziness(Fuzziness.ONE)
                               .boost(8)
                )
                .should(
                        QueryBuilders.matchQuery("name", search)
                                .fuzziness(Fuzziness.AUTO)
                                .boost(6)
                )
                .should(
                        QueryBuilders.matchPhrasePrefixQuery("description", search)
                                .slop(0)
                                .boost(4)
                )
                .should(
                        QueryBuilders.matchPhrasePrefixQuery("description", search)
                                .slop(1)
                                .boost(2)
                )
                .should(
                        QueryBuilders.matchQuery("description", search)
                                .operator(Operator.AND)
                                .fuzziness(Fuzziness.AUTO)
                                .boost(1)
                );
        searchSourceBuilder.query(boolQueryBuilder).size(5);
        searchRequest.source(searchSourceBuilder);

        // response
        List<Product> searchResults = new ArrayList<>();
        try {
            SearchResponse searchResponse =  esClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for(SearchHit hit : searchHits) searchResults.add(Product.createProductFromMap(hit.getSourceAsMap()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("an error occurred during search response fetching");
            // TODO: can be improved
        }
        return searchResults;
    }

    /*- Indexing -*/
    public boolean isIndexed(String ean) {
        GetRequest getRequest = new GetRequest("product", ean);
        try {
            return esClient.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("an unexpected error occured during the request");
        }
    }

    public boolean indexExists(String index) {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        try {
            return esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("an unexpected error occurred during the request");
        }
    }

    public void indexProduct(Product product) {
        try {
            // request
            IndexRequest indexRequest = new IndexRequest("product");
            indexRequest.id(product.getEan());
            indexRequest.source(Product.createMapFromProduct(product));

            // response
            IndexResponse indexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT);
            ReplicationResponse .ShardInfo shardInfo = indexResponse.getShardInfo();

            System.out.println("indexing product "+ product.getEan() +" = ");
            if(shardInfo.getSuccessful() == 0) System.out.println("SUCCESS");
            else                               System.out.println("FAILURE");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("an unexpected error occurred during the request or the response");
        }

    }

    public void reIndexProduct(Product product) {

    }

    public void indexAll(String index) {

    }

    public void reIndexAll(String index) {

    }

    /*- Delete -*/
    public void deleteProductIndex() {

    }

}
