package com.sonexa.app.data.provider

import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LiveEventsProvider {

    suspend fun getLiveEventsFeed(city: String? = null, category: String? = null): LiveEventsFeedResponse =
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.musicApiService.getLiveEventsFeed(city, category)
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getFallbackFeed(city, category)
        }

    suspend fun getLiveEventDetail(id: String): LiveEventDetailResponse =
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.musicApiService.getLiveEventDetail(id)
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getFallbackDetail(id)
        }

    suspend fun toggleReminder(id: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.musicApiService.toggleLiveEventReminder(id)
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext (resp.body()?.get("isReminderSet") as? Boolean) ?: true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            true
        }

    private fun getFallbackFeed(city: String?, category: String?): LiveEventsFeedResponse {
        val allEvents = listOf(
            LiveEventDto(
                id = "ev_diljit_mum",
                title = "Diljit Dosanjh • Dil-Luminati Tour 2026",
                artistName = "Diljit Dosanjh",
                artistImageUrl = "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg",
                bannerUrl = "https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=1200&q=80",
                venue = "Jio World Garden, BKC, Mumbai",
                city = "Mumbai",
                date = "Sat, 24 Oct 2026",
                time = "7:00 PM IST",
                priceStarting = "₹1,999",
                status = "SELLING_FAST",
                category = "Stadium Tour",
                bookingUrl = "https://in.bookmyshow.com",
                lineup = listOf("Diljit Dosanjh", "Special Guest DJ", "Live Dhol Band"),
                setlist = listOf(
                    EventSetlistTrackDto("st_1", "Lover", "Diljit Dosanjh", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "3:12", 192000),
                    EventSetlistTrackDto("st_2", "GOAT", "Diljit Dosanjh", "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "3:44", 224000),
                    EventSetlistTrackDto("st_3", "Born to Shine", "Diljit Dosanjh", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "3:33", 213000)
                )
            ),
            LiveEventDto(
                id = "ev_arijit_del",
                title = "Arijit Singh • Symphony World Tour Live",
                artistName = "Arijit Singh",
                artistImageUrl = "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg",
                bannerUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1200&q=80",
                venue = "Jawaharlal Nehru Stadium, New Delhi",
                city = "Delhi NCR",
                date = "Sun, 15 Nov 2026",
                time = "6:30 PM IST",
                priceStarting = "₹2,499",
                status = "SELLING_FAST",
                category = "Stadium Tour",
                bookingUrl = "https://in.bookmyshow.com",
                lineup = listOf("Arijit Singh", "Grand Royal Philharmonic Symphony"),
                setlist = listOf(
                    EventSetlistTrackDto("st_5", "Kesariya", "Arijit Singh", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg", "4:28", 268000),
                    EventSetlistTrackDto("st_6", "Tum Hi Ho", "Arijit Singh", "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg", "4:22", 262000)
                )
            ),
            LiveEventDto(
                id = "ev_sunburn_goa",
                title = "Sunburn Goa 2026 • Asia's Premier Dance Festival",
                artistName = "Various Global Artists",
                artistImageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=1200&q=80",
                venue = "Vagator Beach Arena, North Goa",
                city = "Goa",
                date = "28-31 Dec 2026",
                time = "3:00 PM IST Onwards",
                priceStarting = "₹3,999",
                status = "LIVE_NOW",
                category = "Festival",
                bookingUrl = "https://in.bookmyshow.com",
                lineup = listOf("Martin Garrix", "Hardwell", "Ritviz", "Lost Stories", "Nucleya"),
                setlist = listOf(
                    EventSetlistTrackDto("st_8", "Liggi", "Ritviz", "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80", "3:02", 182000)
                )
            ),
            LiveEventDto(
                id = "ev_karan_blr",
                title = "Karan Aujla • It Was All A Dream Arena Tour",
                artistName = "Karan Aujla",
                artistImageUrl = "https://c.saavncdn.com/artists/Karan_Aujla_003_20230818090514_500x500.jpg",
                bannerUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=1200&q=80",
                venue = "Manpho Convention Center Grounds, Bengaluru",
                city = "Bengaluru",
                date = "Fri, 18 Dec 2026",
                time = "7:30 PM IST",
                priceStarting = "₹1,499",
                status = "UPCOMING",
                category = "Stadium Tour",
                bookingUrl = "https://in.bookmyshow.com",
                lineup = listOf("Karan Aujla", "Ikky", "Guest Artists"),
                setlist = listOf(
                    EventSetlistTrackDto("st_10", "Tauba Tauba", "Karan Aujla", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://c.saavncdn.com/artists/Karan_Aujla_003_20230818090514_500x500.jpg", "3:27", 207000)
                )
            )
        )

        val filtered = allEvents.filter { e ->
            (city.isNullOrBlank() || city.equals("All", true) || e.city.equals(city, true)) &&
            (category.isNullOrBlank() || category.equals("All", true) || e.category.equals(category, true))
        }

        return LiveEventsFeedResponse(
            success = true,
            title = "Live Concerts & Tours",
            cities = listOf("All", "Mumbai", "Delhi NCR", "Bengaluru", "Goa", "Pune", "Hyderabad", "London", "Dubai", "New York"),
            categories = listOf("All", "Stadium Tour", "Festival", "Acoustic", "Club"),
            featuredTours = allEvents.take(3),
            events = filtered
        )
    }

    private fun getFallbackDetail(id: String): LiveEventDetailResponse {
        val feed = getFallbackFeed(null, null)
        val event = feed.events.find { it.id == id } ?: feed.events.first()
        val tiers = listOf(
            EventTicketTierDto("tier_silver", "Silver Phase 1 (General Access)", event.priceStarting, "Access to general standing zone and food court.", listOf("General Admission", "Free Parking Zone C"), true),
            EventTicketTierDto("tier_gold", "Gold Fanpit (Close to Stage)", "₹3,499", "Exclusive entrance, front-of-stage fanpit access, dedicated bar counter.", listOf("Priority Entrance", "Front of Stage Access", "1 Complimentary Drink"), true),
            EventTicketTierDto("tier_vip", "VIP Platinum Lounge", "₹7,999", "Elevated viewing deck, unlimited gourmet snacks, air-conditioned lounge & artist merchandise kit.", listOf("Elevated Deck View", "Free Flow Beverages", "Exclusive Merch Pack", "Valet Parking"), true)
        )
        return LiveEventDetailResponse(true, event, tiers, feed.events.filter { it.id != event.id })
    }
}
