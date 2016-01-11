These .cmd files are the windows batch files to create the bundle.jar for release to maven.

The manual process for release to maven is as follows:
1) Change the build version in build.gradle file by changing the variable: 
      shareFileJarVersion to appropriate. Currently its 3.0.9

2) run the writeNewPom task from gradle. 
	This will generate the build\libs\sharefile-api-<version>.pom file

3) run the build task from gradle. This will generate 3 new jars in the 
     build\libs\ folder.  (viz the main jar and sources and javadocs jar)

4) sign all the jars using the citrix pgp key and password. Use the sign.cmd 
     batch file to do this so that it will sign all the above 4 files in one go 
     and generate several .asc files
     
    Commandline :  sign.cmd  <jar_version>. 
    Example: if your jar version was from step 1) was 3.1.2  then run sign.cmd  3.1.2

5) Create a bundle.jar by running: make_bundle.cmd  <jar_version>
   Example:  make_bundle.cmd  3.1.2
   
6) This should generate a bundle.jar file for upload to maven.    
   We use the sona type repo manager to host the jars for staging for maven.
   a) Login to the following link: https://oss.sonatype.org/#staging-upload
   b)  Seletc Upload Mode as Artifact bundle on the web page and upload the above generated bundle.jar
   c) click release
   
7) cleanup the stale files by running the cleanup.cmd if required.
 
   
For more details follow the following links:
http://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html
http://kirang89.github.io/blog/2013/01/20/uploading-your-jar-to-maven-central/
   



