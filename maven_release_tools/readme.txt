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
    
    [Note: 
      you will need the Citrix PGP keys to be imported on you system for this signing to work.
      You can find the citrix_private_key.asc and citrix_public_key.asc files in here. Import them 
      using following commands.   The signing also needs the password. Check the password from NileshP
     	
     	$ gpg --import my_private_key.asc
        $ gpg --import my_public_key.asc
    ]

5) Create a bundle.jar by running: make_bundle.cmd  <jar_version>
   Example:  make_bundle.cmd  3.1.2
   
6) This should generate a bundle.jar file for upload to maven.    
   We use the sona type repo manager to host the jars for staging for maven.
   a) Login to the following link: https://oss.sonatype.org/#staging-upload
   b)  Seletc Upload Mode as Artifact bundle on the web page and upload the above generated bundle.jar
   c) Note the name of the staging artifact. should be something like: comcitrix-<some number>
   d) Click on Staging profiles and search for the above artifact. Select release. 
      The jars should be available for public after few hours.
   
7) cleanup the stale files by running the cleanup.cmd if required.
 
   
For more details follow the following links:
http://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html
http://kirang89.github.io/blog/2013/01/20/uploading-your-jar-to-maven-central/
   



