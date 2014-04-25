package sorcer.core.service;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.sun.jini.start.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Rafał Krupiński
 */
public class ServiceModule extends AbstractModule {
    private final LifeCycle lc;
    private String[] serviceConfigArgs;

    public ServiceModule(LifeCycle lc, String[] serviceConfigArgs) {
        this.lc = lc;
        this.serviceConfigArgs = serviceConfigArgs;
    }

    @Override
    protected void configure() {
        bind(String[].class).toInstance(serviceConfigArgs);
        bind(LifeCycle.class).toInstance(lc);

        bindListener(HasInitMethod.INSTANCE, new TypeListener() {
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register(InitInvoker.INSTANCE);
            }
        });
    }
}

class HasInitMethod extends AbstractMatcher<TypeLiteral<?>> {
    public boolean matches(TypeLiteral<?> tpe) {
        return check(tpe.getRawType());
    }

    private boolean check(Class<?> type) {
        return InitInvoker.find(type) != null;
    }

    public static final HasInitMethod INSTANCE = new HasInitMethod();
}

class InitInvoker implements InjectionListener {
    private final static Logger log = LoggerFactory.getLogger(InitInvoker.class);

    public void afterInjection(Object injectee) {
        try {
            Method init = find(injectee.getClass());
            if (init == null)
                throw new IllegalArgumentException("No init method found");
            else {
                log.info("Initializing {}", injectee);
                init.invoke(injectee);
            }
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }

    public static Method find(Class<?> type) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class))
                return method;
        }
        Class<?> superclass = type.getSuperclass();

        if (superclass != Object.class)
            return find(superclass);
        else
            return null;
    }

    public static final InitInvoker INSTANCE = new InitInvoker();
}

