package services.custom.exceptions.elasticsearch;

import org.elasticsearch.action.bulk.BulkItemResponse;

public class BulkItemResponseFailedException extends RuntimeException {

    public BulkItemResponseFailedException(BulkItemResponse.Failure failure) {
        super(
                "failure ID = " + failure.getId() +
                " | " + "failure Index = " + failure.getIndex() +
                " | " + "failure Cause = " + failure.getCause() +
                " | " + "failure Message = " + failure.getMessage()
        );
    }
}
