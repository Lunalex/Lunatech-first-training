package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.nashorn.internal.parser.JSONParser;
import play.libs.ws.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CompletionStage;

import static services.EsJsonBodyParam.MATCH_ALL;
import static services.EsJsonBodyParam.QUERY;

public class ElasticsearchService implements WSBodyReadables, WSBodyWritables {

    private final WSClient ws;

    private final ObjectMapper mapper;

    private final static String ELASTICSEARCH_URL = "http://localhost:9200";
    @Inject
    public ElasticsearchService(WSClient ws, ObjectMapper mapper) {
        this.ws = ws;
        this.mapper = mapper;
    }

    public JsonNode makeElasticsearchQuery(HttpMethod httpMethod, EsIndex esIndex, EsRequestType esRequestType, Map<String, String> requestParameters, ObjectNode jsonBody) {
        WSRequest esRequest = this.buildEsRequestUrl(esIndex, esRequestType, requestParameters);
        return this.getFullJsonResponseFromElasticsearch(httpMethod, esRequest, jsonBody);
    }

    public JsonNode formatJsonEsResponse(JsonNode rawJsonEs){
        JsonNode jsonEsHits = rawJsonEs.get("hits").get("hits").deepCopy();
        for(JsonNode productNode : jsonEsHits){
            ObjectNode newJson = mapper.createObjectNode();
            newJson.set("ean", productNode.get("_source").get("ean"));
            newJson.set("name", productNode.get("_source").get("name"));
            newJson.set("description", productNode.get("_source").get("description"));
            ((ObjectNode) productNode).removeAll();
            ((ObjectNode) productNode).setAll(newJson);
        }
        return jsonEsHits;
    }

    public JsonNode getFullRawJsonFromElasticsearch(){
        ObjectNode jsonBody = mapper.createObjectNode();
        ObjectNode childNode1 = mapper.createObjectNode();
        childNode1.set(MATCH_ALL.getParam(), mapper.createObjectNode());
        jsonBody.set(QUERY.getParam(), childNode1);
        return this.makeElasticsearchQuery(HttpMethod.GET, EsIndex.PRODUCT, EsRequestType.SEARCH, new HashMap<>(), jsonBody);
    }

    /*-- private --*/
    private WSRequest buildEsRequestUrl(EsIndex esIndex, EsRequestType esRequestType, Map<String, String> queryParameters) {
        WSRequest esRequest = ws.url(ELASTICSEARCH_URL + esIndex.getIndex() + esRequestType.getType());
        if(queryParameters.size() != 0){
            for(Map.Entry<String, String> param : queryParameters.entrySet()){
                if(param.getKey().equals(EsRequestQueryParam.UNIQUE_ID.getParam())){
                    esRequest.setUrl(esRequest.getUrl() + "/" + param.getValue());
                } else {
                    esRequest.addQueryParameter(param.getKey(), param.getValue());
                }
            }
        }
        esRequest.setContentType("application/json");
        return esRequest;
    }

    private JsonNode getFullJsonResponseFromElasticsearch(HttpMethod httpMethod, WSRequest esRequest, ObjectNode esJsonBody) {
        try{
            CompletionStage<WSResponse> esFutureResponse = this.getEsFutureResponse(httpMethod, esRequest, esJsonBody);
            return esFutureResponse.toCompletableFuture().get().asJson();
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private CompletionStage<WSResponse> getEsFutureResponse(HttpMethod httpMethod, WSRequest esRequest, ObjectNode esRequestJsonBody) {
        if(httpMethod == HttpMethod.POST){
            return esRequest.post(esRequestJsonBody);
        }
        if(httpMethod == HttpMethod.PUT){
            return esRequest.put(esRequestJsonBody);
        }
        if(httpMethod == HttpMethod.DELETE){
            return esRequest.setBody(esRequestJsonBody).delete();
        }
        return esRequest.setBody(esRequestJsonBody).get();
    }

    /*-- getter --*/

    public ObjectMapper getMapper() {
        return mapper;
    }

}
