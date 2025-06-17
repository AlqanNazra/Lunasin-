package com.example.lunasin.Frontend.UI.Home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Frontend.UI.Navigation.Screen
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.ViewModel.Hutang.PiutangViewModel
import com.example.lunasin.Frontend.ViewModel.Profile.ProfileViewModel
import com.example.lunasin.Frontend.ViewModel.Profile.ProfileViewModelFactory
import com.example.lunasin.R
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.example.lunasin.Frontend.UI.Navigation.BottomNavigationBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    hutangViewModel: HutangViewModel,
    piutangViewModel: PiutangViewModel,
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val user = FirebaseAuth.getInstance().currentUser
    val hutangSaya by hutangViewModel.hutangSayaList.collectAsState()
    val piutangSaya by piutangViewModel.piutangSayaList.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val totalHutang = hutangSaya.sumOf { it.totalHutang ?: 0.0 }
    val totalPiutang = piutangSaya.sumOf { it.totalHutang ?: 0.0 }

    // Arahkan ke login jika pengguna tidak terautentikasi
    LaunchedEffect(userId) {
        if (userId.isEmpty()) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            hutangViewModel.ambilHutangSaya(userId)
            piutangViewModel.ambilPiutangSaya(userId)
        }
    }

    // Tangani pesan error dari ProfileViewModel
    LaunchedEffect(profileViewModel.errorMessage) {
        profileViewModel.errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                profileViewModel.errorMessage = null
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, snackbarHostState) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.primary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Teks "Welcome to Lunasin, [Nama dari Firestore]"
            Text(
                text = "Welcome to Lunasin, ${profileViewModel.name.ifEmpty { user?.displayName ?: "Pengguna" }}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
//                        OptionCard("Laporan", R.drawable.ic_chart, Color(0xFFFF9800), Modifier.weight(1f)) {
//                            coroutineScope.launch {
//                                navController.navigate("stats_screen/$userId")
//                            }
//                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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

                    Text(
                        "Piutang Saya",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = colors[page])
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
                                "Rp ${String.format("%,.0f", values[page])}",
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
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = hutang.namapinjaman,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Total Hutang: Rp ${String.format("%,.0f", hutang.totalHutang)}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF3F51B5))
            )
            Text(
                text = "Jatuh tempo: ${hutang.tanggalBayar.ifEmpty { "Belum ditentukan" }}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Color.Gray)
            )
        }
    }
}