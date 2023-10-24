using LoginWebapp.Models;
using LoginWebapp.Util;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

var _authkey = builder.Configuration.GetValue<string>("JwtSettings:SecurityKey");

builder.Services.AddLocalization(options => options.ResourcesPath = "Resources");

builder.Services.Configure<RequestLocalizationOptions>(options =>
{
    var supportedCultures = new[]
    {
        new CultureInfo("en-US"),
        new CultureInfo("ja-JP"),
        new CultureInfo("en-IN"),
    };

    options.SupportedCultures = supportedCultures;
    options.SupportedUICultures = supportedCultures;
    options.DefaultRequestCulture = new RequestCulture("en-US");
});



builder.Services.AddSingleton<SharedResourceService>();


//Jwt authentication starts here
//To Get the value from the appsettings.json
//For the microsoft login i will created cookie authentication

    builder.Services.AddAuthentication(item=>{
        item.DefaultSignInScheme = CookieAuthenticationDefaults.AuthenticationScheme; 
        item.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme ;
        item.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
    }).AddJwtBearer(item=>{
        item.RequireHttpsMetadata=true;
        item.SaveToken=true;
        item.TokenValidationParameters = new TokenValidationParameters()
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_authkey)),
            ValidateIssuer = false,
            ValidateAudience = false,
            ClockSkew = TimeSpan.Zero
            };
    }).AddMicrosoftAccount(options =>
    {
        options.ClientId = builder.Configuration["MicrosoftClientId"];
        options.ClientSecret = builder.Configuration["MicrosoftSecretId"];
    }).AddCookie(CookieAuthenticationDefaults.AuthenticationScheme,options =>
    {
        options.LoginPath = "/Home/ProcessLogin"; // Specify your login path
        options.LogoutPath = "/Home/LogOut"; // Specify your logout path
    });

var _jwtsettings = builder.Configuration.GetSection("JwtSettings");
builder.Services.Configure<JwtSettings>(_jwtsettings);
//Jwt authentication Ends and Microsoft Authentication Ends


//Scoped service to generate the token
builder.Services.AddScoped<JwtTokenGenerator>();

//
builder.Services.AddControllersWithViews();

// Add services to the container.
builder.Services.AddSession(options =>
{
    options.IdleTimeout = TimeSpan.FromMinutes(30);
    options.Cookie.HttpOnly = true;
    options.Cookie.IsEssential = true;
});

//Storing the cache to make faster retrieval
//builder.Services.AddDistributedMemoryCache(); 

//When We using the web api we need this code 
// builder.Services.AddHttpClient();

var app = builder.Build();


// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}
app.UseAuthentication();

app.UseAuthorization();

app.UseSession();

app.UseHttpsRedirection();

app.UseStaticFiles();

app.UseRouting();

var localizationOptions = app.Services.GetRequiredService<IOptions<RequestLocalizationOptions>>();
app.UseRequestLocalization(localizationOptions.Value);


// app.Use(async (context, next) =>
//     {
//     await next();

//     if (context.Response.StatusCode == (int)System.Net.HttpStatusCode.Unauthorized)
//     {
//         await context.Response.WriteAsync("Token Validation Has Failed. Request Access Denied");
//     }
// });

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=DisplayLoginPage}");

app.Run();
