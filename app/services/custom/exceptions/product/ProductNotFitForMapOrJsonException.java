package services.custom.exceptions.product;

public class ProductNotFitForMapOrJsonException extends RuntimeException {

    public ProductNotFitForMapOrJsonException() {
        super("parameter 'Product p' must have an Ean and a Name");
    }
}
