package services;

public enum EsField
{
    NONE(""),
    EAN("ean"),
    NAME("name"),
    DESCRIPTION("description"),
    PROPERTIES("properties"),
    TYPE("type");

    private String field;

    EsField(String field){
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
