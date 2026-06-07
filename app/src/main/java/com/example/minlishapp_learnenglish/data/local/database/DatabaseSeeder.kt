package com.example.minlishapp_learnenglish.data.local.database

import android.content.Context
import android.util.Log
import com.example.minlishapp_learnenglish.data.local.dao.WordDao
import com.example.minlishapp_learnenglish.data.local.entity.DeckEntity
import com.example.minlishapp_learnenglish.data.local.entity.NotificationSettingsEntity
import com.example.minlishapp_learnenglish.data.local.entity.VocabularyWordEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

object DatabaseSeeder {
    private const val TAG = "DatabaseSeeder"
    private const val SEED_ASSET_FILE = "seed_vocabulary.json"
    private val catalogSeedMutex = Mutex()
    private val userSeedMutex = Mutex()

    suspend fun seedCatalogIfEmpty(context: Context, database: MinLishDatabase) {
        catalogSeedMutex.withLock {
            val deckDao = database.deckDao()
            val currentSeedDeckCount = deckDao.countSeedDecks()
            if (currentSeedDeckCount > 0) {
                Log.d(TAG, "Catalog seed skipped. Database already has $currentSeedDeckCount seed decks.")
                return@withLock
            }

            Log.d(TAG, "Catalog seed started.")
            val wordDao = database.wordDao()
            val seedDecks = readSeedDecksFromAsset(context).ifEmpty {
                Log.d(TAG, "Asset seed is missing or empty. Using fallback sample data.")
                fallbackSeedDecks()
            }

            seedDecks.forEach { seedDeck ->
                val deckId = deckDao.insertDeck(
                    DeckEntity(
                        userId = null,
                        name = seedDeck.name,
                        description = seedDeck.description,
                        tags = seedDeck.tags,
                        isSeed = true,
                        isReadOnly = false,
                        sourceName = seedDeck.sourceName,
                        sourceUnit = seedDeck.sourceUnit
                    )
                )
                seedWords(
                    deckId = deckId,
                    words = seedDeck.words,
                    wordDao = wordDao
                )
            }
            Log.d(TAG, "Catalog seed completed with ${seedDecks.size} decks.")
        }
    }

    suspend fun seedUserIfNeeded(database: MinLishDatabase, userId: Long) {
        userSeedMutex.withLock {
            val notificationDao = database.notificationSettingsDao()
            if (notificationDao.getSettings(userId) != null) {
                Log.d(TAG, "User seed skipped for userId=$userId.")
                return@withLock
            }

            notificationDao.upsertSettings(NotificationSettingsEntity(userId = userId))
            Log.d(TAG, "User seed completed for userId=$userId.")
        }
    }

    private fun readSeedDecksFromAsset(context: Context): List<SeedDeck> {
        return runCatching {
            val jsonText = context.assets.open(SEED_ASSET_FILE)
                .bufferedReader()
                .use { it.readText() }
            parseSeedDecks(JSONArray(jsonText))
        }.onFailure { error ->
            Log.d(TAG, "Could not read $SEED_ASSET_FILE: ${error.message}")
        }.getOrDefault(emptyList())
    }

    private fun parseSeedDecks(array: JSONArray): List<SeedDeck> {
        return buildList {
            for (index in 0 until array.length()) {
                val deckJson = array.optJSONObject(index) ?: continue
                val words = parseSeedWords(deckJson.optJSONArray("words") ?: JSONArray())
                if (words.isEmpty()) continue

                add(
                    SeedDeck(
                        name = deckJson.optCleanString("name") ?: "Vocabulary Unit ${index + 1}",
                        description = deckJson.optCleanString("description"),
                        tags = deckJson.optStringList("tags"),
                        sourceName = deckJson.optCleanString("sourceName") ?: "Local Seed",
                        sourceUnit = deckJson.optCleanString("sourceUnit") ?: deckJson.optCleanString("name"),
                        words = words
                    )
                )
            }
        }
    }

    private fun parseSeedWords(array: JSONArray): List<SeedWord> {
        return buildList {
            for (index in 0 until array.length()) {
                val wordJson = array.optJSONObject(index) ?: continue
                val word = wordJson.optCleanString("word")
                val meaning = wordJson.optCleanString("meaning")
                if (word == null || meaning == null) continue

                add(
                    SeedWord(
                        word = word,
                        pronunciation = wordJson.optCleanString("pronunciation"),
                        meaning = meaning,
                        description = wordJson.optCleanString("description"),
                        example = wordJson.optCleanString("example"),
                        note = wordJson.optCleanString("note"),
                        suggestion = wordJson.optCleanString("suggestion"),
                        imageUrl = wordJson.optCleanString("imageUrl"),
                        wordAudioUrl = wordJson.optCleanString("wordAudioUrl"),
                        meaningAudioUrl = wordJson.optCleanString("meaningAudioUrl"),
                        exampleAudioUrl = wordJson.optCleanString("exampleAudioUrl")
                    )
                )
            }
        }
    }

