package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static services.EsProductField.*;
import static services.EsJsonBodyParam.*;

public class ElasticsearchService implements WSBodyReadables, WSBodyWritables {

    private final WSClient ws;

    private final ObjectMapper mapper;

    private final static String ELASTICSEARCH_URL = "http://localhost:9200";
    @Inject
    public ElasticsearchService(WSClient ws, ObjectMapper mapper) {
        this.ws = ws;
        this.mapper = mapper;
    }

    public JsonNode makeElasticsearchBulkIndexing(EsIndex esIndex, String ndJson){
        return this.makeElasticsearchQuery(HttpMethod.POST, esIndex, EsRequestType.BULK, new HashMap<>(), ndJson);
    }

    public JsonNode makeElasticsearchQuery(HttpMethod httpMethod, EsIndex esIndex, EsRequestType esRequestType, Map<String, String> requestParameters, Object body) {
        WSRequest esRequest = this.buildEsRequestUrl(esIndex, esRequestType, requestParameters);
        return this.getFullJsonResponseFromElasticsearch(httpMethod, esRequest, body);
    }

    public JsonNode formatJsonEsResponse(JsonNode rawJsonEs){
        System.out.println("=================================");
        System.out.println("RAW JSON ES RESPONSE");
        System.out.println(rawJsonEs);
        System.out.println("=================================");
        if(rawJsonEs.size() > 0){
            JsonNode jsonEsHits = rawJsonEs.get(HITS.getParam()).get(HITS.getParam()).deepCopy();
            for(JsonNode productNode : jsonEsHits){
                ObjectNode newJson = mapper.createObjectNode();
                newJson.set(EAN.getField(), productNode.get(SOURCE.getParam()).get(EAN.getField()));
                newJson.set(NAME.getField(), productNode.get(SOURCE.getParam()).get(NAME.getField()));
                newJson.set(DESCRIPTION.getField(), productNode.get(SOURCE.getParam()).get(DESCRIPTION.getField()));
                ((ObjectNode) productNode).removeAll();
                ((ObjectNode) productNode).setAll(newJson);
            }
            return jsonEsHits;
        }
        return mapper.createObjectNode();
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
        if(esRequestType.equals(EsRequestType.BULK)){
            esRequest.setContentType("application/x-ndjson");
        } else {
            esRequest.setContentType("application/json");
        }
        return esRequest;
    }

    private JsonNode getFullJsonResponseFromElasticsearch(HttpMethod httpMethod, WSRequest esRequest, Object esJsonBody) {
        try{
            CompletionStage<WSResponse> esFutureResponse = this.getEsFutureResponse(httpMethod, esRequest, esJsonBody);
            return esFutureResponse.toCompletableFuture().get().asJson();
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private CompletionStage<WSResponse> getEsFutureResponse(HttpMethod httpMethod, WSRequest esRequest, Object esRequestJsonBody) {
        if(httpMethod == HttpMethod.POST){
            if(esRequest.getContentType().isPresent() && esRequest.getContentType().get().equals("application/x-ndjson")) return esRequest.post((String) esRequestJsonBody);
            return esRequest.post((ObjectNode) esRequestJsonBody);
        }
        if(httpMethod == HttpMethod.PUT){
            return esRequest.put((ObjectNode) esRequestJsonBody);
        }
        if(httpMethod == HttpMethod.DELETE){
            if(esRequest.getContentType().isPresent() && esRequest.getContentType().get().equals("application/x-ndjson")) return esRequest.setBody((String) esRequestJsonBody).delete();
            return esRequest.setBody((ObjectNode) esRequestJsonBody).delete();
        }
        return esRequest.setBody((ObjectNode) esRequestJsonBody).get();
    }

    /*-- getter --*/

    public ObjectMapper getMapper() {
        return mapper;
    }

}
