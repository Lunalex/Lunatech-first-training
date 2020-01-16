package services;

import models.Product;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
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
import services.custom.exceptions.elasticsearch.BulkRequestFailedException;
import services.custom.exceptions.elasticsearch.EsResponseCannotBeFetchedException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static services.ProductRepository.BATCH_SIZE;

public class EsService {

    private final RestHighLevelClient esClient;
    private final ProductRepository repo;
    private static final String PRODUCT_INDEX = "product";

    @Inject
    public EsService(ProductRepository repo) {
        this.repo = repo;
        this.esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
        // TODO: need to investigate when it could be interesting to close the esClient
    }

    public List<Product> searchProducts(String search) {
        // request
        SearchRequest searchRequest = new SearchRequest(PRODUCT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
                .should(
                       QueryBuilders.matchQuery("ean", search)
                               .prefixLength(8)
                               .fuzziness(Fuzziness.ZERO)
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

        if(this.indexExists()) {
            try {
                SearchResponse searchResponse =  esClient.search(searchRequest, RequestOptions.DEFAULT);
                SearchHit[] searchHits = searchResponse.getHits().getHits();
                for(SearchHit hit : searchHits) searchResults.add(Product.createProductFromMap(hit.getSourceAsMap()));
            } catch (Exception e) {
                throw new EsResponseCannotBeFetchedException(e);
            }
        }

        return searchResults;
    }

    /*- Indexing -*/
    public void indexProduct(Product product) {
        try {
            // request
            IndexRequest indexRequest = new IndexRequest(PRODUCT_INDEX);
            indexRequest.id(product.getEan());
            indexRequest.source(Product.createMapFromProduct(product));

            // response
            IndexResponse indexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT);
            ReplicationResponse .ShardInfo shardInfo = indexResponse.getShardInfo();

            System.out.println("indexing product in ES "+ product.getEan() +" = ");
            if(shardInfo.getSuccessful() == 0) System.out.println("FAILURE");
            else                               System.out.println("SUCCESS");
        } catch (Exception e) {
            throw new EsResponseCannotBeFetchedException(e);
        }
    }

    public void indexAll() {
        // clean EsShards
        this.deleteAll();
        // reindex all products batch by batch
        int batchNumber = 0;
        boolean keepIndexing = true;
        while(keepIndexing) {
            keepIndexing = this.bulkIndexByBatch(batchNumber);
            batchNumber++;
        }
        System.out.printf("indexation done with %s%n batch", batchNumber);
    }

    /*- Delete -*/
    public void deleteProduct(String ean) {
        if(this.isIndexed(ean)) {
            try {
                // request
                DeleteRequest deleteRequest = new DeleteRequest(PRODUCT_INDEX, ean);

                // response
                DeleteResponse deleteResponse = esClient.delete(deleteRequest, RequestOptions.DEFAULT);
                ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();

                System.out.println("deleting product in ES "+ ean +" = ");
                if(shardInfo.getSuccessful() == 0) System.out.println("FAILURE");
                else                               System.out.println("SUCCESS");

            } catch (IOException e) {
                throw new EsResponseCannotBeFetchedException(e);
            }
        }
    }

    public void deleteAll() {
        if(this.indexExists()) {
            try{
                DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(PRODUCT_INDEX);
                esClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw new EsResponseCannotBeFetchedException(e);
            }
        }
    }

    /*- private -*/
    private boolean bulkIndexByBatch(int batchNumber) {
        List<Product> productList = repo.batch(batchNumber);
        this.bulkIndex(productList);
        return productList.size() == BATCH_SIZE;
    }

    private void bulkIndex(List<Product> productList) {
        BulkRequest bulkRequest = new BulkRequest();
        productList.forEach(product -> bulkRequest.add(new IndexRequest(PRODUCT_INDEX).id(product.getEan()).source(Product.createJsonSourceFromProduct(product))));
        try {
            BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            bulkResponse.forEach(BulkItemResponse::getFailureMessage);
        } catch (IOException e) {
            throw new BulkRequestFailedException(e);
        }
    }

    private boolean isIndexed(String ean) {
        GetRequest getRequest = new GetRequest(PRODUCT_INDEX, ean);
        try {
            return esClient.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsResponseCannotBeFetchedException(e);
        }
    }

    private boolean indexExists() {
        GetIndexRequest getIndexRequest = new GetIndexRequest(PRODUCT_INDEX);
        try {
            return esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsResponseCannotBeFetchedException(e);
        }
    }

}
