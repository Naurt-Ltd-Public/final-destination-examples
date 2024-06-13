import fs from 'fs';
import express from 'express';
import axios from 'axios';

const API_KEY: string = fs.readFileSync('api.key', 'utf-8');

const app = express();
const port = 3000;



interface NaurtRequest {
    address_string?: string | null;
    latitude?: number | null;
    longitude?: number | null;
    additional_matches?: boolean | null;
    distance_filter?: number | null;
}

interface NaurtGeojson {
    features: Feature[],
    type: string
}

interface Feature {
    geometry: Coordinates,
    type: string,
    properties: Properties
}

interface Properties {
    naurt_type: string
}

interface Coordinates {
    coordinates: any[],
    type: string
}

interface DestinationResponse {
    id: string;
    address: string;
    geojson: NaurtGeojson;
    distance?: number;
}

interface NaurtResponse {
    best_match?: DestinationResponse;
    additional_matches?: DestinationResponse[];
    version?: string;
}

function parseJson<T>(jsonString: string): T | null {
    try {
        return JSON.parse(jsonString) as T;
    } catch (_) {
        return null;
    }
}

function toJSON(obj: Object): string {
    const filteredObj = Object.fromEntries(
        Object.entries(obj).filter(([_, value]) => value != null)
    );

    return JSON.stringify(filteredObj);
}

function getStringOrNull(value: unknown): string | null {
    return typeof value === 'string' ? value : null;
}

function getNumberOrNull(value: unknown): number | null {
    if (typeof value === 'string' && !isNaN(Number(value))) {
      return Number(value);
    }
    return null;
  }

app.get("/", async (req, res) => {

    const address = getStringOrNull(req.query.address);
    const lat = getNumberOrNull(req.query.lat);
    const lon = getNumberOrNull(req.query.lon);

    const request: NaurtRequest = {
        address_string: address, 
        latitude: lat,
        longitude: lon,
        additional_matches: true,
        distance_filter: null
    };

    try {
        const body: string = toJSON(request);

        const response = await axios.post("https://api.naurt.net/final-destination/v1", body, {
            headers: {
                "Content-Type": "application/json",
                "Authorization": API_KEY
            }
        });

        const naurtResponse: NaurtResponse = response.data;

        const mapHtml = generateMapHTML(naurtResponse);

        res.status(200).send(mapHtml);
    } catch (error) {

        if (axios.isAxiosError(error)) {
            res.status(500).json(error.response?.data);
        } else {
            res.status(500).json({
                message: (error as Error).message
            });
        }
    }
    
})


function generateMapHTML(naurtData: NaurtResponse): string {

    var data = [];

    var layout = {};

    if (naurtData.best_match !== undefined) {
        for (var feat of naurtData.best_match.geojson.features) {
            if (feat.properties.naurt_type === "naurt_door") {

                const points: number[][] = feat.geometry.coordinates;

                layout = {
                    mapbox: {
                        style: "carto-positron", 
                        center: {
                            lat: points[0][1],
                            lon: points[0][0]
                        },
                        zoom: 15
                    },
                    showlegend: false,
                    autosize: true
                };

                data.push({
                    type: "scattermapbox",
                    lat: points.map(coords => coords[1]),
                    lon: points.map(coords => coords[0]),
                    text: `${naurtData.best_match?.address} - ${feat.properties.naurt_type}`,
                    marker: {size: 9}
                })
            } else {
                const points: number[][][] = feat.geometry.coordinates;
                
                for (var shape of points) {
                    data.push({
                        type: "scattermapbox",
                        fill: "toself",
                        lat: shape.map(coords => coords[1]),
                        lon: shape.map(coords => coords[0]),
                        text: `${naurtData.best_match?.address} - ${feat.properties.naurt_type}`,
                        mode: "lines"
                    })
                }
                
            }
        }
    }

    if (naurtData.additional_matches !== undefined) {
        for (var match of naurtData.additional_matches) {
            for (var feat of match.geojson.features) {
                if (feat.properties.naurt_type === "naurt_door") {
                    const points: number[][] = feat.geometry.coordinates;
                    data.push({
                        type: "scattermapbox",
                        lat: points.map(coords => coords[1]),
                        lon: points.map(coords => coords[0]),
                        text: `${match.address} - ${feat.properties.naurt_type}`,
                        marker: {size: 9}
                    })
                } else {
                    const points: number[][][] = feat.geometry.coordinates;
                    
                    for (var shape of points) {
                        data.push({
                            type: "scattermapbox",
                            fill: "toself",
                            lat: shape.map(coords => coords[1]),
                            lon: shape.map(coords => coords[0]),
                            text: `${match.address} - ${feat.properties.naurt_type}`,
                            mode: "lines"
                        })
                    }
                    
                }
            }
        }
    }
    

    
    

    const htmlContent = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Plotly Map</title>
  <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
</head>
<style>
.container {
    width: 100%;
    height: 100vh;
}
</style>
<body>
  <div class="container" id="mapDiv"></div>
  <script>
    var data = JSON.parse('${JSON.stringify(data)}');

    var layout = JSON.parse('${JSON.stringify(layout)}');

    Plotly.newPlot("mapDiv", data, layout);
  </script>
</body>
</html>
`;

    return htmlContent;
}


app.listen(port, () => {
    console.log(`Server is running on http://localhost:${port}`);
})
