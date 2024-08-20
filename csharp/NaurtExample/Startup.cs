
namespace NaurtWebServer
{
    public class Startup
    {
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddControllers(); // Add support for controllers

            services.AddSingleton<Handler>();
        }

        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseRouting();

            app.UseEndpoints(endpoints =>
            {
                var handler = app.ApplicationServices.GetRequiredService<Handler>();
                endpoints.MapGet("/", handler.Handle);
            });
        }

    }
}