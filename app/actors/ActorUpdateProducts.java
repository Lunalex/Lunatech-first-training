package actors;

import actors.ActorUpdateProductsProtocol.*;
import actors.ActorFilterProductsProtocol.*;
import actors.ActorFilterProducts;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import models.Product;
import play.Logger;
import play.libs.Files;
import play.mvc.Http;
import services.ProductRepository;

import java.util.*;

import services.ProductsService;

public class ActorUpdateProducts extends AbstractActor {

    public static Props props(ProductRepository repo, ProductsService productsService, ActorSystem system) {
        return Props.create(ActorUpdateProducts.class, () -> new ActorUpdateProducts(repo, productsService, system));
    }

    private final ProductRepository repo;
    private final ProductsService productsService;
    private final ActorRef filterProductsActor;

    ActorUpdateProducts(ProductRepository repo, ProductsService productsService, ActorSystem system) {
        this.repo = repo;
        this.productsService = productsService;
        this.filterProductsActor = system.actorOf(ActorFilterProducts.props(repo));
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
                            System.out.println("arrived in first actor");
                            // TODO: try if in a tell we cannot replace the full actorRef by "self" (that would prove this "self" is referring to whomever called the tell)
                            Http.MultipartFormData.FilePart<Files.TemporaryFile> newPicture = productsService.getPictureFromRequest(setupPicture.getRequest());
                            System.out.println("newPicture ? = "+ (newPicture == null ? "nothing":"something"));

                            if (newPicture == null) filterProductsActor.tell(new filterProductsByName(setupPicture.getExactName(), PictureUpdateAnswerMessage.NO_NEW_PICTURE), filterProductsActor);
                            else {
                                System.out.println("in the else of first actor");
                                boolean pictureUpdated = this.setupPictureByProductName(repo.searchByName(setupPicture.getExactName()), newPicture);
                                System.out.println("pictureUpdated ? = "+ (pictureUpdated ? "true":"false"));
                                if(pictureUpdated) filterProductsActor.tell(new filterProductsByName(setupPicture.getExactName(), PictureUpdateAnswerMessage.PICTURE_UPDATED), filterProductsActor);
                                if(!pictureUpdated) filterProductsActor.tell(new filterProductsByName(setupPicture.getExactName(), PictureUpdateAnswerMessage.PICTURE_NOT_UPDATED), filterProductsActor);
                            }
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

    // OLD setupPictureByProductName method
    /*private Map<String, Object> setupPictureByProductName(String exactName, Http.Request request) {
        Map<String, Object> responseObject = new HashMap<>();
        responseObject.put("products", new ArrayList<>());
        responseObject.put("pictureUpdated", false);

        if (exactName.isEmpty()) return responseObject;

        List<Product> searchList = repo.searchByName(exactName);
        searchList.parallelStream().forEach(product -> {
            int originalPictureSize = product.getPicture().length;
            productsService.setupPicture(request, product);
            boolean isPictureUpdated = product.getPicture().length != originalPictureSize;

            if (isPictureUpdated) {
                repo.save(product);
                responseObject.put("pictureUpdated", true);
            }
        });
        responseObject.put("products", searchList);
        return responseObject;
    }*/


    // EXAMPLE of .filter on a stream
    /*public List<Product> getFilteredList(String search) {
        if (search.isEmpty()) {
            return new ArrayList<>();
        }
        List<Product> databaseList = repo.getAllProductsAsList();
        return databaseList.stream()
                .filter(product -> product.getDescription() != null && product.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }*/
}
