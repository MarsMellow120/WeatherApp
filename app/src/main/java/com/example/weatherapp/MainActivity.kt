package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val CITY: String = "Crestview, US"
    val API: String = "312c5d2d866dd573e600a53c92f51bb3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weatherTask()
    }

    private fun weatherTask() = GlobalScope.launch(Dispatchers.Main) {
        try {
            // Showing the ProgressBar, Making the main design GONE
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE

            val result = async(Dispatchers.IO) {
                URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(Charsets.UTF_8)
            }.await()

            // Handle the result in the UI thread
            handleWeatherResult(result)
        } catch (e: Exception) {
            // Handle exceptions
            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
        }
    }

    private fun handleWeatherResult(result: String) {
        try {
            // Extracting JSON returns from the API
            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val updatedAt: Long = jsonObj.getLong("dt")
            val updatedAtText =
                "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))
            val tempCelsius = main.getString("temp")
            val tempMinCelsius = main.getString("temp_min")
            val tempMaxCelsius = main.getString("temp_max")
            val tempFahrenheit = ((tempCelsius.toDouble() * 9/5) + 32).toInt().toString() + "°F"
            val tempMinFahrenheit = "Min Temp: " + ((tempMinCelsius.toDouble() * 9/5) + 32).toInt().toString() + "°F"
            val tempMaxFahrenheit = "Max Temp: " + ((tempMaxCelsius.toDouble() * 9/5) + 32).toInt().toString() + "°F"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")
            val sunrise: Long = sys.getLong("sunrise")
            val sunset: Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")
            val weatherDescription = weather.getString("description")
            val address = jsonObj.getString("name") + ", " + sys.getString("country")


            // Populating extracted data into our views
            findViewById<TextView>(R.id.address).text = address
            findViewById<TextView>(R.id.updated_at).text = updatedAtText
            findViewById<TextView>(R.id.status).text = weatherDescription.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            findViewById<TextView>(R.id.temp).text = tempFahrenheit
            findViewById<TextView>(R.id.temp_min).text = tempMinFahrenheit
            findViewById<TextView>(R.id.temp_max).text = tempMaxFahrenheit
            findViewById<TextView>(R.id.sunrise).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
            findViewById<TextView>(R.id.sunset).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
            findViewById<TextView>(R.id.wind).text = windSpeed
            findViewById<TextView>(R.id.pressure).text = pressure
            findViewById<TextView>(R.id.humidity).text = humidity

            // Views populated, Hiding the loader, Showing the main design
            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
        } catch (e: Exception) {
            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
        }
    }
}