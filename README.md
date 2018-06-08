# gradle-credential-wrapper

Current distribution URL:

https://bintray.com/chesapeaketechnology/maven-public/download_file?file_path=com/chesapeaketechnology/wrapper/gradle-credential-wrapper/4.8.0/gradle-credential-wrapper-4.8.0-bin.zip

## Purpose 
This custom Gradle distribution currently provides two main functions.

1. This custom gradle wrapper applies the [gradle-credentials-plugin][5] via a plugin script so credentials can be used 
in a build's `buildscript{}` closure, among others.

2. This distribution contains a script plugin to read in an encrypted username and password via the 
[gradle-credential-plugin][5] and inject the unencrypted credentials into a Gradle property that may 
be used at practically any point in your project's build. This eliminates the need for your build to explicitly decrypt your 
credentials (but doesn't preclude it from doing so). Primarily, this provides the benefit of being able to reduce boilerplate 
code in your build files. This is done by allowing the use of your credentials in your `settings.gradle` file (and really 
anywhere you need them). In your project's settings file, you can now take advantage of the [pluginManagement][10] 
construct to specify private plugin repositories without having to version control your private login credentials (obviously 
not a great idea).

As is mentioned on the [gradle-credential-plugin site][5] on which this custom distribution builds off of, 
the functionality of this script is not meant to provide a high level of security. It does provide another way to avoid 
storing your credentials in plain text while having those credentials available where they otherwise may not be accessible 
(e.g. settings.gradle).

## Setup of custom distribution
This project assumes basic familiarity with gradle and the gradle wrapper. Read [here][3] for more information on setting 
up the gradle wrapper in your project.

To use the gradle-credential-wrapper in your project, first find the version you want on [jcenter][4]. In your project's 
*gradle/wrapper/gradle-wrapper.properties* file, set the "distributionUrl" to the path of the gradle-credential-wrapper 
from jcenter.

**Example gradle-wrapper.properties file:**
>distributionBase=GRADLE_USER_HOME  
distributionPath=wrapper/dists  
zipStoreBase=GRADLE_USER_HOME  
zipStorePath=wrapper/dists  
distributionUrl=\<jcenter link to version you want\>

After you have the distribution URL set to the gradle-credential-wrapper, running ./gradlew will download the gradle-credential-wrapper 
distribution you specified.
Now you will have access to your credentials in your gradle build script.

### Updating gradle-credential-wrapper once set up
gradle-credential-wrapper contains a convenience task which allows you to update the distribution URL from the command line 
(instead of having to edit the properties file manually).

The task may be run as follows:

`./gradlew updateDistributionUrl -PdistributionUrl=https://bintray.com/chesapeaketechnology/maven-public/download_file?file_path=com/chesapeaketechnology/wrapper/gradle-credential-wrapper/4.6.0/gradle-credential-wrapper-4.6.0-bin.zip`

**_Warning:_** If you insert an invalid URL, you will break the wrapper.
If you break the wrapper via an invalid URL, you will have to set the wrapper *distributionUrl* manually, as described above.
This functionality is planned to be expanded in the future to be more like gradle's `./gradlew wrapper --gradle-version <version>` functionality. 

## Credential injection configuration
After the custom distribution is set up you can configure the credential injection properties, if desired.

Before the plugin script can inject your username and password into the Gradle properties for later use, the script needs to know 
what username and password **keys** are associated with your username and password. You must set these **key** values in 
your project's `gradle.properties` file. These keys must be assigned to two system properties:

* credentialWrapperUsernameKey
* credentialWrapperPasswordKey

Additionally, if you need to override a particular project's default key value, you could set the value in your 
`${gradleUserHomeDir}/gradle.properties` file (e.g. you store your credentials to different key names than the rest of the 
people that work on the project).

As an example, assume that your username key is "myUsername" and your password key is "myPassword". Note that these are **NOT** 
your username and password but the key names where your encrypted username and password are stored via the [gradle-credentials-plugin][5]. 
Here is an example of how it would map:

```
# File: gradle.properties
systemProp.credentialWrapperUsernameKey=myUsername
systemProp.credentialWrapperPasswordKey=myPassword
```

Notice that the properties are prepended with `systemProp.`. That is necessary for Gradle to set the property as a system 
property since the project properties will not yet be available at the time the script runs.

If the properties aren't set, a warning will be logged via Gradle's logger and the credential's won't be injected for later 
use. An error won't be thrown but if your project is expecting the credential's to be injected, they will not exist.

## How to use the injected credentials

We use the fact that the gradle instance is [extension aware][6] to inject the credentials at runtime. The username and 
password will be injected into properties that can in turn be read in the `settings.gradle` file. This provides the login 
information for any point during our build!

The init script will inject the decrypted username and password from the credentials file. The username and password get stored in the following variables:

* gradle.ext.credentialWrapperUsername
* gradle.ext.credentialWrapperPassword

These credentials will now be available to our `settings.gradle` file (among others) and we can use them to add the private 
repositories that contain our plugin(s) (using the pluginManagement block as described [here][10]).

For example:

```
# File: settings.gradle
pluginManagement {
    repositories {
        maven {
            url 'https://<path to my private maven repository>
            credentials {
                username = gradle.ext.credentialWrapperUsername
                password = gradle.ext.credentialWrapperPassword
            }
        }
    }
}
```

From there, we can simplify our gradle scripts by applying our plugins via the [Gradle plugin DSL][7] (as long 
as the plugin's are set up properly [when published][8]):

```
plugins {
    id 'your-private-custom-plugin' version '1.0.0'
}
```

**Note:** _If you need to apply plugins that aren't applied by your custom plugin and aren't set up properly to be 
identified via the plugins closure (see [here][8] for details), it is recommended to create a custom plugin 
to wrap those plugins so they may be applied without having to add a `buildscirpt {}` closure to your build, which increases 
excess code. For example:_

```
plugins {
    id 'your-private-custom-plugin' version '1.0.0'
    id 'custom-plugin-to-wrap-others' version '1.0.0'
}
```

## Custom Credential Passphrase
If you use a custom passphrase with your credentials, you will need to put the passphrase in the gradle.properties file 
located at `${gradleUserHomeDir}/gradle.properties` (create it if it doesn't exist). This is your local gradle.properties 
file, not the gradle.properties file that gets checked in with your project. As an example:

```
# File: gradleUserHomeDir/gradle.properties
systemProp.credentialsPassphrase=mySecretPassPhrase
```

Again, note the prepended `systemProp.`.

Using `-PcredentialsPassphrase=mySecretPassPhrase` on the command line won't work as the project has not been evaluated 
yet. Additionally, attempting to pass the value along as a system property using '-D' does not, surprisingly, make the 
value available to the init script—Gradle does make it available later in the build process.

## Important Gradle Concepts
### Runtime Extensions
The Gradle object is extension aware, we use this fact to inject the username and password into the settings script. See 
[here][9] and [here][6] for more details.

### Gradle Daemon
From testing, it appears that Gradle may use the wrong daemon if you have a custom distribution daemon running and the standard gradle 
distribution of the same version. If that is the case and you run into issues, you can stop all of the daemons for a particular 
version by running `./gradlew --stop`. This should ensure you are using the distribution you expect the next time you run Gradle. 
Additionally you can run the build without the daemon: `./gradlew --no-daemon`

## Troubleshooting
You can see additional logging statements made by the plugin scripts by enabling output of info messages:
`./gradlew --info`

## Versioning
This project attempts to follow standard semantic versioning as much as possible.
That being said, since this project wraps another piece of software that is also versioned, it can get a little complicated.
In an attempt to simplify the versioning and make it as expressive as possible, the artifact versions of this project follow 
the major and minor version numbers of the gradle version it is wrapping 
(e.g. for X.Y.z, the wrapper version follows X and Y but not necessarily z).

Since all versions of this project will be wrapping the same major and minor versions of the gradle wrapper, patch updates 
will incorporate patch versions of the wrapper and possible changes to this project.
For example, if the gradle version you want bundled with the credentials is 4.4.1, you might need to set your distribution 
URL to version 4.4.2 of the gradle-credential-wrapper (due to improvements, for example).

## Notes
This project was partially inspired by the example at 
[Gradle Goodness: Distribute Custom Gradle in Our Company][1] and is built on the work of the [gradle-credentials-plugin][5].

[1]: http://mrhaki.blogspot.com/2012/10/gradle-goodness-distribute-custom.html
[2]: https://services.gradle.org/distributions
[3]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[4]: https://bintray.com/chesapeaketechnology/maven-public/gradle-credential-wrapper
[5]: https://github.com/etiennestuder/gradle-credentials-plugin
[6]: https://docs.gradle.org/current/dsl/org.gradle.api.plugins.ExtensionAware.html
[7]: https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block
[8]: https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
[9]: https://medium.com/tooploox/where-do-gradle-properties-come-from-bf77e43ee572
[10]: https://docs.gradle.org/current/userguide/plugins.html#sec:custom_plugin_repositories