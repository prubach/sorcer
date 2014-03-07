/*
* A SORCER Provider dependency injection definitions.
* It uses component entry names by sorcer.core.provider.SorcerProvider.
*/
import net.jini.core.entry.Entry
import net.jini.lookup.entry.Comment
import net.jini.lookup.entry.Location
import net.jini.lookup.ui.MainUI
import org.rioproject.config.Component
import sorcer.ui.serviceui.UIComponentFactory
import sorcer.ui.serviceui.UIDescriptorFactory

import static sorcer.core.SorcerConstants.SORCER_VERSION

@Component('sorcer.core.provider.ServiceProvider')
class Account1ProviderConfig {
    /* service provider generic properties */
    String properties = "prv1.properties";
    String name = "Account1";
    String description = "Account Service 1";
    String iconName = "/config/sorcer.png";
    Entry[] entries = [
            new Comment("Teaching example"),
            new Location("3", "310", "Sorcersoft.com Lab"),
            UIDescriptorFactory.getUIDescriptor(MainUI.ROLE, new UIComponentFactory(new URL("artifact:org.sorcersoft.sorcer:account-sui:" + SORCER_VERSION),
                    "sorcer.account.provider.ui.AccountUI")),
            UIDescriptorFactory.getUIDescriptor(MainUI.ROLE, new UIComponentFactory(new URL("artifact:org.sorcersoft.sorcer:account-sui:" + SORCER_VERSION),
                    "sorcer.account.provider.ui.mvc.AccountView"))
    ]
    //Class[] getBeanClasses() {
    //    return [sorcer.account.provider.AccountImpl.class]
    //}
    Class[] getPublishedInterfaces() {
        return [Thread.currentThread().contextClassLoader.loadClass(sorcer.account.provider.Account.class.getName()),
                Thread.currentThread().contextClassLoader.loadClass(sorcer.account.provider.ServiceAccount.class.getName())
        ]
    }
    boolean monitorEnabled = true;
    boolean spaceEnabled = true;
}
