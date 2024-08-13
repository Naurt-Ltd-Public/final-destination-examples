
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace NaurtWebServer
{


    public class Handler
    {
        private readonly HttpClient _httpClient;
        private readonly string _apiKey;
        private readonly string template;


        public Handler()
        {
            this._httpClient = new HttpClient();
            this._apiKey = File.ReadAllText("api.key");

            this._httpClient.DefaultRequestHeaders.Add("Authorization", this._apiKey);

            string templatesPath = Path.Combine(Directory.GetCurrentDirectory(), "Templates");
            string templatePath = Path.Combine(templatesPath, "index.html");
            this.template = File.ReadAllText(templatePath);

        }

        public async Task Handle(HttpContext context)
        {


            var queryParams = context.Request.Query;
            QueryParser queryParser = new QueryParser(queryParams);

            var options = new JsonSerializerOptions
            {
                DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull,
                PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower
            };

            string jsonContent = JsonSerializer.Serialize(queryParser, options);

            var content = new StringContent(jsonContent, System.Text.Encoding.UTF8, "application/json");

            var response = await this._httpClient.PostAsync("https://api.naurt.net/final-destination/v1", content);



            if (response.IsSuccessStatusCode)
            {
                string reply = await response.Content.ReadAsStringAsync();

                string renderedTemplate = this.template.Replace("#NaurtResponse", reply);

                context.Response.ContentType = "text/html";
                context.Response.StatusCode = 200;
                await context.Response.WriteAsync(renderedTemplate);
            }
            else
            {
                context.Response.StatusCode = 500;
                context.Response.ContentType = "application/json";
                var reply = await response.Content.ReadAsStringAsync();
                await context.Response.WriteAsync(reply);
            }
        }
    }
}