package services;

public enum EsRequestQueryParam
{
    INCLUDING_FIELDS_FROM_SOURCE("_source_includes"),
    EXCLUDING_FIELDS_FROM_SOURCE("_source_excludes"),
    UNIQUE_ID("unique_id");

    private String param;

    EsRequestQueryParam(String param){
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
