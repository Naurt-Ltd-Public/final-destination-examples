using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;

namespace SimpleHttpServer
{
    public class Startup
    {
        public void ConfigureServices(IServiceCollection services)
        {
            // Add services to the container if needed
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
                endpoints.MapGet("/", async context =>
                {
                    await context.Response.WriteAsync("Hello, World!");
                });

                endpoints.MapGet("/hello", async context =>
                {
                    await context.Response.WriteAsync("Hello from /hello endpoint!");
                });

                endpoints.MapPost("/post", async context =>
                {
                    await context.Response.WriteAsync("Hello from POST endpoint!");
                });
            });
        }
    }
}
