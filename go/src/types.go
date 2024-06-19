package main

import (
	"encoding/json"
	"errors"
)

type NaurtRequest struct {
	AddressString     string `json:"address_string,omitempty"`
	Latitude          string `json:"latitude,omitempty"`
	Longitude         string `json:"longitude,omitempty"`
	AdditionalMatches bool   `json:"additional_matches,omitempty"`
}

type NaurtResponse struct {
	BestMatch         *DestinationResponse   `json:"best_match,omitempty"`
	AdditionalMatches *[]DestinationResponse `json:"additional_matches,omitempty"`
	Version           string                 `json:"version,omitempty"`
}

type DestinationResponse struct {
	ID       string       `json:"id"`
	Address  string       `json:"address"`
	Geojson  NaurtGeojson `json:"geojson"`
	Distance float32      `json:"distance,omitempty"`
}

type NaurtGeojson struct {
	Features []Feature `json:"features"`
	TypeVal  string    `json:"type"`
}

type Feature struct {
	Geometry   Coordinates `json:"geometry"`
	TypeVal    string      `json:"type"`
	Properties Properties  `json:"properties"`
}

type Coordinates struct {
	Coordinates CoordinatesWrapper `json:"coordinates"`
	TypeVal     string             `json:"type"`
}

type CoordinatesWrapper struct {
	Number       [][]float32
	NestedNumber [][][]float32
}

func (f *CoordinatesWrapper) UnmarshalJSON(data []byte) error {

	var doubleArray [][]float32
	if err := json.Unmarshal(data, &doubleArray); err == nil {
		f.Number = doubleArray
		return nil
	}

	var tripleArray [][][]float32
	if err := json.Unmarshal(data, &tripleArray); err == nil {
		f.NestedNumber = tripleArray
		return nil
	}

	return errors.New("`CoordinatesWrapper` did not find valid format")
}

type Properties struct {
	NaurtType    string
	Contributors []string
}