    private suspend fun seedWords(
        deckId: Long,
        words: List<SeedWord>,
        wordDao: WordDao
    ) {
        wordDao.insertWords(
            words.map { seedWord ->
                VocabularyWordEntity(
                    deckId = deckId,
                    word = seedWord.word,
                    pronunciation = seedWord.pronunciation,
                    meaning = seedWord.meaning,
                    description = seedWord.description,
                    example = seedWord.example,
                    collocation = null,
                    relatedWords = emptyList(),
                    note = seedWord.note,
                    suggestion = seedWord.suggestion,
                    imageUrl = seedWord.imageUrl,
                    wordAudioUrl = seedWord.wordAudioUrl,
                    meaningAudioUrl = seedWord.meaningAudioUrl,
                    exampleAudioUrl = seedWord.exampleAudioUrl
                )
            }
        )
    }

    private fun JSONObject.optCleanString(name: String): String? {
        return optString(name, "")
            .trim()
            .takeIf { it.isNotEmpty() && it != "null" }
    }

    private fun JSONObject.optStringList(name: String): List<String> {
        val array = optJSONArray(name) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val value = array.optString(index, "").trim()
                if (value.isNotEmpty()) add(value)
            }
        }
    }

    private fun fallbackSeedDecks(): List<SeedDeck> {
        return listOf(
            SeedDeck(
                name = "Daily Life",
                description = "Common words for everyday situations.",
                tags = listOf("beginner", "daily"),
                sourceName = "MinLish Starter",
                sourceUnit = "Unit 1: Daily Life",
                words = listOf(
                    SeedWord("hello", "/heh-loh/", "xin chao", example = "Hello, how are you?"),
                    SeedWord("water", "/waw-ter/", "nuoc", example = "I drink water every day."),
                    SeedWord("family", "/fam-uh-lee/", "gia dinh", example = "My family is very kind."),
                    SeedWord("friend", "/frend/", "ban be", example = "She is my best friend."),
                    SeedWord("school", "/skool/", "truong hoc", example = "I go to school by bike."),
                    SeedWord("breakfast", "/brek-fuhst/", "bua sang", example = "I eat breakfast at seven.")
                )
            ),
            SeedDeck(
                name = "Travel Basics",
                description = "Simple vocabulary for trips and directions.",
                tags = listOf("beginner", "travel"),
                sourceName = "MinLish Starter",
                sourceUnit = "Unit 2: Travel",
                words = listOf(
                    SeedWord("ticket", "/tik-it/", "ve", example = "I bought a train ticket."),
                    SeedWord("airport", "/air-port/", "san bay", example = "The airport is very busy."),
                    SeedWord("hotel", "/hoh-tel/", "khach san", example = "We stay at a small hotel."),
                    SeedWord("map", "/map/", "ban do", example = "Please show me the map."),
                    SeedWord("left", "/left/", "ben trai", example = "Turn left at the corner."),
                    SeedWord("right", "/rite/", "ben phai", example = "Turn right after the bank.")
                )
            ),
            SeedDeck(
                name = "Classroom English",
                description = "Useful words for learning English in class.",
                tags = listOf("beginner", "classroom"),
                sourceName = "MinLish Starter",
                sourceUnit = "Unit 3: Classroom",
                words = listOf(
                    SeedWord("book", "/book/", "quyen sach", example = "Open your book, please."),
                    SeedWord("listen", "/lis-uhn/", "lang nghe", example = "Listen to the teacher."),
                    SeedWord("repeat", "/ri-peet/", "lap lai", example = "Please repeat this sentence."),
                    SeedWord("answer", "/an-ser/", "cau tra loi", example = "Write your answer here."),
                    SeedWord("question", "/kwes-chuhn/", "cau hoi", example = "Do you have a question?"),
                    SeedWord("homework", "/hohm-wurk/", "bai tap ve nha", example = "I finished my homework.")
                )
            )
        )
    }

    private data class SeedDeck(
        val name: String,
        val description: String?,
        val tags: List<String>,
        val sourceName: String?,
        val sourceUnit: String?,
        val words: List<SeedWord>
    )

    private data class SeedWord(
        val word: String,
        val pronunciation: String?,
        val meaning: String,
        val description: String? = null,
        val example: String? = null,
        val note: String? = null,
        val suggestion: String? = null,
        val imageUrl: String? = null,
        val wordAudioUrl: String? = null,
        val meaningAudioUrl: String? = null,
        val exampleAudioUrl: String? = null
    )
}
