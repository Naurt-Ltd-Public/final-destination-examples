package main

import (
	"encoding/json"
	"errors"
	"fmt"

	grob "github.com/MetalBlueberry/go-plotly/graph_objects"
)

func extractLats(points [][]float32) []float32 {
	lats := []float32{}

	for _, point := range points {
		lats = append(lats, point[1])
	}

	return lats
}

func extractLons(points [][]float32) []float32 {
	lons := []float32{}

	for _, point := range points {
		lons = append(lons, point[0])
	}

	return lons
}

func plotNaurt(response NaurtResponse) (string, error) {

	traces := []grob.Trace{}

	var layout *grob.Layout

	if response.BestMatch != nil {
		extractNaurtInner(response.BestMatch, &traces, &layout, true)
	}

	if response.AdditionalMatches != nil {
		for _, data := range *response.AdditionalMatches {
			extractNaurtInner(&data, &traces, &layout, false)
		}

	}

	if layout == nil {
		return "", errors.New("no best match found")
	}

	fig := &grob.Fig{
		Data:   traces,
		Layout: layout,
	}

	jsonFig, err := json.Marshal(fig)
	if err != nil {
		return "", err
	}

	return string(jsonFig), nil

}

func extractNaurtInner(data *DestinationResponse, traces *[]grob.Trace, layout **grob.Layout, bestMatch bool) {
	for _, feat := range data.Geojson.Features {
		if feat.Geometry.Coordinates.Number != nil {
			// naurt_door

			if bestMatch {
				*layout = &grob.Layout{
					Mapbox: &grob.LayoutMapbox{
						Style: "carto-positron",
						Center: &grob.LayoutMapboxCenter{
							Lat: float64(feat.Geometry.Coordinates.Number[0][1]),
							Lon: float64(feat.Geometry.Coordinates.Number[0][0]),
						},
						Zoom: 15.0,
					},
					Showlegend: grob.False,
					Autosize:   grob.True,
				}
			}

			trace := &grob.Scattermapbox{
				Type:   "scattermapbox",
				Lat:    extractLats(feat.Geometry.Coordinates.Number),
				Lon:    extractLons(feat.Geometry.Coordinates.Number),
				Text:   fmt.Sprintf("%s - %s", data.Address, feat.Properties.NaurtType),
				Marker: &grob.ScattermapboxMarker{Size: 9.0},
			}

			*traces = append(*traces, trace)

		} else if feat.Geometry.Coordinates.NestedNumber != nil {
			// naurt_parking or naurt_building

			for _, shape := range feat.Geometry.Coordinates.NestedNumber {

				trace := &grob.Scattermapbox{
					Type: "scattermapbox",
					Lat:  extractLats(shape),
					Lon:  extractLons(shape),
					Text: fmt.Sprintf("%s - %s", data.Address, feat.Properties.NaurtType),
					Mode: grob.ScattermapboxModeLines,
					Fill: grob.ScattermapboxFillToself,
				}

				*traces = append(*traces, trace)
			}
		}
	}
}
