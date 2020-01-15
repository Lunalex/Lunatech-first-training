package services.custom.exceptions.elasticsearch;

public class EsResponseCannotBeFetchedException extends RuntimeException {

    public EsResponseCannotBeFetchedException(Exception e) {
        super(e.getMessage());
    }
}
