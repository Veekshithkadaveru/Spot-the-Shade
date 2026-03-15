package app.krafted.spottheshade.ui.screens

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.krafted.spottheshade.data.model.GameResult
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import app.krafted.spottheshade.R
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.ui.navigation.Screen
import app.krafted.spottheshade.ui.screens.components.GameResultOverlay
import app.krafted.spottheshade.ui.screens.components.GridItem
import app.krafted.spottheshade.ui.screens.components.StaggeredGrid
import app.krafted.spottheshade.ui.screens.components.TopInfoPanel
import app.krafted.spottheshade.ui.screens.components.calculateGridAndItemSize
import app.krafted.spottheshade.ui.theme.LocalThemeColors
import app.krafted.spottheshade.game.GameUiEvent
import app.krafted.spottheshade.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun GameplayScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val gridShakeAnimation = remember { Animatable(0f) }
    val itemAnimations = remember { mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>() }
    var revealedTargetId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(gameState.grid) {
        val currentIds = gameState.grid.map { it.id }.toSet()

        // Clean up animations for items that no longer exist
        val itemsToRemove = itemAnimations.keys.filterNot { id -> id in currentIds }
        itemsToRemove.forEach { id ->
            itemAnimations.remove(id)
        }

        // More aggressive cleanup: Keep only current grid items to prevent unbounded growth
        if (itemAnimations.size > currentIds.size * 2) {
            // Keep only animations for current items
            val animationsToKeep = itemAnimations.filterKeys { it in currentIds }
            itemAnimations.clear()
            itemAnimations.putAll(animationsToKeep)
        }

        // Add animations for new items only with proper bounds checking
        gameState.grid.forEach { item ->
            if (!itemAnimations.containsKey(item.id)) {
                itemAnimations[item.id] = Animatable(1f)
            }
        }

        revealedTargetId = null
    }

    // Start game when screen loads
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    // Listen for UI events from the ViewModel (animations + haptics)
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is GameUiEvent.CorrectTap -> {
                    itemAnimations[event.itemId]?.let { animatable ->
                        coroutineScope.launch {
                            animatable.animateTo(1.2f, tween(100))
                            animatable.animateTo(1f, spring())
                        }
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                is GameUiEvent.LevelUp -> {
                }

                is GameUiEvent.IncorrectTap -> {
                    itemAnimations[event.itemId]?.let { animatable ->
                        coroutineScope.launch {
                            animatable.animateTo(0.8f, tween(100))
                            animatable.animateTo(1f, spring())
                        }
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is GameUiEvent.Timeout -> {
                    coroutineScope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(100)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                is GameUiEvent.GameOver -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is GameUiEvent.TimeWarning -> {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                is GameUiEvent.TimeCritical -> {
                    coroutineScope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(150)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }

                is GameUiEvent.TimeUrgent -> {
                    coroutineScope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(100)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(100)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                is GameUiEvent.RevealAnswer -> {
                    revealedTargetId = event.targetId
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
                    coroutineScope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        delay(300)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        delay(200)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            }
        }
    }

    // Professional game over transition with refined timing
    var hasNavigatedToGameOver by remember { mutableStateOf(false) }
    LaunchedEffect(gameState.gameResult) {
        if (gameState.gameResult == GameResult.GameOver && !hasNavigatedToGameOver) {
            hasNavigatedToGameOver = true
            delay(500)
            navController.navigate(
                Screen.GameOver.createRoute(
                    gameState.score
                )
            ) {
                popUpTo(Screen.MainMenu.route) {
                    inclusive = false
                }
            }
        }
    }

    // Show score popup on correct answer
    LaunchedEffect(gameState.score) {
        if (gameState.score > 0) {
            // Can add a score popup animation here if desired
        }
    }

    // Cleanup timer and animations when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetGame()
            // Clear all animations to prevent memory leaks
            itemAnimations.clear()
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
                                currentGrid.forEachIndexed { index, item ->
                                    val scale = itemAnimations[item.id]?.value ?: 1f
                                    GridItem(
                                        item = item,
                                        itemSize = itemSize,
                                        scale = scale,
                                        isRevealing = revealedTargetId == item.id,
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

            // Skip Level Button
            val isSkipInProgress by viewModel.isSkipInProgress.collectAsState()
            val context = LocalContext.current

            val isSkipEnabled = !isSkipInProgress && gameState.isGameActive && !gameState.hasSkippedLevel
            val skipGradient = if (gameState.hasSkippedLevel) {
                Brush.horizontalGradient(listOf(Color(0xFF3A3A4A), Color(0xFF4A4A5A)))
            } else {
                Brush.horizontalGradient(themeColors.buttonSecondary)
            }

            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = if (isSkipEnabled) 10.dp else 0.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = themeColors.buttonSecondary.lastOrNull() ?: Color.Transparent,
                        spotColor = themeColors.buttonSecondary.firstOrNull() ?: Color.Transparent
                    )
            ) {
                Button(
                    onClick = {
                        if (isSkipEnabled) {
                            val activity = context as? android.app.Activity
                            activity?.let { act -> viewModel.skipLevel(act) }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isSkipEnabled,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = skipGradient, shape = RoundedCornerShape(16.dp))
                            .border(
                                width = 2.dp,
                                color = if (gameState.hasSkippedLevel) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSkipInProgress) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Loading Ad...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else if (gameState.hasSkippedLevel) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = stringResource(R.string.skip_used),
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.skip_used),
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.skip_level),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color.White.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(5.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AD",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
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
        onUseExtraTime = { 
            // In a real activity context, we'd pass the actual Activity.
            // Since this is a Compose view inside an Activity, we can use LocalContext.
            val activity = it as? android.app.Activity
            activity?.let { act ->
                viewModel.useExtraTime(act)
            }
        },
        onGoToMenu = {
            navController.navigate(Screen.MainMenu.route) {
                popUpTo(Screen.MainMenu.route) {
                    inclusive = false
                }
            }
        }
    )
}
