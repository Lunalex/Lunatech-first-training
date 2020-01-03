package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Product;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.ApiService;
import services.ElasticsearchService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static services.ProductRepository.PAGE_SIZE;

@Singleton
public class ApiController extends Controller {

    private final ApiService apiService;
    private final ElasticsearchService es;

    @Inject
    public ApiController(ApiService apiService, ElasticsearchService es) {
        this.apiService = apiService;
        this.es = es;
    }

    public Result showProductsElastic(int pageIndex) {
        JsonNode jsonFetched = this.es.getFormattedJsonEs().deepCopy();
        return this.showProductsJson(jsonFetched, pageIndex);
    }

    public Result showProductsElasticDefault(){
        return this.showProductsElastic(0);
    }

    public Result showRawJsonElastic(){
        JsonNode jsonFetched = this.es.getRawJsonFromElasticsearch().deepCopy();
        return ok(jsonFetched);
    }

    public Result showProductsElasticJsonFormatted(){
        JsonNode jsonFetched = this.es.getFormattedJsonEs();
        return ok(jsonFetched);
    }

    public Result showProductsApi(int pageIndex) {
        JsonNode jsonFetched = this.apiService.getJson().deepCopy();
        return this.showProductsJson(jsonFetched, pageIndex);
    }

    public Result getLightProductsApi() {
        JsonNode jsonFetched = this.apiService.getJson().deepCopy();
        if (jsonFetched == null) return notFound(views.html.notFound404.render());
        for (JsonNode jsonProductNode : jsonFetched) {
            if (jsonProductNode.has("picture")) ((ObjectNode) jsonProductNode).remove("picture");
        }
        return ok(jsonFetched);
    }

    public Result getFullProductsApi() {
        JsonNode json = this.apiService.getJson().deepCopy();
        if (json == null) return notFound(views.html.notFound404.render());
        return ok(json);
    }

    public Result getLightProductsApiIndirectMethod() {
        JsonNode json = this.apiService.getJson().deepCopy();
        if (json == null) return notFound(views.html.notFound404.render());
        List<Product> lightList = this.deserializeJsonToListAsFull(json);
        JsonNode lightJson = this.serializeArrayToJson(lightList);
        return ok(lightJson);
    }


    /* --- PRIVATE methods --- */

    private List<Product> deserializeJsonToListAsFull(JsonNode json) {
        return this.deserializeJsonToListAsPage(json, 0, json.size());
    }

    private List<Product> deserializeJsonToListAsPage(JsonNode json, int pageIndex, int pageSize) {
        // shorten json to the desired page & convert to Array
        List<Product> lightList = new ArrayList<>();
        int startIndex = pageIndex * pageSize;
        int endIndex = (startIndex + pageSize) - 1;
        for (int i = startIndex; i <= endIndex; i++) {
            if(json.has(i)){
                Product currentProduct = Json.fromJson(json.get(i), Product.class);
                Product lightProduct = new Product(currentProduct.getEan(), currentProduct.getName(), currentProduct.getDescription());
                lightList.add(lightProduct);
            }
        }
        return lightList;
    }

    private JsonNode serializeArrayToJson(List<Product> productList) {
        return Json.toJson(productList);
    }

    private int findNumberOfProductsFromJson(JsonNode json) {
        int numberOfProducts = 0;
        if (json == null) return numberOfProducts;
        for (JsonNode jsonProduct : json) {
            numberOfProducts++;
        }
        return numberOfProducts;
    }

    private int findTotalNumberOfPages(int totalProducts) {
        return totalProducts % PAGE_SIZE > 0 ? (totalProducts / PAGE_SIZE) + 1 : totalProducts / PAGE_SIZE;
    }

    private Result showProductsJson(JsonNode jsonFetched, int pageIndex) {
        if (jsonFetched == null) return notFound(views.html.notFound404.render());
        List<Product> productListFromJson = this.deserializeJsonToListAsPage(jsonFetched, pageIndex, PAGE_SIZE);
        int totalNumberOfPages = this.findTotalNumberOfPages(this.findNumberOfProductsFromJson(jsonFetched));
        return ok(views.html.showProductsApi.render(productListFromJson, pageIndex, totalNumberOfPages));
    }

}
