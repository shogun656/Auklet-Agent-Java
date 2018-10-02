# Auklet for Java

This is the official Java agent for Auklet. It officially supports Java 9+.
It is currently tested on Oracle java Hotspot JVM. We plan on doing more 
testing on other currently active JVMs.


# Features 

1. Automatic report of unhandled exceptions.
2. Location, and system metrics identification for all issues.


# Quickstart

1. Download the agent jar file and import it in the java application.
2. Initialize the auklet agent at the very beginning of `main` using app_id and api_key as following:
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

