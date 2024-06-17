import requests
from pydantic import BaseModel
from typing import Optional, Dict, List
import json
from keplergl import KeplerGl
from flask import Flask, request

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

    address = request.args.get("address", type=str)
    latitude = request.args.get("latitude", type=float)
    longitude = request.args.get("longitude", type=float)
    distance = request.args.get("distance", type=int)

    body = NaurtRequest(
        address_string=address,
        latitude=latitude,
        longitude=longitude,
        additional_matches=True,
        distance_filter=distance,
    ).dict(exclude_none=True)

    response = requests.post(ENDPOINT, headers=headers, json=body)

    naurt_response = NaurtResponse.parse_raw(response.text)

    my_map = KeplerGl()

    for feature in naurt_response.best_match.geojson["features"]:
        my_map.add_data(data=feature, name="{} - {}".format(naurt_response.best_match.address, feature["properties"]["naurt_type"]))

        if feature["properties"]["naurt_type"] == "naurt_door":
            config = make_config(feature["geometry"]["coordinates"][0][1], feature["geometry"]["coordinates"][0][0])
            my_map.config = config
            

    for additional_match in naurt_response.additional_matches:
        for feature in additional_match.geojson["features"]:
            my_map.add_data(data=feature, name="{} - {}".format(additional_match.address, feature["properties"]["naurt_type"]))
    
    html = my_map._repr_html_()

    return html, 200

def make_config(latitude: float, longitude: float) -> Dict:
    config = {
                "version": "v1",
                "config": {
                    "mapState": {
                        "latitude": latitude,
                        "longitude": longitude,
                        "zoom": 15
                    }
                }
            }
    
    return config

if __name__ == "__main__":
   app.run(debug=True)