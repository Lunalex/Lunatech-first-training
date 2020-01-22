package actors;

import actors.ActorUpdateProductsProtocol.*;
import akka.actor.AbstractActor;
import akka.actor.Props;
import models.Product;
import play.libs.Files;
import play.mvc.Http;
import services.product.ProductRepository;

import java.util.*;

import services.product.ProductsService;

public class ActorUpdateProducts extends AbstractActor {

    public static Props props(ProductRepository repo, ProductsService productsService) {
        return Props.create(ActorUpdateProducts.class, () -> new ActorUpdateProducts(repo, productsService));
    }

    private final ProductRepository repo;
    private final ProductsService productsService;

    ActorUpdateProducts(ProductRepository repo, ProductsService productsService) {
        this.repo = repo;
        this.productsService = productsService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        loadProductsCsv.class,
                        loadProducts -> {
                            List<Product> productsLoaded = productsService.getListOfProductsFromCsv(loadProducts.getCsvFile());
                            sender().tell(productsLoaded, self());
                        })
                .match(
                        setupPictureMultipleProducts.class,
                        setupPicture -> {
                            Http.MultipartFormData.FilePart<Files.TemporaryFile> newPicture = productsService.getPictureFromRequest(setupPicture.getRequest());
                            if (newPicture != null) this.setupPictureByProductName(repo.searchByName(setupPicture.getExactName()), newPicture);
                        })
                .build();
    }

    private boolean setupPictureByProductName(List<Product> productsList, Http.MultipartFormData.FilePart<Files.TemporaryFile> picture) {
        if (productsList.isEmpty()) return false;
        for (Product product : productsList) {
            int originalPictureSize = product.getPicture().length;
            productsService.setupPicture(picture, product);
            if (product.getPicture().length == originalPictureSize) return false;
            else repo.save(product);
        }
        return true;
    }
}
