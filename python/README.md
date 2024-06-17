# Naurt's Final Destination API in Python

This shows off a very basic implementation of using Naurt's final destination API
in Python

## Set Up and Usage 

First, make an `api.key` file next to `main.py`. 
Don't have a key? Get one from [the dashboard](https://dashboard.naurt.com/sign-up).
It's free and you don't need a credit card to sign up.

Create and activate a virtual environment and install the dependencies 

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Then run with 

```bash
python main.py
```

This will start the Flask development server. In a webbrowser you can use it, 
for example `http://localhost:5000/?address=Grand%20Hotel,%20Brighton` will 
search for the Grand Hotel in Brighton.

The following search terms are available

- `address`
- `latitude`
- `longitude`
- `distance`

Please note this is not intended for production environments - error handling is 
barebones at best! This is only for demo purposes.
