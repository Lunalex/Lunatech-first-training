package actors;

import play.mvc.Http;
import java.io.File;

public class ActorUpdateProductsProtocol {


    public static class loadProductsCsv {

        private final File csvFile;

        public loadProductsCsv(File csvFile) {
            this.csvFile = csvFile;
        }

        File getCsvFile() {
            return csvFile;
        }
    }

    public static class setupPictureMultipleProducts {

        private final String exactName;
        private final Http.Request request;

        public setupPictureMultipleProducts(String exactName, Http.Request request) {
            this.exactName = exactName;
            this.request = request;
        }

        String getExactName() {
            return exactName;
        }

        Http.Request getRequest() {
            return request;
        }
    }


}
