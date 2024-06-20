# Naurt's Final Destination API in Golang

This shows off a very basic implementation of using Naurt's final destination API
in Golang

## Set Up and Usage 

First, make an `api.key` file next to `go.mod`. 
Don't have a key? Get one from [the dashboard](https://dashboard.naurt.com/sign-up).
It's free and you don't need a credit card to sign up.

Install the dependencies with 

```bash
go mod download
```

Then run with 

```bash
go build -C src -o naurt
./src/naurt
```

This will start the development server. In a webbrowser you can use it, 
for example `http://localhost:8080/?address=Grand%20Hotel,%20Brighton` will 
search for the Grand Hotel in Brighton.

The following search terms are available

- `address`
- `latitude`
- `longitude`

Please note this is not intended for production environments - error handling is 
barebones at best! This is only for demo purposes.
