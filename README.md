# Auklet for Java

This is the official Java agent for Auklet. It officially supports Java 9+.
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
       Auklet.init(<App_Id>, <Api_key>);
       ...
   }
}
```

# Authorization

To authorize your application you need to provide both an API key and app ID. 
These values are available in the connection settings of your application as well as during initial setup.

