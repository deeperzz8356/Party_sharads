package com.dumbcharades.app.di

import android.content.Context
import androidx.room.Room
import com.dumbcharades.app.BuildConfig
import com.dumbcharades.app.data.local.AppDatabase
import com.dumbcharades.app.data.local.GameDao
import com.dumbcharades.app.data.network.GameApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun json(): Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Provides @Singleton fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "dumb-charades.db").fallbackToDestructiveMigration().build()

    @Provides fun dao(db: AppDatabase): GameDao = db.gameDao()

    @Provides @Singleton fun workManager(@ApplicationContext context: Context): androidx.work.WorkManager = androidx.work.WorkManager.getInstance(context)

    @Provides @Singleton fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton fun api(json: Json, auth: FirebaseAuth): GameApi {
        val authInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            val token = runCatching { runBlocking { auth.currentUser?.getIdToken(false)?.await()?.token } }.getOrNull()
            if (token != null) builder.header("Authorization", "Bearer $token")
            chain.proceed(builder.build())
        }
        val client = OkHttpClient.Builder().addInterceptor(authInterceptor).build()
        return Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build().create(GameApi::class.java)
    }
}
