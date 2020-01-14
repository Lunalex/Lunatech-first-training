package controllers;

import models.Product;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;
import services.EsService;
import services.ProductRepository;

import javax.inject.Inject;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static play.mvc.Results.*;

public class EsController {

    private final EsService esService;
    private final ProductRepository repo;
    private final FormFactory formFactory;

    @Inject
    public EsController(EsService esService, ProductRepository repo, FormFactory formFactory) {
        this.esService = esService;
        this.repo = repo;
        this.formFactory = formFactory;
    }

    public Result showEsPageDefault(Http.Request request){
        return ok(views.html.elasticsearch.render(formFactory.form(String.class), new ArrayList<>(), request));
    }

    public Result search(String search, Http.Request request) {
        Form<String> esForm = formFactory.form(String.class).bindFromRequest(request);
        List<Product> esProductsFinal = esService.searchProducts(search);
        return ok(views.html.elasticsearch.render(esForm, esProductsFinal, request));
    }

    /*- Indexing -*/
    public Result indexProduct(String ean) {
        Optional<Product> maybeProduct = repo.findByEan(ean);
        if(!maybeProduct.isPresent())   return notFound(views.html.notFound404.render());

        Product product = maybeProduct.get();

        if(!esService.isIndexed(product.getEan()))  esService.indexProduct(product);
        if(esService.isIndexed(product.getEan()))   esService.reIndexProduct(product);
        return redirect(routes.ProductController.showProductsDefault());
    }

    public Result indexAllProducts() {
        if(!esService.indexExists("product"))   esService.indexAll("product");
        if(esService.indexExists("product"))    esService.reIndexAll("product");
        return redirect(routes.ProductController.showProductsDefault());
    }

    /*- Delete -*/
    public Result deleteAllProducts() {
        esService.deleteProductIndex();
        return redirect(routes.EsController.showEsPageDefault());
    }






}
