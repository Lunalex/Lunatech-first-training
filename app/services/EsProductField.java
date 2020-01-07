package services;

public enum EsProductField
{
    NONE(""),
    EAN("ean"),
    NAME("name"),
    DESCRIPTION("description");

    private String field;

    EsProductField(String field){
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
