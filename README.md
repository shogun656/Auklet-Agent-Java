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
<dependency>
  <groupId>io.auklet</groupId>
  <artifactId>auklet-agent-java</artifactId>
  <version>...</version>
</dependency>
```

## Gradle
`compile 'io.auklet:auklet-agent-java:<version>'`

## Configuration/Authorization
To authorize your application you need to provide both an API key and app ID. These values are available in the connection settings of your application as well as during initial setup.

Unless you use the `Config` object to manually set your Auklet credentials, you will need to set one of the following on the JVM where you are running Auklet:
* The environment variables `AUKLET_APP_ID` and `AUKLET_API_KEY`
* JVM system properties `auklet.app.id` and `auklet.api.key`

On Android, you will need to add
`<uses-permission android:name="android.permission.INTERNET" />` to your manifest.

## Code

### Java
```
// Use this to start the agent using env vars/JVM sysprops...
Auklet.init()
// ...or use this to configure manually...
Auklet.init(new Auklet.Config()
    .setAppId("...")
    .setApiKey("...")
    // ...set other options here
)
// ...or no code at all (see below).

// Explicitly send an exception to Auklet.
Auklet.send(new Exception())
```

#### Auto-Start (Java only)
Set the environment variable `AUKLET_AUTO_START` or the JVM system property `auklet.auto.start` to `true` to have the agent start alongside the JVM. In this configuration, the agent will only send to Auklet exceptions that are not caught within a thread or by a thread handler. If you want to explicitly catch and report some exceptions to Auklet, do not use this method.

### Android
```
// Use this to start the agent using env vars/JVM sysprops...
Auklet.init(new Auklet.Config().setAndroidContext(getApplicationContext()))
// ...or use this to configure manually.
Auklet.init(new Auklet.Config()
    .setAndroidContext(getApplicationContext())
    .setAppId("...")
    .setApiKey("...")
    // ...set other options here
)

// Explicitly send an exception to Auklet.
Auklet.send(new Exception())
```

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
