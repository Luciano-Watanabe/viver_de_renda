package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Bill
import com.example.data.model.InvestmentSettings
import com.example.data.model.PurchasedAsset

@Database(entities = [Bill::class, InvestmentSettings::class, PurchasedAsset::class], version = 2, exportSchema = false)
abstract class ViverDatabase : RoomDatabase() {
    abstract val dao: ViverDao

    companion object {
        @Volatile
        private var INSTANCE: ViverDatabase? = null

        fun getDatabase(context: Context): ViverDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ViverDatabase::class.java,
                    "viver_de_renda_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
