package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Product;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.*;

import javax.inject.Inject;
import java.util.*;

import static services.EsJsonBodyField.*;
import static services.EsProductField.*;
import static services.EsJsonBodyParam.*;
import static services.EsIndex.PRODUCT;

public class ElasticsearchController extends Controller {

    private final ElasticsearchService es;
    private final JsonService jsonService;
    private final ProductRepository repo;
    private final FormFactory formFactory;
    // TODO: const below not used yet = will be useful for the pagination if implemented
    //private static final int ES_PAGE_SIZE = 5;

    @Inject
    public ElasticsearchController(ElasticsearchService es, JsonService jsonService, ProductRepository repo, FormFactory formFactory) {
        this.es = es;
        this.jsonService = jsonService;
        this.repo = repo;
        this.formFactory = formFactory;
    }

    /*-- index --*/

    public Result indexOneProductByEan(String ean){
        if(ean.length() != 8)                   throw new Error("ean must be 8 characters long");
        if(!repo.findByEan(ean).isPresent())    return notFound("impossible to index "+ean+" - product doesn't exists");

        // get product from Db
        Product productFound = repo.findByEan(ean).get();

        // make json body
        ObjectNode jsonBody = es.getMapper().createObjectNode();
        jsonBody.put(EAN.getField(), productFound.getEan());
        jsonBody.put(NAME.getField(), productFound.getName());
        jsonBody.put(DESCRIPTION.getField(), productFound.getDescription());

        // add url parameters
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put(EsRequestQueryParam.UNIQUE_ID.getParam(), ean);

        // fetch response
        JsonNode jsonResponse = es.makeElasticsearchQuery(HttpMethod.PUT, PRODUCT, EsRequestType.UNIQUE_ID,  requestParameters, jsonBody);
        return redirect(routes.ElasticsearchController.showFullRawJsonElastic());
    }

    public Result indexAllDbProducts(){
        List<Product> productList = repo.getAllProductsAsList();
        StringBuilder ndJsonBodyBuilder = new StringBuilder();

        for(Product product : productList) {
            // ----> create id
            ObjectNode jsonCreate1 = es.getMapper().createObjectNode();
            ObjectNode jsonCreate1Child = es.getMapper().createObjectNode();
            jsonCreate1Child.put(ID.getParam(), product.getEan());
            jsonCreate1.set(CREATE.getParam(), jsonCreate1Child);
            // ----> define product added
            ObjectNode jsonProduct1 = es.getMapper().createObjectNode();
            jsonProduct1.put(EAN.getField(), product.getEan());
            jsonProduct1.put(NAME.getField(), product.getName());
            jsonProduct1.put(DESCRIPTION.getField(), product.getDescription());
            // ----> adding to ndJson
            ndJsonBodyBuilder.append(jsonCreate1).append(System.getProperty("line.separator")).append(jsonProduct1).append(System.getProperty("line.separator"));
        }
        String ndJson = ndJsonBodyBuilder.toString();
        System.out.println("ndJson");
        System.out.println(ndJson);
        System.out.println("--------------------------");
        System.out.println("----------------------------------");
        System.out.println("---------------------------------------");

        JsonNode jsonResponse = es.makeElasticsearchBulkIndexing(PRODUCT, ndJson);
        System.out.println("jsonResponse");
        System.out.println(jsonResponse);

        return ok(jsonResponse);
        //return ok(elasticsearch.formatJsonEsResponse(jsonResponse));
        //return redirect(routes.ElasticsearchController.showAllProductsElasticDefault());
    }

    /*-- search --*/

    public Result searchProducts(String search, Http.Request request) {
        Form<String> esForm = formFactory.form(String.class).bindFromRequest(request);
        List<Product> esProductsFinal = this.getProductsListFromSearch(search);

        System.out.println("esProductsFinal");
        System.out.println(esProductsFinal);
        return ok(views.html.elasticsearch.render(esForm, esProductsFinal, request));
    }

    private List<Product> getProductsListFromSearch(String search){
        ObjectNode jsonBody = es.getMapper().createObjectNode();

        // TODO: for pagination add "size" and "from" parameters (at the same level than QUERY and SORT)

        // sort
        ArrayNode sortNode = es.getMapper().createArrayNode();
        sortNode.add(SCORE.getField());
        jsonBody.set(SORT.getParam(), sortNode);

        // search
        ObjectNode searchNode = this.buildSearchNode(search);
        jsonBody.set(QUERY.getParam(), searchNode);

        JsonNode jsonResponse = es.makeElasticsearchQuery(HttpMethod.GET, PRODUCT, EsRequestType.SEARCH, new HashMap<>(), jsonBody);
        System.out.println("= jsonResponse all 3 queries at the same time =");
        System.out.println(jsonResponse);
        System.out.println("-----------------------");
        return jsonService.deserializeJsonToListAsFull(es.formatJsonEsResponse(jsonResponse));
    }

    private ObjectNode buildSearchNode(String search) {
        ArrayNode shouldNode = es.getMapper().createArrayNode();
        // ean
        ObjectNode eanNode = this.buildFieldNode(search, EAN);
        shouldNode.add(eanNode);
        // name
        ObjectNode nameNode = this.buildFieldNode(search, NAME);
        shouldNode.add(nameNode);
        // description
        ObjectNode descriptionNode = this.buildFieldNode(search, DESCRIPTION);
        shouldNode.add(descriptionNode);
        // searchNode
        ObjectNode searchNode = es.getMapper().createObjectNode();
        ObjectNode boolNode = es.getMapper().createObjectNode();
        boolNode.set(SHOULD.getParam(),shouldNode);
        searchNode.set(BOOL.getParam(), boolNode);
        return searchNode;
    }

    private ObjectNode buildFieldNode(String search, EsProductField productField) {
        // fieldParametersNode
        ObjectNode fieldParametersNode = es.getMapper().createObjectNode();
        fieldParametersNode.put(QUERY.getParam(), search);
        fieldParametersNode.put(FUZZINESS.getParam(), AUTO.getField());
        // productFieldNode
        ObjectNode productFieldNode = es.getMapper().createObjectNode();
        productFieldNode.set(productField.getField(), fieldParametersNode);
        // matchNode
        ObjectNode matchNode = es.getMapper().createObjectNode();
        matchNode.set(MATCH.getParam(), productFieldNode);

        return matchNode;
    }



    /*-- delete --*/
    public Result deleteAllProductsFromEs(){
        JsonNode jsonResponse = es.makeElasticsearchQuery(HttpMethod.DELETE, PRODUCT, EsRequestType.NONE, new HashMap<>(), es.getMapper().createObjectNode());
        return redirect(routes.ElasticsearchController.showFullRawJsonElastic());
    }

    /*-- show --*/

    public Result showElasticsearchPageDefault(Http.Request request){
        return ok(views.html.elasticsearch.render(formFactory.form(String.class), new ArrayList<>(), request));
    }

    public Result showFullRawJsonElastic(){
        return ok(es.getFullRawJsonFromElasticsearch());
    }

    public Result showFullFormattedJsonElastic(){
        JsonNode jsonFormatted = es.formatJsonEsResponse(es.getFullRawJsonFromElasticsearch());
        return ok(jsonFormatted);
    }

}
