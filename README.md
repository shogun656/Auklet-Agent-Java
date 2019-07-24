<p align="center"><a href="https://auklet.io"><img src="https://s3.amazonaws.com/auklet/static/github_readme_java.png" alt="Auklet - Problem Solving Software for Java"></a></p>

# Auklet for Java
[![Maintainability](https://api.codeclimate.com/v1/badges/e1ced62442c7cf49d58a/maintainability)](https://codeclimate.com/github/aukletio/Auklet-Agent-Java/maintainability)

This is the Java agent for Auklet, officially supporting Java 7+ and Android Jelly Bean or higher (4.1.x+, API level 16+).

# Features

* Automatic report of unhandled exceptions.
* Location and system metrics identification for all issues.


# Quickstart
The Auklet Java agent is deployed to [JCenter](https://bintray.com/bintray/jcenter).

## Maven
```
<repository>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
  <id>central</id>
  <name>bintray</name>
  <url>http://jcenter.bintray.com</url>
</repository>

<dependency>
  <groupId>io.auklet</groupId>
  <artifactId>auklet-agent-java</artifactId>
  <version>...</version>
</dependency>
```

## Gradle (Java)
```
repositories {
    // other repositories
    jcenter()
}

implementation 'io.auklet:auklet-agent-java:<version>'
```

## Gradle (Android)
In the project's `build.gradle` file.
```
allprojects {
    repositories {
        // other repositories
        jcenter()
    }
}
```

In the module's `build.gradle` file.
```
implementation 'io.auklet:auklet-agent-java:<version>'
```

## Configuration/Authorization
To authorize your application you need to provide both an API key and app ID. These values are available in the connection settings of your application as well as during initial setup.

Unless you use the `Config` object to manually set your Auklet credentials, you will need to set one of the following on the JVM where you are running Auklet:
* The environment variables `AUKLET_APP_ID` and `AUKLET_API_KEY`
* JVM system properties `auklet.app.id` and `auklet.api.key`

On Android, you will need to add
`<uses-permission android:name="android.permission.INTERNET" />` to your manifest.

## Code

### Java
To configure Auklet agent using env vars/JVM sysprops, all you would need is
```
Auklet.init();
```

To configure the Auklet agent manually, you would need to set an app id and an api key.
```
Auklet.init(new Auklet.Config()
    .setAppId("...")
    .setApiKey("...")
    // ...set other options here
);
```

### Android
To configure Auklet agent using env vars/JVM sysprops, all you would need is
```
Auklet.init(new Auklet.Config().setAndroidContext(getApplicationContext()));
```

To configure the Auklet agent manually, you would need to set an app id and an api key.
```
Auklet.init(new Auklet.Config()
    .setAndroidContext(getApplicationContext())
    .setAppId("...")
    .setApiKey("...")
    // ...set other options here
);
```

### Sending Exceptions
By default, Auklet sets itself as the JVM's default uncaught exception handler; thus, any exceptions that bubble up out of any thread will be sent to Auklet. This can be disabled via any one of the following:
- `new Config().setUncaughtExceptionHandler(false)`
- Setting env var `AUKLET_UNCAUGHT_EXCEPTION_HANDLER` to `false`
- Setting JVM system property `auklet.uncaught.exception.handler` to `false`

If you are catching the exception and not letting it bubble up, you can use this syntax to send it to Auklet.
```
try {
    // code that can cause an exception
} catch (Exception e) {
    Auklet.send(e);
}
```

#### Auto-Start (Java only)
Set the environment variable `AUKLET_AUTO_START` or the JVM system property `auklet.auto.start` to `true` to have the agent start alongside the JVM. In this configuration, the agent will only send to Auklet exceptions that are not caught within a thread or by a thread handler (see the previous section for details). If you want to explicitly catch and report some exceptions to Auklet, do not use this method.


#### Note Regarding TLS Support
The auklet.io data pipeline uses TLS 1.2. According to [Android docs](https://developer.android.com/reference/javax/net/ssl/SSLSocket#protocols), TLS 1.2 is enabled by default starting with API level 20 (Android version 4.4W). If your application supports API levels 16 through 19, you may need to do additional work to ensure that TLS 1.2 is available.

If TLS 1.2 is not available on a device, the Auklet agent will fail to initialize and will log relevant messages to SLF4J, but this will not interrupt your application. Notably, since `Auklet.init()` never throws any exceptions, you will not be notified by the agent if it fails to initialize for this reason, so you should take care to ensure TLS 1.2 is available prior to invoking `Auklet.init()`.

You may find [this post](https://medium.com/tech-quizlet/working-with-tls-1-2-on-android-4-4-and-lower-f4f5205629a) relevant in adding TLS 1.2 compatibility for devices on API levels 16 through 19.

# Compiling/Running Locally
Auklet for Java uses Gradle 4 and requires Java 7+ to compile (Java 9+ recommended).

```
git clone git@github.com:aukletio/Auklet-Agent-Java.git
cd Auklet-Agent-Java
# This stores the Auklet agent in your local Maven repository with artifact version "local-build", which you can use per the instructions above.
./gradlew publishToMavenLocal
```

When compiling on Java 7, you will see warnings related to dependencies compiled for newer versions of Java; these warnings can be ignored.

When compiling on Java 8, use this syntax fix cross-compilation warnings (requires installing JDK 7): `JDK7_HOME=/path/to/java7/home ./gradlew publishToMavenLocal`

# Questions? Problems? Ideas?

To get support, report a bug or suggest future ideas for Auklet, go to https://help.auklet.io and click the blue button in the lower-right corner to send a message to our support team.
