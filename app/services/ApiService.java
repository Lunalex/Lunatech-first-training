package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Product;
import play.libs.Json;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static services.product.ProductRepository.PAGE_SIZE;

public class ApiService implements WSBodyReadables, WSBodyWritables {

    private final WSClient ws;
    private final ObjectMapper mapper;
    private JsonNode json;

    @Inject
    public ApiService(WSClient ws, ObjectMapper mapper){
        this.ws = ws;
        this.mapper = mapper;
        this.json = null;
    }

    public JsonNode getJson(){
        this.setJsonFromApi();
        return json;
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

    private void setJsonFromApi() {
        WSRequest request = ws.url("http://localhost:9000/api");
        try {
            CompletionStage<WSResponse> futureResponse = request.get();
            JsonNode fetchedJson = futureResponse.toCompletableFuture().get().asJson();
            this.json = mapper.readTree(fetchedJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
