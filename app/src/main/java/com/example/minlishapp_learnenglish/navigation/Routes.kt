package com.example.minlishapp_learnenglish.navigation

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Login = "login"
    const val Home = "main/home"
    const val Decks = "main/decks"
    const val Learn = "main/learn"
    const val LearnDeck = "main/learn/deck/{deckId}/{mode}"
    const val Progress = "main/progress"
    const val Profile = "main/profile"
    const val CreateDeck = "decks/create"
    const val DeckDetail = "decks/{deckId}"
    const val AddWord = "decks/{deckId}/words/add"
    const val EditWord = "decks/{deckId}/items/{itemId}/edit"
    const val ReviewResults = "learning/review-results"

    fun deckDetail(deckId: Long): String = "decks/$deckId"
    fun addWord(deckId: Long): String = "decks/$deckId/words/add"
    fun editWord(deckId: Long, itemId: Long): String = "decks/$deckId/items/$itemId/edit"
    fun learnDeck(deckId: Long, mode: String = "deck_all"): String = "main/learn/deck/$deckId/$mode"
}
