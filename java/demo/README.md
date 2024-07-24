# Naurt's Final Destination API in Java

This shows off a very basic implementation of using Naurt's final destination API
in Java


## Set Up and Usage 

First, make an `api.key` file next to `pom.xml`. 
Don't have a key? Get one from [the dashboard](https://dashboard.naurt.com/sign-up).
It's free and you don't need a credit card to sign up.

To install dependencies: (from project root)

```bash
mvn clean install
```

To run:

```bash
mvn exec:java -Dexec.mainClass="com.example.DemoApplication"
```

This will start a server. You can use it in a webbrowser to get maps.
Query with `http://localhost:8080/?address_string=sheffield university`

There query terms available are

- `address_string`

Please note this is not intended for production environments - error handling is 
barebones at best! This is only for demo purposes.
