package services;

public enum EsJsonBodyParam
{
    NONE(""),
    ACKNOWLEDGED("acknowledged"),
    BOOL("bool"),
    CREATE("create"),
    ID("_id"),
    FROM("from"),
    FUZZINESS("fuzziness"),
    HITS("hits"),
    MATCH("match"),
    MATCH_ALL("match_all"),
    MATCH_BOOL_PREFIX("match_phrase_prefix"),
    MATCH_PHRASE_PREFIX("match_phrase_prefix"),
    MULTI_MATCH("multi_match"),
    QUERY("query"),
    SHOULD("should"),
    SIZE("size"),
    SORT("sort"),
    SOURCE("_source"),
    TYPE("type"),
    UPDATE("update");

    private String param;

    EsJsonBodyParam(String param){
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
