package services;

import models.Product;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
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

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

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
            if(shardInfo.getSuccessful() == 0) System.out.println("FAILURE");
            else                               System.out.println("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("DOC INDEXING: an unexpected error occurred during the request or the response");
        }
    }

    public void reIndexProduct(Product product) {
        try {
            // request
            UpdateRequest updateRequest = new UpdateRequest("product", product.getEan());
            updateRequest.doc(jsonBuilder()
                            .startObject()
                                .field("ean", product.getEan())
                                .field("name", product.getName())
                                .field("description", product.getDescription())
                            .endObject()
            );

            // response
            UpdateResponse updateResponse = esClient.update(updateRequest, RequestOptions.DEFAULT);
            ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();

            System.out.println("updating product in ES "+ product.getEan() +" = ");
            if(shardInfo.getSuccessful() == 0) System.out.println("FAILURE");
            else                               System.out.println("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("DOC UPDATE: an unexpected error occurred during the request or the response");
        }
    }

    public void indexAll(String index) {

    }

    public void reIndexAll(String index) {

    }

    /*- Delete -*/
    public void deleteProduct(String ean) {
        try {
            // request
            DeleteRequest deleteRequest = new DeleteRequest("product", ean);

            // response
            DeleteResponse deleteResponse = esClient.delete(deleteRequest, RequestOptions.DEFAULT);
            ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();

            System.out.println("deleting product in ES "+ ean +" = ");
            if(shardInfo.getSuccessful() == 0) System.out.println("FAILURE");
            else                               System.out.println("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("DOC DELETION: an unexpected error occurred during the request or the response");
        }
    }

    public void deleteAll() {

    }

}
