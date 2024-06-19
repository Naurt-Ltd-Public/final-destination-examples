package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"sync"

	grob "github.com/MetalBlueberry/go-plotly/graph_objects"
	"github.com/MetalBlueberry/go-plotly/offline"
)

var (
	apiKey string
	once   sync.Once
)

func initialiseApiKey() {
	// Use sync.Once to ensure this is executed only once
	once.Do(func() {
		// Read file content from a file
		file, err := os.Open("api.key")
		if err != nil {
			return
		}

		defer file.Close()

		content, err := io.ReadAll(file)
		if err != nil {
			return
		}

		apiKey = string(content)
	})
}

func makeNaurtRequest(address string, latitude string, longitude string) (NaurtResponse, error) {
	url := "https://api.naurt.net/final-destination/v1"

	data := NaurtRequest{
		AddressString:     address,
		Latitude:          latitude,
		Longitude:         longitude,
		AdditionalMatches: false,
	}

	jsonData, err := json.Marshal(data)

	if err != nil {
		return NaurtResponse{}, errors.New("Json Error")
	}

	fmt.Println(string(jsonData))

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return NaurtResponse{}, errors.New("Request error")
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", apiKey)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return NaurtResponse{}, errors.New("Failed to make request")
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return NaurtResponse{}, errors.New("Ca not ready body")
	}

	naurt, err := convertResponseToNaurt(string(body))
	if err != nil {
		return NaurtResponse{}, err
	}

	return naurt, nil

}

func convertResponseToNaurt(resp string) (NaurtResponse, error) {
	var naurt NaurtResponse

	if err := json.Unmarshal([]byte(resp), &naurt); err != nil {
		return NaurtResponse{}, err
	}

	return naurt, nil
}

func convertToMap() {
	fig := &grob.Fig{
		Data: grob.Traces{
			&grob.Bar{
				Type: grob.TraceTypeBar,
				X:    []float64{1, 2, 3},
				Y:    []float64{1, 2, 3},
			},
		},
		Layout: &grob.Layout{
			Title: &grob.LayoutTitle{
				Text: "A Figure Specified By Go Struct",
			},
		},
	}

	offline.Show(fig)
}

func handler(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()

	address := query.Get("address")
	lat := query.Get("latitude")
	lon := query.Get("longitude")

	response, err := makeNaurtRequest(address, lat, lon)
	if err != nil {
		fmt.Fprintf(w, err.Error())
		return
	}

	naurtJson, err := json.Marshal(response)
	if err != nil {
		fmt.Fprintf(w, err.Error())
		return
	}

	fmt.Fprintf(w, string(naurtJson))
}

func main() {

	initialiseApiKey()

	convertToMap()

	http.HandleFunc("/", handler)

	fmt.Println("Starting the server!")
	if err := http.ListenAndServe(":8080", nil); err != nil {
		fmt.Println(err)
	}
}
