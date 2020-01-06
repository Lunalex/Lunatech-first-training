package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Product;
import play.mvc.Controller;
import play.mvc.Result;
import services.ApiService;
import services.ElasticsearchService;
import services.JsonService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

public class ApiController extends Controller {

    private final ApiService apiService;
    private final JsonService jsonService;
    private final ElasticsearchService es;

    @Inject
    public ApiController(ApiService apiService, JsonService jsonService, ElasticsearchService es) {
        this.apiService = apiService;
        this.jsonService = jsonService;
        this.es = es;
    }

    public Result showProductsApi(int pageIndex) {
        JsonNode jsonFetched = this.apiService.getJson().deepCopy();
        return this.jsonService.showProductsJson(jsonFetched, pageIndex);
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
        List<Product> lightList = this.jsonService.deserializeJsonToListAsFull(json);
        JsonNode lightJson = this.jsonService.serializeArrayToJson(lightList);
        return ok(lightJson);
    }

}
