package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Product;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.*;

import javax.inject.Inject;
import java.util.*;

import static services.EsField.*;
import static services.EsIndex.PRODUCT;
import static services.EsJsonBodyParam.*;

public class ElasticsearchController extends Controller {

    private final ElasticsearchService es;
    private final JsonService jsonService;
    private final ProductRepository repo;
    private final FormFactory formFactory;

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
        System.out.println("productList");
        System.out.println(productList);
        System.out.println("--------------------------");
        System.out.println("----------------------------------");
        System.out.println("---------------------------------------");
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
        System.out.println("ndJsonBodyBuilder");
        System.out.println(ndJsonBodyBuilder);
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
        List<Product> esProductsFinal = new ArrayList<>();

        // search by ean
        List<Product> esProductsEan = this.findProductsByField(search, EAN);
        if(!esProductsEan.isEmpty())            esProductsFinal.addAll(esProductsEan);
        else                                    esForm = esForm.withError(new ValidationError("ean", "no ean corresponding to your search has been found"));

        // search by name
        List<Product> esProductsName = this.findProductsByField(search, NAME);
        if(!esProductsName.isEmpty())           esProductsFinal.addAll(esProductsName);
        else                                    esForm = esForm.withError(new ValidationError("name", "no name corresponding to your search has been found"));

        // search by description
        List<Product> esProductsDescription = this.findProductsByField(search, DESCRIPTION);
        if(!esProductsDescription.isEmpty())     esProductsFinal.addAll(esProductsDescription);
        else                                    esForm = esForm.withError(new ValidationError("description", "no description corresponding to your search has been found"));

        System.out.println("esProductsFinal");
        System.out.println(esProductsFinal);
        return ok(views.html.elasticsearch.render(esForm, esProductsFinal, request));
    }



    private List<Product> findProductsByField(String search, EsField fieldSearched){
        ObjectNode jsonBody = es.getMapper().createObjectNode();
        ObjectNode childNode1 = es.getMapper().createObjectNode();
        ObjectNode childNode2 = es.getMapper().createObjectNode();

        childNode2.put(fieldSearched.getField(), search);
        childNode1.set(MATCH.getParam(), childNode2);
        jsonBody.set(QUERY.getParam(), childNode1);

        JsonNode jsonResponse = es.makeElasticsearchQuery(HttpMethod.GET, PRODUCT, EsRequestType.SEARCH, new HashMap<>(), jsonBody);
        System.out.println("jsonResponse findBy "+fieldSearched.getField());
        System.out.println(jsonResponse);
        System.out.println("-----------------------");
        return jsonService.deserializeJsonToListAsFull(es.formatJsonEsResponse(jsonResponse));
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
