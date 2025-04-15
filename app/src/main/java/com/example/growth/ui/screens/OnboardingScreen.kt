package com.example.growth.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.growth.R
import kotlinx.coroutines.launch

// Data class for onboarding pages
data class OnboardingPage(
    val title: String,
    val description: String,
    val image: Int // TODO: Put these drawables in resources
)

// Onboarding page content composable
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
            painter = painterResource(id = page.image),
            contentDescription = null,
            modifier = Modifier
                .size(220.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

// Main onboarding screen
@Composable
fun OnboardingScreen(
    navController: NavController,
    onGetStarted: () -> Unit = { navController.navigate("home") }
) {
    val coroutineScope = rememberCoroutineScope()
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Growth",
            description = "Track your plant's journey from seedling to bloom",
            image = R.drawable.ic_onboarding_1
        ),
        OnboardingPage(
            title = "Capture Progress",
            description = "Take regular photos to create beautiful time-lapses",
            image = R.drawable.ic_onboarding_2
        ),
        OnboardingPage(
            title = "Permission Request",
            description = "We need camera and storage access to save your plant photos",
            image = R.drawable.ic_onboarding_3
        )
    )

    val pagerState = rememberPagerState { pages.size }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    // Request permissions and navigate to home
                    onGetStarted()
                }
            },
            modifier = Modifier
                .padding(bottom = 32.dp, top = 16.dp)
                .width(200.dp)
        ) {
            Text(if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next")
        }
    }
}