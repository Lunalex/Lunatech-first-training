package models;

import org.elasticsearch.common.xcontent.XContentBuilder;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import services.custom.exceptions.product.JsonSourceGenerationFromProductFailedException;
import services.custom.exceptions.product.MapSizeNotFitForProductException;
import services.custom.exceptions.product.ProductNotFitForMapOrJsonException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Entity
@Table(name = "product")
@Constraints.Validate
public class Product implements Constraints.Validatable<List<ValidationError>> {

    @Id
    private String ean;
    private String name;
    private String description;
    @Size(max = 883647)
    private byte[] picture;

    @Override
    public List<ValidationError> validate() {
        final List<ValidationError> errors = new ArrayList<>();
        return errors;
    }

    @Override
    public String toString() {
        return String.format("%s (ean = %s)", name, ean);
    }

    public Product() {

    }

    public Product(String ean, String name, String description) {
        this.ean = ean;
        this.name = name;
        this.description = description;
    }

    public static Product createProductFromMap(Map<String, Object> sourceFetchedFromEs){
        if(sourceFetchedFromEs.size() != 3) throw new MapSizeNotFitForProductException();
        return new Product(
                (String) sourceFetchedFromEs.get("ean"),
                (String) sourceFetchedFromEs.get("name"),
                (String) sourceFetchedFromEs.get("description")
                );
    }

    public static Map<String, String> createMapFromProduct(Product p) {
        if(p.getEan().isEmpty() || p.getName().isEmpty()) throw new ProductNotFitForMapOrJsonException();
        Map<String, String> productParameters = new HashMap<>();
        productParameters.put("ean", p.getEan());
        productParameters.put("name", p.getName());
        productParameters.put("description", p.getDescription());
        return productParameters;
    }

    public static XContentBuilder createJsonSourceFromProduct(Product p) {
        if(p.getEan().isEmpty() || p.getName().isEmpty()) throw new ProductNotFitForMapOrJsonException();
        try {
            return jsonBuilder().startObject()
                    .field("ean", p.getEan())
                    .field("name", p.getName())
                    .field("description", p.getDescription())
                    .endObject();
        } catch (IOException e) {
            throw new JsonSourceGenerationFromProductFailedException(e);
        }
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

}


