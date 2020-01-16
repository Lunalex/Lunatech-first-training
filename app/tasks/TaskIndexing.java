package tasks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import services.EsService;

@Singleton
public class TaskIndexing implements Runnable {

    private final EsService esService;

    @Inject
    public TaskIndexing(EsService esService) {
        this.esService = esService;
    }

    @Override
    public void run() {
        esService.indexAll();
    }
}
