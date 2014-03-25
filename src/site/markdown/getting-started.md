Getting Started with SORCER
=====

Author: [Paweł Rubach](pawel.rubach@sorcersoft.com)

##Prerequisites
### [Java JDK 1.6 or newer, preferably Oracle Java JDK 1.7](http://java.oracle.com)

SORCER is cross-platform, so any OS supporting Java 1.6 or higher should be ok but the current distribution
was tested on Windows XP/7 32/64bit, Linux (Debian/Ubuntu/Centos) 32/64bit and Mac OS X 10.5.x.
##Installation
###Distribution

Go to our <a href="download.html">download page</a> and pick the most suitable distribution:
<ul>
    <li>if you're running Windows - use the Windows EXE setup file</li>
    <li>on other OSes use the JAR installer. After you've downloaded the file you can try double-clicking it,
        however, depending on your OS configuration this may not invoke the installer. In that case open a
        terminal window and run
        <pre>java -jar sorcer-1.0-setup.jar</pre>
    </li>
    <li>if you prefer a manual install please use the zip file.</li>
</ul>
###Installation on a headless machine
If you only have a console based (headless) access to a machine you can perform the installation using the text-based installer by invoking:

<pre>java -Djava.awt.headless=true -jar sorcer-1.0-setup.jar</pre>

### Setting SORCER_HOME environment variable
After the installation it is recommended to set the SORCER_HOME environment variable to point to the main sorcer folder, for example: /home/user/sorcer.

This step may be omitted on Windows as the Windows installer adds the SORCER_HOME variable automatically, it
is, however, required to logout and log back in after the installation to make sure this change is updated
in your environment. The installer adds SORCER_HOME to your current user's environment. If you would  like
to use SORCER from other accounts please add SORCER_HOME variable to your system environment. You can do that
manually in the
<tt>Control Panel -> System -> Advanced System Settings -> Environment Settings </tt>
On Windows Vista/7/8 you can also run the script below as administrator (start cmd.exe by right clicking
it and selecting "Run as Administrator".
<pre>setx -m SORCER_HOME "C:\sorcer"</pre>
On UNIX there are many ways to do it depending on your OS and configuration. You can add it
<ul>
    <li>system-wide to /etc/environment, or create a /etc/profile.d/sorcer.sh script</li>
    <li>per user - by adding it to .profile or .bash_profile etc.</li>
</ul>
The SORCER_HOME variable is not necessary if you only plan to use the basic SORCER services provided in the
distribution, however, if you'd like to build and run examples or your own service providers you have to add
SORCER_HOME to your environment.

### Files and folders in the Sorcer installation
The Sorcer installation directory (<tt>SORCER_HOME</tt>) contains the following folders:
<ul>
    <li><tt>bin</tt> - startup scripts</li>
    <li><tt>configs</tt> - configuration files
        <ul>
            <li><tt>\ sorcer.env</tt> - the main SORCER environment config file</li>
            <li><tt>\ logback.groovy</tt> - SORCER logging levels config</li>
        </ul>
    </li>
    <li><tt>docs</tt> - documentation and license files</li>
    <li><tt>examples</tt> - examples that demonstrate how to create services in SORCER
    </li>
    <li><tt>lib</tt> - directory with all sorcer and third-party jar files</li>
    <li><tt>logs</tt> - empty directory for logs</li>
    <li><tt>Uninstaller</tt> - not available if you've installed SORCER from a zip file.
        <ul>
            <li><tt>\ Uninstaller.jar</tt> - application that performs an uninstall of SORCER</li>
        </ul>
    </li>
</ul>

### Starting Sorcer
The <tt>$SORCER_HOME/bin</tt> folder contains startup scripts. For basic commands both OS-dependent shell scripts are
provided as well as cross-platform Ant scripts.
        <ul>
            <li>To start the basic SORCER services please run:
            </li>
        </ul>
        <pre>$SORCER_HOME/bin/sorcer-boot
        </pre>
The sorcer-boot script is used to boot all SORCER services. Basic services can be started using different profiles depending on the "-P" parameter.
The same script may be used to boot any SORCER service provider by following the script with the path to the configuration xxx-cfg.jar file
or simply giving a colon and the name of the config module: :xxx-cfg

To see all options of the sorcer-boot command please run:

<pre>$SORCER_HOME/bin/sorcer-boot -h
</pre>

<ul>
    <li>To start the SORCER Service Browser run:</li>
</ul>
<pre>$SORCER_HOME/bin/sorcer-browser
</pre>
<ul>
    <li>To start the SORCER Network Shell please run:</li>
</ul>

<pre>$SORCER_HOME/bin/nsh
</pre>
On UNIX you can use the rlwrap utility to enable the history of executed commands etc. Please install rlwrap and the shell will be started using rlwrap to enable history.
### Further steps

If all basic SORCER services, the SORCER Service Browser and the Network Shell (nsh) run correctly that means
that you have successfully setup your SORCER environment. Congratulations! You are ready to try out the provided
examples. Please read on our [Running provided SORCER examples](examples.html) guide.
