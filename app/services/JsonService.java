package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.Product;
import play.libs.Json;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;
import static services.ProductRepository.PAGE_SIZE;

public class JsonService {

    public JsonService() {
    }

    public int findNumberOfProductsFromJson(JsonNode json) {
        int numberOfProducts = 0;
        if (json == null) return numberOfProducts;
        for (JsonNode jsonProduct : json) {
            numberOfProducts++;
        }
        return numberOfProducts;
    }

    public int findTotalNumberOfPages(int totalProducts) {
        return totalProducts % PAGE_SIZE > 0 ? (totalProducts / PAGE_SIZE) + 1 : totalProducts / PAGE_SIZE;
    }

    public JsonNode serializeArrayToJson(List<Product> productList) {
        return Json.toJson(productList);
    }

    public List<Product> deserializeJsonToListAsPage(JsonNode json, int pageIndex, int pageSize) {
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

    public List<Product> deserializeJsonToListAsFull(JsonNode json) {
        return this.deserializeJsonToListAsPage(json, 0, json.size());
    }

    public Result showProductsJson(JsonNode jsonFetched, int pageIndex) {
        if (jsonFetched == null) return notFound(views.html.notFound404.render());
        List<Product> productListFromJson = this.deserializeJsonToListAsPage(jsonFetched, pageIndex, PAGE_SIZE);
        int totalNumberOfPages = this.findTotalNumberOfPages(this.findNumberOfProductsFromJson(jsonFetched));
        return ok(views.html.showProductsApi.render(productListFromJson, pageIndex, totalNumberOfPages));
    }

    public Result showJson(JsonNode json){
        return ok(json);
    }

}
