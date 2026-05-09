package com.example.greetingcard

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

// ==========================================
// 1. DATA MODELS, STATE & UTILITIES
// ==========================================

const val DAY_IN_MS = 86400000L // 24 * 60 * 60 * 1000

fun formatFullDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    return formatter.format(Date(timestamp))
}

data class Donor(
    val id: Int, var name: String, var age: String, var gender: String, var group: String,
    var phone: String, var altPhone: String, var address: String, var distance: Double,
    var lat: Double, var lng: Double, var lastDonationMs: Long,
    var responseSpeed: Int, var freq: Int, var activeDaysAgo: Int,
    var calcDist: Double = 0.0, var smartScore: Double = 0.0, var isEligible: Boolean = false
)

data class Hospital(
    val id: Int, var name: String, var address: String, var lat: Double, var lng: Double,
    var email: String, var phone: String, var landline: String
)

val bloodGroups = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")

class AppState {
    var isDarkTheme by mutableStateOf(false)
    var hasOnboarded by mutableStateOf(false)
    var currentTab by mutableStateOf("search")

    // Slide Overlays
    var showUserDetail by mutableStateOf<Donor?>(null)
    var showHospitalDetail by mutableStateOf<Hospital?>(null)
    var showSettings by mutableStateOf(false)
    var showStaticPage by mutableStateOf<String?>(null)
    var showCertificate by mutableStateOf(false)
    var showEmergencySheet by mutableStateOf(false)

    // Data Setup
    val nowMs = System.currentTimeMillis()
    val myHistory = mutableStateListOf(nowMs - (100 * DAY_IN_MS))
    var myLiveLocation by mutableStateOf<Pair<Double, Double>?>(null)

    val enrolledUsers = mutableStateListOf(
        Donor(1, "Rahul Sharma", "28", "Male", "O+", "9876543210", "9876543201", "Sector 4", 5.0, 28.6139, 77.2090, nowMs - (100 * DAY_IN_MS), 5, 8, 1),
        Donor(2, "Priya Singh", "24", "Female", "A+", "9876543211", "9876543202", "Green Valley", 12.0, 28.5677, 77.2201, nowMs - (40 * DAY_IN_MS), 15, 3, 5),
        Donor(3, "Amit Kumar", "35", "Male", "O+", "9876543212", "N/A", "Highway Rd", 18.0, 28.6883, 77.2069, nowMs - (150 * DAY_IN_MS), 2, 12, 0),
        Donor(4, "Neha Gupta", "29", "Female", "O+", "9876543213", "N/A", "West End", 3.0, 28.6200, 77.2100, nowMs - (110 * DAY_IN_MS), 30, 1, 10)
    )

    val enrolledHospitals = mutableStateListOf(
        Hospital(1, "City Care Hospital", "123 Main St, Medical District", 12.9716, 77.5946, "emergency@citycare.com", "9988776655", "011-2345678"),
        Hospital(2, "Apollo Lifeline", "45 West Avenue, Suburbia", 13.0827, 80.2707, "bloodbank@apollo.com", "9988776644", "011-8765432")
    )
}

// ==========================================
// 2. MAIN ACTIVITY & THEME
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appState = remember { AppState() }
            RaktaVahiniTheme(darkTheme = appState.isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(appState)
                }
            }
        }
    }
}

@Composable
fun RaktaVahiniTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val lightColors = lightColorScheme(
        primary = Color(0xFFD32F2F),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFEBEE),
        onPrimaryContainer = Color(0xFFD32F2F),
        background = Color(0xFFF4F6F8),
        surface = Color.White,
        onBackground = Color(0xFF1D1B20),
        onSurface = Color(0xFF1D1B20),
        outline = Color(0xFFEEEEEE),
        surfaceVariant = Color(0xFFE0E0E0)
    )
    val darkColors = darkColorScheme(
        primary = Color(0xFFEF5350),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF3B1C1C),
        onPrimaryContainer = Color(0xFFEF5350),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onBackground = Color.White,
        onSurface = Color.White,
        outline = Color(0xFF333333),
        surfaceVariant = Color(0xFF2C2C2C)
    )
    MaterialTheme(colorScheme = if (darkTheme) darkColors else lightColors, content = content)
}

// ==========================================
// 3. NAVIGATION & APP ROOT
// ==========================================

