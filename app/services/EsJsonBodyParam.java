package services;

public enum EsJsonBodyParam
{
    NONE(""),
    QUERY("query"),
    MATCH("match"),
    MULTI_MATCH("multi_match"),
    MATCH_ALL("match_all"),
    CREATE("create"),
    UPDATE("update"),
    DOC("doc"),
    ID("_id"),
    TYPE("type"),
    ACKNOWLEDGED("acknowledged"),
    HITS("hits"),
    SOURCE("_source");

    private String param;

    EsJsonBodyParam(String param){
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
