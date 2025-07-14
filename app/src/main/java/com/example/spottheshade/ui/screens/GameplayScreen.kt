package com.example.spottheshade.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.navigation.Screen
import com.example.spottheshade.ui.screens.components.GameResultOverlay
import com.example.spottheshade.ui.screens.components.GridItem
import com.example.spottheshade.ui.screens.components.StaggeredGrid
import com.example.spottheshade.ui.screens.components.TopInfoPanel
import com.example.spottheshade.ui.screens.components.calculateGridAndItemSize
import com.example.spottheshade.viewmodel.GameUiEvent
import com.example.spottheshade.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun GameplayScreen(
    navController: NavHostController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    val gridShakeAnimation = remember { Animatable(0f) }
    val itemAnimations = remember { mutableMapOf<Int, Animatable<Float, AnimationVector1D>>() }

    LaunchedEffect(gameState.grid) {
        val currentIds = gameState.grid.map { it.id }.toSet()
        itemAnimations.keys.retainAll { id -> id in currentIds }
    }

    gameState.grid.forEach { item ->
        itemAnimations.putIfAbsent(item.id, Animatable(1f))
    }

    // Start game when screen loads
    LaunchedEffect(key1 = true) {
        viewModel.startGame()
    }

    // Listen for UI events from the ViewModel
    LaunchedEffect(key1 = viewModel.uiEvents) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is GameUiEvent.CorrectTap -> {
                    itemAnimations[event.itemId]?.let { animatable ->
                        coroutineScope.launch {
                            animatable.animateTo(1.2f, tween(100))
                            animatable.animateTo(1f, spring())
                        }
                    }
                }
                is GameUiEvent.IncorrectTap -> {
                    itemAnimations[event.itemId]?.let { animatable ->
                        coroutineScope.launch {
                            animatable.animateTo(0.8f, tween(100))
                            animatable.animateTo(1f, spring())
                        }
                    }
                }
                is GameUiEvent.ShakeGrid -> {
                    coroutineScope.launch {
                        gridShakeAnimation.animateTo(15f, tween(50))
                        gridShakeAnimation.animateTo(-15f, tween(50))
                        gridShakeAnimation.animateTo(10f, tween(50))
                        gridShakeAnimation.animateTo(-10f, tween(50))
                        gridShakeAnimation.animateTo(5f, tween(50))
                        gridShakeAnimation.animateTo(0f, spring())
                    }
                }
            }
        }
    }

    // Navigate to Game Over screen when game ends
    LaunchedEffect(gameState.gameResult) {
        if (gameState.gameResult == GameResult.GameOver) {
            navController.navigate(Screen.GameOver.createRoute(gameState.score, gameState.level)) {
                // Clear the back stack so user can't go back to gameplay
                popUpTo(Screen.MainMenu.route) { inclusive = false }
            }
        }
    }
    
    // Show score popup on correct answer
    LaunchedEffect(gameState.score) {
        if (gameState.score > 0) {
            // Can add a score popup animation here if desired
        }
    }

    // Cleanup timer when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetGame()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Score, Level, and Timer
        TopInfoPanel(
            level = gameState.level,
            score = gameState.score,
            lives = gameState.lives,
            highScore = userPreferences.highScore,
            timeRemaining = gameState.timeRemaining,
        )

        // Game Grid with Dynamic Sizing and Transitions
        AnimatedContent(
            targetState = gameState.grid,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(400))) togetherWith
                fadeOut(animationSpec = tween(300))
            },
            label = "gridTransition"
        ) { currentGrid ->
            if (currentGrid.isNotEmpty()) {
                val columns = sqrt(currentGrid.size.toDouble()).toInt()
                val (gridSize, itemSize) = calculateGridAndItemSize(columns)

                // Grid container with shake animation
                Box(
                    modifier = Modifier
                        .size(gridSize)
                        .graphicsLayer {
                            translationX = gridShakeAnimation.value
                        }
                ) {
                    // Grid content
                    StaggeredGrid(columns = columns) {
                        currentGrid.forEach { item ->
                            val scale = itemAnimations[item.id]?.value ?: 1f
                            GridItem(
                                item = item,
                                itemSize = itemSize,
                                scale = scale,
                                onTapped = {
                                    if (gameState.isGameActive) {
                                        viewModel.onGridItemTapped(item.id)
                                }
                            }
                            )
                        }
                    }
                }
            }
        }
        }

    // Show result overlay
    GameResultOverlay(
        gameResult = gameState.gameResult,
        lives = gameState.lives,
        onContinue = { viewModel.continueAfterLifeLoss() },
        onDeclineExtraTime = { viewModel.declineExtraTime() },
        onUseExtraTime = { viewModel.useExtraTime() },
        onGoToMenu = { navController.popBackStack() }
    )
}