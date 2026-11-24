package com.adso.pizca.data.network

import com.adso.pizca.data.api.CalificacionesApi
import com.adso.pizca.data.api.TheMealDbApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    /** Siempre termina en "/". El 1 es la clave gratis de prueba. */
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val json = Json {
        ignoreUnknownKeys = true   // TheMealDB manda campos que no usamos (strTags, strYoutube...)
        coerceInputValues = true   // si llega null donde hay valor por defecto, usa el valor por defecto
        isLenient = true           // acepta JSON un poco desordenado
    }

    /** Imprime en Logcat cada petición y su respuesta */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val mealDbApi: TheMealDbApi by lazy {
        retrofit.create(TheMealDbApi::class.java)
    }

    // ---------- Apps Script: calificaciones de la comunidad ----------

    /** URL del Apps Script SIN el "exec" final y terminando en "/" (Retrofit lo exige) */
    private const val APPS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbxGG_-EpmTSUefABEhsJrcdVHGnzpWYIXx3-BtV1trR5w6twX4iw1WKFtZV2gfhS2WX/"

    // Un segundo Retrofit: cada Retrofit tiene una sola dirección base
    private val retrofitAppsScript: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(APPS_SCRIPT_URL)
            .client(okHttpClient)   // el mismo cliente: también se ve en Logcat
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val calificacionesApi: CalificacionesApi by lazy {
        retrofitAppsScript.create(CalificacionesApi::class.java)
    }
}