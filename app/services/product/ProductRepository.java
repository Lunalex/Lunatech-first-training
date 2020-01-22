package services.product;

import io.ebean.PagedList;
import models.Product;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class ProductRepository {

    private final EbeanServer ebeanServer;
    public final static int PAGE_SIZE = 5;
    public final static int BATCH_SIZE = 100;

    @Inject
    public ProductRepository(EbeanConfig ebeanConfig){
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    public List<Product> getAllProductsAsList() {
        return ebeanServer.find(Product.class)
                .order("ean")
                .findList();
    }

    public List<Product> batch(int batchNumber) {
        return ebeanServer.find(Product.class)
                .order("ean")
                .setFirstRow(batchNumber * BATCH_SIZE)
                .setMaxRows(BATCH_SIZE)
                .findList();
    }

    public PagedList<Product> page (int pageIndex) {
        return ebeanServer.find(Product.class)
                .order("ean")
                .setFirstRow(pageIndex * PAGE_SIZE)
                .setMaxRows(PAGE_SIZE)
                .findPagedList();
    }

    public Optional<Product> findByEan(String ean) {
        return ebeanServer.find(Product.class)
                .where()
                .ieq("ean", ean)
                .findOneOrEmpty();
    }

    public List<Product> searchByName(String exactName) {
        return ebeanServer.find(Product.class)
                .where()
                .ieq("name", exactName)
                .findList();
    }

    public List<Product> searchByDescription(String termInDescription) {
        return ebeanServer.find(Product.class)
                .where()
                .icontains("description", termInDescription)
                .findList();
    }

    public void save(Product p) {
        ebeanServer.save(p);
    }

    public void delete(String ean) {
        this.findByEan(ean).ifPresent(ebeanServer::delete);
    }



}
