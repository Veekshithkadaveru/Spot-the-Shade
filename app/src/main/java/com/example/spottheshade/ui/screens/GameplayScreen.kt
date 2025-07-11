package com.example.spottheshade.ui.screens

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.example.spottheshade.viewmodel.GameViewModel
import com.example.spottheshade.viewmodel.GameViewModelFactory
import kotlin.math.sqrt

@Composable
fun GameplayScreen(
    navController: NavHostController,
    viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(LocalContext.current))
) {
    val gameState by viewModel.gameState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())

    // Start game when screen loads (use stable key)
    LaunchedEffect(key1 = true) {
        viewModel.startGame()
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
                // Show current shape
                val shapeDisplay = when (gameState.currentShape) {
                    ShapeType.CIRCLE -> "● Circles"
                    ShapeType.SQUARE -> "■ Squares"
                    ShapeType.TRIANGLE -> "▲ Triangles"
                }
                Text(
                    text = shapeDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
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

        // High Score display
        Text(
            text = "High Score: ${userPreferences.highScore} • Best Level: ${userPreferences.highestLevel}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp)
        )

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

        // Game Grid with Dynamic Sizing
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
                // Dynamic sizing based on grid size
                val columns = sqrt(gameState.grid.size.toDouble()).toInt()
                val maxGridDimension = 10
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val shapePadding = 8.dp

                val itemSize = remember(columns, gameState.currentShape) {
                    val baseSize = ((screenWidth - (shapePadding * (columns + 1))) / columns).coerceAtMost(80.dp)
                    // Scale down triangles slightly to match visual weight of circles/squares
                    val triangleScaleFactor = 0.85f
                    if (gameState.currentShape == ShapeType.TRIANGLE) baseSize * triangleScaleFactor else baseSize
                }

                // Create grid using nested Columns and Rows (no scrolling issues)
                Column(
                    verticalArrangement = Arrangement.spacedBy(shapePadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    for (row in 0 until columns) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(shapePadding)
                        ) {
                            for (col in 0 until columns) {
                                val index = row * columns + col
                                if (index < gameState.grid.size) {
                                    val item = gameState.grid[index]
                                    GameShape(
                                        color = item.color,
                                        shape = item.shape,
                                        enabled = gameState.isGameActive,
                                        onClick = { viewModel.onGridItemTapped(item.id) },
                                        size = itemSize
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

                            // Show shape unlock notifications
                            when (gameState.level) {
                                10 -> {
                                    Text(
                                        text = "🎊 SQUARES UNLOCKED! 🎊",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                20 -> {
                                    Text(
                                        text = "🎊 TRIANGLES UNLOCKED! 🎊",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
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

                        }
                    }
                }
            }
        }

        // Instructions
        if (gameState.isGameActive) {
            val shapeInstruction = when (gameState.currentShape) {
                ShapeType.CIRCLE -> "Find the circle with a different shade!"
                ShapeType.SQUARE -> "Find the square with a different shade!"
                ShapeType.TRIANGLE -> "Find the triangle with a different shade!"
            }
            Text(
                text = shapeInstruction,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}

@Composable
fun GameShape(
    color: Color,
    shape: ShapeType,
    onClick: () -> Unit,
    enabled: Boolean,
    size: Dp = 80.dp
) {
    when (shape) {
        ShapeType.CIRCLE -> {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
                    .clickable(enabled = enabled) { onClick() }
            )
        }

        ShapeType.SQUARE -> {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(color)
                    .clickable(enabled = enabled) { onClick() }
            )
        }

        ShapeType.TRIANGLE -> {
            Canvas(
                modifier = Modifier
                    .size(size)
                    .clickable(enabled = enabled) { onClick() }
            ) {
                drawTriangle(color, size.toPx())
            }
        }
    }
}

fun DrawScope.drawTriangle(color: Color, size: Float) {
    val path = Path().apply {
        // Create an equilateral triangle
        val halfSize = size / 2f
        val height = (size * 0.866f) // height of equilateral triangle
        val yOffset = (size - height) / 2f // center vertically

        moveTo(halfSize, yOffset) // top point
        lineTo(0f, size - yOffset) // bottom left
        lineTo(size, size - yOffset) // bottom right
        close()
    }
    drawPath(path, color)
}

@Preview(showBackground = true)
@Composable
fun GameShapeVariationsPreview() {
    SpotTheShadeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Shape Variations Preview",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Show different shapes with samples
            val baseColor = Color.hsl(240f, 0.8f, 0.65f)
            val targetColor = Color.hsl(240f, 0.8f, 0.55f)

            // Circles (Level 1-9)
            Text("Levels 1-9: Circles", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.padding(8.dp)) {
                repeat(3) { index ->
                    GameShape(
                        color = if (index == 1) targetColor else baseColor,
                        shape = ShapeType.CIRCLE,
                        onClick = {},
                        enabled = true,
                        size = 60.dp
                    )
                }
            }

            // Squares (Level 10-19)
            Text("Levels 10-19: Squares", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.padding(8.dp)) {
                repeat(3) { index ->
                    GameShape(
                        color = if (index == 2) targetColor else baseColor,
                        shape = ShapeType.SQUARE,
                        onClick = {},
                        enabled = true,
                        size = 60.dp
                    )
                }
            }

            // Triangles (Level 20+)
            Text("Levels 20+: Triangles", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.padding(8.dp)) {
                repeat(3) { index ->
                    GameShape(
                        color = if (index == 0) targetColor else baseColor,
                        shape = ShapeType.TRIANGLE,
                        onClick = {},
                        enabled = true,
                        size = 60.dp
                    )
                }
            }
        }
    }
}