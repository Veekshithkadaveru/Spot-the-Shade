package com.example.spottheshade.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.GridItem
import com.example.spottheshade.navigation.Screen
import com.example.spottheshade.ui.theme.SpotTheShadeTheme
import com.example.spottheshade.viewmodel.GameViewModel
import kotlin.math.sqrt

@Composable
fun GameplayScreen(
    navController: NavHostController,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()

    // Start game when screen loads (use stable key)
    LaunchedEffect(key1 = true) {
        viewModel.startGame()
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Level: ${gameState.level}",
                    style = MaterialTheme.typography.headlineSmall
                )
                // Show difficulty indicator
                val difficulty = when {
                    gameState.level <= 10 -> "Easy"
                    gameState.level <= 25 -> "Medium"
                    gameState.level <= 40 -> "Hard"
                    gameState.level <= 55 -> "Expert"
                    gameState.level <= 70 -> "Master"
                    else -> "Legendary"
                }
                Text(
                    text = difficulty,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Score: ${gameState.score}",
                    style = MaterialTheme.typography.headlineSmall
                )
                // Lives display
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lives: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    // Show heart emoji for each life (max 3)
                    repeat(gameState.lives) {
                        Text(
                            text = "❤️",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    // Show empty hearts for lost lives (up to 3 total)
                    repeat(3 - gameState.lives) {
                        Text(
                            text = "🤍",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // Timer with extra time indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (gameState.hasUsedExtraTime && gameState.isGameActive) {
                Text(
                    text = "⚡ EXTRA TIME",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = "Time: ${gameState.timeRemaining}s",
                style = MaterialTheme.typography.headlineMedium,
                color = when {
                    gameState.hasUsedExtraTime && gameState.isGameActive -> MaterialTheme.colorScheme.tertiary
                    gameState.timeRemaining <= 3 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Game Grid (2x2)
        if (gameState.grid.isNotEmpty()) {
            // Hide grid when timed out (but not when using extra time)
            val shouldHideGrid =
                gameState.gameResult == GameResult.Timeout && !gameState.hasUsedExtraTime

            if (shouldHideGrid) {
                // Show placeholder when grid is hidden
                val columns = sqrt(gameState.grid.size.toDouble()).toInt()
                val placeholderSize = when {
                    columns <= 3 -> 200.dp
                    columns <= 4 -> 300.dp
                    columns <= 6 -> 350.dp
                    columns <= 7 -> 380.dp
                    else -> if (gameState.level >= 91) 420.dp else 320.dp
                }

                Box(
                    modifier = Modifier
                        .size(placeholderSize)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🕐",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "Time's Up!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Simple grid with good-looking circles
                val columns = sqrt(gameState.grid.size.toDouble()).toInt()

                // Calculate available screen width (assume 360dp typical width minus padding)
                val availableWidth = 320.dp // Conservative estimate for most phones
                val spacing = 8.dp * (columns - 1) // Total spacing between circles
                val maxCircleSize = (availableWidth - spacing) / columns

                // Finger-friendly circle sizes for the new progression
                val idealSize = when {
                    columns <= 3 -> 85.dp  // 2x2, 3x3: Large and comfortable 
                    columns <= 4 -> 70.dp  // 4x4: Good size for long gameplay (8-20 levels)
                    columns <= 6 -> 55.dp  // 5x5, 6x6: Medium grids, still very tappable
                    columns <= 7 -> 45.dp  // 7x7: Smaller but comfortable
                    else -> {
                        // 8x8: Bigger circles for levels 91+ to help with difficult color recognition
                        if (gameState.level >= 91) 48.dp else 38.dp
                    }
                }

                val circleSize = minOf(idealSize, maxCircleSize)

                // Create grid using nested Columns and Rows (no scrolling issues)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    for (row in 0 until columns) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until columns) {
                                val index = row * columns + col
                                if (index < gameState.grid.size) {
                                    val item = gameState.grid[index]
                                    GameCircle(
                                        color = item.color,
                                        enabled = gameState.isGameActive,
                                        onClick = { viewModel.onGridItemTapped(item.id) },
                                        size = circleSize
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Game Result
        gameState.gameResult?.let { result ->
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (result) {
                        is GameResult.Correct -> {
                            Text(
                                text = "🎉 Correct!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("You found the different shade!")
                        }

                        is GameResult.Wrong -> {
                            Text(
                                text = "❌ Wrong!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text("Try again to spot the difference!")
                        }

                        is GameResult.Timeout -> {
                            Text(
                                text = "⏰ Time's Up!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text("You ran out of time!")
                        }
                        
                        is GameResult.GameOver -> {
                            Text(
                                text = "💀 Game Over!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text("No more lives remaining!")
                        }


                    }

                    when (result) {
                        is GameResult.Timeout -> {
                            // Special case for timeout - show extra time option
                            if (!gameState.hasUsedExtraTime) {
                                Column(
                                    modifier = Modifier.padding(top = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Need extra seconds?",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.useExtraTime() }
                                        ) {
                                            Text("⚡ +5 Seconds")
                                        }
                                        if (gameState.lives > 0) {
                                            Button(onClick = { viewModel.continueAfterLifeLoss() }) {
                                                Text("Continue • ${gameState.lives} ❤️ left")
                                            }
                                        } else {
                                            Button(onClick = { viewModel.playAgain() }) {
                                                Text("Play Again")
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = { navController.navigate(Screen.MainMenu.route) },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("Main Menu")
                                    }
                                }
                            } else {
                                // Already used extra time, show continue or game over options
                                Row(
                                    modifier = Modifier.padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (gameState.lives > 0) {
                                        Button(onClick = { viewModel.continueAfterLifeLoss() }) {
                                            Text("Continue • ${gameState.lives} ❤️ left")
                                        }
                                    } else {
                                        Button(onClick = { viewModel.playAgain() }) {
                                            Text("Play Again")
                                        }
                                    }
                                    Button(onClick = { navController.navigate(Screen.MainMenu.route) }) {
                                        Text("Main Menu")
                                    }
                                }
                            }
                        }

                        is GameResult.Correct -> {
                            // Show next level option for correct answers
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { viewModel.nextLevel() }) {
                                    Text("Next Level")
                                }
                                Button(onClick = { viewModel.playAgain() }) {
                                    Text("Restart")
                                }
                            }
                            Button(
                                onClick = { navController.navigate(Screen.MainMenu.route) },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Main Menu")
                            }
                        }

                        is GameResult.Wrong -> {
                            // Wrong answer - show continue if lives remaining
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (gameState.lives > 0) {
                                    Button(onClick = { viewModel.continueAfterLifeLoss() }) {
                                        Text("Continue • ${gameState.lives} ❤️ left")
                                    }
                                } else {
                                    Button(onClick = { viewModel.playAgain() }) {
                                        Text("Play Again")
                                    }
                                }
                                Button(onClick = { navController.navigate(Screen.MainMenu.route) }) {
                                    Text("Main Menu")
                                }
                            }
                        }
                        
                        is GameResult.GameOver -> {
                            // Game over - only restart or main menu options
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Final Score: ${gameState.score}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Reached Level: ${gameState.level}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(onClick = { viewModel.playAgain() }) {
                                        Text("Play Again")
                                    }
                                    Button(onClick = { navController.navigate(Screen.MainMenu.route) }) {
                                        Text("Main Menu")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Instructions
        if (gameState.isGameActive) {
            Text(
                text = "Find the circle with a different shade!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}

@Composable
fun GameCircle(
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    enabled: Boolean,
    size: Dp = 80.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable(enabled = enabled) { onClick() }
    )
}

@Preview(showBackground = true)
@Composable
fun GameGrid8x8Preview() {
    SpotTheShadeTheme {
        // Create an 8x8 grid (64 items) with one different shade - levels 91+
        val baseColor = Color.hsl(120f, 0.8f, 0.65f) // Green base color
        val targetColor =
            Color.hsl(120f, 0.8f, 0.62f) // Slightly darker green (subtle difference for high level)
        val targetPosition = 28 // Position in the grid

        val grid = List(64) { index ->
            GridItem(
                id = index,
                color = if (index == targetPosition) targetColor else baseColor,
                isTarget = index == targetPosition
            )
        }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "8×8 Grid Preview (Level 91+)",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 8x8 grid with enhanced size (like level 91+)
            val columns = 8
            val circleSize = 48.dp // Enhanced size for levels 91+
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.size((circleSize * columns + 6.dp * (columns - 1)))
            ) {
                items(grid) { item ->
                    Box(
                        modifier = Modifier
                            .size(circleSize)
                            .clip(CircleShape)
                            .background(item.color)
                            .clickable { /* Preview - no action */ }
                    )
                }
            }

            Text(
                text = "Find the circle with a different shade!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}