package com.SzpontCompany.check.data.map

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Wyczerpujące testy dla funkcjonalności Map i Lokalizacji
 * Punkt 9: Mapy -> zliczanie kroków / lokalizacja znajomych
 */
class RouteRepositoryTest {

    private lateinit var routeRepository: FakeRouteRepository

    @Before
    fun setUp() {
        routeRepository = FakeRouteRepository()
    }

    // ==================== TESTY TRAS I KROKÓW ====================

    @Test
    fun `testSaveAndRetrieveRoute - should save route history`() = runTest {
        val route = Route(
            id = "route_1",
            distanceKm = 5.5,
            durationMs = 3600000, // 1 godzina
            steps = 6000
        )

        routeRepository.saveRoute(route)

        val history = routeRepository.getRoutesHistory()
        assertTrue("Historia powinna zawierać zapisaną trasę", history.any { it.id == "route_1" })
        assertEquals(5.5, history.first().distanceKm, 0.0)
    }

    @Test
    fun `testCalculateRouteStats - should estimate steps from distance`() = runTest {
        val points = listOf(
            LatLngPoint(52.2297, 21.0122),
            LatLngPoint(52.2300, 21.0150)
        )

        val routeStats = routeRepository.calculateStatsFromPoints(points)

        assertTrue("Dystans powinien być większy od 0", routeStats.distanceKm > 0)
        assertTrue("Kroki powinny zostać przeliczone z dystansu", routeStats.steps > 0)
    }

    // ==================== TESTY LOKALIZACJI ZNAJOMYCH ====================

    @Test
    fun `testShareLocationWithFriends - should update user location on map`() = runTest {
        val myLocation = LatLngPoint(50.0647, 19.9450) // Kraków
        val myUserId = "user_me"

        routeRepository.shareMyLocation(myUserId, myLocation)

        val sharedLocation = routeRepository.getLastKnownLocation(myUserId)
        assertEquals(50.0647, sharedLocation?.lat)
        assertEquals(19.9450, sharedLocation?.lng)
    }

    @Test
    fun `testGetFriendsLocations - should retrieve locations of group members`() = runTest {
        // Symulacja znajomych udostępniających lokalizację
        routeRepository.shareMyLocation("friend_1", LatLngPoint(52.0, 21.0))
        routeRepository.shareMyLocation("friend_2", LatLngPoint(52.1, 21.1))

        val friendsLocations = routeRepository.getFriendsLocations()

        assertEquals("Powinno pobrać lokalizacje dwóch znajomych", 2, friendsLocations.size)
        assertTrue(friendsLocations.any { it.userId == "friend_1" })
    }

    @Test
    fun `testLocationPrivacy - should not share location if sharing is disabled`() = runTest {
        routeRepository.setLocationSharingEnabled(false)
        val myLocation = LatLngPoint(50.0647, 19.9450)

        val result = runCatching {
            routeRepository.shareMyLocation("user_me", myLocation)
        }

        assertTrue("Powinno rzucić błąd prywatności lub zignorować, gdy udostępnianie jest wyłączone", result.isFailure || !routeRepository.isLocationShared("user_me"))
    }
}

// =========================================================================
// FAKE REPOSITORY - symuluje logikę map i znajomych
// =========================================================================

class FakeRouteRepository {
    private val routes = mutableListOf<Route>()
    private val friendLocations = mutableMapOf<String, FriendLocationData>()
    private var isSharingEnabled = true

    suspend fun saveRoute(route: Route) {
        routes.add(route)
    }

    fun getRoutesHistory(): List<Route> = routes

    fun calculateStatsFromPoints(points: List<LatLngPoint>): Route {
        val distance = points.size * 1.5 // 1.5 km na punkt
        val steps = (distance * 1300).toInt() // ok. 1300 kroków na km
        return Route(distanceKm = distance, steps = steps)
    }

    fun setLocationSharingEnabled(enabled: Boolean) {
        isSharingEnabled = enabled
    }

    fun shareMyLocation(userId: String, location: LatLngPoint) {
        if (!isSharingEnabled) throw Exception("Location sharing disabled")
        friendLocations[userId] = FriendLocationData(userId, location.lat, location.lng, System.currentTimeMillis())
    }

    fun getLastKnownLocation(userId: String): LatLngPoint? {
        val loc = friendLocations[userId] ?: return null
        return LatLngPoint(loc.lat, loc.lng)
    }

    fun getFriendsLocations(): List<FriendLocationData> = friendLocations.values.toList()

    fun isLocationShared(userId: String): Boolean = friendLocations.containsKey(userId)
}

data class FriendLocationData(
    val userId: String,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)