package services.custom.exceptions.product;

public class MapSizeNotFitForProductException extends RuntimeException {

    public MapSizeNotFitForProductException() {
        super("an instance of Product needs 3 fields to be created");
    }
}
