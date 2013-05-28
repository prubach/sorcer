Getting the sources
=====

...using IntelliJ Idea
----

1. Open IntelliJ Idea
2. If previous project is opened, close it (menu File, Close project)
3. Click Check out from version control and select git

    ![Checkout project](getting/idea/step3.png)

4. Enter "Git repository URL":

        https://github.com/sorcersoft/sorcer.git

    ![Clone repository](getting/idea/step4.png)

    Click Next.

5. Create directory for idea projects or use an existing one.

    Click clone

...using (Windows) console
----

1. Open Menu Start
2. Click Run
3. Enter cmd, click Run
4. In the shell execute

        mkdir IdeaProjects
        cd IdeaProjects
        git clone https://github.com/sorcersoft/sorcer.git

5. Open IntelliJ Idea
6. If previous project is opened, close it (menu File, Close project)
7. Click Import project

    ![Import Project](getting/console/step7.png)

8. In the directory tree select newly created IdeaProjects/sorcer/pom.xml and click OK.

    ![Select pom.xml](getting/console/step8.png)

9. Click Next

    ![Import project from Maven](getting/console/step9.png)

9. Do not select any profiles, clinc Next

    ![Select profiles](getting/console/step10.png)

9. Make sure org.sorcersoft.sorcer:sorcer:1.0-SNAPSHOT is selected

    ![Select modules](getting/console/step11.png)

9. Select Java SDK, or setup one if there is none

    ![Select Java SDK](getting/console/step12.png)

9. (Leave the defaults) Clik Finish

    ![Project name](getting/console/step13.png)
