package services;

public enum EsField
{
    NONE(""),
    EAN("ean"),
    NAME("name"),
    DESCRIPTION("description");

    private String field;

    EsField(String field){
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
