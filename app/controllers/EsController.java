package controllers;

import models.Product;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;
import services.EsService;
import services.ProductRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static play.mvc.Results.ok;

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

    /*-- display page --*/
    public Result showEsPageDefault(Http.Request request){
        return ok(views.html.elasticsearch.render(formFactory.form(String.class), new ArrayList<>(), request));
    }

    // index all for the first time or reindex all (if already indexed)
    /*public Result indexAll() {

    }*/

    // index-create one more product or index-update existing product
    /*public Result indexOne(String ean) {

    }*/

    public Result search(String search, Http.Request request) {
        Form<String> esForm = formFactory.form(String.class).bindFromRequest(request);
        List<Product> esProductsFinal = esService.searchProducts(search);
        return ok(views.html.elasticsearch.render(esForm, esProductsFinal, request));
    }




}
