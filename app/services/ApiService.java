package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

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
