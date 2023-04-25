package com.tirthdalwadi.memesclub

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tirthdalwadi.memesclub.databinding.ActivityMainBinding
import com.tirthdalwadi.memesclub.databinding.ActivityMainBinding.inflate


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val url = "https://meme-api.com/gimme"
    var currentMemeUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        callAPI()

        binding.btnNext.setOnClickListener {
            binding.progressBar.isVisible = true
            callAPI()
        }

        binding.btnShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey! Checkout this funny meme from reddit: $currentMemeUrl"
            )
            sharingIntent.putExtra(Intent.EXTRA_TITLE, "Sharing a meme from Reddit")
            sharingIntent.type = "text/plain"
            startActivity(Intent.createChooser(sharingIntent, "Share using"))
        }
    }


    private fun callAPI() {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                currentMemeUrl = response.getString("url")

                Glide.with(this).load(currentMemeUrl).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.isVisible = false
                        return false
                    }

                }).into(binding.ivMemes)
            },
            {
                Log.d("MemeStringUrl", it.toString())
            }
        )

        MySingleton.getInstance(this.applicationContext).addToRequestQueue(jsonObjectRequest)
    }
}

class MySingleton constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: MySingleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: MySingleton(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}