@Composable
fun AppRoot(state: AppState) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else if (!state.hasOnboarded) {
        OnboardingScreen { state.hasOnboarded = true }
    } else {
        MainScreen(state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(state: AppState) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    val haptic: () -> Unit = {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    val showToast: (String) -> Unit = { msg ->
        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("🩸 Rakta-Vahini", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = { haptic(); state.showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                val tabs = listOf(
                    Triple("search", "Search", Icons.Default.Search),
                    Triple("profile", "Profile", Icons.Default.Person),
                    Triple("hospitals", "Hospitals", Icons.Default.AddCircle),
                    Triple("users", "Users", Icons.Default.Face),
                    Triple("history", "Log", Icons.Default.DateRange)
                )
                tabs.forEach { (id, title, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        selected = state.currentTab == id,
                        onClick = { haptic(); state.currentTab = id },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { haptic(); state.showEmergencySheet = true },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199))), CircleShape)
            ) { Text("🚨", fontSize = 24.sp) }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (state.currentTab) {
                "search" -> SearchTab(state, haptic, showToast)
                "profile" -> ProfileTab(state, haptic, showToast)
                "hospitals" -> HospitalsTab(state, haptic)
                "users" -> UsersTab(state, haptic)
                "history" -> HistoryTab(state, haptic)
            }

            // Overlays
            SlideOverlay(visible = state.showUserDetail != null, onBack = { state.showUserDetail = null }) {
                state.showUserDetail?.let { UserDetailScreen(it, haptic) }
            }
            SlideOverlay(visible = state.showHospitalDetail != null, onBack = { state.showHospitalDetail = null }) {
                state.showHospitalDetail?.let { HospitalDetailScreen(it, haptic) }
            }
            SlideOverlay(visible = state.showSettings, onBack = { state.showSettings = false }) {
                SettingsScreen(state, haptic)
            }
            SlideOverlay(visible = state.showStaticPage != null, onBack = { state.showStaticPage = null }) {
                state.showStaticPage?.let { StaticPageScreen(it, haptic, showToast) { state.showStaticPage = null } }
            }
            SlideOverlay(visible = state.showCertificate, onBack = { state.showCertificate = false }) {
                CertificateScreen(haptic, showToast)
            }
        }

        if (state.showEmergencySheet) {
            ModalBottomSheet(onDismissRequest = { state.showEmergencySheet = false }) {
                EmergencySOSSheet(haptic, showToast) { state.showEmergencySheet = false }
            }
        }
    }
}

