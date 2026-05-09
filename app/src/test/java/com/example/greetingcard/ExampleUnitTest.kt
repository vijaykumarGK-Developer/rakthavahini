package com.example.greetingcard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtSpaceScreenTest {

    // Setup ComposeTestRule
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNextArtworkButton_updatesTitleAndArtist() {
        // 1. Setup Needed: Launch the UI
        composeTestRule.setContent {
            ArtSpaceScreen()
        }

        // 2. Initial Assertion: Verify the first artwork is displayed
        composeTestRule.onNodeWithText("Starry Night").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vincent van Gogh").assertIsDisplayed()

        // 3. Action: Tap the "Next Artwork" button
        composeTestRule.onNodeWithText("Next Artwork").performClick()

        // 4. Verification: Assert the UI updated to the second artwork
        composeTestRule.onNodeWithText("Mona Lisa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leonardo da Vinci").assertIsDisplayed()
    }
}