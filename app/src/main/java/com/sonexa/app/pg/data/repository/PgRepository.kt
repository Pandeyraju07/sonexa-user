package com.sonexa.app.pg.data.repository

import com.sonexa.app.pg.data.api.PgApiService
import com.sonexa.app.pg.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class PgRepository(private val apiService: PgApiService) {

    suspend fun searchProperties(
        city: String? = null,
        locality: String? = null,
        query: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        genderPolicy: String? = null,
        propertyType: String? = null,
        roomType: String? = null,
        onlyAvailable: Boolean? = null,
        sortBy: String? = "RELEVANCE",
        page: Int = 0,
        size: Int = 15
    ): Result<PageData<PropertySummary>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchProperties(
                city = city,
                locality = locality,
                query = query,
                latitude = latitude,
                longitude = longitude,
                radiusKm = radiusKm,
                minPrice = minPrice,
                maxPrice = maxPrice,
                genderPolicy = genderPolicy,
                propertyType = propertyType,
                roomType = roomType,
                onlyAvailable = onlyAvailable,
                sortBy = sortBy,
                page = page,
                size = size
            )
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to fetch properties"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPropertyDetail(id: String): Result<PropertyDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPropertyDetail(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "Property not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(propertyId: String, isCurrentlyFavorite: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isCurrentlyFavorite) {
                apiService.removeFavorite(propertyId)
                Result.success(false)
            } else {
                apiService.addFavorite(propertyId)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<PropertySummary>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFavorites()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to fetch favorites"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun compareProperties(propertyIds: List<String>): Result<List<PropertyComparison>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.compareProperties(propertyIds)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to compare properties"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scheduleVisit(
        propertyId: String,
        roomTypeId: String?,
        date: String,
        timeSlot: String,
        comments: String?
    ): Result<VisitRequest> = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf(
                "propertyId" to propertyId,
                "roomTypeId" to roomTypeId,
                "preferredDate" to date,
                "preferredTimeSlot" to timeSlot,
                "userComments" to comments
            )
            val response = apiService.scheduleVisit(payload)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to schedule visit"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBooking(
        propertyId: String,
        roomTypeId: String,
        moveInDate: String,
        idempotencyKey: String
    ): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf(
                "propertyId" to propertyId,
                "roomTypeId" to roomTypeId,
                "moveInDate" to moveInDate,
                "idempotencyKey" to idempotencyKey
            )
            val response = apiService.createBooking(payload)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "Booking failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerDashboard(): Result<OwnerDashboard> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOwnerDashboard()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to fetch dashboard metrics"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
