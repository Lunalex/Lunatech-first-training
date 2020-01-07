package services;

public enum EsJsonBodyField
{
    NONE(""),
    ASCENDING("asc"),
    AUTO("auto"),
    DESCENDING("desc"),
    DOC("doc"),
    SCORE("_score");


    private String field;

    EsJsonBodyField(String field){
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
