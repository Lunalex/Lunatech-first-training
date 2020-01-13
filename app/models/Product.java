package models;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.*;

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

    public static Product createProductFromMap(Map<String, Object> sourceFetchedFromEs) throws Exception {
        if(sourceFetchedFromEs.size() != 3) throw new Exception("a Product need 3 fields to be created");
        return new Product(
                (String) sourceFetchedFromEs.get("ean"),
                (String) sourceFetchedFromEs.get("name"),
                (String) sourceFetchedFromEs.get("description")
                );
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


