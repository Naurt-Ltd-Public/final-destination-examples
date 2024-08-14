import Vapor
import Leaf 

struct Config {
    static let apiKey: String = {
        let filePath = "api.key"
        do {
            let fileContents = try String(contentsOfFile: filePath, encoding: .utf8)
            return fileContents
        } catch {
            fatalError("API Key file not found")
        }
    }()
}

func main() throws {

    var env = try Environment.detect()
    let app = Application(env)
    defer { app.shutdown() }

    app.views.use(.leaf)

    try configureRoutes(app)

    try app.run()
}

func configureRoutes(_ app: Application) throws {
    // Define a route for the root URL
    app.get(use: handler)


}

struct NaurtRequest: Content {
    let addressString: String?
    let additionalMatches: Bool?
    let latitude: Float?
    let longitude: Float?

    func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CustomCodingKeys.self)

        if let addressString = self.addressString {
            try container.encode(addressString, forKey: .addressString)
        }

        if let additionalMatches = self.additionalMatches {
            try container.encode(additionalMatches, forKey: .additionalMatches)
        }

        if let latitude = self.latitude {
            try container.encode(latitude, forKey: .latitude)
        }

        if let longitude = self.longitude {
            try container.encode(longitude, forKey: .longitude)
        }
    }

    enum CustomCodingKeys: String, CodingKey {
        case addressString = "address_string"
        case additionalMatches = "additional_matches"
        case latitude = "latitude"
        case longitude = "longitude"
    }
}

func handler(req: Request) async throws -> Response {

    let addressString = req.query[String.self, at: "address_string"]
    let additionalMatches = req.query[Bool.self, at: "additional_matches"]
    let latitude = req.query[Float.self, at: "latitude"]
    let longitude = req.query[Float.self, at: "longitude"]

    let response = try await req.client.post("https://api.naurt.net/final-destination/v1") { req in

    let naurtRequest = NaurtRequest(addressString: addressString, additionalMatches: additionalMatches, latitude: latitude, longitude: longitude)
    try req.content.encode(naurtRequest)
    
    req.headers.add(name: "Authorization", value: Config.apiKey)
    }


    guard response.status == .ok else {
        return Response(status: .internalServerError, body: .init(string: "Failed to fetch data from the external API"))
    }

    let jsonString = String(buffer: response.body!)

    let context = ["NaurtResponse": jsonString]
    return try await req.view.render("index", context).encodeResponse(status: .ok, for: req)
}

try main()