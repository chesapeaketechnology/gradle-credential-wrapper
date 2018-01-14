# gradle-credential-wrapper
##Purpose 
Generate a custom gradle wrapper that applies the [gradle-credentials-plugin][5] via an init script so credentials can be used 
in a build's build script closure, among others.

##Usage

###Setup
This project assumes basic familiarity with gradle and the gradle wrapper. Read [here][3] for more information on setting up the gradle wrapper in your project.

To use the gradle-credential-wrapper in your project, first find the version you want on [jcenter][4]. In your project's *gradle/wrapper/gradle-wrapper.properties* file, set the "distributionUrl" to the path of the gradle-credential-wrapper from jcenter.

**Example gradle-wrapper.properties file:**
>distributionBase=GRADLE_USER_HOME  
distributionPath=wrapper/dists  
zipStoreBase=GRADLE_USER_HOME  
zipStorePath=wrapper/dists  
distributionUrl=\<jcenter link to version you want\>

After you have the distribution URL set to the gradle-credential-wrapper, running ./gradlew will download the gradle-credential-wrapper distribution you specified.
Now you will have access to your credentials in your gradle build script.
###Updating gradle-credential-wrapper once set up
gradle-credential-wrapper contains a convenience task which allows you to update the distribution URL from the command line (instead of having to edit the properties file manually).

The task may be run as follows:

`./gradlew updateDistributionUrl -PdistributionUrl=<jcenter link to version you want>`

**_Warning:_** If you insert an invalid URL, you will break the wrapper.
If you break the wrapper via an invalid URL, you will have to set the wrapper *distributionUrl* manually, as described above.
This functionality is planned to be expanded in the future to be more like gradle's `./gradlew wrapper --gradle-version <version>` functionality. 

 

##Versioning
This project attempts to follow standard semantic versioning as much as possible.
That being said, since this project wraps another piece of software that is also versioned, it can get a little complicated.
In an attempt to simplify the versioning and make it as expressive as possible, the artifact versions of this project follow the major and minor version numbers of the gradle version it is wrapping 
(e.g. for X.Y.z, the wrapper version follows X and Y but not necessarily z).

Since all versions of this project will be wrapping the same major and minor versions of the gradle wrapper, patch updates will incorporate patch versions of the wrapper and possible changes to this project.
For example, if the gradle version you want bundled with the credentials is 4.4.1, you might set your distribution URL to version 4.4.5 of the gradle-credential-wrapper.
 

##Notes
This project was partially inspired by the example at 
[Gradle Goodness: Distribute Custom Gradle in Our Company][1].

[1]: http://mrhaki.blogspot.com/2012/10/gradle-goodness-distribute-custom.html
[2]: https://services.gradle.org/distributions
[3]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[4]: https://bintray.com/bintray/jcenter
[5]: https://github.com/etiennestuder/gradle-credentials-plugin