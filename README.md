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

* The agent needs a dedicated folder to create and stores the configuration files on the local machine. 
Upon every restart the agent tries to create configuration files if they don't already exist. 
* If the user wants Auklet agent to use specific folder then it can be done using the following command: 
`java -Dauklet.config.dir="custom/file/path/dir" MyApp`
* If the `auklet.config.dir` sysProperty is not defined then the agent chooses the directory in the following order
of its accessibility: working directory -> home directory -> temp directory

# Authorization

To authorize your application you need to provide both an API key and app ID.
These values are available in the connection settings of your application as well as during initial setup.

# Questions? Problems? Ideas?

To get support, report a bug or suggest future ideas for Auklet, go to https://help.auklet.io and click the blue button in the lower-right corner to send a message to our support team.
