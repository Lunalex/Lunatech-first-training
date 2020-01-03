package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ElasticsearchService implements WSBodyReadables, WSBodyWritables {

    private final WSClient ws;
    private final ObjectMapper mapper;

    @Inject
    public ElasticsearchService(WSClient ws, ObjectMapper mapper) {
        this.ws = ws;
        this.mapper = mapper;
    }

    public JsonNode getRawJsonFromElasticsearch(){
        ObjectNode childNode = mapper.createObjectNode();
        childNode.set("match_all", mapper.createObjectNode());
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("query", childNode);
        WSRequest request = ws.url("http://localhost:9200/product/_search").setBody(rootNode);
        try{
            CompletionStage<WSResponse> futureResponse = request.get();
            return futureResponse.toCompletableFuture().get().asJson();
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JsonNode getFormattedJsonEs(){
        JsonNode rawJsonEs = this.getRawJsonFromElasticsearch();
        JsonNode jsonEsHits = rawJsonEs.get("hits").get("hits").deepCopy();
        for(JsonNode productNode : jsonEsHits){
            ObjectNode newJson = mapper.createObjectNode();
            newJson.set("ean", productNode.get("_source").get("ean"));
            newJson.set("name", productNode.get("_source").get("name"));
            newJson.set("description", productNode.get("_source").get("description"));
            ((ObjectNode) productNode).removeAll();
            ((ObjectNode) productNode).setAll(newJson);
        }
        System.out.println("new Json = "+jsonEsHits);
        return jsonEsHits;
    }


}
