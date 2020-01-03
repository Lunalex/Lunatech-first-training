package controllers;

import actors.PictureUpdateAnswerMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Files;
import io.ebean.PagedList;
import models.Product;
import play.data.validation.ValidationError;
import play.libs.Files.TemporaryFile;
import play.Environment;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.ProductRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Singleton
public class ProductController extends Controller {

    private final FormFactory formFactory;
    private final MessagesApi messagesApi;
    private final Environment environment;
    private final ProductRepository repo;

    @Inject
    public ProductController(FormFactory formFactory, MessagesApi messagesApi, Environment environment, ProductRepository repo) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.environment = environment;
        this.repo = repo;
    }

    public Result showProducts(Http.Request request, int pageIndex) {
        PagedList<Product> currentPage = repo.page(pageIndex);
        return ok(views.html.showProducts.render(currentPage, request, messagesApi.preferred(request)));
    }

    public Result showProductsDefault(Http.Request request){
        return this.showProducts(request, 0);
    }

    public Result showProduct(Http.Request request, String ean) {
        Optional<Product> maybeProduct = repo.findByEan(ean);
        if (!maybeProduct.isPresent()) {
            return notFound(views.html.notFound404.render());
        }
        Product myProduct = maybeProduct.get();
        return ok(views.html.showProduct.render(request, messagesApi.preferred(request), myProduct));
    }

    public Result showProductImage(String ean) {
        // map s'utilise sur une Liste d'Optional (ou Optional seul) et va retourner une List du type determiné
        // le else s'exécutera si la liste retournée est vide
        return repo.findByEan(ean)
                .map(product -> (
                        product.getPicture() != null ? ok(product.getPicture()) : notFound(environment.getFile("public/images/product-default.png"))
                ).as("image/png"))
                .orElse(notFound("Error - there is no product with the EAN: " + ean));
    }

    public Result prepareNew(Http.Request request) {
        Form<Product> newForm = formFactory.form(Product.class);
        return ok(views.html.newProduct.render(newForm, request, messagesApi.preferred(request)));
    }

    public Result prepareEdit(Http.Request request, String ean) {
        Optional<Product> maybeProduct = repo.findByEan(ean);
        if (!maybeProduct.isPresent()) {
            return notFound(views.html.notFound404.render());
        }
        Product productToEdit = maybeProduct.get();
        Form<Product> formToEdit = formFactory.form(Product.class).fill(productToEdit);
        return ok(views.html.editProduct.render(formToEdit, productToEdit, request, messagesApi.preferred(request)));
    }

    public Result saveProduct(Http.Request request) {
        Form<Product> productForm = formFactory.form(Product.class).bindFromRequest(request);
        Product newProduct = productForm.get();

        // errors
        productForm = this.checkErrorsNew(request, productForm, newProduct);
        if (productForm.hasErrors()) {
            return badRequest(views.html.newProduct.render(productForm, request, messagesApi.preferred(request)));
        }
        // save
        repo.save(newProduct);
        return redirect(routes.ProductController.showProductsDefault()).flashing("productCreated", newProduct.toString());
    }

    public Result saveEditProduct(Http.Request request, String ean) {
        Form<Product> productForm = formFactory.form(Product.class).bindFromRequest(request);
        Product newProduct = productForm.get();

        // productDb exists?
        Optional<Product> maybeProductDb = repo.findByEan(ean);
        if (!maybeProductDb.isPresent()) {
            return badRequest(views.html.newProduct.render(productForm, request, messagesApi.preferred(request)));
        }
        Product productDb = maybeProductDb.get();

        // errors
        productForm = this.checkErrorsEdit(request, productForm, newProduct);
        if (productForm.hasErrors()) {
            return badRequest(views.html.editProduct.render(productForm, productDb, request, messagesApi.preferred(request)));
        }
        // no picture = productDb
        if (newProduct.getPicture() == null && productDb.getPicture() != null) {
            newProduct.setPicture(productDb.getPicture());
        }
        // delete old & save new
        repo.delete(newProduct.getEan());
        repo.save(newProduct);
        return redirect(routes.ProductController.showProductsDefault()).flashing("productEdited", newProduct.toString());
    }

    public Result deleteProduct(Http.Request request, String ean) {
        Optional<Product> maybeProduct = repo.findByEan(ean);
        if (!maybeProduct.isPresent()) {
            return notFound(views.html.notFound404.render());
        }
        repo.delete(ean);
        return redirect(routes.ProductController.showProductsDefault());
    }

    /* -- ELASTICSEARCH -- */

    /*public Result searchExactName(String name) {

    }*/

    /* -- API-RELATED -- */

    public Result generateProductsApiURL() {
        List<Product> myProducts = repo.getAllProductsAsList();
        JsonNode productsJson = Json.toJson(myProducts);
        return ok(productsJson);
    }

    /* -- PRIVATE -- */

    private void setupPicture(Http.Request request, Product newProduct) {
        Http.MultipartFormData<TemporaryFile> formData = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<TemporaryFile> picture = formData.getFile("pictureFile");
        if (picture != null) {
            newProduct.setPicture(getBytesFromFile(picture));
        }
    }

    private Form<Product> checkErrorsEdit(Http.Request request, Form<Product> productForm, Product newProduct) {
        // name
        String productName = newProduct.getName();
        if (productName.isEmpty()) productForm = productForm.withError(new ValidationError("name", "error.required"));
        if (productName.length() < 3)
            productForm = productForm.withError(new ValidationError("name", "error.name.length"));

        // picture
        this.setupPicture(request, newProduct);
        if (newProduct.getPicture() != null && newProduct.getPicture().length > 880000)
            productForm = productForm.withError(new ValidationError("picture", "error.picture,size"));

        return productForm;
    }

    private Form<Product> checkErrorsNew(Http.Request request, Form<Product> productForm, Product newProduct) {
        // ean
        String productEan = newProduct.getEan();
        if (productEan.isEmpty()) productForm = productForm.withError(new ValidationError("ean", "error.required"));
        if (productEan.length() != 8)
            productForm = productForm.withError(new ValidationError("ean", "error.ean.length"));
        if (productEan.contains("AAA"))
            productForm = productForm.withError(new ValidationError("ean", "error.ean.contains.charSequence"));

        // name
        String productName = newProduct.getName();
        if (productName.isEmpty()) productForm = productForm.withError(new ValidationError("name", "error.required"));
        if (productName.length() < 3)
            productForm = productForm.withError(new ValidationError("name", "error.name.length"));

        // eanAlreadyUsed
        Optional<Product> maybeProductDb = repo.findByEan(productEan);
        if (maybeProductDb.isPresent())
            productForm = productForm.withError(new ValidationError("eanAlreadyUsed", productEan));

        // picture
        this.setupPicture(request, newProduct);
        if (newProduct.getPicture() != null && newProduct.getPicture().length > 880000)
            productForm = productForm.withError(new ValidationError("picture", "error.picture,size"));

        return productForm;
    }

    /* -- STATIC -- */
    public static byte[] getBytesFromFile(Http.MultipartFormData.FilePart<TemporaryFile> picture) {
        TemporaryFile tempFile = picture.getRef();
        File file = tempFile.path().toFile();
        byte[] arrays;
        try {
            arrays = Files.toByteArray(file);
        } catch (IOException e) {
            return null;
        }
        if (arrays.length == 0) {
            return null;
        }
        return arrays;
    }

}
