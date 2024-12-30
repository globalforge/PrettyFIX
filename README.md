# PrettyFIX
Webservice that accepts a raw FIX message and displays a pretty version of message with tag names and tag values definitions included in the output.

Build the App
-------------
There are 2 dependencies in the pom.xml file that are not available in the maven repo so you must install them manually in your local maven repository.

1. **lib\fix-2.2.3.jar**
2. **lib\infix-2.2.3.jar**

### Instructions

1. Install apache-maven-3.9.2 or later.
2. Add apache-maven-3.9.2\bin to your %PATH% variable
2. cd to lib\
3. Excute the below two maven commands to add the libraries to your Maven .m2 repository.

mvn install:install-file -Dfile=fix-2.2.3.jar -DgroupId=com.globalforge -DartifactId=fix -Dversion=2.2.3 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=infix-2.2.3.jar -DgroupId=com.globalforge -DartifactId=infix -Dversion=2.2.3 -Dpackaging=jar -DgeneratePom=true