// ==========================================
// 4. TABS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTab(state: AppState, haptic: () -> Unit, showToast: (String) -> Unit) {
    var selectedGroup by remember { mutableStateOf("O+") }
    var radius by remember { mutableFloatStateOf(20f) }
    var isSearching by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val fetchLocation: () -> Unit = {
        haptic()
        showToast("📍 Accessing GPS...")
        coroutineScope.launch {
            delay(800)
            state.myLiveLocation = Pair(28.6139, 77.2090)
            state.enrolledUsers.forEach { u -> u.lat += (Math.random() - 0.5) * 0.2; u.lng += (Math.random() - 0.5) * 0.2 }
            showToast("✅ Live Location Captured!")
            isSearching = true
            delay(400)
            isSearching = false
        }
    }

    val triggerSearch: () -> Unit = {
        haptic()
        coroutineScope.launch { isSearching = true; delay(400); isSearching = false }
    }

    LaunchedEffect(selectedGroup, radius) { triggerSearch() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Find Eligible Donors", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = fetchLocation,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("📍 Fetch My Live Location", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            CustomCard {
                ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = !groupExpanded }) {
                    OutlinedTextField(
                        value = selectedGroup, onValueChange = {}, readOnly = true,
                        label = { Text("Required Blood Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                        bloodGroups.forEach { bg ->
                            DropdownMenuItem(
                                text = { Text(bg) },
                                onClick = { selectedGroup = bg; groupExpanded = false; triggerSearch() }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Search Radius: ${radius.toInt()} km", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Slider(value = radius, onValueChange = { radius = it }, valueRange = 1f..100f)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("INTELLIGENT RESULTS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isSearching) {
            items(2) { SkeletonCard() }
        } else {
            val matches = state.enrolledUsers.filter { u ->
                val daysSince = (System.currentTimeMillis() - u.lastDonationMs) / DAY_IN_MS
                u.isEligible = daysSince > 90
                u.calcDist = if (state.myLiveLocation != null) calculateDistance(state.myLiveLocation!!.first, state.myLiveLocation!!.second, u.lat, u.lng) else u.distance
                u.smartScore = u.calcDist + (u.responseSpeed * 0.1) + (u.activeDaysAgo * 0.5) - (u.freq * 0.2)
                u.group == selectedGroup && u.calcDist <= radius && u.isEligible
            }.sortedBy { it.smartScore }

            if (matches.isEmpty()) {
                item { Text("No eligible donors found within ${radius.toInt()}km.", modifier = Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center, color = Color.Gray) }
            } else {
                itemsIndexed(matches) { index, donor ->
                    DonorListCard(donor, isBestMatch = index == 0) { state.showUserDetail = donor; haptic() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(state: AppState, haptic: () -> Unit, showToast: (String) -> Unit) {
    var viewType by remember { mutableStateOf("user") }

    var uName by remember { mutableStateOf("") }
    var uAge by remember { mutableStateOf("") }
    var uGender by remember { mutableStateOf("Male") }
    var uBg by remember { mutableStateOf("O+") }
    var uPhone by remember { mutableStateOf("") }
    var uAlt by remember { mutableStateOf("") }
    var uAddr by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var bgExpanded by remember { mutableStateOf(false) }

    var hName by remember { mutableStateOf("") }
    var hAddr by remember { mutableStateOf("") }
    var hLoc by remember { mutableStateOf("") }
    var hPhone by remember { mutableStateOf("") }
    var hLand by remember { mutableStateOf("") }
    var hEmail by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Registration Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            SegmentedControl(listOf("👤 Individual", "🏥 Hospital"), viewType == "user") { isUser ->
                haptic(); viewType = if(isUser) "user" else "hospital"
            }
        }

        if (viewType == "user") {
            item {
                Button(onClick = { haptic(); showToast("📍 Map location accessed.") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1976D2)), shape = RoundedCornerShape(12.dp)) { Text("📍 Map My Current Location for Form", fontWeight = FontWeight.Bold) }

                CustomCard {
                    Text("Personal Details", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = uName, onValueChange = { uName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = uAge, onValueChange = { uAge = it }, label = { Text("Age") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                        ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = uGender, onValueChange = {}, readOnly = true, label = { Text("Gender") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(10.dp))
                            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                                listOf("Male", "Female", "Other").forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { uGender = g; genderExpanded = false }) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(expanded = bgExpanded, onExpandedChange = { bgExpanded = !bgExpanded }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = uBg, onValueChange = {}, readOnly = true, label = { Text("Blood Group") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgExpanded) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(10.dp))
                            ExposedDropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                                bloodGroups.forEach { bg -> DropdownMenuItem(text = { Text(bg) }, onClick = { uBg = bg; bgExpanded = false }) }
                            }
                        }
                        OutlinedTextField(value = uPhone, onValueChange = { uPhone = it }, label = { Text("Primary Phone") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = uAlt, onValueChange = { uAlt = it }, label = { Text("Alt. Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = uAddr, onValueChange = { uAddr = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    SolidButton("Save User Profile") {
                        haptic()
                        state.enrolledUsers.add(0, Donor(System.currentTimeMillis().toInt(), uName.ifEmpty{"New User"}, uAge, uGender, uBg, uPhone, uAlt, uAddr, 5.0, 28.6, 77.2, System.currentTimeMillis() - (100 * DAY_IN_MS), 10, 0, 0))
                        showToast("✅ Registered!")
                    }
                }

                CustomCard {
                    Text("Log Donation", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Update timer to pause requests.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { haptic(); state.myHistory.add(0, System.currentTimeMillis()); state.showCertificate = true; showToast("❤️ Logged!") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF444444)), shape = RoundedCornerShape(12.dp)) { Text("I Donated Today", fontWeight = FontWeight.Bold) }
                }
            }
        } else {
            item {
                CustomCard {
                    Text("Hospital/Bank Details", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = hName, onValueChange = { hName = it }, label = { Text("Hospital Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = hAddr, onValueChange = { hAddr = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = hLoc, onValueChange = { hLoc = it }, label = { Text("Google Maps Link / GPS") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = hPhone, onValueChange = { hPhone = it }, label = { Text("Emergency Mobile") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                        OutlinedTextField(value = hLand, onValueChange = { hLand = it }, label = { Text("Landline") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = hEmail, onValueChange = { hEmail = it }, label = { Text("Official Email ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    SolidButton("Register Hospital") {
                        haptic()
                        state.enrolledHospitals.add(0, Hospital(System.currentTimeMillis().toInt(), hName.ifEmpty{"New Hospital"}, hAddr, 12.0, 77.0, hEmail, hPhone, hLand))
                        showToast("✅ Registered!")
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalsTab(state: AppState, haptic: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Verified Hospitals", fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp)) }
        itemsIndexed(state.enrolledHospitals) { _, h ->
            CustomCard(onClick = { haptic(); state.showHospitalDetail = h }) {
                Text(h.name, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("📍 ${h.address}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top=4.dp))
            }
        }
    }
}

@Composable
fun UsersTab(state: AppState, haptic: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Registered Donors", fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp)) }
        itemsIndexed(state.enrolledUsers) { _, u ->
            val daysSince = (System.currentTimeMillis() - u.lastDonationMs) / DAY_IN_MS
            val isEl = daysSince > 90
            CustomCard(onClick = { haptic(); state.showUserDetail = u }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BloodBadge(u.group)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(u.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Age: ${u.age} • ${u.gender}", fontSize = 12.sp, color = Color.Gray)
                        Text(if(isEl) "✅ Eligible" else "🚫 Cooling Period", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isEl) Color(0xFF2E7D32) else Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTab(state: AppState, haptic: () -> Unit) {
    var viewType by remember { mutableStateOf("user") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Donation Logs", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            SegmentedControl(listOf("👤 Users Log", "🏥 Hospital Log"), viewType == "user") { isUser ->
                haptic(); viewType = if(isUser) "user" else "hospital"
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (viewType == "user") {
            itemsIndexed(state.myHistory) { _, d ->
                CustomCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BloodBadge("🩸")
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("You (Personal)", fontWeight = FontWeight.Bold)
                            Text(formatFullDate(d), fontSize = 12.sp)
                        }
                    }
                }
            }
            itemsIndexed(state.enrolledUsers) { _, u ->
                CustomCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BloodBadge(u.group)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(u.name, fontWeight = FontWeight.Bold)
                            Text("Donated: ${formatFullDate(u.lastDonationMs)}", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            itemsIndexed(state.enrolledUsers) { i, u ->
                val hosp = state.enrolledHospitals[i % state.enrolledHospitals.size].name
                CustomCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BloodBadge(u.group)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(u.name, fontWeight = FontWeight.Bold)
                            Text("🏥 To: $hosp", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. DETAIL OVERLAYS
// ==========================================

@Composable
fun UserDetailScreen(u: Donor, haptic: () -> Unit) {
    val context = LocalContext.current
    val daysSince = (System.currentTimeMillis() - u.lastDonationMs) / DAY_IN_MS
    val isEl = daysSince > 90

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        BloodBadge(u.group, size = 80.dp, textSize = 32.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(u.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Age: ${u.age} • Gender: ${u.gender}", fontWeight = FontWeight.Bold, color = Color.Gray)
        Box(modifier = Modifier.padding(top=8.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("★ ${u.freq} Lifetime Donations", fontSize = 10.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth().background(if(isEl) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), RoundedCornerShape(8.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
            Text(if(isEl) "✅ Eligible" else "🚫 Not Eligible", color = if(isEl) Color(0xFF2E7D32) else Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomCard {
            Text("📍 Address: ${u.address}")
            Text("🕒 Last Donated: ${formatFullDate(u.lastDonationMs)}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("📱 Primary Mobile: +91 ${u.phone}")
            Text("📞 Alternate Mobile: +91 ${u.altPhone}")
        }

        Spacer(modifier = Modifier.height(16.dp))
        SolidButton("📞 Secure Call") {
            haptic()
            context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${u.phone}".toUri()))
        }
    }
}

@Composable
fun HospitalDetailScreen(h: Hospital, haptic: () -> Unit) {
    val context = LocalContext.current
    Text(h.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    CustomCard {
        Text("📍 Address: ${h.address}", modifier = Modifier.padding(bottom = 8.dp))
        Text("🌍 Map Coordinates: ${h.lat} N, ${h.lng} E", modifier = Modifier.padding(bottom = 8.dp))
        Text("✉️ Email: ${h.email}", modifier = Modifier.padding(bottom = 8.dp))
        Text("📱 Mobile: ${h.phone}", modifier = Modifier.padding(bottom = 8.dp))
        Text("☎️ Landline: ${h.landline}")
    }
    Spacer(modifier = Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SolidButton("📞 Mobile", modifier = Modifier.weight(1f)) {
            haptic(); context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${h.phone}".toUri()))
        }
        OutlinedButton("☎️ Landline", modifier = Modifier.weight(1f)) {
            haptic(); context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${h.landline}".toUri()))
        }
    }
}

@Composable
fun SettingsScreen(state: AppState, haptic: () -> Unit) {
    CustomCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("🌙 Dark Mode", fontWeight = FontWeight.Bold)
            Switch(checked = state.isDarkTheme, onCheckedChange = { haptic(); state.isDarkTheme = it })
        }
    }
    val pages = listOf("ℹ️ About Us" to "about", "❓ FAQ" to "faq", "🔒 Terms & Privacy" to "privacy", "🎧 Contact Support" to "support")
    pages.forEach { (title, id) ->
        CustomCard(onClick = { haptic(); state.showStaticPage = id }) { Text(title, fontWeight = FontWeight.Bold) }
    }
    CustomCard(onClick = { haptic(); state.showStaticPage = "report" }) { Text("⚠️ Report Issue", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) }
}

@Composable
fun StaticPageScreen(page: String, haptic: () -> Unit, showToast: (String) -> Unit, onClose: () -> Unit) {
    Column {
        when (page) {
            "about" -> { Text("About Rakta-Vahini", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Built to eliminate noise in blood emergencies using GenAI and smart location matching. We save lives.") }
            "faq" -> { Text("FAQ", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Q: How is eligibility calculated?\nA: A strict 90-day cooling period is enforced automatically.\n\nQ: Is my number public?\nA: No, calls are routed via Intents.") }
            "privacy" -> { Text("Privacy Policy", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("We do not sell data. Location is used solely for haversine emergency matching.") }
            "support", "report" -> {
                Text("Support Ticket", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = "", onValueChange = {}, placeholder = { Text("Describe your issue...") }, modifier = Modifier.fillMaxWidth().height(150.dp))
                Spacer(modifier = Modifier.height(16.dp))
                SolidButton("Submit") { haptic(); onClose(); showToast("Ticket Submitted.") }
            }
        }
    }
}

@Composable
fun CertificateScreen(haptic: () -> Unit, showToast: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Donation Logged!", fontSize = 24.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().border(8.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp)).background(Color.White).padding(20.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Certificate of Heroism", color = Color(0xFFD32F2F), fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Presented by Rakta-Vahini Network", fontSize = 12.sp, color = Color.Gray)
                Text("🏅", fontSize = 60.sp, modifier = Modifier.padding(vertical = 20.dp))
                Text("Life Saver", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Awarded for donating blood on", color = Color.Black)
                Text(formatFullDate(System.currentTimeMillis()), fontWeight = FontWeight.Bold, color = Color.Black)
                Text("ID: RV-${System.currentTimeMillis()}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top=30.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SolidButton("📥 Save PDF Certificate") { haptic(); showToast("Certificate saved to Downloads!") }
        Text("Eligibility timer reset to 90 days.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top=10.dp))
    }
}

// ==========================================
// 6. SHARED UI COMPONENTS
// ==========================================

@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFD32F2F)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🩸", fontSize = 80.sp)
            Text("Rakta-Vahini", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Intelligent Blood Network", color = Color.White)
        }
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Save Lives Digitally", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
        Text("🤝", fontSize = 80.sp, modifier = Modifier.padding(vertical = 40.dp))
        Text("Welcome to the next-gen blood donation network. Experience smart matching, live tracking, and digital certificates.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(40.dp))
        SolidButton("Get Started") { onFinish() }
    }
}

@Composable
fun SlideOverlay(visible: Boolean, onBack: () -> Unit, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFD32F2F)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Text("Details", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) { content() }
        }
    }
}

@Composable
fun CustomCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val modifier = if (onClick != null) Modifier.fillMaxWidth().clickable { onClick() } else Modifier.fillMaxWidth()
    Card(modifier = modifier.padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun BloodBadge(group: String, size: androidx.compose.ui.unit.Dp = 50.dp, textSize: androidx.compose.ui.unit.TextUnit = 18.sp) {
    Box(modifier = Modifier.size(size).background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Text(group, color = Color(0xFFD32F2F), fontWeight = FontWeight.Black, fontSize = textSize)
    }
}

@Composable
fun SolidButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
fun OutlinedButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth(), border = BorderStroke(2.dp, Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
fun SegmentedControl(items: List<String>, isFirstActive: Boolean, onTabSwitched: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE), RoundedCornerShape(10.dp)).padding(4.dp)) {
        items.forEachIndexed { index, title ->
            val isActive = if (index == 0) isFirstActive else !isFirstActive
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isActive) Color.White else Color.Transparent).clickable { onTabSwitched(index == 0) }.padding(12.dp), contentAlignment = Alignment.Center) {
                Text(title, color = if (isActive) Color(0xFFD32F2F) else Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DonorListCard(donor: Donor, isBestMatch: Boolean, onClick: () -> Unit) {
    CustomCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloodBadge(donor.group)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(donor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (isBestMatch) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("🌟 Best Match", fontSize = 10.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("📍 ${donor.calcDist} km away (Live)", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(initialValue = 0f, targetValue = 1000f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1500, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "")
    val brush = Brush.linearGradient(colors = listOf(Color(0xFFF0F0F0), Color(0xFFE0E0E0), Color(0xFFF0F0F0)), start = Offset.Zero, end = Offset(x = translateAnim, y = translateAnim))

    CustomCard {
        Row {
            Box(modifier = Modifier.size(50.dp).background(brush, RoundedCornerShape(12.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(brush, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).background(brush, RoundedCornerShape(4.dp)))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencySOSSheet(haptic: () -> Unit, showToast: (String) -> Unit, onDismiss: () -> Unit) {
    var sosGroup by remember { mutableStateOf("O+") }
    var groupExpanded by remember { mutableStateOf(false) }
    var sosUnits by remember { mutableStateOf("1") }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Emergency Broadcast", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
        Text("This will send an SOS push notification to eligible donors and verified hospitals in your radius.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = !groupExpanded }) {
            OutlinedTextField(value = sosGroup, onValueChange = {}, readOnly = true, label = { Text("Needed Blood Group") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(10.dp))
            ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                bloodGroups.forEach { bg -> DropdownMenuItem(text = { Text(bg) }, onClick = { sosGroup = bg; groupExpanded = false }) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = sosUnits, onValueChange = { sosUnits = it }, label = { Text("Units Required") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { haptic(); onDismiss(); showToast("🚨 SOS Broadcasted! Pushing to nearby $sosGroup donors...") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199)))).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("🚨 Broadcast SOS Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// Distance utility
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
    return String.format(Locale.US, "%.1f", r * 2 * atan2(sqrt(a), sqrt(1 - a))).toDouble()
}

// ==========================================
// 7. PREVIEW SECTION
// ==========================================g

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() { RaktaVahiniTheme(false) { SplashScreen() } }

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen() { RaktaVahiniTheme(false) { OnboardingScreen {} } }

@Preview(showBackground = true)
@Composable
fun PreviewSearchTab() { RaktaVahiniTheme(false) { SearchTab(AppState(), {}, {}) } }

@Preview(showBackground = true)
@Composable
fun PreviewProfileTab() { RaktaVahiniTheme(false) { ProfileTab(AppState(), {}, {}) } }

@Preview(showBackground = true)
@Composable
fun PreviewHospitalsTab() { RaktaVahiniTheme(false) { HospitalsTab(AppState()) {} } }

@Preview(showBackground = true)
@Composable
fun PreviewUsersTab() { RaktaVahiniTheme(false) { UsersTab(AppState()) {} } }

@Preview(showBackground = true)
@Composable
fun PreviewHistoryTab() { RaktaVahiniTheme(false) { HistoryTab(AppState()) {} } }

@Preview(showBackground = true)
@Composable
fun PreviewUserDetail() {
    RaktaVahiniTheme(false) {
        val dummyDonor = Donor(1, "Rahul Sharma", "28", "Male", "O+", "9876543210", "9876543201", "Sector 4", 5.0, 28.6, 77.2, System.currentTimeMillis() - (100 * DAY_IN_MS), 5, 8, 1)
        UserDetailScreen(dummyDonor) {}
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmergencySheet() { RaktaVahiniTheme(false) { EmergencySOSSheet({}, {}, {}) } }

@Preview(showBackground = true)
@Composable
fun PreviewCertificate() { RaktaVahiniTheme(false) { CertificateScreen({}, {}) } }