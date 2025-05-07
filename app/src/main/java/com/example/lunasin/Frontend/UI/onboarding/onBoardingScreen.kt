package com.example.lunasin.Frontend.UI.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lunasin.utils.OnboardingDotsIndicator
import com.example.lunasin.utils.OnboardingNavigationButtons
import kotlinx.coroutines.launch



@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .padding(bottom = 16.dp)
        )
        Text(text = page.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = page.description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun OnboardingScreen(
    pages: List<OnboardingPage> = onboardingPages,
    onFinish: () -> Unit = {}
) {
    val pagerState = rememberPagerState { pages.size }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        OnboardingDotsIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage
        )

        OnboardingNavigationButtons(
            currentPage = pagerState.currentPage,
            pageCount = pages.size,
            onPreviousClicked = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            onNextClicked = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onFinishClicked = onFinish
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    MaterialTheme {
        OnboardingScreen()
    }
}
