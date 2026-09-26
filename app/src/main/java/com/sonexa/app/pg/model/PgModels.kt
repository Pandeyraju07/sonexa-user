package com.sonexa.app.pg.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PgApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val error: PgApiError? = null
)

data class PgApiError(
    val code: String? = null,
    val message: String? = null
)

data class PageData<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 15,
    val totalElements: Long = 0,
    val totalPages: Int = 0
)

data class PropertySummary(
    val id: String,
    val name: String,
    val propertyType: String = "CO_ED",
    val genderPolicy: String = "ANY",
    val verificationStatus: String = "VERIFIED",
    val listingStatus: String = "PUBLISHED",
    @SerializedName("isVerified") val isVerified: Boolean = true,
    @SerializedName("isFeatured") val isFeatured: Boolean = false,
    val locality: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceKm: Double? = null,
    val coverImageUrl: String? = null,
    val startingRent: BigDecimal = BigDecimal.ZERO,
    val estimatedStartingTotalCost: BigDecimal = BigDecimal.ZERO,
    val totalBeds: Long = 0,
    val availableBeds: Long = 0,
    @SerializedName("isFullyOccupied") val isFullyOccupied: Boolean = false,
    val averageRating: Double = 4.5,
    val reviewCount: Long = 0,
    val topAmenities: List<String> = emptyList(),
    val availableRoomTypes: List<String> = emptyList(),
    val listingQualityScore: Int = 85
)

data class PropertyDetail(
    val id: String,
    val ownerId: String,
    val ownerName: String? = null,
    val ownerMobile: String? = null,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val name: String,
    val description: String? = null,
    val propertyType: String = "CO_ED",
    val genderPolicy: String = "ANY",
    val totalFloors: Int = 1,
    val noticePeriodDays: Int = 30,
    val lockInPeriodMonths: Int = 1,
    val verificationStatus: String = "VERIFIED",
    val listingStatus: String = "PUBLISHED",
    val listingQualityScore: Int = 85,
    @SerializedName("isFeatured") val isFeatured: Boolean = false,
    val address: PropertyAddress? = null,
    val media: List<PropertyMedia> = emptyList(),
    val rules: List<PropertyRule> = emptyList(),
    val amenities: List<Amenity> = emptyList(),
    val roomTypes: List<RoomType> = emptyList(),
    val minMonthlyRent: BigDecimal = BigDecimal.ZERO,
    val maxMonthlyRent: BigDecimal = BigDecimal.ZERO,
    val totalBeds: Long = 0,
    val availableBeds: Long = 0,
    @SerializedName("isFullyOccupied") val isFullyOccupied: Boolean = false,
    val averageRating: Double = 4.5,
    val reviewCount: Long = 0
)

