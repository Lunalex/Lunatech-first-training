package controllers;

import actors.*;
import actors.HelloActorProtocol.*;

import actors.ActorUpdateProductsProtocol.*;
import actors.ActorFilterProductsProtocol.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import io.ebean.Ebean;
import models.Product;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import scala.compat.java8.FutureConverters;
import services.product.ProductRepository;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;

import play.Logger;
import services.product.ProductsService;

public class AdminController extends Controller {

    private final ProductRepository repo;
    private final ProductsService productsService;
    private final FormFactory formFactory;
    private final ActorRef helloActor;
    private final ActorRef updateProductsActor;
    private final ActorRef filterProductsActor;

    @Inject
    public AdminController(ProductRepository repo, ProductsService productsService, ActorSystem system, FormFactory formFactory) {
        this.repo = repo;
        this.productsService = productsService;
        this.formFactory = formFactory;
        this.helloActor = system.actorOf(HelloActor.getProps());
        this.updateProductsActor = system.actorOf(ActorUpdateProducts.props(repo, productsService));
        this.filterProductsActor = system.actorOf(ActorFilterProducts.props(repo));
    }

    public Result showAdmin(Http.Request request) {
        Form<Product> filterForm = formFactory.form(Product.class);
        return ok(views.html.admin.render(filterForm, request));
    }

    public Result flushAllProducts() {
        Ebean.deleteAll(repo.getAllProductsAsList());
        return redirect(routes.ProductController.showProductsDefault());
    }

    public Result loadProductsFromCsv() {
        File myCsv = new File("public/ikea-names.csv");
        List<Product> productsCsv = productsService.getListOfProductsFromCsv(myCsv);
        for (Product p : productsCsv) {
            repo.save(p);
        }
        return redirect(routes.ProductController.showProductsDefault());
    }

    // --- using ACTORS ---
    public CompletionStage<Result> sayHello (String name) {
        return FutureConverters.toJava(ask(helloActor, new SayHello(name),1000))
                .thenApply(response -> ok((String) response));
    }

    public CompletionStage<Result> loadProductsFromCsvActor() {
        return FutureConverters.toJava(ask(updateProductsActor, new loadProductsCsv(new File("public/ikea-names.csv")), 1000))
                .thenApply(response -> {
                    List<Product> productsCsv = (List<Product>) response;
                    for (Product p : productsCsv) {
                        repo.save(p);
                    }
                    return redirect(routes.ProductController.showProductsDefault());
                });
    }

    public CompletionStage<Result> filterProductsByDescription(String search) {
        return FutureConverters.toJava(ask(filterProductsActor, new filterProductsByDescription(search), 1000))
                .thenApplyAsync( response -> {
                    List<Product> filteredList = (List<Product>) response;
                    return ok(views.html.listProductsFiltered.render(filteredList));
                });
    }

    public CompletionStage<Result> filterProductsByName(String name) {
        return FutureConverters.toJava(ask(filterProductsActor, new filterProductsByNameFromRoute(name), 1000))
                .thenApplyAsync( response -> {
                    List<Product> filteredList = (List<Product>) response;
                    return ok(views.html.listProductsFiltered.render(filteredList));
                });
    }

    public Result filterProductsByNameAndSetupPicture(Http.Request request) {
        Form<Product> filterForm = formFactory.form(Product.class).bindFromRequest(request);
        String name = filterForm.get().getName();
        updateProductsActor.tell(new setupPictureMultipleProducts(name, request), updateProductsActor);
        return redirect(routes.AdminController.filterProductsByName(name));
    }

}
