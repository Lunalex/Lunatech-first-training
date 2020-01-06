package services;

public enum EsRequestType
{
    NONE(""),
    BULK("/_bulk"),
    SEARCH("/_search"),
    UNIQUE_ID("/_doc"),
    MULTIPLE_ID("/_mget");

    private String type;

    EsRequestType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
