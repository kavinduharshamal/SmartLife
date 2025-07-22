package com.example.smartlife.data

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SongDatabaseHelper (private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null,DATABASE_VERSION) {
    companion object{
        // ✅ FIXED: Changed the database name to avoid conflicts with the Room database.
        private const val DATABASE_NAME = "smartlife_songs.db"
        private const val DATABASE_VERSION = 1 // Reset version for the new database

        const val TABLE_NAME = "songs"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_LINK = "link"
        const val COLUMN_MOOD = "mood"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
            // 1. Create songs table
            val createSongsTableQuery = """
                CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_LINK TEXT,
                $COLUMN_MOOD TEXT
                )
            """
            it.execSQL(createSongsTableQuery)

            // 2. Create Activity table
            val createActivityTable = """
                CREATE TABLE activities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    mood TEXT,
                    activity TEXT)
            """

            it.execSQL(createActivityTable)

            // 3. Insert all initial data when the database is first created.
            insertInitialData(it)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // For a simple app, we can just drop and recreate the tables on upgrade.
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS activities")
        onCreate(db)
    }

    private fun insertInitialData(db: SQLiteDatabase) {

        //Insert songs data
        insertSong(db, "Show me love", "https://www.youtube.com/watch?v=329iFlRszxs&list=RD329iFlRszxs&start_radio=1", "Happy")
        insertSong(db, "Birds of a feather", "https://www.youtube.com/watch?v=qGPet7oiWds&list=RDqGPet7oiWds&start_radio=1", "Happy")
        insertSong(db, "Calm Down", "https://www.youtube.com/watch?v=W50sgQV1pl0&list=RDW50sgQV1pl0&start_radio=1", "Happy")
        insertSong(db, "Unstoppable", "https://www.youtube.com/watch?v=oS07d8Gr4tw&list=RDoS07d8Gr4tw&start_radio=1", "Happy")
        insertSong(db, "Hall of fame", "https://www.youtube.com/watch?v=3Kxf2dHlDpQ&list=RD3Kxf2dHlDpQ&start_radio=1", "Happy")
        insertSong(db, "Greatest", "https://www.youtube.com/watch?v=YbWkR6mFI6s&list=RDYbWkR6mFI6s&start_radio=1", "Happy")
        insertSong(db, "Cheap Thrills", "https://www.youtube.com/watch?v=mY9fNwGE7YA&list=RDmY9fNwGE7YA&start_radio=1", "Sad")
        insertSong(db, "Shape of you", "https://www.youtube.com/watch?v=JGwWNGJdvx8&list=PLPSCssPYXhWTTcpNZwYoEQWt8Wc8KO0NV", "Sad")
        insertSong(db, "See you again", "https://www.youtube.com/watch?v=RgKAFK5djSk&list=PLPSCssPYXhWTTcpNZwYoEQWt8Wc8KO0NV&index=2", "Sad")
        insertSong(db, "Another Love", "https://www.youtube.com/watch?v=mY9fNwGE7YA&list=RDmY9fNwGE7YA&start_radio=1", "Sad")
        insertSong(db, "Say you won't let go", "https://www.youtube.com/watch?v=0yW7w8F2TVA&list=RD0yW7w8F2TVA&start_radio=1", "Sad")
        insertSong(db, "Photograph", "https://www.youtube.com/watch?v=HpphFd_mzXE&list=RDHpphFd_mzXE&start_radio=1", "Sad")
        insertSong(db, "Mood", "https://www.youtube.com/watch?v=GrAchTdepsU&list=RDGrAchTdepsU&start_radio=1", "Angry")
        insertSong(db, "Unstoppable", "https://www.youtube.com/watch?v=oS07d8Gr4tw&list=RDoS07d8Gr4tw&start_radio=1", "Angry")
        insertSong(db, "Calm Down", "https://www.youtube.com/watch?v=W50sgQV1pl0&list=RDW50sgQV1pl0&start_radio=1", "Angry")
        insertSong(db, "Disturbed", "https://www.youtube.com/watch?v=u9Dg-g7t2l4&list=RDQMVgjuDDv2JP0&start_radio=1", "Angry")
        insertSong(db, "Thunder", "https://www.youtube.com/watch?v=fKopy74weus&list=RDQMVgjuDDv2JP0&index=4", "Angry")
        insertSong(db, "Angry Too", "https://www.youtube.com/watch?v=MqekZVbtI2Q&list=RDMqekZVbtI2Q&start_radio=1", "Angry")
        insertSong(db, "Thousand Years", "https://www.youtube.com/watch?v=NZGHXy1IAHM&list=RDNZGHXy1IAHM&start_radio=1", "Romantic")
        insertSong(db, "Thinking Out Loud", "https://www.youtube.com/watch?v=XMPgVZtADtQ&list=RDXMPgVZtADtQ&start_radio=1", "Romantic")
        insertSong(db, "Baby", "https://www.youtube.com/watch?v=kffacxfA7G4&list=RDXMPgVZtADtQ&index=3", "Romantic")
        insertSong(db, "Forever Young", "https://www.youtube.com/watch?v=_jEP347F6_g&list=RD_jEP347F6_g&start_radio=1", "Romantic")
        insertSong(db, "Best Day of my Life", "https://www.youtube.com/watch?v=vJ9KFEJVISo&list=RDvJ9KFEJVISo&start_radio=1", "Romantic")
        insertSong(db, "You are the Reason", "https://www.youtube.com/watch?v=ByfFurjQDb0&list=RDByfFurjQDb0&start_radio=1", "Romantic")
        insertSong(db, "Savage Love", "https://www.youtube.com/watch?v=fRrkXJu4OeE&list=RDXMPgVZtADtQ&index=9", "Stressed")
        insertSong(db, "Senorita", "https://www.youtube.com/watch?v=Pkh8UtuejGw&list=RDXMPgVZtADtQ&index=13", "Stressed")
        insertSong(db, "Memories", "https://www.youtube.com/watch?v=o2DXt11SMNI&list=RDXMPgVZtADtQ&index=15", "Stressed")
        insertSong(db, "Stressed Out", "https://www.youtube.com/watch?v=JRqRVAUlA4E&list=RDJRqRVAUlA4E&start_radio=1", "Stressed")
        insertSong(db, "All of me", "https://www.youtube.com/watch?v=450p7goxZqg&list=PLwpFrtWg2EJF3KZy3URO7qZ3fpoZPH-ex&index=3", "Stressed")
        insertSong(db, "Ordinary", "https://www.youtube.com/watch?v=u2ah9tWTkmk&list=PLwpFrtWg2EJF3KZy3URO7qZ3fpoZPH-ex&index=7", "Stressed")
        insertSong(db, "The Lazy Song", "https://www.youtube.com/watch?v=fLexgOxsZu0&list=RDEMgEsSBB7GgTqDUsqz-X50Iw&start_radio=1&rv=o2DXt11SMNI", "Tired")
        insertSong(db, "Just the way you are", "https://www.youtube.com/watch?v=LjhCEhWiKXk&list=RDEMgEsSBB7GgTqDUsqz-X50Iw&index=16", "Tired")
        insertSong(db, "Despacito", "https://www.youtube.com/watch?v=kJQP7kiw5Fk&list=RDEM949q3ncRbPygD371mCoe9A&index=5", "Tired")
        insertSong(db, "I'm so Tired", "https://www.youtube.com/watch?v=fvjpE_wFL5A&list=RDfvjpE_wFL5A&start_radio=1", "Tired")
        insertSong(db, "I Like me Better", "https://www.youtube.com/watch?v=bnVkf-z28YU", "Tired")
        insertSong(db, "Locked Away", "https://www.youtube.com/watch?v=0m6O7vzjVro&list=RD0m6O7vzjVro&start_radio=1", "Tired")

        //Insert mood-activity data
        insertActivity(db, "Happy", "Go for a walk or dance")
        insertActivity(db, "Happy", "Dance to your favorite upbeat song.")
        insertActivity(db, "Happy", "Take fun selfies or pictures of things you love.")
        insertActivity(db, "Sad", "Watch a feel-good movie")
        insertActivity(db, "Sad", "Write in a journal about how you feel.")
        insertActivity(db, "Sad", "Hug a pillow or soft blanket for comfort.")
        insertActivity(db, "Romantic", "Plan a candlelight dinner")
        insertActivity(db, "Romantic", "Write a cute message or poem for your partner or crush.")
        insertActivity(db, "Romantic", "Listen to love songs or a romantic playlist.")
        insertActivity(db, "Stressed", "Try meditation or breathing exercise")
        insertActivity(db, "Stressed", "Squeeze a stress ball or stretch your hands.")
        insertActivity(db, "Stressed", "Write down 3 things you're grateful for.")
        insertActivity(db, "Tired", "Take a power nap or rest")
        insertActivity(db, "Tired", "Do light stretching or yoga.")
        insertActivity(db, "Tired", "Drink water and take deep breaths by the window.")
        insertActivity(db, "Angry", "Do 20 jumping jacks or punch a pillow.")
        insertActivity(db, "Angry", "Take 10 deep breaths while counting backward.")
        insertActivity(db, "Angry", "Scribble on paper or draw how you feel.")
    }

    private fun insertSong(db: SQLiteDatabase, name: String, link: String, mood: String) {
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_NAME WHERE name = ? AND link = ?", arrayOf(name, link))
        if (!cursor.moveToFirst()) {
            val values = ContentValues().apply {
                put(COLUMN_NAME, name)
                put(COLUMN_LINK, link)
                put(COLUMN_MOOD, mood)
            }
            db.insert(TABLE_NAME, null, values)
        }
        cursor.close()
    }

    private fun insertActivity(db: SQLiteDatabase, mood: String, activity: String) {
        val cursor = db.rawQuery("SELECT 1 FROM activities WHERE mood = ? AND activity = ?", arrayOf(mood, activity))
        if (!cursor.moveToFirst()) {
            val values = ContentValues().apply {
                put("mood", mood)
                put("activity", activity)
            }
            db.insert("activities", null, values)
        }
        cursor.close()
    }

    fun getSongsByMood(mood: String): List<Song> {
        val songs = mutableListOf<Song>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_MOOD = ?",
            arrayOf(mood),
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val link = getString(getColumnIndexOrThrow(COLUMN_LINK))
                songs.add(Song(name, link))
            }
            close()
        }

        return songs
    }

    fun getActivitiesByMood(mood: String): List<String> {
        val activities = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.query(
            "activities",
            arrayOf("activity"),
            "mood = ?",
            arrayOf(mood),
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val activity = getString(getColumnIndexOrThrow("activity"))
                activities.add(activity)
                println("Activity fetched: $activity")
            }
            close()
        }
        return activities
    }
}
data class Song(val name: String, val link: String)
