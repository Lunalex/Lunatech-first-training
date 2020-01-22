package services.product;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import models.Product;
import org.apache.commons.lang3.RandomStringUtils;
import play.libs.Files;
import java.io.File;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;

import static controllers.ProductController.getBytesFromFile;

public class ProductsService {

    public ProductsService(){

    }

    public Http.MultipartFormData.FilePart<Files.TemporaryFile> getPictureFromRequest(Http.Request request) {
        Http.MultipartFormData<Files.TemporaryFile> formData = request.body().asMultipartFormData();
        return formData.getFile("pictureFile");
    }

    public void setupPicture(Http.MultipartFormData.FilePart<Files.TemporaryFile> picture, Product newProduct) {
        if (picture != null) {
            byte[] pictureInBytes = getBytesFromFile(picture);
            if(pictureInBytes != null && pictureInBytes.length < 880000 ) newProduct.setPicture(pictureInBytes);
        }
    }

    public List<Product> getListOfProductsFromCsv(File csvFile) {
        if (!csvFile.exists()) {
            System.out.println("Le fichier " + csvFile.getAbsolutePath() + " n'existe pas");
            return new ArrayList<>();
        }
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        CsvParser parser = new CsvParser(settings);
        // call beginParsing to read records one by one, iterator-style.
        parser.beginParsing(csvFile);
        String[] row;
        List<Product> products = new ArrayList<>();
        while ((row = parser.parseNext()) != null) {
            final String ean = RandomStringUtils.randomAlphanumeric(8);
            final String name = row[0];
            final String description = row[1];
            final Product p = new Product(ean, name, description);
            products.add(p);
        }
        parser.stopParsing();
        return products;
    }







}
