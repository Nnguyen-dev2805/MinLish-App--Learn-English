package com.example.minlishapp_learnenglish.navigation

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "main/home"
    const val Decks = "main/decks"
    const val Learn = "main/learn"
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
}
