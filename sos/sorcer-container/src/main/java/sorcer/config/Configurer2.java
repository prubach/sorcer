package sorcer.config;

import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.*;
import com.google.inject.util.Modules;
import net.jini.config.Configuration;

import java.lang.reflect.Field;


/**
 * @author Rafał Krupiński
 */
public class Configurer2 {
    public void configure(Module module) {
        Modules.combine(module, new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(Matchers.any(), new TypeListener() {
                    @Override
                    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                        Class<? super I> rawType = type.getRawType();
                        String defaultComponent = null;
                        if (rawType.isAnnotationPresent(Component.class))
                            defaultComponent = rawType.getAnnotation(Component.class).value();

                        for (Field field : rawType.getDeclaredFields()) {
                            if (field.isAnnotationPresent(ConfigEntry.class)) {
                                encounter.register(new MembersInjector<I>() {
                                    @Inject
                                    protected Configuration configuration;

                                    @Override
                                    public void injectMembers(I instance) {

                                    }
                                });
                            }
                        }

                    }
                });
            }
        });
    }
}
