package com.example.lunasin.Frontend.UI.Home


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.R
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(navController: NavController, hutangViewModel: HutangViewModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val user = FirebaseAuth.getInstance().currentUser // Ambil user untuk displayName
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
        bottomBar = { BottomNavigationBar(navController) },
        backgroundColor = MaterialTheme.colorScheme.primary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Teks "Welcome to Lunasin, [Nama Pengguna]" di atas sebelah kiri
            Text(
                text = "Welcome to Lunasin, ${user?.displayName ?: "Pengguna"}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Warna putih agar kontras dengan background primary
                ),
                modifier = Modifier
                    .padding(start = 16.dp, top = 24.dp)
                    .align(Alignment.TopStart)
            )

            Card(
                shape = RoundedCornerShape(topStartPercent = 12, topEndPercent = 12),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .align(Alignment.BottomCenter),
                elevation = 8.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SwipableDebtCards(
                        totalHutang = totalHutang,
                        totalPiutang = totalPiutang
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OptionCard("Hutang", R.drawable.ic_wallet, Color(0xFF4CAF50), Modifier.weight(1f)) {
                            navController.navigate("list_utang_screen")
                        }
                        OptionCard("Piutang", R.drawable.ic_business, Color(0xFF00BCD4), Modifier.weight(1f)) {
                            navController.navigate("list_hutang_screen")
                        }
                        OptionCard("Laporan", R.drawable.ic_chart, Color(0xFFFF9800), Modifier.weight(1f)) {
                            navController.navigate("laporan_hutang_screen")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hutang Saya (Saya berhutang ke orang lain)
                    Text(
                        "Hutang Saya",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )
                    if (hutangSaya.isEmpty()) {
                        Text("Belum ada hutang", style = MaterialTheme.typography.bodySmall)
                    } else {
                        hutangSaya.take(3).forEach { hutang ->
                            HutangItemMini(hutang)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hutang Teman Saya (Orang lain berhutang ke saya)
                    Text(
                        "Hutang Teman Saya",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )
                    if (piutangSaya.isEmpty()) {
                        Text("Belum ada piutang", style = MaterialTheme.typography.bodySmall)
                    } else {
                        piutangSaya.take(3).forEach { hutang ->
                            HutangItemMini(hutang)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipableDebtCards(
    totalHutang: Double,
    totalPiutang: Double
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    val titles = listOf("Total Hutang Anda", "Teman Berhutang ke Anda")
    val values = listOf(totalHutang, totalPiutang)
    val colors = listOf(Color(0xFF3F51B5), Color(0xFF009688))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        for (page in 0..1) {
            val offsetFactor = (page - pagerState.currentPage).toFloat()

            val animatedOffset by animateFloatAsState(
                targetValue = offsetFactor * 40f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "offset"
            )
            val animatedScale by animateFloatAsState(
                targetValue = 1f - (offsetFactor.absoluteValue * 0.05f),
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "scale"
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f - (offsetFactor.absoluteValue * 0.2f),
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        translationY = animatedOffset
                        scaleX = animatedScale
                        scaleY = animatedScale
                        alpha = animatedAlpha
                    }
                    .zIndex(1f - offsetFactor.absoluteValue)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors[page],
                    elevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(Color.White.copy(alpha = 0.05f), radius = 100f, center = Offset(300f, 40f))
                            drawCircle(Color.White.copy(alpha = 0.08f), radius = 60f, center = Offset(200f, 130f))
                        }

                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(titles[page], color = Color.White, fontSize = 16.sp)
                            Text(
                                NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(values[page]),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Perbarui informasi secara berkala",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        VerticalPager(
            state = pagerState,
            userScrollEnabled = true,
            modifier = Modifier
                .matchParentSize()
                .zIndex(2f)
                .background(Color.Transparent)
        ) {}
    }
}

@Composable
fun OptionCard(
    title: String,
    iconRes: Int,
    iconColor: Color = Color.Black,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun HutangItemMini(hutang: Hutang) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = hutang.namapinjaman,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Total Hutang: ${hutang.totalHutang?.let { String.format("%,.0f", it) } ?: "0"}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF3F51B5))
            )
            Text(
                text = "Jatuh tempo: ${hutang.tanggalBayar.ifEmpty { "Belum ditentukan" }}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Color.Gray)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem("Home", R.drawable.ic_home, "home_screen"),
            BottomNavItem("Search", R.drawable.ic_search, "search_screen"),
            BottomNavItem("Stats", R.drawable.ic_chart, "stats_screen"),
            BottomNavItem("Profile", R.drawable.ic_profile, "profile_screen")
        )

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            BottomNavigationItem(
                icon = {
                    Box(
                        modifier = if (isSelected) {
                            Modifier
                                .size(36.dp) // Ukuran lebih besar untuk background bulat
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(6.dp) // Padding agar ikon tidak terlalu besar
                        } else {
                            Modifier.size(24.dp)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // Hindari stack berulang
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// Data class untuk item navigasi
data class BottomNavItem(val label: String, val icon: Int, val route: String)
