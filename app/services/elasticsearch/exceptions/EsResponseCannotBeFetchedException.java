package services.elasticsearch.exceptions;

public class EsResponseCannotBeFetchedException extends RuntimeException {

    public EsResponseCannotBeFetchedException(Exception e) {
        super(e.getMessage());
    }
}
