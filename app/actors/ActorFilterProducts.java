package actors;

import actors.ActorFilterProductsProtocol.*;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import models.Product;
import services.product.ProductRepository;

import java.util.List;

public class ActorFilterProducts extends AbstractActor {

    public static Props props(ProductRepository repo){
        return Props.create(ActorFilterProducts.class, () -> new ActorFilterProducts(repo));
    }

    private final ProductRepository repo;

    ActorFilterProducts(ProductRepository repo){
        this.repo = repo;
    }


    @Override
    public Receive createReceive() {
        //TODO: find out why it asked to add the NEW below when the ReceiveBuilder of ActorUpdateProducts doesn't need one...
        return new ReceiveBuilder()
                .match(filterProductsByDescription.class,
                        filterProducts -> {
                            List<Product> productsFiltered = repo.searchByDescription(filterProducts.getTermInDescription());
                            sender().tell(productsFiltered, self());
                        })
                .match(
                        filterProductsByNameFromRoute.class,
                        filterProducts -> {
                            List<Product> productsFiltered = repo.searchByName(filterProducts.getExactName());
                            sender().tell(productsFiltered, self());
                        })
                .match(
                        filterProductsByName.class,
                        filterProducts -> repo.searchByName(filterProducts.getExactName())
                )
                .build();
    }

}
