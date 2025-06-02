package com.example.growth.ui.screens

// UI Components
import androidx.compose.foundation.Image // For displaying images
import androidx.compose.foundation.layout.* // Layout components
import androidx.compose.foundation.pager.HorizontalPager // Swipeable pages
import androidx.compose.foundation.pager.rememberPagerState // Tracks current page
// Material Design 3 components
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
// State/Nav
import androidx.compose.runtime.Composable // Marks as UI function
import androidx.compose.runtime.rememberCoroutineScope // For background animations
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource // Loads images from app resources
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // For navigation
import com.example.growth.R // Access to app resources (images, strings)
import kotlinx.coroutines.launch // For smooth page animations

// Data class for onboarding pages
// Stores content for each onboarding screen
data class OnboardingPage(
    val title: String, // Page heading
    val description: String, // Page text
    val image: Int // Reference to image file
)

// Onboarding page content composable
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column( // Arranges items vertically
        modifier = Modifier
            .fillMaxSize() // Takes full screen
            .padding(24.dp), // Adds 24dp padding
        horizontalAlignment = Alignment.CenterHorizontally, // Centers horizontally
        verticalArrangement = Arrangement.Center // Centers vertically
    ) {
        // Image (top)
        Image(
            painter = painterResource(id = page.image), // Loads image from resources
            contentDescription = null,
            modifier = Modifier
                .size(220.dp) // Fixed size
                .padding(bottom = 24.dp) // Space below image
        )

        // Title (middle)
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium, // Large font
            textAlign = TextAlign.Center, // Centers text
            modifier = Modifier.padding(bottom = 12.dp) // Space below title
        )

        // Description (bottom)
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge, // Medium font
            textAlign = TextAlign.Center // Centers text
        )
    }
}

// Main onboarding screen
@Composable
fun OnboardingScreen(
    navController: NavController, // For navigation, handles screen transitions
    // Called when onboarding finishes, defaults to navigating home
    onGetStarted: () -> Unit = { navController.navigate("home") }
) {
    val coroutineScope = rememberCoroutineScope() // For smooth page animations
    // List of onboarding pages
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

    val pagerState = rememberPagerState { pages.size } // Tracks current page (starts at 0)

    Column( // Vertical layout
        modifier = Modifier.fillMaxSize(), // Full screen
        horizontalAlignment = Alignment.CenterHorizontally // Centers children
    ) {
        // HorizontalPager (swipeable pages)
        HorizontalPager(
            state = pagerState, // Tracks current page
            modifier = Modifier.weight(1f) // Takes all available space
        ) { page -> // Renders each page
            OnboardingPageContent(page = pages[page])
        }

        // Next or Get Started button
        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) { // If not last page
                    coroutineScope.launch { // Smooth scroll animation
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else { // Last page
                    onGetStarted() // Navigate to home (or request permissions)
                }
            },
            modifier = Modifier
                .padding(bottom = 32.dp, top = 16.dp) // Spacing
                .width(200.dp) // Fixed width
        ) {
            Text(if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next")
        }
    }
}