use std::fs;

use plotly::{
    common::{Marker, Mode},
    layout::{Center, Mapbox, MapboxStyle},
    scatter_mapbox::Fill,
    Layout, Plot, ScatterMapbox, Trace,
};
use reqwest::Client;
use rocket::{response::content::RawJson, State};
use rocket_dyn_templates::{tera::Context, Template};
use serde::{Deserialize, Serialize};
use serde_json::Value;

#[macro_use]
extern crate rocket;

#[rocket::main]
async fn main() {
    let api_key = fs::read_to_string("api.key").unwrap();

    let figment = rocket::Config::figment()
        .merge(("port", 8080))
        .merge(("address", "0.0.0.0"));

    if let Err(e) = rocket::custom(figment)
        .attach(Template::fairing())
        // .attach(config)
        .mount("/", routes![handler])
        .manage(api_key)
        // .manage(bucket_info)
        .launch()
        .await
    {
        println!("Did not run. Error: {:?}", e)
    }
}

const NAURT_URL: &'static str = "https://api.naurt.net/final-destination/v1";

#[derive(Serialize)]
struct NaurtRequest {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub address_string: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub latitude: Option<f64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub longitude: Option<f64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub additional_matches: Option<bool>,
}

#[derive(Deserialize)]
struct NaurtResponse {
    pub best_match: DestinationResponse,
    pub additional_matches: Option<Vec<DestinationResponse>>,
    pub version: String,
}

#[derive(Deserialize)]
struct DestinationResponse {
    pub id: String,
    pub address: String,
    pub geojson: NaurtGeojson,
    pub distance: Option<f64>,
}

#[derive(Deserialize)]
struct NaurtGeojson {
    pub features: Vec<Feature>,
    #[serde(rename(deserialize = "type"))]
    pub type_val: String,
    pub properties: Value,
}

#[derive(Deserialize)]
struct Feature {
    pub geometry: Coordinates,
    #[serde(rename(deserialize = "type"))]
    pub type_val: String,
    pub properties: Properties,
}

#[derive(Deserialize)]
struct Coordinates {
    // #[serde(flatten)]
    pub coordinates: CoordinatesWrapper,
    #[serde(rename(deserialize = "type"))]
    pub type_val: String,
}

#[derive(Deserialize)]
#[serde(untagged)]
enum CoordinatesWrapper {
    Number(Vec<Vec<f64>>),
    NestedNumber(Vec<Vec<Vec<f64>>>),
}

#[derive(Deserialize)]
struct Properties {
    pub naurt_type: String,
    pub contributors: Option<Vec<String>>,
}

#[get("/?<address>&<latitude>&<longitude>&<additional_matches>")]
async fn handler(
    api_key: &State<String>,
    address: Option<String>,
    latitude: Option<f64>,
    longitude: Option<f64>,
    additional_matches: Option<bool>,
) -> Result<Template, RawJson<String>> {
    let client = Client::new();

    let request = NaurtRequest {
        address_string: address,
        latitude: latitude,
        longitude: longitude,
        additional_matches: additional_matches,
    };

    let response = client
        .post(NAURT_URL)
        .json(&request)
        .header("Authorization", api_key.as_str())
        .send()
        .await;

    let reply = match response {
        Ok(x) => x,
        Err(y) => return Err(RawJson(format!("\"Request Error\":\"{}\"", y))),
    };

    let body = reply.text().await;

    let json_text = match body {
        Ok(x) => x,
        Err(y) => return Err(RawJson(format!("\"Body Error\":\"{}\"", y))),
    };

    let naurt_response = match serde_json::from_str::<NaurtResponse>(&json_text) {
        Ok(x) => x,
        Err(y) => return Err(RawJson(format!("\"Json Error\":\"{}\"", y))),
    };

    let plotly = extract_naurt(&naurt_response);

    let mut context = Context::new();
    context.insert("data", &plotly);

    return Ok(Template::render("index", context.into_json()));
}

fn extract_naurt(response: &NaurtResponse) -> String {
    let mut plot = Plot::new();
    let mut layout = None;
    let mut traces: Vec<Box<dyn Trace>> = vec![];

    extract_naurt_inner(&response.best_match, &mut traces, &mut layout, true);

    if let Some(additional_matches) = &response.additional_matches {
        for dest in additional_matches {
            extract_naurt_inner(dest, &mut traces, &mut layout, false);
        }
    }

    plot.add_traces(traces);
    plot.set_layout(layout.unwrap());

    return plot.to_inline_html(Some("mapDiv"));
}

fn extract_naurt_inner(
    data: &DestinationResponse,
    traces: &mut Vec<Box<dyn Trace>>,
    layout: &mut Option<Layout>,
    best_match: bool,
) {
    for feat in &data.geojson.features {
        match &feat.geometry.coordinates {
            CoordinatesWrapper::Number(coords) => {
                if best_match {
                    *layout = Some(
                        Layout::new()
                            .mapbox(
                                Mapbox::new()
                                    .style(MapboxStyle::CartoPositron)
                                    .center(Center::new(coords[0][1], coords[0][0]))
                                    .zoom(15),
                            )
                            .show_legend(false)
                            .auto_size(true),
                    );
                }

                let trace = ScatterMapbox::new(
                    coords.iter().map(|z| z[1]).collect(),
                    coords.iter().map(|z| z[0]).collect(),
                )
                .text(format!("{} - {}", data.address, feat.properties.naurt_type))
                .mode(Mode::Markers)
                .marker(Marker::new().size(13));

                traces.push(trace);
            }
            CoordinatesWrapper::NestedNumber(coords) => {
                for shape in coords {
                    let trace = ScatterMapbox::new(
                        shape.iter().map(|z| z[1]).collect(),
                        shape.iter().map(|z| z[0]).collect(),
                    )
                    .text(format!("{} - {}", data.address, feat.properties.naurt_type))
                    .mode(Mode::Lines)
                    .fill(Fill::ToSelf);

                    traces.push(trace);
                }
            }
        }
    }
}
