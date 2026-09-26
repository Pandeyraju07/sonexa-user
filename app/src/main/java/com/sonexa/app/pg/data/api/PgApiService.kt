package com.sonexa.app.pg.data.api

import com.sonexa.app.pg.model.*
import retrofit2.http.*
import java.util.UUID

interface PgApiService {

    // 1. Discovery & Search
    @GET("search/properties")
    suspend fun searchProperties(
        @Query("city") city: String? = null,
        @Query("locality") locality: String? = null,
        @Query("query") query: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radiusKm") radiusKm: Double? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("genderPolicy") genderPolicy: String? = null,
        @Query("propertyType") propertyType: String? = null,
        @Query("roomType") roomType: String? = null,
        @Query("onlyAvailable") onlyAvailable: Boolean? = null,
        @Query("sortBy") sortBy: String? = "RELEVANCE",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 15
    ): PgApiResponse<PageData<PropertySummary>>

    // 2. Property Details & Room Inventory
    @GET("properties/{id}")
    suspend fun getPropertyDetail(@Path("id") id: String): PgApiResponse<PropertyDetail>

    @GET("properties/{id}/room-types")
    suspend fun getRoomTypes(@Path("id") id: String): PgApiResponse<List<RoomType>>

    @GET("properties/{id}/rooms")
    suspend fun getRooms(@Path("id") id: String): PgApiResponse<List<Room>>

    @GET("amenities")
    suspend fun getAmenities(): PgApiResponse<List<AmenityCategory>>

    // 3. Favorites
    @POST("favorites/{propertyId}")
    suspend fun addFavorite(@Path("propertyId") propertyId: String): PgApiResponse<String>

    @DELETE("favorites/{propertyId}")
    suspend fun removeFavorite(@Path("propertyId") propertyId: String): PgApiResponse<String>

    @GET("favorites")
    suspend fun getFavorites(): PgApiResponse<List<PropertySummary>>

    @GET("favorites/check/{propertyId}")
    suspend fun checkFavorite(@Path("propertyId") propertyId: String): PgApiResponse<Map<String, Boolean>>

    // 4. Comparison
    @POST("comparison")
    suspend fun compareProperties(@Body propertyIds: List<String>): PgApiResponse<List<PropertyComparison>>

    // 5. Leads & Visits & Bookings
    @POST("leads")
    suspend fun createLead(@Body payload: Map<String, Any?>): PgApiResponse<Lead>

    @POST("visits")
    suspend fun scheduleVisit(@Body payload: Map<String, Any?>): PgApiResponse<VisitRequest>

    @GET("visits/my")
    suspend fun getMyVisits(): PgApiResponse<List<VisitRequest>>

    @POST("bookings")
    suspend fun createBooking(@Body payload: Map<String, Any?>): PgApiResponse<Booking>

    @GET("bookings/my")
    suspend fun getMyBookings(): PgApiResponse<List<Booking>>

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: String): PgApiResponse<Booking>

    // 6. Reviews
    @POST("reviews")
    suspend fun createReview(@Body payload: Map<String, Any?>): PgApiResponse<Review>

    @GET("reviews/property/{propertyId}")
    suspend fun getPropertyReviews(
        @Path("propertyId") propertyId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PgApiResponse<PageData<Review>>

    // 7. Owner Operations
    @GET("owner/dashboard")
    suspend fun getOwnerDashboard(): PgApiResponse<OwnerDashboard>

    @GET("owner/properties")
    suspend fun getOwnerProperties(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PgApiResponse<PageData<PropertySummary>>

    @POST("owner/properties")
    suspend fun createOwnerProperty(@Body payload: Map<String, Any?>): PgApiResponse<PropertyDetail>

    @POST("owner/properties/{id}/room-types")
    suspend fun addRoomType(@Path("id") id: String, @Body payload: Map<String, Any?>): PgApiResponse<RoomType>

    @POST("owner/properties/{id}/rooms")
    suspend fun addRoom(@Path("id") id: String, @Body payload: Map<String, Any?>): PgApiResponse<Room>

    @POST("owner/properties/{id}/submit-verification")
    suspend fun submitForVerification(@Path("id") id: String): PgApiResponse<String>

    @POST("owner/properties/{id}/publish")
    suspend fun publishListing(@Path("id") id: String): PgApiResponse<String>

    @POST("owner/properties/{id}/pause")
    suspend fun pauseListing(@Path("id") id: String): PgApiResponse<String>

    @GET("owner/leads")
    suspend fun getOwnerLeads(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 15
    ): PgApiResponse<PageData<Lead>>

    @PATCH("owner/leads/{id}/status")
    suspend fun updateLeadStatus(
        @Path("id") id: String,
        @Query("status") status: String,
        @Query("notes") notes: String? = null
    ): PgApiResponse<Lead>

    @GET("owner/visits")
    suspend fun getOwnerVisits(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 15
    ): PgApiResponse<PageData<VisitRequest>>

    @PATCH("owner/visits/{id}/status")
    suspend fun updateVisitStatus(
        @Path("id") id: String,
        @Query("status") status: String,
        @Query("feedback") feedback: String? = null
    ): PgApiResponse<VisitRequest>

    @GET("owner/bookings")
    suspend fun getOwnerBookings(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 15
    ): PgApiResponse<PageData<Booking>>

    @POST("owner/bookings/{id}/approve")
    suspend fun approveBooking(@Path("id") id: String): PgApiResponse<Booking>

    @POST("owner/bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") id: String): PgApiResponse<Booking>

    // 8. Subscriptions
    @GET("subscriptions/plans")
    suspend fun getSubscriptionPlans(): PgApiResponse<List<SubscriptionPlan>>
}
