package services.elasticsearch.exceptions;

public class BulkRequestFailedException extends RuntimeException {

    public BulkRequestFailedException(Exception e) {
        super(e.getMessage());
    }
}
