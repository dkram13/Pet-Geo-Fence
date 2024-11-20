package com.example.pgfapp.DatabaseStuff

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pgfapp.DatabaseStuff.DAOs.BoundsDao
import com.example.pgfapp.DatabaseStuff.DAOs.BoundsPetDao
import com.example.pgfapp.DatabaseStuff.DAOs.PetLocationDao
import com.example.pgfapp.DatabaseStuff.DAOs.PetsDao
import com.example.pgfapp.DatabaseStuff.Entities.Bounds
import com.example.pgfapp.DatabaseStuff.Entities.BoundsPet
import com.example.pgfapp.DatabaseStuff.Entities.PetLocation
import com.example.pgfapp.DatabaseStuff.Entities.Pets


@Database(entities = [Bounds::class, BoundsPet::class, Pets::class, PetLocation::class], version =2, exportSchema = false)
@TypeConverters(GeoPointsConverter::class)
abstract class UserDatabase: RoomDatabase() {

    abstract fun BoundsDao(): BoundsDao
    abstract fun BoundsPetDao(): BoundsPetDao
    abstract fun PetsDao(): PetsDao
    abstract fun PetLocationDao(): PetLocationDao

    companion object{
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                )
                    .fallbackToDestructiveMigration() // Automatically clears the database
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}