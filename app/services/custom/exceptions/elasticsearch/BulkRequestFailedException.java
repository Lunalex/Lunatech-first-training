package services.custom.exceptions.elasticsearch;

public class BulkRequestFailedException extends RuntimeException {

    public BulkRequestFailedException(Exception e) {
        super(e.getMessage());
    }
}
