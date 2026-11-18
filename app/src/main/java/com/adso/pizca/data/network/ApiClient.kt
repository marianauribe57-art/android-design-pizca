package com.adso.pizca.data.network

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
}