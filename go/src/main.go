package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"html/template"
	"io"
	"net/http"
	"os"
	"strconv"
	"sync"
)

var (
	apiKey string
	once   sync.Once
)

var tmpl *template.Template

func initialiseApiKey() {
	// Use sync.Once to ensure this is executed only once
	once.Do(func() {
		// Read file content from a file
		file, err := os.Open("api.key")
		if err != nil {
			panic("`api.key` not found")
		}

		defer file.Close()

		content, err := io.ReadAll(file)
		if err != nil {
			panic("Could not read API key to string")
		}

		apiKey = string(content)
	})
}

func makeNaurtRequest(address string, latitude *float64, longitude *float64) (NaurtResponse, error) {
	url := "https://api.naurt.net/final-destination/v1"

	data := NaurtRequest{
		AddressString:     address,
		Latitude:          latitude,
		Longitude:         longitude,
		AdditionalMatches: true,
	}

	jsonData, err := json.Marshal(data)
	if err != nil {
		return NaurtResponse{}, err
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return NaurtResponse{}, err
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", apiKey)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return NaurtResponse{}, err
	}

	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return NaurtResponse{}, err
	}

	var naurt NaurtResponse
	if err := json.Unmarshal([]byte(body), &naurt); err != nil {
		return NaurtResponse{}, err
	}

	return naurt, err
}

type PageData struct {
	Data string
}

func handler(w http.ResponseWriter, r *http.Request) {

	query := r.URL.Query()

	address := query.Get("address")

	var latitude *float64
	if query.Has("latitude") {
		tmp, err := strconv.ParseFloat(query.Get("latitude"), 64)
		if err != nil {
			fmt.Fprintf(w, err.Error())
			return
		}
		latitude = &tmp
	}

	var longitude *float64
	if query.Has("longitude") {
		tmp, err := strconv.ParseFloat(query.Get("longitude"), 64)
		if err != nil {
			fmt.Fprintf(w, err.Error())
			return
		}
		longitude = &tmp
	}

	resp, err := makeNaurtRequest(address, latitude, longitude)
	if err != nil {
		fmt.Fprintf(w, err.Error())
		return
	}

	mapJson, err := plotNaurt(resp)
	if err != nil {
		fmt.Fprintf(w, err.Error())
		return
	}

	data := PageData{Data: mapJson}

	e := tmpl.Execute(w, data)
	if e != nil {
		fmt.Fprintf(w, e.Error())
	}
}

func main() {

	initialiseApiKey()

	tmpl = template.Must(template.ParseFiles("templates/index.html"))

	http.HandleFunc("/", handler)

	fmt.Println("Starting the server!")
	if err := http.ListenAndServe(":8080", nil); err != nil {
		fmt.Println(err)
	}
}