data class PropertyAddress(
    val id: String? = null,
    val addressLine1: String = "",
    val addressLine2: String? = null,
    val locality: String = "",
    val landmark: String? = null,
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class PropertyMedia(
    val id: String? = null,
    val mediaUrl: String = "",
    val mediaType: String = "IMAGE",
    val category: String = "EXTERIOR",
    val displayOrder: Int = 0,
    @SerializedName("isCover") val isCover: Boolean = false
)

data class PropertyRule(
    val id: String? = null,
    val ruleTitle: String = "",
    val ruleDescription: String? = null,
    val iconName: String? = null,
    @SerializedName("isStrict") val isStrict: Boolean = false
)

data class Amenity(
    val id: String,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val name: String,
    val code: String,
    val iconUrl: String? = null,
    val isActive: Boolean = true
)

data class AmenityCategory(
    val id: String,
    val name: String,
    val code: String,
    val displayOrder: Int = 0,
    val icon: String? = null,
    val amenities: List<Amenity> = emptyList()
)

data class RoomType(
    val id: String,
    val propertyId: String? = null,
    val name: String,
    val occupancyType: String = "DOUBLE",
    val maxOccupancy: Int = 2,
    val roomSizeSqft: Int? = null,
    val baseMonthlyRent: BigDecimal = BigDecimal.ZERO,
    val securityDeposit: BigDecimal = BigDecimal.ZERO,
    val maintenanceCharge: BigDecimal? = BigDecimal.ZERO,
    val electricityPolicy: String = "METERED",
    val foodPolicy: String = "INCLUDED",
    val foodCharge: BigDecimal? = BigDecimal.ZERO,
    val laundryCharge: BigDecimal? = BigDecimal.ZERO,
    val amenities: List<Amenity> = emptyList(),
    val pricingBreakdown: PricingBreakdown? = null,
    val availableBedsCount: Long = 0
)

data class PricingBreakdown(
    val baseMonthlyRent: BigDecimal = BigDecimal.ZERO,
    val securityDeposit: BigDecimal = BigDecimal.ZERO,
    val maintenanceCharge: BigDecimal = BigDecimal.ZERO,
    val electricityPolicy: String = "METERED",
    val estimatedElectricityCharge: BigDecimal = BigDecimal.ZERO,
    val foodPolicy: String = "INCLUDED",
    val foodCharge: BigDecimal = BigDecimal.ZERO,
    val laundryCharge: BigDecimal = BigDecimal.ZERO,
    val estimatedTotalMonthlyCost: BigDecimal = BigDecimal.ZERO,
    val initialMoveInPayable: BigDecimal = BigDecimal.ZERO
)

data class Room(
    val id: String,
    val propertyId: String? = null,
    val roomTypeId: String,
    val roomTypeName: String? = null,
    val roomNumber: String,
    val floorNumber: Int = 1,
    val isActive: Boolean = true,
    val beds: List<Bed> = emptyList()
)

data class Bed(
    val id: String,
    val roomId: String? = null,
    val propertyId: String? = null,
    val bedCode: String,
    val status: String = "AVAILABLE"
)

data class Lead(
    val id: String,
    val propertyId: String,
    val propertyName: String,
    val userId: String,
    val userName: String,
    val userMobile: String,
    val roomTypeId: String? = null,
    val roomTypeName: String? = null,
    val source: String = "APP_ENQUIRY",
    val status: String = "NEW",
    val userNotes: String? = null,
    val ownerNotes: String? = null,
    val createdAt: String? = null
)

data class VisitRequest(
    val id: String,
    val propertyId: String,
    val propertyName: String,
    val propertyLocality: String? = null,
    val propertyCity: String? = null,
    val userId: String,
    val userName: String,
    val userMobile: String,
    val roomTypeId: String? = null,
    val roomTypeName: String? = null,
    val preferredDate: String,
    val preferredTimeSlot: String,
    val status: String = "REQUESTED",
    val userComments: String? = null,
    val ownerFeedback: String? = null
)

data class Booking(
    val id: String,
    val bookingReference: String,
    val propertyId: String,
    val propertyName: String,
    val propertyLocality: String? = null,
    val propertyCity: String? = null,
    val userId: String,
    val userName: String,
    val userMobile: String,
    val roomTypeId: String,
    val roomTypeName: String,
    val roomNumber: String? = null,
    val bedCode: String? = null,
    val moveInDate: String,
    val monthlyRent: BigDecimal = BigDecimal.ZERO,
    val securityDeposit: BigDecimal = BigDecimal.ZERO,
    val totalInitialAmount: BigDecimal = BigDecimal.ZERO,
    val status: String = "PENDING_OWNER_APPROVAL",
    val createdAt: String? = null
)

data class Review(
    val id: String,
    val propertyId: String,
    val userId: String,
    val userName: String,
    val userProfilePhoto: String? = null,
    val overallRating: Double = 5.0,
    val cleanlinessRating: Double? = null,
    val foodRating: Double? = null,
    val securityRating: Double? = null,
    val wifiRating: Double? = null,
    val maintenanceRating: Double? = null,
    val ownerBehaviourRating: Double? = null,
    val valueForMoneyRating: Double? = null,
    val locationRating: Double? = null,
    val reviewText: String? = null,
    @SerializedName("isVerifiedStay") val isVerifiedStay: Boolean = false,
    val createdAt: String? = null
)

data class PropertyComparison(
    val propertyId: String,
    val name: String,
    val locality: String = "",
    val city: String = "",
    val coverImageUrl: String? = null,
    val propertyType: String = "CO_ED",
    val genderPolicy: String = "ANY",
    @SerializedName("isVerified") val isVerified: Boolean = true,
    val startingRent: BigDecimal = BigDecimal.ZERO,
    val estimatedTotalMonthlyCost: BigDecimal = BigDecimal.ZERO,
    val typicalDeposit: BigDecimal = BigDecimal.ZERO,
    val roomTypeOptions: List<String> = emptyList(),
    val availableBeds: Long = 0,
    @SerializedName("isFullyOccupied") val isFullyOccupied: Boolean = false,
    val hasAc: Boolean = false,
    val hasAttachedBathroom: Boolean = false,
    val hasWifi: Boolean = false,
    val hasFoodIncluded: Boolean = false,
    val hasLaundry: Boolean = false,
    val hasParking: Boolean = false,
    val hasPowerBackup: Boolean = false,
    val hasSecurityCctv: Boolean = false,
    val hasGym: Boolean = false,
    val averageRating: Double = 4.5,
    val reviewCount: Long = 0
)

data class OwnerDashboard(
    val totalProperties: Long = 0,
    val totalBeds: Long = 0,
    val occupiedBeds: Long = 0,
    val availableBeds: Long = 0,
    val occupancyRatePercentage: Double = 0.0,
    val totalLeads: Long = 0,
    val newLeads: Long = 0,
    val upcomingVisits: Long = 0,
    val pendingBookingRequests: Long = 0,
    val monthlyListingViews: Long = 0,
    val leadToBookingConversionRate: Double = 0.0
)

data class SubscriptionPlan(
    val planType: String,
    val title: String,
    val description: String,
    val price: BigDecimal,
    val billingPeriod: String,
    val propertyLimit: Int,
    val listingLimit: Int,
    val featuredListingLimit: Int,
    val features: List<String> = emptyList(),
    @SerializedName("isPopular") val isPopular: Boolean = false
)
