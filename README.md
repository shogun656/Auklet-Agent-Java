# Auklet for Java

This is the official Java agent for Auklet. It officially supports Java 8+.
It is currently tested on Oracle java Hotspot JVM. We plan on doing more
testing on other currently active JVMs.


# Features

* Automatic report of unhandled exceptions.
* Location, and system metrics identification for all issues.


# Quickstart

* Download the agent jar file and import it in the java application.
* Initialize the auklet agent at the very beginning of `main` using app_id and api_key as following:
```
import io.auklet.agent.Auklet;

public class demo {

   public static void main(String []arg) {
       Auklet.init("<App_Id>", "<Api_key>");
       ...
   }
}

// or if you have your own implementation of JVM shutdown hook we recommend the following

public class Demo {

   public static void main(String []arg) {
       Auklet.init("<App_Id>", "<Api_key>", false);
       ...
       Auklet.shutdown()
   }
}
// Be Careful while calling `Auklet.init(<App_Id>, <Api_key>, false)` as
// Auklet mqtt client may not disconnect cleanly and you may have problems
// reconnecting in the future. It is important that you call `Auklet.shutdown()`
// where you think the program might exit or inside your own shutdown hook.
```

* The agent needs a dedicated folder to create and store its configuration files. The agent will create these files 
on startup if they do not exist, or will use files that are already available on disk.
* By default, the agent will use the following JVM system properties, in order, to determine the location from which 
to read/write its configuration files: `user.dir`, `user.home`, `java.io.tmpdir`. 
The agent will use the first one of these locations that is writable.
* If you need to use a specific location for the agent's configuration files other than what is listed above, you can 
set the JVM system property `auklet.config.dir` or use env variable `AUKLET_CONFIG_DIR` and the agent will attempt to 
use that value first, assuming it is writable. 
If it is set to a non-writable directory, or if the property is not set, the agent will fallback to the 
other properties described above.

# Authorization

To authorize your application you need to provide both an API key and app ID.
These values are available in the connection settings of your application as well as during initial setup.

# Questions? Problems? Ideas?

To get support, report a bug or suggest future ideas for Auklet, go to https://help.auklet.io and click the blue button in the lower-right corner to send a message to our support team.
