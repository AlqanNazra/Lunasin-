package com.example.lunasin.Frontend.UI.Home

// DIUBAH: Menambahkan import untuk SimpleDateFormat dan Date
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.R
import com.example.lunasin.utils.formatRupiah
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, hutangViewModel: HutangViewModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val user = FirebaseAuth.getInstance().currentUser
    val hutangSaya by hutangViewModel.hutangSayaList.collectAsState()
    val piutangSaya by hutangViewModel.piutangSayaList.collectAsState()

    val totalHutang = hutangSaya.sumOf { it.totalHutang ?: 0.0 }
    val totalPiutang = piutangSaya.sumOf { it.totalHutang ?: 0.0 }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            hutangViewModel.ambilHutangSaya(userId)
            hutangViewModel.ambilPiutangSaya(userId)
        }
    }

    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController) },
        containerColor = MaterialTheme.colorScheme.primary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Welcome to Lunasin, ${user?.displayName ?: "Pengguna"}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(start = 24.dp, top = 24.dp)
                    .align(Alignment.TopStart)
            )

            Card(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .align(Alignment.BottomCenter),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    SwipableDebtCards(
                        totalHutang = totalHutang,
                        totalPiutang = totalPiutang
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OptionCard("Hutang", R.drawable.ic_wallet, MaterialTheme.colorScheme.error, Modifier.weight(1f)) {
                            navController.navigate("list_utang_screen")
                        }
                        OptionCard("Piutang", R.drawable.ic_business, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                            navController.navigate("list_hutang_screen")
                        }
                        OptionCard("Laporan", R.drawable.ic_chart, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) {
                            navController.navigate("statistic_screen")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Hutang Saya", style = MaterialTheme.typography.titleLarge)
                    if (hutangSaya.isEmpty()) {
                        Text("Belum ada hutang", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        hutangSaya.take(3).forEach { hutang -> HutangItemMini(hutang) }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Hutang Teman Saya", style = MaterialTheme.typography.titleLarge)
                    if (piutangSaya.isEmpty()) {
                        Text("Belum ada piutang", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        piutangSaya.take(3).forEach { hutang -> HutangItemMini(hutang) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipableDebtCards(totalHutang: Double, totalPiutang: Double) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val titles = listOf("Total Hutang Anda", "Teman Berhutang ke Anda")
    val values = listOf(totalHutang, totalPiutang)
    val colors = listOf(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.primaryContainer)
    val onColors = listOf(MaterialTheme.colorScheme.onErrorContainer, MaterialTheme.colorScheme.onPrimaryContainer)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        for (page in 0..1) {
            val offsetFactor = (page - pagerState.currentPage).toFloat() + pagerState.currentPageOffsetFraction
            val animatedScale by animateFloatAsState(targetValue = 1f - (offsetFactor.absoluteValue * 0.2f), label = "")
            val animatedAlpha by animateFloatAsState(targetValue = 1f - (offsetFactor.absoluteValue * 0.5f), label = "")

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors[page]),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        alpha = animatedAlpha
                    }
                    .zIndex(1f - offsetFactor.absoluteValue)
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text(titles[page], color = onColors[page], style = MaterialTheme.typography.titleMedium)
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(values[page]),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = onColors[page]
                        )
                        Text(
                            "Geser ke atas/bawah untuk melihat",
                            style = MaterialTheme.typography.bodySmall,
                            color = onColors[page].copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .matchParentSize()
                .zIndex(2f)
        ) {
            // Biarkan kosong
        }
    }
}

@Composable
fun OptionCard(title: String, iconRes: Int, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(title, style = MaterialTheme.typography.labelLarge)
    }
}

// DIUBAH: Membuat fungsi helper untuk format tanggal
private fun formatDate(date: Date?): String {
    if (date == null) return "Belum ditentukan"
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
    return formatter.format(date)
}

@Composable
fun HutangItemMini(hutang: Hutang) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = hutang.namapinjaman,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total Hutang: ${hutang.totalHutang?.let { formatRupiah(it) } ?: "Rp0,00"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                // DIUBAH: Memanggil fungsi formatDate yang baru, bukan .ifBlank
                text = "Jatuh tempo: ${formatDate(hutang.tanggalBayar)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val items = listOf(
            BottomNavItem("Home", R.drawable.ic_home, "home_screen"),
            BottomNavItem("Search", R.drawable.ic_search, "search_screen"),
            BottomNavItem("Stats", R.drawable.ic_chart, "statistic_screen"),
            BottomNavItem("Profile", R.drawable.ic_profile, "profile_screen")
        )

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { Icon(painter = painterResource(id = item.icon), contentDescription = item.label) },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: Int, val route: String)