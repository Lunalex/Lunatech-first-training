package modules;

import com.google.inject.AbstractModule;
import tasks.TaskIndexing;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        System.out.println("start");
        bind(TaskIndexing.class).asEagerSingleton();
        System.out.println("end");
    }

}
