package com.dumbcharades.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dumbcharades.app.data.local.AppDatabase
import com.dumbcharades.app.data.local.GameDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun json(): Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Provides @Singleton fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "dumb-charades.db")
            .addMigrations(object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE decks ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE sessions ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                }
            }).build()

    @Provides fun dao(db: AppDatabase): GameDao = db.gameDao()

    @Provides @Singleton fun workManager(@ApplicationContext context: Context): androidx.work.WorkManager = androidx.work.WorkManager.getInstance(context)

    @Provides @Singleton fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

}
