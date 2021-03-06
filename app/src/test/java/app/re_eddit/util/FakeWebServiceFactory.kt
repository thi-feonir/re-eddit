package app.re_eddit.util

import app.re_eddit.BuildConfig
import app.re_eddit.api.service.RedditService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object FakeWebServiceFactory {
    val webService by lazy { createRedditWebService() }
    val mockWebServer = MockWebServer()

    private fun createRedditWebService(): RedditService {
        val debuggable = BuildConfig.DEBUG
        val httpClient =
            createHttpClient(
                logger = createLogger(
                    debuggable
                )
            )

        mockWebServer.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
            .build()

        return retrofit.create(RedditService::class.java)
    }

    private fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private fun createLogger(debuggable: Boolean): Interceptor {
        val logger = HttpLoggingInterceptor()
        logger.level = if (debuggable) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logger
    }

    private fun createHttpClient(logger: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addNetworkInterceptor { chain ->
                var request = chain.request()
                val url = request.url
                request = request.newBuilder()
                    .url(url.newBuilder()
                        .addQueryParameter("raw_json", "1") // So tokens aren't escaped
                        .build())
                    .build()
                chain.proceed(request)
            }
            .build()
    }
}