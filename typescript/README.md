# Naurt's Final Destination API in Typescript

This shows off a very basic implementation of using Naurt's final destination API
in Typescript

Detailed Walkthrough available at: [Integrating Naurt’s Geocoder with TypeScript](https://www.naurt.com/blog-posts/integrating-naurts-geocoder-with-typescript)

## Set Up and Usage 

First, make an `api.key` file next to `package.json`. 
Don't have a key? Get one from [the dashboard](https://dashboard.naurt.com/sign-up).
It's free and you don't need a credit card to sign up.

To install dependencies:

```bash
bun install
```

To run:

```bash
bun start
```

This will start an express server. You can use it in a webbrowser to get maps.
Query with `http://localhost:3000/?address=grand%20hotel,%20brighton`

There query terms available are

- `address`
- `lat`
- `lon`

Please note this is not intended for production environments - error handling is 
barebones at best! This is only for demo purposes.
