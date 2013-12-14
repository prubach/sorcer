/*
* A SORCER Provider dependency injection definitions.
* It uses component entry names by sorcer.core.provider.SorcerProvider.
*/
import net.jini.core.entry.Entry
import net.jini.lookup.ui.MainUI
import org.rioproject.config.Component
import sorcer.ui.serviceui.UIComponentFactory
import sorcer.ui.serviceui.UIDescriptorFactory

import static sorcer.core.SorcerConstants.SORCER_VERSION

@Component('sorcer.core.provider.ServiceProvider')
class HelloWorld {
    /* service provider generic properties */
    String name = "HelloWorld";
    String description = "HelloWorld Service";
    Class[] publishedInterfaces = [sorcer.ex0.HelloWorld.class]
    // service beans
    Class[] beanClasses = [sorcer.ex0.HelloWorldImpl.class]
    String iconName = "/config/sorcer.png";

    Entry[] entries = [
            UIDescriptorFactory.getUIDescriptor(MainUI.ROLE, new UIComponentFactory(new URL("artifact:org.sorcersoft.sorcer:ex0-sui:" + SORCER_VERSION), "sorcer.ex0.HelloWorldImplUI"))
    ]

    boolean monitorEnabled = false;
    boolean spaceEnabled = false;
}
