package services;

public enum EsIndex
{
    NONE(""),
    PRODUCT("/product");

    private String index;

    EsIndex(String index){
        this.index = index;
    }

    public String getIndex() {
        return index;
    }
}
