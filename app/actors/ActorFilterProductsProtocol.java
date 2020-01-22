package actors;

public class ActorFilterProductsProtocol {

    public static class filterProductsByDescription {

        private final String termInDescription;

        public filterProductsByDescription(String termInDescription) {
            this.termInDescription = termInDescription;
        }

        public String getTermInDescription() {
            return termInDescription;
        }
    }

    public static class filterProductsByName {

        private final String exactName;

        public filterProductsByName(String exactName) {
            this.exactName = exactName;
        }

        public String getExactName() {
            return exactName;
        }
    }

    public static class filterProductsByNameFromRoute {

        private final String exactName;

        public filterProductsByNameFromRoute(String exactName) {
            this.exactName = exactName;
        }

        public String getExactName() {
            return exactName;
        }
    }

}
