# Naurt's Final Destination API in Rust

This shows off a very basic implementation of using Naurt's final destination API
in Rust

## Set Up and Usage 

First, make an `api.key` file next to `Cargo.toml`. 
Don't have a key? Get one from [the dashboard](https://dashboard.naurt.com/sign-up).
It's free and you don't need a credit card to sign up.

Install the dependencies with 

Then run with 

```bash
cargo run
```

This will start the development server. In a webbrowser you can use it, 
for example `http://localhost:8080/?address=Grand%20Hotel,%20Brighton` will 
search for the Grand Hotel in Brighton.

The following search terms are available

- `address`
- `latitude`
- `longitude`
- `additional_matches`

Please note this is not intended for production environments - error handling is 
barebones at best! This is only for demo purposes.
