package main

import grob "github.com/MetalBlueberry/go-plotly/graph_objects"

func plotNaurt(response NaurtResponse) (string, error) {

	trace1 := &grob.Bar{}
	trace2 := &grob.Bar{}

	trace3 := &grob.Scattermapbox{}

	traces := []grob.Trace{trace1, trace2}

	traces = append(traces, trace3)

	if response.BestMatch != nil {
		for _, feat := range response.BestMatch.Geojson.Features {
			if feat.Geometry.Coordinates.Number != nil {
				// naurt_door

			} else if feat.Geometry.Coordinates.NestedNumber != nil {
				// naurt_parking or naurt_building
			}
		}
	}

	fig := &grob.Fig{
		Data:   traces,
		Layout: layout,
	}

}
