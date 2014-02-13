# Troubleshooting

###Problem with hostname resolution
To run correctly SORCER needs a properly setup network configuration, in particular, it is very important for the JVM to able to resolve your hostname to a correct IP address - most probably not a loopback interface <tt>(127.x.x.x)</tt>. Sorcer reads the <tt>sorcer.env</tt> file located in <tt>SORCER_HOME/configs</tt> and sets the hostname for the codeserver (webster) according to the <tt>provider.webster.interface</tt> property. By default it is set to <tt>${localhost}</tt>. This value is automatically substituted during the Sorcer booting process by your hostname.
Sometimes, in particular, on Linux hosts the default <tt>/etc/hosts</tt> file maps your hostname to your loopback interface. In that case Sorcer may run into problems finding the codeserver. Therefore please make sure your <tt>/etc/hosts</tt> resolves your hostname to your external IP.</p><p>A similar situation might occur if you invoke a service on the network and the machines involved cannot resolve their partner's hostnames to the correct IP addresses. If you have no DNS or reverse DNS server in your network please set these hostnames in the <tt>/etc/hosts</tt> files of all involved hosts.</p><p>If your machine has multiple IP addresses you may force Sorcer to use a particular one by setting <tt>provider.webster.interface</tt> to the chosen IP address.
In case you want to run offline you can set it to your loopback address, but remember to change it when you are back online since it will prevent your local Sorcer services from communicating with any other services on your network.

###Java GUI in VirtualBox Guest
If you're running a windows guest inside a VirtualBox host you may run into problems executing any Java GUIs. It results from problems with the way Java handles 3D calls. A workaround is to set the following env variable in your system:
<tt>set J2D_D3D=false</tt>
