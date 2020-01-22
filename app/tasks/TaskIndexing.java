package tasks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import services.elasticsearch.EsService;

@Singleton
public class TaskIndexing {

    private final EsService esService;

    @Inject
    public TaskIndexing(EsService esService) {
        this.esService = esService;
        System.out.println("TaskIndexing: 'constr'");
        this.initialize();
    }

    public void initialize() {
        System.out.println("TaskIndexing: 'run'");
        esService.indexAll();
    }
}
