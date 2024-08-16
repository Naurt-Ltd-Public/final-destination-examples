# Naurt's Final Destination API in Java

This shows off a very basic implementation of using Naurt's final destination API
in Java

## Set Up and Usage 

First, make an `api.key` file next to `pom.xml`. 
Don't have a key? Get one from [the dashboard](https://dashboard.naurt.com/sign-up).
It's free and you don't need a credit card to sign up.

Then run with 

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.example.App" 
```

This will start the development server. In a webbrowser you can use it, 
for example `http://localhost:8080/?address_string=Grand%20Hotel,%20Brighton` will 
search for the Grand Hotel in Brighton.

The following search terms are available

- `address_string`
- `latitude`
- `longitude`
- `additional_matches`

Please note this is not intended for production environments - error handling is 
barebones at best! This is only for demo purposes.
