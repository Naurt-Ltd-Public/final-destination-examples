
namespace NaurtWebServer
{
    public class QueryParser
    {
        public string? addressString { get; set; }
        public bool? additionalMatches { get; set; }
        public float? latitude { get; set; }
        public float? longitude { get; set; }

        public QueryParser(IQueryCollection queryParams)
        {
            this.addressString = queryParams["address_string"];
            this.additionalMatches = this.ParseBool(queryParams["additional_matches"]);
            this.latitude = this.ParseFloat(queryParams["latitude"]);
            this.longitude = this.ParseFloat(queryParams["longitude"]);
        }

        private float? ParseFloat(string? query)
        {
            if (query == null)
            {
                return null;
            }

            if (float.TryParse(query!, out float floatParam))
            {
                return floatParam;
            }
            else
            {
                return null;
            }
        }

        private bool? ParseBool(string? query)
        {
            if (query == null)
            {
                return null;
            }

            if (bool.TryParse(query!, out bool boolParam))
            {
                return boolParam;
            }
            else
            {
                return null;
            }
        }
    }
}