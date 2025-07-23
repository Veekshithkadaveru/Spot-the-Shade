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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.spottheshade.ui.theme.LocalThemeColors
import com.example.spottheshade.viewmodel.GameUiEvent
import com.example.spottheshade.viewmodel.GameViewModel
import kotlinx.coroutines.delay
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
    val haptic = LocalHapticFeedback.current
    val hapticManager = viewModel.getHapticManager()

    val gridShakeAnimation = remember { Animatable(0f) }
    val itemAnimations = remember { mutableMapOf<Int, Animatable<Float, AnimationVector1D>>() }
    var revealedTargetId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(gameState.grid) {
        val currentIds = gameState.grid.map { it.id }.toSet()
        itemAnimations.keys.retainAll { id -> id in currentIds }

        revealedTargetId = null
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

                is GameUiEvent.LevelUp -> {
                    // Celebratory haptic feedback for level progression
                    hapticManager.levelUp(haptic)
                }

                is GameUiEvent.IncorrectTap -> {
                    // Strong haptic feedback for wrong answers
                    hapticManager.wrongTap(haptic)
                    itemAnimations[event.itemId]?.let { animatable ->
                        coroutineScope.launch {
                            animatable.animateTo(0.8f, tween(100))
                            animatable.animateTo(1f, spring())
                        }
                    }
                }

                is GameUiEvent.ShakeGrid -> {
                    // Additional haptic feedback during grid shake
                    hapticManager.gridShake(haptic)
                    coroutineScope.launch {
                        gridShakeAnimation.animateTo(15f, tween(50))
                        gridShakeAnimation.animateTo(-15f, tween(50))
                        gridShakeAnimation.animateTo(10f, tween(50))
                        gridShakeAnimation.animateTo(-10f, tween(50))
                        gridShakeAnimation.animateTo(5f, tween(50))
                        gridShakeAnimation.animateTo(0f, spring())
                    }

                }

                is GameUiEvent.Timeout -> {
                    // Double haptic pulse for timeout
                    hapticManager.timeout(haptic, coroutineScope)
                }

                is GameUiEvent.GameOver -> {
                    // Strong final haptic feedback for game over
                    hapticManager.gameOver(haptic)
                }

                is GameUiEvent.TimeWarning -> {
                    // Light warning pulse at 5 seconds
                    hapticManager.timeWarning(haptic)
                }

                is GameUiEvent.TimeCritical -> {

                    hapticManager.timeCritical(haptic, coroutineScope)
                }

                is GameUiEvent.TimeUrgent -> {

                    hapticManager.timeUrgent(haptic, coroutineScope)
                }

                is GameUiEvent.RevealAnswer -> {

                    revealedTargetId = event.targetId

                    hapticManager.answerReveal(haptic, coroutineScope)

                    coroutineScope.launch {
                        delay(200)
                        // Animate other items to fade slightly for focus
                        gameState.grid.forEach { item ->
                            if (item.id != event.targetId) {
                                itemAnimations[item.id]?.let { anim ->
                                    launch {
                                        anim.animateTo(0.7f, tween(300))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Professional game over transition with refined timing
    LaunchedEffect(gameState.gameResult) {
        if (gameState.gameResult == GameResult.GameOver) {

            delay(500)
            navController.navigate(Screen.GameOver.createRoute(gameState.score, gameState.level)) {
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

    // Natural wallpaper-like gradient background
    val themeColors = LocalThemeColors.current
    val gradientColors = themeColors.gradientColors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Score, Level, and Timer
            TopInfoPanel(
                level = gameState.level,
                score = gameState.score,
                lives = gameState.lives,
                highScore = userPreferences.highScore,
                timeRemaining = gameState.timeRemaining,
                themeColors = themeColors
            )

            // Center the grid in the remaining space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
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


                        Box(
                            modifier = Modifier
                                .size(gridSize)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(12.dp)
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
                                        isRevealing = revealedTargetId == item.id,
                                        hapticManager = hapticManager,
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
        }
    }

    GameResultOverlay(
        gameResult = gameState.gameResult,
        lives = gameState.lives,
        onContinue = { viewModel.continueAfterLifeLoss() },
        onDeclineExtraTime = { viewModel.declineExtraTime() },
        onUseExtraTime = { viewModel.useExtraTime() },
        onGoToMenu = { navController.popBackStack() }
    )
}