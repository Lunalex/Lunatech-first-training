package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Product;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import services.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static services.EsField.*;
import static services.EsJsonBodyParam.*;

public class ElasticsearchController extends Controller {

    private final ElasticsearchService es;
    private final JsonService jsonService;
    private final ProductRepository repo;

    @Inject
    public ElasticsearchController(ElasticsearchService es, JsonService jsonService, ProductRepository repo) {
        this.es = es;
        this.jsonService = jsonService;
        this.repo = repo;
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
        JsonNode jsonResponse = es.makeElasticsearchQuery(HttpMethod.PUT, EsIndex.PRODUCT, EsRequestType.UNIQUE_ID,  requestParameters, jsonBody);
        return redirect(routes.ElasticsearchController.showAllProductsElasticDefault());
    }

    /*public Result indexAllDbProducts(){

    }*/

    /*-- find --*/

    public Result findProductsByName(String name){
        ObjectNode jsonBody = es.getMapper().createObjectNode();
        ObjectNode childNode1 = es.getMapper().createObjectNode();
        ObjectNode childNode2 = es.getMapper().createObjectNode();

        childNode2.put(NAME.getField(), name);
        childNode1.set(MATCH.getParam(), childNode2);
        jsonBody.set(QUERY.getParam(), childNode1);

        JsonNode jsonResponse = es.makeElasticsearchQuery(HttpMethod.GET, EsIndex.PRODUCT, EsRequestType.SEARCH, new HashMap<>(), jsonBody);
        return jsonService.showProductsJson(es.formatJsonEsResponse(jsonResponse), 0);
    }

    /*-- show all --*/

    public Result showAllProductsElastic(int pageIndex) {
        ObjectNode jsonBody = es.getMapper().createObjectNode();
        ObjectNode childNode1 = es.getMapper().createObjectNode();
        childNode1.set(MATCH_ALL.getParam(), es.getMapper().createObjectNode());
        jsonBody.set(QUERY.getParam(), childNode1);
        JsonNode jsonAllProductsEs = es.makeElasticsearchQuery(HttpMethod.GET, EsIndex.PRODUCT, EsRequestType.SEARCH, new HashMap<>(), jsonBody);
        return jsonService.showProductsJson(es.formatJsonEsResponse(jsonAllProductsEs), pageIndex);
    }

    public Result showAllProductsElasticDefault(){
        return this.showAllProductsElastic(0);
    }

    public Result showFullRawJsonElastic(){
        return ok(es.getFullRawJsonFromElasticsearch());
    }

    public Result showFullFormattedJsonElastic(){
        JsonNode jsonFormatted = es.formatJsonEsResponse(es.getFullRawJsonFromElasticsearch());
        return ok(jsonFormatted);
    }

}
