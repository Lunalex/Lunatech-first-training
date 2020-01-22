package modules;

import com.google.inject.AbstractModule;
import tasks.TaskIndexing;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(TaskIndexing.class).asEagerSingleton();
    }

}
