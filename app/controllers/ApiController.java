package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Product;
import play.mvc.Controller;
import play.mvc.Result;
import services.ApiService;

import javax.inject.Inject;
import java.util.List;

import static services.product.ProductRepository.PAGE_SIZE;

public class ApiController extends Controller {

    private final ApiService apiService;

    @Inject
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    public Result showProductsApi(int pageIndex) {
        JsonNode jsonFetched = apiService.getJson().deepCopy();
        if (jsonFetched == null) return notFound(views.html.notFound404.render());
        List<Product> productListFromJson = apiService.deserializeJsonToListAsPage(jsonFetched, pageIndex, PAGE_SIZE);
        int totalNumberOfPages = apiService.findTotalNumberOfPages(apiService.findNumberOfProductsFromJson(jsonFetched));
        return ok(views.html.showProductsApi.render(productListFromJson, pageIndex, totalNumberOfPages));
    }

    public Result getLightProductsApi() {
        JsonNode jsonFetched = apiService.getJson().deepCopy();
        if (jsonFetched == null) return notFound(views.html.notFound404.render());
        for (JsonNode jsonProductNode : jsonFetched) {
            if (jsonProductNode.has("picture")) ((ObjectNode) jsonProductNode).remove("picture");
        }
        return ok(jsonFetched);
    }

    public Result getFullProductsApi() {
        JsonNode json = apiService.getJson().deepCopy();
        if (json == null) return notFound(views.html.notFound404.render());
        return ok(json);
    }

}
