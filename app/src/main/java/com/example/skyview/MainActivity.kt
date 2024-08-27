package com.example.skyview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import com.example.skyview.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupSearchView()
        fetchWeatherData("Hyderabad")
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response =
            retrofit.getWeatherData(cityName, "9f13ec1d25ed3d70fc8cef867e69bd9c", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        updateUI(responseBody, cityName)
                    } else {
                        // Handle the case when the response body is null
                        // Show an error message to the user
                    }
                } else {
                    // Handle the case when the response is not successful
                    // Show an error message to the user based on the response code
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                // Handle network failures or errors here
                // Show an error message to the user
            }
        })
    }

    private fun updateUI(weatherApp: WeatherApp, cityName: String) {
        // UI updates should be done on the main UI thread
        runOnUiThread {
            val temperature = weatherApp.main.temp.toString()
            val humidity = weatherApp.main.humidity
            val windSpeed = weatherApp.wind.speed
            val sunRise = weatherApp.sys.sunrise.toLong()
            val sunset = weatherApp.sys.sunset.toLong()
            val seaLevel = weatherApp.main.pressure
            val condition = weatherApp.weather.firstOrNull()?.main ?: "unknown"
            val maxTemp = weatherApp.main.temp_max
            val minTemp = weatherApp.main.temp_min

            binding.temp.text = "${temperature}°C"
            binding.weather.text = condition
            binding.maxTemp.text = "Max Temp : $maxTemp °C"
            binding.minTemp.text = "Min Temp: $minTemp °C"
            binding.humidity.text = "$humidity %"
            binding.windspeed.text = "$windSpeed m/s"
            binding.sunrise.text = "${time(sunRise)}"
            binding.sunset.text = "${time(sunset)}"
            binding.sea.text = "$seaLevel hPa"
            binding.condition.text = condition
            binding.day.text = dayName(System.currentTimeMillis())
            binding.date.text = date()
            binding.cityName.text = cityName

            changeBackground(condition)
        }
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(System.currentTimeMillis()))
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun changeBackground(conditions: String) {
        when (conditions) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
    }
}

