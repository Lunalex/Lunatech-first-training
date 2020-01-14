package services;

import models.Product;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.*;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.inject.Inject;
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
                               .boost(7)
                )
                .should(
                        QueryBuilders.matchQuery("name", search)
                                .fuzziness(Fuzziness.AUTO)
                                .boost(5)
                )
                .should(
                        QueryBuilders.matchPhrasePrefixQuery("description", search)
                                .slop(1)
                                .boost(3)
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
            throw new ElasticsearchException("an error occured during search response fetching");
            // TODO: can be improved
        }
        return searchResults;
    }

    /*- Indexing -*/
    public boolean isIndexed(String ean) {
        return false;
    }

    public boolean indexExists(String index) {
        return false;
    }

    public void indexProduct(Product product) {

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
