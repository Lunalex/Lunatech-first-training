package services;

import models.Product;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductRepositoryTest {

    // use mockito to intialize EbeanConfig parameters for the test

    @Test
    public void checkThatProductFindAllIsNotEmpty() {
        ProductRepository tested = new ProductRepository();
        assertThat(tested.allProducts().isEmpty()).isFalse();
    }
    @Test
    public void checkThatTheSizeOfProductsIsGreaterThan3() {
        ProductRepository tested = new ProductRepository();
        assertThat(tested.allProducts().size()).isGreaterThan(3);
    }
    @Test
    public void checkThatThereIsOneProductWithEANEqualsTo10000() {
        ProductRepository tested = new ProductRepository();
        assertThat(tested.findByEan("12340000")).isPresent();
    }
    @Test
    public void checkThatFindByNameWorks() {
        ProductRepository tested = new ProductRepository();
        assertThat(tested.searchByName("Chaise")).isNotNull();
        assertThat(tested.searchByName("chaise")).hasSize(2);
    }
    @Test
    public void checkThatFindAllReturnsACopy(){
        ProductRepository tested = new ProductRepository();
        List<Product> allProductsOutsideRepository=tested.allProducts();
        // Voyons ce qu'il se passe si nous vidons allProductsOutsideRepository
        allProductsOutsideRepository.clear();
        // AllProducts doit toujours retourner la liste originale
        assertThat(tested.allProducts().isEmpty()).isFalse();
    }
    @Test
    public void checkThatSaveWorks() {
        String ean = "12345678";
        String name= "One unit super test";
        String description = "bla bla bla";
        ProductRepository tested = new ProductRepository();
        Product myProduct = new Product(ean,name,description);
        assertThat(tested.findByEan(ean)).isNotPresent();
        tested.save(myProduct);
        assertThat(tested.findByEan(ean)).isPresent();
    }
    @Test
    public void checkThatRemoveWorks() {
        String ean = "12333456623456";
        String name= "One unit super test";
        String description = "bla bla bla";
        Product myProduct = new Product(ean,name,description);
        ProductRepository tested = new ProductRepository();
        assertThat(tested.findByEan(ean)).isNotPresent();
        tested.save(myProduct);
        assertThat(tested.findByEan(ean)).isPresent();
        tested.delete(myProduct.getEan());
        assertThat(tested.findByEan(ean)).isNotPresent();
    }
    @Test
    public void checkThatSaveDoesNotCreateDuplicate() {
        String ean = "12333456623456";
        String name= "Un objet";
        String description = "si on sauve 2 fois l'objet, il ne doit pas être sauvegardé une deuxième fois";
        Product productOne = new Product(ean,name,description);
        String ean2 = ean;
        String name2= "Un autre objet mais le même EAN";
        String description2 = "si on sauve 2 fois l'objet, il ne doit pas être sauvegardé une deuxième fois";
        Product productTwo = new Product(ean2, name2, description2);
        ProductRepository tested = new ProductRepository();
        assertThat(tested.findByEan(ean)).isNotPresent();
        tested.save(productOne);
        tested.save(productTwo);
        assertThat(tested.findByEan(ean)).isPresent();
        assertThat(tested.findByEan(ean).get().getName().equals(name2));
        assertThat(tested.findByEan(ean).get().getDescription().equals(description2));
    }
};