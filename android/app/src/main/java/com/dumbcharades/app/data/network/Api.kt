package com.dumbcharades.app.data.network

import com.dumbcharades.app.model.Deck
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.json.JsonObject

@Serializable data class DeckListResponse(@SerialName("server_version") val serverVersion: Int, val decks: List<Deck>)
@Serializable data class UserProfile(val uid: String)
@Serializable data class CustomDeckWrite(val title: String, val prompts: List<com.dumbcharades.app.model.Prompt>, val id: String? = null, @SerialName("expected_version") val expectedVersion: Int? = null)
@Serializable data class DeleteResponse(val id: String, val deleted: Boolean, val version: Int)
@Serializable data class NetworkTeam(val id: String, val name: String, val players: List<String>, val score: Int)
@Serializable data class NetworkPromptResult(@SerialName("prompt_id") val promptId: String, val text: String, val status: String, @SerialName("elapsed_seconds") val elapsedSeconds: Float)
@Serializable data class NetworkRound(val index: Int, @SerialName("active_team_id") val activeTeamId: String, @SerialName("actor_name") val actorName: String, val score: Int, val prompts: List<NetworkPromptResult>, @SerialName("game_type") val gameType: String)
@Serializable data class NetworkGameWrite(
    val id: String,
    val mode: String,
    @SerialName("game_type") val gameType: String,
    val playlist: List<String>,
    @SerialName("timer_seconds") val timerSeconds: Int,
    @SerialName("total_rounds") val totalRounds: Int?,
    @SerialName("current_round") val currentRound: Int,
    @SerialName("selected_deck_ids") val selectedDeckIds: List<String>,
    val teams: List<NetworkTeam>,
    val rounds: List<NetworkRound>,
    val completed: Boolean,
    @SerialName("updated_at") val updatedAt: String,
)

interface GameApi {
    @POST("v1/auth/bootstrap") suspend fun bootstrap(): UserProfile
    @GET("v1/decks") suspend fun decks(@Query("sinceVersion") sinceVersion: Int? = null): DeckListResponse
    @GET("v1/decks/{id}") suspend fun deck(@Path("id") id: String): Deck
    @GET("v1/custom-decks") suspend fun customDecks(@Query("include_deleted") includeDeleted: Boolean = true): List<Deck>
    @POST("v1/custom-decks") suspend fun createCustomDeck(@Body body: CustomDeckWrite): Deck
    @PUT("v1/custom-decks/{id}") suspend fun updateCustomDeck(@Path("id") id: String, @Body body: CustomDeckWrite): Deck
    @DELETE("v1/custom-decks/{id}") suspend fun deleteCustomDeck(@Path("id") id: String): DeleteResponse
    @POST("v1/game-sessions") suspend fun saveGame(@Body body: NetworkGameWrite): JsonObject
}
