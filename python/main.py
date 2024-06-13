import requests
from pydantic import BaseModel
from typing import Optional, Dict, List
import json
from keplergl import KeplerGl
from flask import Flask, make_response

ENDPOINT = "https://api.naurt.net/final-destination/v1"

class NaurtRequest(BaseModel):
    address_string: Optional[str] = None
    country: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    distance_filter: Optional[float] = None
    additional_matches: Optional[bool] = None

class DestinationRresponse(BaseModel):
    id: str
    address: str
    geojson: Dict
    distance: Optional[float] = None


class NaurtResponse(BaseModel):
    best_match: Optional[DestinationRresponse] = None
    additional_matches: Optional[List[DestinationRresponse]] = None
    contributors: Optional[List[str]] = None
    version: Optional[str] = None


with open("api.key") as file:
    API_KEY = file.read()

app = Flask(__name__)

@app.get("/")
def main():
    headers = {"Authorization": API_KEY, "content-type": "application/json"}

    body = NaurtRequest(
        address_string="Grand Hotel",
        latitude=50.83,
        longitude=-0.13,
        additional_matches=False,
        distance_filter=25000,
    ).dict(exclude_none=True)
    response = requests.post(ENDPOINT, headers=headers, json=body)

    print(response.text)

    naurt_response = NaurtResponse.parse_raw(response.text)

    my_map = KeplerGl()

    for feature in naurt_response.best_match.geojson["features"]:
        my_map.add_data(data=feature, name=feature["properties"]["naurt_type"])
    
    html = my_map._repr_html_()

    return html, 200

if __name__ == "__main__":
   app.run(debug=True)