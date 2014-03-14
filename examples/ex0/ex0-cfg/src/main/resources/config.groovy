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
class HelloWorldProviderConfig {
    /* service provider generic properties */
    String name = "HelloWorld";
    String description = "HelloWorld Service";
    String iconName = "config/sorcer.png";
    Entry[] entries = [
            UIDescriptorFactory.getUIDescriptor(MainUI.ROLE, new UIComponentFactory(new URL("artifact:org.sorcersoft.sorcer:ex0-sui:" + SORCER_VERSION),
                    "sorcer.ex0.HelloWorldImplUI"))
    ]
    Class[] getBeanClasses() {
        return [sorcer.ex0.HelloWorldImpl.class]
    }
    Class[] getPublishedInterfaces() {
        return [Thread.currentThread().contextClassLoader.loadClass(sorcer.ex0.HelloWorld.class.getName())]
    }
    boolean monitorEnabled = false;
    boolean spaceEnabled = false;
}
