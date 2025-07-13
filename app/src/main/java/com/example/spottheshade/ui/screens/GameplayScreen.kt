package com.example.spottheshade.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.ShapeType
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.navigation.Screen
import com.example.spottheshade.ui.theme.SpotTheShadeTheme
import com.example.spottheshade.viewmodel.GameUiEvent
import com.example.spottheshade.viewmodel.GameViewModel
import com.example.spottheshade.viewmodel.GameViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.sqrt

@Composable
fun GameplayScreen(
    navController: NavHostController,
    viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(LocalContext.current))
) {
    val gameState by viewModel.gameState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    val gridShakeAnimation = remember { Animatable(0f) }
    val itemAnimations = remember { mutableMapOf<Int, Animatable<Float, AnimationVector1D>>() }

    // This effect cleans up animations for items that are no longer in the grid to prevent memory leaks.
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
            currentShape = gameState.currentShape,
            score = gameState.score,
            lives = gameState.lives,
            highScore = userPreferences.highScore,
            highestLevel = userPreferences.highestLevel,
            timeRemaining = gameState.timeRemaining,
            hasUsedExtraTime = gameState.hasUsedExtraTime
        )

        // Game Grid with Dynamic Sizing and Transitions
        AnimatedContent(
            targetState = gameState.level,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(400))) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "gridTransition"
        ) { currentLevel ->
            if (gameState.grid.isNotEmpty()) {
                val columns = sqrt(gameState.grid.size.toDouble()).toInt()
                val (gridSize, itemSize) = calculateGridAndItemSize(columns, gameState.level)

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
                        gameState.grid.forEach { item ->
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

@Composable
fun TopInfoPanel(
    level: Int,
    currentShape: ShapeType,
    score: Int,
    lives: Int,
    highScore: Int,
    highestLevel: Int,
    timeRemaining: Int,
    hasUsedExtraTime: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Level: $level",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            val difficulty = when {
                level <= 10 -> "Easy"
                level <= 25 -> "Medium"
                level <= 40 -> "Hard"
                level <= 55 -> "Expert"
                level <= 70 -> "Master"
                else -> "Legendary"
            }
            Text(
                text = difficulty,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            // Animated score
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(200))
                },
                label = "score"
            ) { targetScore ->
                Text(
                    text = "Score: $targetScore",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Best: $highScore",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }

    // Timer and Lives
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Lives: ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            // Animated hearts
            Row {
                for (i in 1..3) {
                    val filled = i <= lives
                    Crossfade(
                        targetState = filled,
                        animationSpec = tween(500),
                        label = "heart"
                    ) { isFilled ->
                        Text(
                            text = if (isFilled) "❤️" else "🤍",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Timer with pulse animation when time is low
        val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
        val pulseScale = if (timeRemaining in 1..5) {
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                ), label = "pulse"
            ).value
        } else {
            1f
        }

        Text(
            text = "Time: ${timeRemaining}s",
            style = MaterialTheme.typography.titleLarge,
            color = when {
                timeRemaining <= 5 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
        )
    }
}

@Composable
fun StaggeredGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val itemWidth = constraints.maxWidth / columns
        val itemConstraints = constraints.copy(
            minWidth = itemWidth,
            maxWidth = itemWidth
        )
        val placeables = measurables.map { measurable -> measurable.measure(itemConstraints) }
        val height = placeables.maxOfOrNull { placeable -> placeable.height } ?: 0
        
        val rows = (placeables.size + columns - 1) / columns
        layout(constraints.maxWidth, height * rows) {
            var x = 0
            var y = 0
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x, y)
                if ((index + 1) % columns == 0) {
                    x = 0
                    y += height
                } else {
                    x += itemWidth
                }
            }
        }
    }
}

@Composable
fun GridItem(
    item: com.example.spottheshade.data.model.GridItem,
    itemSize: Dp,
    scale: Float,
    onTapped: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(itemSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(onClick = onTapped)
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawShape(item.shape, item.color, size.minDimension)
        }
    }
}

@Composable
fun GameResultOverlay(
    gameResult: GameResult?,
    lives: Int,
    onContinue: () -> Unit,
    onDeclineExtraTime: () -> Unit,
    onUseExtraTime: () -> Unit,
    onGoToMenu: () -> Unit
) {
    AnimatedVisibility(
        visible = gameResult in listOf(GameResult.Wrong, GameResult.Timeout, GameResult.OfferContinue),
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                .clickable(enabled = false, onClick = {}), // Block background clicks
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (gameResult) {
                            GameResult.Wrong -> "Wrong Shade!"
                            GameResult.Timeout -> "Time's Up!"
                            GameResult.OfferContinue -> "Use a life to continue?"
                            else -> ""
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Show options based on result
                    when (gameResult) {
                        GameResult.Wrong -> {
                            Button(
                                onClick = onContinue,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Try Again")
                            }
                        }
                        GameResult.Timeout -> {
                            Text(
                                text = "Watch an ad for 5 extra seconds?",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = onUseExtraTime) {
                                    Text("Get Extra Time")
                                }
                                Button(
                                    onClick = onDeclineExtraTime,
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text("No Thanks")
                                }
                            }
                        }
                        GameResult.OfferContinue -> {
                            Text(
                                text = "You have $lives ${if (lives == 1) "life" else "lives"} left.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = onContinue) {
                                    Text("Continue")
                                }
                                Button(
                                    onClick = onGoToMenu,
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text("Main Menu")
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun calculateGridAndItemSize(columns: Int, level: Int): Pair<Dp, Dp> {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val gridPadding = 32.dp // Total padding for the grid on screen
    val availableWidth = screenWidth - gridPadding

    val itemPadding = 4.dp * 2 // Padding inside each grid item
    val maxItemSize = (availableWidth / columns) - itemPadding

    // Dynamically adjust size based on level progression
    val baseSize = when {
        columns <= 3 -> 100.dp
        columns <= 4 -> 90.dp
        columns <= 5 -> 80.dp
        columns <= 6 -> 70.dp
        columns <= 7 -> 60.dp
        else -> 50.dp
    }

    val itemSize = minOf(baseSize, maxItemSize)
    val gridSize = (itemSize + itemPadding) * columns

    return Pair(gridSize, itemSize)
}

fun DrawScope.drawShape(shape: ShapeType, color: Color, size: Float) {
    when (shape) {
        ShapeType.CIRCLE -> {
            drawCircle(color, radius = size / 2f)
        }
        ShapeType.SQUARE -> {
            drawRect(color, size = androidx.compose.ui.geometry.Size(size, size))
        }
        ShapeType.TRIANGLE -> {
            val scale = 0.85f // Visually scale down triangle
            val scaledSize = size * scale
            val offset = (size - scaledSize) / 2f

            val path = Path().apply {
                moveTo(offset + scaledSize / 2f, offset) // Top center
                lineTo(offset + scaledSize, offset + scaledSize) // Bottom right
                lineTo(offset, offset + scaledSize) // Bottom left
                close()
            }
            drawPath(path, color)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameplayScreenPreview() {
    SpotTheShadeTheme {
        // This is a simplified preview and won't have a real ViewModel
        // You can create a fake ViewModel for better previews
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Level: 1 | Score: 0")
            Text("Time: 10s")
            Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                Text("Game Grid Preview")
            }
        }
    }
}