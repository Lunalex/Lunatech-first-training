package actors;

import actors.ActorFilterProductsProtocol.*;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import controllers.routes;
import models.Product;
import play.Logger;
import services.ProductRepository;

import java.util.List;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

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
                        filterProducts -> {
                            List<Product> productsFiltered = repo.searchByName(filterProducts.getExactName());

                            // below: the redirect is read without error but it did not redirect anywhere as the Receive seems to only send BACK data via ASK or return VOID
                            // thus I cannot have my page loaded with the correct PictureUpdateAnswerMessage
                            // TODO: conclusion = investigate how to return a RESULT in an actor
                            redirect(routes.AdminController.filterProductsByName(filterProducts.getExactName()));
//                            ok(views.html.listProductsFiltered.render(productsFiltered, filterProducts.getAnswerMessage()));
                        })
                .build();
    }

}
