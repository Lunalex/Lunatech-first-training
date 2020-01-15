package services.custom.exceptions.product;

public class JsonSourceGenerationFromProductFailedException extends RuntimeException {

    public JsonSourceGenerationFromProductFailedException(Exception e) {
        super(e.getMessage());
    }
}
