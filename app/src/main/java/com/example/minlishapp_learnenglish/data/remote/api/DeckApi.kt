package com.example.minlishapp_learnenglish.data.remote.api

import com.example.minlishapp_learnenglish.data.remote.dto.CreateDeckRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.CreateVocabularyItemRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.DeckListResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.DeckResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.UpdateDeckRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.UpdateVocabularyItemRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.VocabularyItemListResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.VocabularyItemResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DeckApi {
    @GET("decks")
    suspend fun getDecks(): DeckListResponseDto

    @POST("decks")
    suspend fun createDeck(@Body request: CreateDeckRequestDto): DeckResponseDto

    @GET("decks/{deck_id}")
    suspend fun getDeck(@Path("deck_id") deckId: Long): DeckResponseDto

    @PATCH("decks/{deck_id}")
    suspend fun updateDeck(
        @Path("deck_id") deckId: Long,
        @Body request: UpdateDeckRequestDto
    ): DeckResponseDto

    @DELETE("decks/{deck_id}")
    suspend fun deleteDeck(@Path("deck_id") deckId: Long)

    @GET("decks/{deck_id}/items")
    suspend fun getDeckItems(@Path("deck_id") deckId: Long): VocabularyItemListResponseDto

    @POST("decks/{deck_id}/items")
    suspend fun createDeckItem(
        @Path("deck_id") deckId: Long,
        @Body request: CreateVocabularyItemRequestDto
    ): VocabularyItemResponseDto

    @PATCH("items/{item_id}")
    suspend fun updateItem(
        @Path("item_id") itemId: Long,
        @Body request: UpdateVocabularyItemRequestDto
    ): VocabularyItemResponseDto

    @DELETE("items/{item_id}")
    suspend fun deleteItem(@Path("item_id") itemId: Long)
}
