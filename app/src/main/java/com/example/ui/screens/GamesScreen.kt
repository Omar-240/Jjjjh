package com.example.ui.screens

import android.graphics.Point
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    var selectedGameId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative Neon Glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
                .blur(40.dp)
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "Lumina Arcade",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "10 OFFLINE ARCADE SUITE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (selectedGameId != null) {
                                    selectedGameId = null
                                } else {
                                    viewModel.currentScreen = "dashboard"
                                }
                            },
                            modifier = Modifier.testTag("games_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedGameId == null) {
                    // Games selection grid
                    GamesDashboard(
                        accentColor = accentColor,
                        onSelectGame = { selectedGameId = it }
                    )
                } else {
                    // Render the chosen game
                    ActiveGameContainer(
                        gameId = selectedGameId!!,
                        accentColor = accentColor,
                        onExit = { selectedGameId = null }
                    )
                }
            }
        }
    }
}

// Data class representing a game
data class GameInfo(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: String,
    val difficulty: String
)

@Composable
fun GamesDashboard(accentColor: Color, onSelectGame: (String) -> Unit) {
    val gamesList = remember {
        listOf(
            GameInfo("snake", "Neon Snake", "Navigate the grid, eat glowing blocks, grow infinite.", Icons.Default.ChevronRight, "RETRO", "EASY"),
            GameInfo("tictactoe", "Tic-Tac-Toe Pro", "Defeat the smart minimax Computer AI in tic-tac-toe.", Icons.Default.Grid3x3, "STRATEGY", "HARD"),
            GameInfo("flappy", "Flappy Neon", "Tap to flap, dodge incoming neon columns.", Icons.Default.FlightTakeoff, "PHYSICS", "HARD"),
            GameInfo("breakout", "Neon Breakout", "Bounce the orb, shatter multiple rows of glowing bricks.", Icons.Default.ViewAgenda, "ACTION", "MEDIUM"),
            GameInfo("space", "Space Asteroids", "Shoot cosmic laser beams to pulverize space rocks.", Icons.Default.RocketLaunch, "SHOOTER", "MEDIUM"),
            GameInfo("simon", "Simon Glow", "Recall and duplicate the flashing electronic lights pattern.", Icons.Default.FlashOn, "MEMORY", "MEDIUM"),
            GameInfo("reflex", "Reflex Speed Tap", "Touch the green light instantly to test your reflexes.", Icons.Default.Timer, "SPEED", "EASY"),
            GameInfo("matchup", "Studio Match Up", "Flip and pair identical studio layout cards.", Icons.Default.Dashboard, "PUZZLE", "EASY"),
            GameInfo("whack", "Glow Whack-A-Mole", "Tap the blinking nodes fast in a dynamic 15s frenzy.", Icons.Default.TouchApp, "ARCADE", "MEDIUM"),
            GameInfo("2048", "Slide 2048", "Slide, merge matching digits, and solve the math grid.", Icons.Default.Grid4x4, "PUZZLE", "HARD"),
            GameInfo("guess", "Neon Guessing", "خمن الرقم السري بين 1 و100 بأقل عدد من المحاولات وجرب ذكائك.", Icons.Default.QuestionMark, "PUZZLE", "EASY"),
            GameInfo("math", "Neon Math Rush", "تحدي حسابي سريع! حل المعادلات قبل نهاية العداد.", Icons.Default.Add, "MATH", "MEDIUM"),
            GameInfo("market", "Neon Market Sim", "محاكي تداول وبورصة متكامل بأسعار متغيرة ورسوم بيانية وأخبار عاجلة عشوائية.", Icons.Default.TrendingUp, "SIMULATION", "MEDIUM"),
            GameInfo("lander", "Cosmic Lander", "محاكي هبوط مركبة فضائية واقعي بقوانين الفيزياء والجاذبية والرياح الجانبية وقوة الدفع.", Icons.Default.RocketLaunch, "SIMULATION", "HARD")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glowing Billboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13111E).copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = "100% OFFLINE HUB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Lumina Indie Games",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "No internet required. Perfect for travel, breaks, or sharpening your tactical and motor skills in beautiful neon styling.",
                    fontSize = 11.sp,
                    color = Color(0xFF8E8D9C),
                    lineHeight = 15.sp
                )
            }
        }

        Text(
            text = "Select Arcade Cabinets",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = Color(0xFFA2A1B0),
            letterSpacing = 1.sp
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 1200.dp),
            userScrollEnabled = false
        ) {
            items(gamesList.size) { idx ->
                val game = gamesList[idx]
                Card(
                    onClick = { onSelectGame(game.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, Color(0xFF262436), RoundedCornerShape(20.dp))
                        .testTag("arcade_card_${game.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151422))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = game.icon,
                                    contentDescription = game.title,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF232230), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = game.category,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = game.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = game.description,
                                fontSize = 9.5.sp,
                                color = Color(0xFF8E8D9C),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ActiveGameContainer(gameId: String, accentColor: Color, onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (gameId) {
            "snake" -> NeonSnakeGame(accentColor, onExit)
            "tictactoe" -> TicTacToeGame(accentColor, onExit)
            "flappy" -> FlappyNeonGame(accentColor, onExit)
            "breakout" -> NeonBreakoutGame(accentColor, onExit)
            "space" -> SpaceAsteroidsGame(accentColor, onExit)
            "simon" -> SimonGlowGame(accentColor, onExit)
            "reflex" -> ReflexSpeedTapGame(accentColor, onExit)
            "matchup" -> StudioMatchUpGame(accentColor, onExit)
            "whack" -> WhackAMoleGame(accentColor, onExit)
            "2048" -> Slide2048Game(accentColor, onExit)
            "guess" -> NeonGuessGame(accentColor, onExit)
            "math" -> NeonMathRushGame(accentColor, onExit)
            "market" -> NeonMarketSimGame(accentColor, onExit)
            "lander" -> CosmicLanderGame(accentColor, onExit)
            else -> {
                Text("Game not found", color = Color.White)
            }
        }
    }
}

// ---------------------------------------------------------
// 1. NEON SNAKE
// ---------------------------------------------------------
@Composable
fun NeonSnakeGame(accentColor: Color, onExit: () -> Unit) {
    val gridSize = 15
    var snake by remember { mutableStateOf(listOf(Point(5, 5), Point(5, 6), Point(5, 7))) }
    var direction by remember { mutableStateOf(Point(0, -1)) } // Default Moving Up
    var food by remember { mutableStateOf(Point(3, 3)) }
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = gameOver) {
        while (!gameOver) {
            delay(180)
            val head = snake.first()
            val nextPoint = Point(head.x + direction.x, head.y + direction.y)

            // Bound detection or self collision
            if (nextPoint.x < 0 || nextPoint.x >= gridSize || nextPoint.y < 0 || nextPoint.y >= gridSize || snake.contains(nextPoint)) {
                gameOver = true
            } else {
                val newSnake = mutableListOf(nextPoint)
                newSnake.addAll(snake)
                if (nextPoint == food) {
                    score += 10
                    // spawn food in random unoccupied space
                    var newFood: Point
                    do {
                        newFood = Point(Random.nextInt(gridSize), Random.nextInt(gridSize))
                    } while (newSnake.contains(newFood))
                    food = newFood
                } else {
                    newSnake.removeAt(newSnake.size - 1)
                }
                snake = newSnake
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Neon Snake", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Score: $score", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)
        }

        // The game board
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .background(Color(0xFF100F1B), RoundedCornerShape(12.dp))
                .border(2.dp, accentColor, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSizeW = size.width / gridSize
                val cellSizeH = size.height / gridSize

                // Draw food
                drawRoundRect(
                    color = Color(0xFFFF007F),
                    topLeft = Offset(food.x * cellSizeW, food.y * cellSizeH),
                    size = Size(cellSizeW - 2, cellSizeH - 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )

                // Draw snake
                snake.forEachIndexed { index, point ->
                    val color = if (index == 0) accentColor else accentColor.copy(alpha = 0.6f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(point.x * cellSizeW, point.y * cellSizeH),
                        size = Size(cellSizeW - 2, cellSizeH - 2),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                    )
                }
            }

            if (gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GAME OVER", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFFF007F))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                snake = listOf(Point(5, 5), Point(5, 6), Point(5, 7))
                                direction = Point(0, -1)
                                food = Point(3, 3)
                                score = 0
                                gameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Navigation controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { if (direction.y == 0) direction = Point(0, -1) },
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF201E2E), CircleShape)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconButton(
                    onClick = { if (direction.x == 0) direction = Point(-1, 0) },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF201E2E), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(36.dp))
                IconButton(
                    onClick = { if (direction.x == 0) direction = Point(1, 0) },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF201E2E), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White)
                }
            }

            IconButton(
                onClick = { if (direction.y == 0) direction = Point(0, 1) },
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF201E2E), CircleShape)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White)
            }
        }

        Text(
            text = "Exit to Games Arcade",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier
                .clickable { onExit() }
                .padding(8.dp)
        )
    }
}

// ---------------------------------------------------------
// 2. TIC-TAC-TOE PRO
// ---------------------------------------------------------
@Composable
fun TicTacToeGame(accentColor: Color, onExit: () -> Unit) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) } // "Player", "CPU", "Tie", null

    fun checkWin(b: List<String>): String? {
        val winCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // diagonals
        )
        for (combo in winCombos) {
            if (b[combo[0]].isNotBlank() && b[combo[0]] == b[combo[1]] && b[combo[1]] == b[combo[2]]) {
                return b[combo[0]]
            }
        }
        if (b.all { it.isNotBlank() }) return "Tie"
        return null
    }

    fun makeCpuMove() {
        val win = checkWin(board)
        if (win != null) {
            winner = if (win == "X") "Player" else if (win == "O") "CPU" else "Tie"
            return
        }

        // Smart Simple AI: 1. Win if possible, 2. Block if possible, 3. Take center, 4. Take random
        val availableIndices = board.indices.filter { board[it].isBlank() }
        if (availableIndices.isEmpty()) return

        var chosenMove = -1

        // Check if CPU can win
        for (idx in availableIndices) {
            val testBoard = board.toMutableList()
            testBoard[idx] = "O"
            if (checkWin(testBoard) == "O") {
                chosenMove = idx
                break
            }
        }

        // Block player win
        if (chosenMove == -1) {
            for (idx in availableIndices) {
                val testBoard = board.toMutableList()
                testBoard[idx] = "X"
                if (checkWin(testBoard) == "X") {
                    chosenMove = idx
                    break
                }
            }
        }

        // Take center
        if (chosenMove == -1 && board[4].isBlank()) {
            chosenMove = 4
        }

        // Default to random
        if (chosenMove == -1) {
            chosenMove = availableIndices.random()
        }

        val newBoard = board.toMutableList()
        newBoard[chosenMove] = "O"
        board = newBoard

        val finalWin = checkWin(board)
        if (finalWin != null) {
            winner = if (finalWin == "X") "Player" else if (finalWin == "O") "CPU" else "Tie"
        } else {
            isPlayerTurn = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tic-Tac-Toe Pro", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)

        Text(
            text = when {
                winner == "Player" -> "Victory! You defeated the CPU!"
                winner == "CPU" -> "CPU wins! Better luck next time."
                winner == "Tie" -> "It's a tie!"
                isPlayerTurn -> "Your Turn (X)"
                else -> "CPU Thinking..."
            },
            color = if (winner != null) accentColor else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        // 3x3 Grid Board
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF131122), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF2D2C3D), RoundedCornerShape(16.dp))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(9) { idx ->
                    val value = board[idx]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B192A))
                            .border(1.dp, Color(0xFF33314B), RoundedCornerShape(12.dp))
                            .clickable(enabled = value.isBlank() && isPlayerTurn && winner == null) {
                                val newBoard = board.toMutableList()
                                newBoard[idx] = "X"
                                board = newBoard
                                val res = checkWin(board)
                                if (res != null) {
                                    winner = if (res == "X") "Player" else if (res == "O") "CPU" else "Tie"
                                } else {
                                    isPlayerTurn = false
                                    // Trigger CPU Move
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        makeCpuMove()
                                    }, 450)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (value == "X") accentColor else Color(0xFFFF007F)
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    board = List(9) { "" }
                    isPlayerTurn = true
                    winner = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Restart Game", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 3. FLAPPY NEON
// ---------------------------------------------------------
@Composable
fun FlappyNeonGame(accentColor: Color, onExit: () -> Unit) {
    var birdY by remember { mutableStateOf(250f) }
    var birdVelocity by remember { mutableStateOf(0f) }
    var pipeX by remember { mutableStateOf(500f) }
    var pipeGapY by remember { mutableStateOf(200f) }
    val gapHeight = 150f
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = gameOver) {
        while (!gameOver) {
            delay(30)
            birdVelocity += 1.2f // gravity
            birdY += birdVelocity

            pipeX -= 7f // horizontal movement
            if (pipeX < -80f) {
                pipeX = 500f
                pipeGapY = Random.nextInt(100, 300).toFloat()
                score += 1
            }

            // check bottom collision or top collision
            if (birdY < 0f || birdY > 480f) {
                gameOver = true
            }

            // check column collision
            // Pipe boundary: width 60px, x: pipeX -> pipeX + 60
            // Bird: x: 120, y: birdY, radius 15px
            if (pipeX < 135f && pipeX + 60f > 105f) {
                if (birdY < pipeGapY || birdY > pipeGapY + gapHeight) {
                    gameOver = true
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Flappy Neon", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Score: $score", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFF0F0F1A), RoundedCornerShape(12.dp))
                .border(2.dp, accentColor, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw Columns
                // Top Column
                drawRect(
                    color = Color(0xFFFF007F),
                    topLeft = Offset(pipeX, 0f),
                    size = Size(60.dp.toPx(), pipeGapY)
                )

                // Bottom Column
                drawRect(
                    color = Color(0xFFFF007F),
                    topLeft = Offset(pipeX, pipeGapY + gapHeight),
                    size = Size(60.dp.toPx(), size.height - (pipeGapY + gapHeight))
                )

                // Draw Bird (Circle)
                drawCircle(
                    color = accentColor,
                    radius = 15f,
                    center = Offset(120f, birdY)
                )
            }

            if (gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GAME OVER", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFFF007F))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                birdY = 200f
                                birdVelocity = 0f
                                pipeX = 500f
                                score = 0
                                gameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { if (!gameOver) birdVelocity = -12f },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("FLAP / BOOST", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 4. NEON BREAKOUT
// ---------------------------------------------------------
@Composable
fun NeonBreakoutGame(accentColor: Color, onExit: () -> Unit) {
    var ballX by remember { mutableStateOf(150f) }
    var ballY by remember { mutableStateOf(250f) }
    var speedX by remember { mutableStateOf(6f) }
    var speedY by remember { mutableStateOf(-6f) }
    var paddleX by remember { mutableStateOf(100f) }
    var bricks by remember { mutableStateOf(List(18) { true }) } // 3 rows, 6 columns
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val rowCount = 3
    val colCount = 6

    LaunchedEffect(key1 = gameOver) {
        while (!gameOver) {
            delay(25)
            ballX += speedX
            ballY += speedY

            // wall bounce left/right
            if (ballX < 12f || ballX > 320f) speedX = -speedX

            // top bounce
            if (ballY < 12f) speedY = -speedY

            // paddle check
            if (ballY > 360f && ballY < 370f) {
                if (ballX > paddleX && ballX < paddleX + 110f) {
                    speedY = -speedY
                }
            }

            // bottom check
            if (ballY > 390f) {
                gameOver = true
            }

            // bricks collision
            // bricks row-by-row layout
            val brickW = 50f
            val brickH = 20f
            val startX = 15f
            val startY = 40f

            for (i in bricks.indices) {
                if (bricks[i]) {
                    val row = i / colCount
                    val col = i % colCount
                    val bx = startX + col * (brickW + 4)
                    val by = startY + row * (brickH + 4)

                    // box check
                    if (ballX > bx && ballX < bx + brickW && ballY > by && ballY < by + brickH) {
                        bricks = bricks.toMutableList().apply { this[i] = false }
                        speedY = -speedY
                        score += 20
                        break
                    }
                }
            }

            // check win
            if (bricks.none { it }) {
                bricks = List(18) { true }
                score += 100
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Breakout", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Score: $score", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .width(340.dp)
                .height(400.dp)
                .background(Color(0xFF0C0B14), RoundedCornerShape(12.dp))
                .border(2.dp, accentColor, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Bricks rendering
                val brickW = 50f
                val brickH = 20f
                val startX = 15f
                val startY = 40f

                for (i in bricks.indices) {
                    if (bricks[i]) {
                        val row = i / colCount
                        val col = i % colCount
                        val bx = startX + col * (brickW + 4)
                        val by = startY + row * (brickH + 4)

                        drawRect(
                            color = Color(0xFFFF007F),
                            topLeft = Offset(bx, by),
                            size = Size(brickW, brickH)
                        )
                    }
                }

                // Ball
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(ballX, ballY)
                )

                // Paddle
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(paddleX, 365f),
                    size = Size(110f, 15f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
            }

            if (gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GAME OVER", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFFF007F))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                ballX = 150f
                                ballY = 250f
                                speedX = 6f
                                speedY = -6f
                                bricks = List(18) { true }
                                score = 0
                                gameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Paddle move controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { if (paddleX > 10f) paddleX -= 25f },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26243A)),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", tint = Color.White)
            }

            Button(
                onClick = { if (paddleX < 210f) paddleX += 25f },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26243A)),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 5. SPACE ASTEROIDS
// ---------------------------------------------------------
@Composable
fun SpaceAsteroidsGame(accentColor: Color, onExit: () -> Unit) {
    var playerX by remember { mutableStateOf(160f) }
    var lasers by remember { mutableStateOf(listOf<Offset>()) }
    var asteroids by remember { mutableStateOf(listOf<Offset>()) }
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = gameOver) {
        while (!gameOver) {
            delay(35)
            // Laser move
            lasers = lasers.map { Offset(it.x, it.y - 12f) }.filter { it.y > 0 }

            // Random asteroid spawn
            if (Random.nextInt(100) < 6) {
                asteroids = asteroids + Offset(Random.nextInt(15, 300).toFloat(), 0f)
            }

            // Asteroid move
            asteroids = asteroids.map { Offset(it.x, it.y + 5f) }

            // collision laser with asteroid
            var nextAsteroids = asteroids.toMutableList()
            var nextLasers = lasers.toMutableList()
            val destroyedIndices = mutableListOf<Int>()
            val laserIndicesToRem = mutableListOf<Int>()

            for (aIdx in nextAsteroids.indices) {
                for (lIdx in nextLasers.indices) {
                    val a = nextAsteroids[aIdx]
                    val l = nextLasers[lIdx]
                    if (kotlin.math.abs(a.x - l.x) < 22f && kotlin.math.abs(a.y - l.y) < 22f) {
                        destroyedIndices.add(aIdx)
                        laserIndicesToRem.add(lIdx)
                        score += 10
                        break
                    }
                }
            }

            asteroids = nextAsteroids.filterIndexed { index, _ -> !destroyedIndices.contains(index) }
            lasers = nextLasers.filterIndexed { index, _ -> !laserIndicesToRem.contains(index) }

            // Player collision check with Asteroids
            // Player is located at x: playerX, y: 350
            if (asteroids.any { kotlin.math.abs(it.x - playerX) < 25f && it.y > 330f && it.y < 365f }) {
                gameOver = true
            }

            // Filter out of bounds asteroids
            asteroids = asteroids.filter { it.y < 400f }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Space Asteroids", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Score: $score", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(Color(0xFF0A0912), RoundedCornerShape(12.dp))
                .border(2.dp, accentColor, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Player Space Ship
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(playerX - 15f, 335f),
                    size = Size(30f, 15f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
                drawRect(
                    color = accentColor,
                    topLeft = Offset(playerX - 4f, 325f),
                    size = Size(8f, 10f)
                )

                // Draw lasers
                lasers.forEach { laser ->
                    drawRect(
                        color = Color.Cyan,
                        topLeft = Offset(laser.x - 2f, laser.y),
                        size = Size(4f, 12f)
                    )
                }

                // Draw asteroids
                asteroids.forEach { rock ->
                    drawCircle(
                        color = Color(0xFFFF007F),
                        radius = 12f,
                        center = rock
                    )
                }
            }

            if (gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MISSION FAILED", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFFF007F))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                lasers = emptyList()
                                asteroids = emptyList()
                                score = 0
                                playerX = 160f
                                gameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Deploy Again", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Gamepad controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { if (playerX > 25f) playerX -= 25f },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C2C))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White)
            }

            Button(
                onClick = { if (!gameOver) lasers = lasers + Offset(playerX, 320f) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                shape = CircleShape,
                modifier = Modifier.size(54.dp)
            ) {
                Text("FIRE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { if (playerX < 300f) playerX += 25f },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C2C))
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 6. SIMON GLOW
// ---------------------------------------------------------
@Composable
fun SimonGlowGame(accentColor: Color, onExit: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var playerInput by remember { mutableStateOf(listOf<Int>()) }
    var highlightedQuad by remember { mutableStateOf(-1) }
    var statusMessage by remember { mutableStateOf("Tap Start to Play!") }
    var isCpuPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    fun playSequence() {
        coroutineScope.launch {
            isCpuPlaying = true
            statusMessage = "Watch CPU Light Glows..."
            delay(800)
            for (step in sequence) {
                highlightedQuad = step
                delay(600)
                highlightedQuad = -1
                delay(250)
            }
            highlightedQuad = -1
            isCpuPlaying = false
            statusMessage = "Repeat the light sequence!"
        }
    }

    fun startNewGame() {
        score = 0
        sequence = listOf(Random.nextInt(4))
        playerInput = emptyList()
        playSequence()
    }

    fun handleQuadrantClick(id: Int) {
        if (isCpuPlaying || sequence.isEmpty()) return
        coroutineScope.launch {
            highlightedQuad = id
            delay(200)
            highlightedQuad = -1
        }
        val nextInput = playerInput + id
        playerInput = nextInput

        // compare steps
        val currentStepIdx = playerInput.size - 1
        if (playerInput[currentStepIdx] != sequence[currentStepIdx]) {
            statusMessage = "Incorrect! Final Score: $score"
            sequence = emptyList()
        } else if (playerInput.size == sequence.size) {
            score++
            statusMessage = "Perfect! Keep Going!"
            playerInput = emptyList()
            sequence = sequence + Random.nextInt(4)
            playSequence()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Simon Glow", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
        Text("Score: $score", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)

        Text(
            text = statusMessage,
            color = Color.LightGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        // 2x2 Glowing Quadrants
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(topStart = 60.dp))
                        .background(if (highlightedQuad == 0) Color.Green else Color.Green.copy(alpha = 0.15f))
                        .clickable { handleQuadrantClick(0) }
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(topEnd = 60.dp))
                        .background(if (highlightedQuad == 1) Color.Red else Color.Red.copy(alpha = 0.15f))
                        .clickable { handleQuadrantClick(1) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(bottomStart = 60.dp))
                        .background(if (highlightedQuad == 2) Color.Yellow else Color.Yellow.copy(alpha = 0.15f))
                        .clickable { handleQuadrantClick(2) }
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(bottomEnd = 60.dp))
                        .background(if (highlightedQuad == 3) Color.Blue else Color.Blue.copy(alpha = 0.15f))
                        .clickable { handleQuadrantClick(3) }
                )
            }
        }

        Button(
            onClick = { startNewGame() },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Start Game", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 7. REFLEX SPEED TAP
// ---------------------------------------------------------
@Composable
fun ReflexSpeedTapGame(accentColor: Color, onExit: () -> Unit) {
    var reflexState by remember { mutableStateOf("READY") } // READY, WAITING, FLASHING, DONE
    var reflexMessage by remember { mutableStateOf("Press START, then tap the node the split second it turns BRIGHT GREEN!") }
    var startTime by remember { mutableLongStateOf(0L) }
    var resultMs by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    fun triggerWaiting() {
        reflexState = "WAITING"
        reflexMessage = "Focus... Wait for it..."
        coroutineScope.launch {
            val randomDelay = Random.nextLong(1500, 4500)
            delay(randomDelay)
            if (reflexState == "WAITING") {
                reflexState = "FLASHING"
                reflexMessage = "TAP NOW NOW NOW!"
                startTime = System.currentTimeMillis()
            }
        }
    }

    fun handleNodeTap() {
        if (reflexState == "WAITING") {
            // Early tap penalty
            reflexState = "READY"
            reflexMessage = "Too early! Fail. Tap START to try again."
        } else if (reflexState == "FLASHING") {
            val tapTime = System.currentTimeMillis()
            resultMs = tapTime - startTime
            reflexState = "DONE"
            reflexMessage = "Your reflex response: ${resultMs}ms!"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Reflex Speed Tap", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)

        Text(
            text = reflexMessage,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large tapping node
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    when (reflexState) {
                        "FLASHING" -> Color.Green
                        "WAITING" -> Color(0xFFC02540)
                        else -> accentColor.copy(alpha = 0.25f)
                    }
                )
                .border(2.dp, if (reflexState == "FLASHING") Color.Green else accentColor, CircleShape)
                .clickable { handleNodeTap() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (reflexState) {
                    "FLASHING" -> "TAP!"
                    "WAITING" -> "WAIT..."
                    "DONE" -> "DONE"
                    else -> "READY"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (reflexState == "READY" || reflexState == "DONE") {
            Button(
                onClick = { triggerWaiting() },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Start Reflex Timer", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 8. STUDIO MATCH UP (MEMORY GAME)
// ---------------------------------------------------------
data class MemoryCard(
    val id: Int,
    val icon: ImageVector,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun StudioMatchUpGame(accentColor: Color, onExit: () -> Unit) {
    val icons = remember {
        listOf(
            Icons.Default.Brush, Icons.Default.Camera, Icons.Default.Description,
            Icons.Default.Folder, Icons.Default.Event, Icons.Default.AccountBalance
        )
    }

    var cards by remember {
        mutableStateOf(
            (icons + icons).shuffled().mapIndexed { index, icon ->
                MemoryCard(id = index, icon = icon)
            }
        )
    }

    var flippedIndices by remember { mutableStateOf(listOf<Int>()) }
    var moves by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    fun handleCardClick(clickedIdx: Int) {
        if (flippedIndices.size >= 2 || cards[clickedIdx].isFlipped || cards[clickedIdx].isMatched) return

        // Flip card
        cards = cards.toMutableList().apply {
            this[clickedIdx] = this[clickedIdx].copy(isFlipped = true)
        }

        val nextFlipped = flippedIndices + clickedIdx
        flippedIndices = nextFlipped

        if (nextFlipped.size == 2) {
            moves++
            val first = cards[nextFlipped[0]]
            val second = cards[nextFlipped[1]]

            if (first.icon == second.icon) {
                // Match
                coroutineScope.launch {
                    delay(400)
                    cards = cards.toMutableList().apply {
                        this[nextFlipped[0]] = this[nextFlipped[0]].copy(isMatched = true)
                        this[nextFlipped[1]] = this[nextFlipped[1]].copy(isMatched = true)
                    }
                    flippedIndices = emptyList()
                }
            } else {
                // Unflip
                coroutineScope.launch {
                    delay(1000)
                    cards = cards.toMutableList().apply {
                        this[nextFlipped[0]] = this[nextFlipped[0]].copy(isFlipped = false)
                        this[nextFlipped[1]] = this[nextFlipped[1]].copy(isFlipped = false)
                    }
                    flippedIndices = emptyList()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Studio Match Up", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Moves: $moves", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            userScrollEnabled = false
        ) {
            items(cards.size) { idx ->
                val card = cards[idx]
                val visible = card.isFlipped || card.isMatched

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (visible) Color(0xFF1E1B2D) else Color(0xFF26243A))
                        .border(
                            1.dp,
                            if (card.isMatched) accentColor else Color(0xFF33314B),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { handleCardClick(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    if (visible) {
                        Icon(
                            imageVector = card.icon,
                            contentDescription = "Icon",
                            tint = if (card.isMatched) accentColor else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            text = "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                cards = (icons + icons).shuffled().mapIndexed { index, icon ->
                    MemoryCard(id = index, icon = icon)
                }
                flippedIndices = emptyList()
                moves = 0
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Restart Grid", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 9. GLOW WHACK-A-MOLE
// ---------------------------------------------------------
@Composable
fun WhackAMoleGame(accentColor: Color, onExit: () -> Unit) {
    var activeMoleIndex by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        isRunning = false
    }

    LaunchedEffect(key1 = isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (timeLeft > 0) {
            activeMoleIndex = Random.nextInt(9)
            // wait a little bit depending on difficulty/speed
            delay(850)
        }
        activeMoleIndex = -1
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Glow Whack-A-Mole", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Time: ${timeLeft}s", fontWeight = FontWeight.Bold, color = Color(0xFFFF007F), fontSize = 16.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Score: $score pts", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 15.sp)
        }

        // 3x3 mole field
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131122))
                .border(1.dp, Color(0xFF2A283B), RoundedCornerShape(16.dp))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(9) { idx ->
                    val isActive = idx == activeMoleIndex
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFFFF007F) else Color(0xFF1D1B2D))
                            .border(
                                2.dp,
                                if (isActive) Color(0xFFFF007F) else Color(0xFF2E2C3F),
                                CircleShape
                            )
                            .clickable(enabled = isRunning && isActive) {
                                score += 10
                                activeMoleIndex = -1 // immediately whack him!
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = "Tap",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!isRunning) {
            Button(
                onClick = {
                    score = 0
                    timeLeft = 15
                    isRunning = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(if (timeLeft == 0) "Play Again" else "Start 15s Blitz", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 10. SLIDE 2048
// ---------------------------------------------------------
@Composable
fun Slide2048Game(accentColor: Color, onExit: () -> Unit) {
    var board by remember { mutableStateOf(List(16) { 0 }) }
    var score by remember { mutableIntStateOf(0) }

    fun addRandomTile(b: List<Int>): List<Int> {
        val available = b.indices.filter { b[it] == 0 }
        if (available.isEmpty()) return b
        val nextBoard = b.toMutableList()
        nextBoard[available.random()] = if (Random.nextFloat() < 0.9f) 2 else 4
        return nextBoard
    }

    fun restartGame() {
        score = 0
        var b = List(16) { 0 }
        b = addRandomTile(b)
        b = addRandomTile(b)
        board = b
    }

    LaunchedEffect(Unit) {
        restartGame()
    }

    // Slide line helper
    fun mergeLine(line: List<Int>): Pair<List<Int>, Int> {
        // filter zeros
        val nonZeros = line.filter { it != 0 }
        val merged = mutableListOf<Int>()
        var addedScore = 0
        var skip = false
        for (i in nonZeros.indices) {
            if (skip) {
                skip = false
                continue
            }
            if (i + 1 < nonZeros.size && nonZeros[i] == nonZeros[i + 1]) {
                val valMerged = nonZeros[i] * 2
                merged.add(valMerged)
                addedScore += valMerged
                skip = true
            } else {
                merged.add(nonZeros[i])
            }
        }
        while (merged.size < 4) {
            merged.add(0)
        }
        return Pair(merged, addedScore)
    }

    fun slideLeft() {
        var nextBoard = board.toMutableList()
        var totalGain = 0
        for (r in 0 until 4) {
            val line = listOf(board[r * 4], board[r * 4 + 1], board[r * 4 + 2], board[r * 4 + 3])
            val (merged, pts) = mergeLine(line)
            totalGain += pts
            nextBoard[r * 4] = merged[0]
            nextBoard[r * 4 + 1] = merged[1]
            nextBoard[r * 4 + 2] = merged[2]
            nextBoard[r * 4 + 3] = merged[3]
        }
        if (nextBoard != board) {
            board = addRandomTile(nextBoard)
            score += totalGain
        }
    }

    fun slideRight() {
        var nextBoard = board.toMutableList()
        var totalGain = 0
        for (r in 0 until 4) {
            val line = listOf(board[r * 4 + 3], board[r * 4 + 2], board[r * 4 + 1], board[r * 4])
            val (merged, pts) = mergeLine(line)
            totalGain += pts
            nextBoard[r * 4 + 3] = merged[0]
            nextBoard[r * 4 + 2] = merged[1]
            nextBoard[r * 4 + 1] = merged[2]
            nextBoard[r * 4] = merged[3]
        }
        if (nextBoard != board) {
            board = addRandomTile(nextBoard)
            score += totalGain
        }
    }

    fun slideUp() {
        var nextBoard = board.toMutableList()
        var totalGain = 0
        for (c in 0 until 4) {
            val line = listOf(board[c], board[c + 4], board[c + 8], board[c + 12])
            val (merged, pts) = mergeLine(line)
            totalGain += pts
            nextBoard[c] = merged[0]
            nextBoard[c + 4] = merged[1]
            nextBoard[c + 8] = merged[2]
            nextBoard[c + 12] = merged[3]
        }
        if (nextBoard != board) {
            board = addRandomTile(nextBoard)
            score += totalGain
        }
    }

    fun slideDown() {
        var nextBoard = board.toMutableList()
        var totalGain = 0
        for (c in 0 until 4) {
            val line = listOf(board[c + 12], board[c + 8], board[c + 4], board[c])
            val (merged, pts) = mergeLine(line)
            totalGain += pts
            nextBoard[c + 12] = merged[0]
            nextBoard[c + 8] = merged[1]
            nextBoard[c + 4] = merged[2]
            nextBoard[c] = merged[3]
        }
        if (nextBoard != board) {
            board = addRandomTile(nextBoard)
            score += totalGain
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Slide 2048", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Score: $score", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)
        }

        // 4x4 Tiles Layout
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF131122))
                .border(1.dp, Color(0xFF2A283B), RoundedCornerShape(12.dp))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false
            ) {
                items(16) { idx ->
                    val valTile = board[idx]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (valTile) {
                                    0 -> Color(0xFF1B192A)
                                    2 -> Color(0xFF2D2C42)
                                    4 -> Color(0xFF3B3958)
                                    8 -> Color(0xFF4A2568)
                                    16 -> Color(0xFF751E7C)
                                    32 -> Color(0xFF991475)
                                    64 -> Color(0xFFC7066C)
                                    128 -> Color(0xFFFF007F)
                                    else -> accentColor
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (valTile > 0) {
                            Text(
                                text = valTile.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = if (valTile >= 100) 12.sp else 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Swipe / Controller buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { slideUp() },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF201E2E), CircleShape)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Slide Up", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = { slideLeft() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF201E2E), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Slide Left", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(28.dp))
                IconButton(
                    onClick = { slideRight() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF201E2E), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Slide Right", tint = Color.White)
                }
            }

            IconButton(
                onClick = { slideDown() },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF201E2E), CircleShape)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Slide Down", tint = Color.White)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { restartGame() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C2C))
            ) {
                Text("Restart Board", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Exit",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onExit() }
        )
    }
}

// ---------------------------------------------------------
// 11. NEON GUESS GAME (تخمين الأرقام)
// ---------------------------------------------------------
@Composable
fun NeonGuessGame(accentColor: Color, onExit: () -> Unit) {
    var secretNumber by remember { mutableStateOf(Random.nextInt(1, 101)) }
    var guessInput by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var gameStatus by remember { mutableStateOf("خمّن الرقم السري من 1 إلى 100!") }
    var statusColor by remember { mutableStateOf(Color.White) }
    var proximityText by remember { mutableStateOf("") }
    var proximityColor by remember { mutableStateOf(Color.Gray) }
    var gameOver by remember { mutableStateOf(false) }
    var guessHistory by remember { mutableStateOf(listOf<Int>()) }
    var highScore by remember { mutableStateOf(999) } // Lower is better

    fun checkGuess(guess: Int) {
        if (guess < 1 || guess > 100) {
            gameStatus = "الرجاء إدخال رقم بين 1 و 100!"
            statusColor = Color.Yellow
            return
        }

        attempts++
        guessHistory = listOf(guess) + guessHistory
        val diff = kotlin.math.abs(guess - secretNumber)

        // Proximity feedback
        when {
            diff == 0 -> {
                gameStatus = "مبروك! 🎉 أصبت الرقم السري وهو $secretNumber!"
                statusColor = Color(0xFF4CAF50) // Green
                proximityText = "فوز مستحق!"
                proximityColor = Color(0xFF4CAF50)
                gameOver = true
                if (attempts < highScore) {
                    highScore = attempts
                }
            }
            guess < secretNumber -> {
                gameStatus = "الرقم السري أكبر! ⬆️"
                statusColor = Color(0xFFFF9800) // Orange
            }
            else -> {
                gameStatus = "الرقم السري أصغر! ⬇️"
                statusColor = Color(0xFF03A9F4) // Blue
            }
        }

        if (diff > 0) {
            when {
                diff <= 4 -> {
                    proximityText = "حار جداً! قريب للغاية 🔥"
                    proximityColor = Color(0xFFFF3D00)
                }
                diff <= 12 -> {
                    proximityText = "دافئ! تقترب من الهدف ⛅"
                    proximityColor = Color(0xFFFF9100)
                }
                else -> {
                    proximityText = "بارد! ما زلت بعيداً ❄️"
                    proximityColor = Color(0xFF29B6F6)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Neon Guessing 🔮", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            if (highScore < 999) {
                Text("أفضل نتيجة: $highScore محاولات", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 12.sp)
            }
        }

        // Stats Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131122))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = gameStatus,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = statusColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                if (proximityText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(proximityColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, proximityColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = proximityText,
                            color = proximityColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Text(
                    text = "عدد المحاولات الحالية: $attempts",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // Main Interaction Area
        if (!gameOver) {
            // Input TextField
            OutlinedTextField(
                value = guessInput,
                onValueChange = { if (it.length <= 3) guessInput = it.filter { char -> char.isDigit() } },
                label = { Text("أدخل تخمينك هنا", color = Color.Gray) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFF33314B),
                    focusedLabelColor = accentColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.width(180.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // Direct adjustments buttons (for fast touch tuning)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(-10, -1, 1, 10).forEach { value ->
                    Button(
                        onClick = {
                            val current = guessInput.toIntOrNull() ?: 50
                            val next = (current + value).coerceIn(1, 100)
                            guessInput = next.toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1D2E)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (value > 0) "+$value" else value.toString(),
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val guess = guessInput.toIntOrNull()
                    if (guess != null) {
                        checkGuess(guess)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تأكيد التخمين 🎯", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        } else {
            // Restart block
            Button(
                onClick = {
                    secretNumber = Random.nextInt(1, 101)
                    guessInput = ""
                    attempts = 0
                    gameStatus = "خمّن الرقم السري من 1 إلى 100!"
                    statusColor = Color.White
                    proximityText = ""
                    guessHistory = emptyList()
                    gameOver = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("لعب مرة أخرى 🔄", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // History Log
        if (guessHistory.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "سجل التخمينات السابقة:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    guessHistory.forEach { histGuess ->
                        val isCorrect = histGuess == secretNumber
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFF1D1B2D),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isCorrect) Color(0xFF4CAF50) else Color(0xFF33314B),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = histGuess.toString(),
                                color = if (isCorrect) Color(0xFF4CAF50) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "خروج إلى ساحة الألعاب",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .clickable { onExit() }
                .padding(8.dp)
        )
    }
}

// ---------------------------------------------------------
// 12. NEON MATH RUSH GAME (تحدي الحساب السريع)
// ---------------------------------------------------------
@Composable
fun NeonMathRushGame(accentColor: Color, onExit: () -> Unit) {
    var num1 by remember { mutableStateOf(0) }
    var num2 by remember { mutableStateOf(0) }
    var operation by remember { mutableStateOf("+") }
    var displayedAnswer by remember { mutableStateOf(0) }
    var isCorrectAnswer by remember { mutableStateOf(true) }
    
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var gameOverReason by remember { mutableStateOf("") }
    
    var timeLeft by remember { mutableStateOf(3.0f) }

    fun generateEquation() {
        // Choose operations based on difficulty (as score goes up, numbers get slightly larger)
        val ops = if (score < 5) listOf("+", "-") else listOf("+", "-", "*")
        operation = ops.random()
        
        when (operation) {
            "+" -> {
                num1 = Random.nextInt(2, 30)
                num2 = Random.nextInt(2, 30)
                val realAns = num1 + num2
                isCorrectAnswer = Random.nextBoolean()
                displayedAnswer = if (isCorrectAnswer) realAns else realAns + listOf(-3, -2, -1, 1, 2, 3, 5).random()
            }
            "-" -> {
                num1 = Random.nextInt(10, 40)
                num2 = Random.nextInt(2, num1)
                val realAns = num1 - num2
                isCorrectAnswer = Random.nextBoolean()
                displayedAnswer = if (isCorrectAnswer) realAns else realAns + listOf(-3, -2, -1, 1, 2, 3).random()
            }
            "*" -> {
                num1 = Random.nextInt(2, 10)
                num2 = Random.nextInt(2, 10)
                val realAns = num1 * num2
                isCorrectAnswer = Random.nextBoolean()
                displayedAnswer = if (isCorrectAnswer) realAns else realAns + listOf(-4, -2, 2, 4, 1).random()
            }
        }
    }

    fun startGame() {
        score = 0
        isGameOver = false
        isPlaying = true
        generateEquation()
    }

    fun handleAnswer(userChoice: Boolean) {
        if (userChoice == isCorrectAnswer) {
            score++
            if (score > highScore) {
                highScore = score
            }
            generateEquation()
        } else {
            gameOverReason = "إجابة خاطئة! ❌"
            isGameOver = true
            isPlaying = false
        }
    }

    LaunchedEffect(isPlaying, num1, num2, operation, displayedAnswer) {
        if (isPlaying) {
            timeLeft = 3.0f
            // Fast ticking timer loop
            while (timeLeft > 0f) {
                delay(30)
                timeLeft -= 0.033f
            }
            // If loop finishes, timeout!
            gameOverReason = "انتهى الوقت! ⏱️"
            isGameOver = true
            isPlaying = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Math Rush ⚡", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("أعلى نتيجة: $highScore", fontWeight = FontWeight.Bold, color = Color(0xFFFF007F), fontSize = 14.sp)
        }

        if (isPlaying) {
            // Score Display
            Text(
                text = "النقاط: $score",
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                fontSize = 24.sp
            )

            // Timer Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1C2C))
            ) {
                val progressWidth = (timeLeft / 3.0f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressWidth)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(accentColor, Color(0xFFFF007F))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Equation Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(2.dp, accentColor, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF100F1B))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$num1 $operation $num2",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "= $displayedAnswer",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // True / False Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Correct Button
                Button(
                    onClick = { handleAnswer(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "True", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("صح (True)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // False Button
                Button(
                    onClick = { handleAnswer(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "False", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خطأ (False)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else {
            // Welcome or Game Over Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF131122), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF26243A), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    if (isGameOver) {
                        Text(
                            text = "انتهت اللعبة!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = Color(0xFFFF007F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = gameOverReason,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "نقاطك: $score",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = accentColor
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Math",
                            tint = accentColor,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "تحدي الحساب السريع",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "حل المعادلات في أقل من 3 ثوانٍ لكل سؤال. حافظ على تركيزك!",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { startGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isGameOver) "العب مرة أخرى 🔄" else "ابدأ التحدي الآن 🚀",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "خروج إلى ساحة الألعاب",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .clickable { onExit() }
                .padding(8.dp)
        )
    }
}

// ---------------------------------------------------------
// 13. NEON MARKET SIMULATOR (محاكي البورصة والتداول)
// ---------------------------------------------------------
@Composable
fun NeonMarketSimGame(accentColor: Color, onExit: () -> Unit) {
    // Assets data
    val assetNames = listOf("GLOW" to "تكنولوجيا التوهج", "CYBR" to "سيبر كوين", "NEON" to "الذهب النيوني")
    
    // Game States
    var cash by remember { mutableStateOf(10000.0) }
    var portfolio by remember { mutableStateOf(mapOf("GLOW" to 0, "CYBR" to 0, "NEON" to 0)) }
    var prices by remember { mutableStateOf(mapOf("GLOW" to 250.0, "CYBR" to 85.0, "NEON" to 1200.0)) }
    var histories by remember { 
        mutableStateOf(
            mapOf(
                "GLOW" to listOf(210.0, 220.0, 240.0, 230.0, 250.0),
                "CYBR" to listOf(95.0, 90.0, 82.0, 88.0, 85.0),
                "NEON" to listOf(1180.0, 1190.0, 1210.0, 1195.0, 1200.0)
            )
        )
    }
    
    var selectedAsset by remember { mutableStateOf("GLOW") }
    var day by remember { mutableStateOf(1) }
    var newsHeadline by remember { mutableStateOf("ابدأ يومك الأول في التداول بحكمة وحاول مضاعفة ثروتك!") }
    var newsColor by remember { mutableStateOf(Color.White) }
    
    // Quick trade input
    var tradeAmountInput by remember { mutableStateOf("1") }
    
    // Statistics
    val assetPrice = prices[selectedAsset] ?: 1.0
    val assetCount = portfolio[selectedAsset] ?: 0
    val totalAssetsValue = portfolio.entries.sumOf { (key, qty) -> qty * (prices[key] ?: 0.0) }
    val netWorth = cash + totalAssetsValue
    val startWorth = 10000.0
    val profitLoss = netWorth - startWorth
    val isProfit = profitLoss >= 0

    // News generator
    fun generateNextDay() {
        day++
        
        // Random price fluctuations
        val newPrices = prices.toMutableMap()
        val newHistories = histories.toMutableMap()
        
        // Pick a dynamic market event
        val randomEvent = Random.nextInt(0, 10)
        var eventText = "يوم تداول طبيعي بقلب السوق المالي."
        var targetAsset = ""
        var multiplier = 1.0
        
        when (randomEvent) {
            0 -> {
                targetAsset = "GLOW"
                multiplier = 1.25 + Random.nextDouble(0.0, 0.15)
                eventText = "🔥 طفرة تكنولوجية! تزايد الطلب على شرائح GlowTech بشكل غير مسبوق!"
                newsColor = Color(0xFF4CAF50)
            }
            1 -> {
                targetAsset = "GLOW"
                multiplier = 0.70 - Random.nextDouble(0.0, 0.10)
                eventText = "📉 فضيحة تسريب بيانات! تقرير أمني يكشف ثغرات خطيرة في منصات GlowTech!"
                newsColor = Color(0xFFFF007F)
            }
            2 -> {
                targetAsset = "CYBR"
                multiplier = 1.30 + Random.nextDouble(0.0, 0.20)
                eventText = "🚀 تغريدة من إيلون ماسك الافتراضي تدعم عملة سيبر كوين CYBR بقوة!"
                newsColor = Color(0xFF03A9F4)
            }
            3 -> {
                targetAsset = "CYBR"
                multiplier = 0.65 - Random.nextDouble(0.0, 0.10)
                eventText = "⚠️ حظر مفاجئ! بنك مركزي كبير يفرض قيوداً صارمة على تداولات CYBR!"
                newsColor = Color(0xFFFF9800)
            }
            4 -> {
                targetAsset = "NEON"
                multiplier = 1.15 + Random.nextDouble(0.0, 0.05)
                eventText = "🪙 الملاذ الآمن! اضطرابات اقتصادية عامة تجعل المستثمرين يهرعون لشراء الذهب النيوني NEON!"
                newsColor = Color(0xFFFFD700)
            }
            5 -> {
                targetAsset = "NEON"
                multiplier = 0.90 - Random.nextDouble(0.0, 0.05)
                eventText = "📉 استقرار مالي عام يدفع المتداولين لبيع الذهب النيوني NEON والتوجه للأصول الخطرة."
                newsColor = Color.LightGray
            }
            6 -> {
                eventText = "📈 انتعاش عام في الأسواق المالية وجميع الأسهم تكتسي باللون الأخضر اليوم!"
                newsColor = Color(0xFF4CAF50)
            }
            7 -> {
                eventText = "🛑 هبوط مفاجئ وتصحيح عنيف لجميع الأصول والعملات الرقمية!"
                newsColor = Color(0xFFFF007F)
            }
            else -> {
                eventText = listOf(
                    "📊 تقرير إيجابي للغاية عن معدل الفائدة الافتراضي ينعش التداولات.",
                    "📰 شائعات عن استحواذ عملاق التقنية على شركات النيون الناشئة.",
                    "💼 السيولة تتدفق من جديد وصناع السوق ينفذون صفقات شراء ضخمة."
                ).random()
                newsColor = Color.White
            }
        }
        
        newsHeadline = eventText

        // Apply fluctuation to all assets
        prices.forEach { (key, price) ->
            var fluctuation = if (key == "GLOW") {
                Random.nextDouble(0.85, 1.18) // High volatility
            } else if (key == "CYBR") {
                Random.nextDouble(0.80, 1.25) // Super High volatility
            } else {
                Random.nextDouble(0.96, 1.04) // Safe / stable
            }
            
            // Apply event modifier if matches
            if (key == targetAsset) {
                fluctuation = multiplier
            } else if (randomEvent == 6) {
                fluctuation *= 1.10
            } else if (randomEvent == 7) {
                fluctuation *= 0.85
            }
            
            val newPrice = (price * fluctuation).coerceAtLeast(5.0) // Minimum price is $5
            val formattedPrice = kotlin.math.round(newPrice * 100) / 100.0
            newPrices[key] = formattedPrice
            
            // Update history
            val currentHistory = histories[key] ?: emptyList()
            val nextHistory = (currentHistory + formattedPrice).takeLast(8)
            newHistories[key] = nextHistory
        }
        
        prices = newPrices
        histories = newHistories
    }

    fun buyAsset(quantity: Int) {
        if (quantity <= 0) return
        val cost = quantity * assetPrice
        if (cash >= cost) {
            cash -= cost
            val currentQty = portfolio[selectedAsset] ?: 0
            portfolio = portfolio.toMutableMap().apply { put(selectedAsset, currentQty + quantity) }
        }
    }

    fun sellAsset(quantity: Int) {
        if (quantity <= 0) return
        val currentQty = portfolio[selectedAsset] ?: 0
        if (currentQty >= quantity) {
            cash += quantity * assetPrice
            portfolio = portfolio.toMutableMap().apply { put(selectedAsset, currentQty - quantity) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Exit & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Neon Market Simulator 📈", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text(
                text = "اليوم: $day",
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Net worth Portfolio Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131122)),
            border = BorderStroke(1.dp, Color(0xFF26243A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إجمالي المحفظة المالية (Net Worth)", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = if (isProfit) "+${"%.2f".format(profitLoss)}$" else "${"%.2f".format(profitLoss)}$",
                        color = if (isProfit) Color(0xFF4CAF50) else Color(0xFFFF007F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "${"%.2f".format(netWorth)}$",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("السيولة النقدية (Cash)", color = Color.Gray, fontSize = 11.sp)
                        Text("${"%.2f".format(cash)}$", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("قيمة الأسهم والأصول", color = Color.Gray, fontSize = 11.sp)
                        Text("${"%.2f".format(totalAssetsValue)}$", color = accentColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Breaking News Ticker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF200F1B)),
            border = BorderStroke(1.dp, Color(0xFFFF007F).copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFF007F), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("عاجل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = newsHeadline,
                    color = newsColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Asset Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            assetNames.forEach { (symbol, arName) ->
                val isSel = selectedAsset == symbol
                val price = prices[symbol] ?: 0.0
                val quantityOwned = portfolio[symbol] ?: 0
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedAsset = symbol },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) accentColor.copy(alpha = 0.15f) else Color(0xFF100F1B)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSel) accentColor else Color(0xFF26243A)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(symbol, fontWeight = FontWeight.Bold, color = if (isSel) accentColor else Color.White, fontSize = 14.sp)
                        Text(arName, color = Color.Gray, fontSize = 10.sp)
                        Text("${"%.2f".format(price)}$", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        if (quantityOwned > 0) {
                            Text("تملك: $quantityOwned", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Mini Line Chart for Selected Asset
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0A15)),
            border = BorderStroke(1.dp, Color(0xFF201E35))
        ) {
            val chartHistory = histories[selectedAsset] ?: emptyList()
            if (chartHistory.isNotEmpty()) {
                val maxVal = chartHistory.maxOrNull() ?: 1.0
                val minVal = chartHistory.minOrNull() ?: 0.0
                val range = if (maxVal == minVal) 1.0 else maxVal - minVal

                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "مخطط حركة السعر - $selectedAsset",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val numPoints = chartHistory.size
                            
                            // Draw grid lines
                            val gridLinesCount = 3
                            for (i in 0..gridLinesCount) {
                                val gridY = h * (i.toFloat() / gridLinesCount)
                                drawLine(
                                    color = Color(0xFF23213B),
                                    start = Offset(0f, gridY),
                                    end = Offset(w, gridY),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw trend line
                            val pathPoints = chartHistory.mapIndexed { idx, valPrice ->
                                val x = w * (idx.toFloat() / (numPoints - 1).coerceAtLeast(1))
                                val y = h - ((valPrice - minVal) / range).toFloat() * h
                                Offset(x, y)
                            }

                            // Render points & curves
                            for (i in 0 until pathPoints.size - 1) {
                                drawLine(
                                    color = accentColor,
                                    start = pathPoints[i],
                                    end = pathPoints[i + 1],
                                    strokeWidth = 4f
                                )
                            }
                            
                            // Render dots on points
                            pathPoints.forEach { pt ->
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131122)),
            border = BorderStroke(1.dp, Color(0xFF26243A))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "تنفيذ الصفقات ($selectedAsset)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Buy 1
                    Button(
                        onClick = { buyAsset(tradeAmountInput.toIntOrNull() ?: 1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("شراء", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Sell 1
                    Button(
                        onClick = { sellAsset(tradeAmountInput.toIntOrNull() ?: 1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("بيع", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Quick trade settings & New Day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Input for custom trade volume
                    OutlinedTextField(
                        value = tradeAmountInput,
                        onValueChange = { tradeAmountInput = it.filter { c -> c.isDigit() } },
                        label = { Text("الكمية", color = Color.Gray, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF33314B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // All in button
                    Button(
                        onClick = {
                            val maxToBuy = (cash / assetPrice).toInt()
                            if (maxToBuy > 0) {
                                tradeAmountInput = maxToBuy.toString()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C2E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("أقصى شراء", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    // Sell all button
                    Button(
                        onClick = {
                            if (assetCount > 0) {
                                tradeAmountInput = assetCount.toString()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C2E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("بيع الكل", color = Color(0xFFFF007F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // New Day Button
                Button(
                    onClick = { generateNextDay() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Next Day", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إنهاء اليوم والانتقال لليوم التالي ➡️", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Exit
        Text(
            text = "خروج إلى ساحة الألعاب",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .clickable { onExit() }
                .align(Alignment.CenterHorizontally)
                .padding(8.dp)
        )
    }
}

// ---------------------------------------------------------
// 14. COSMIC LANDER SIMULATOR (محاكي الهبوط الفضائي)
// ---------------------------------------------------------
@Composable
fun CosmicLanderGame(accentColor: Color, onExit: () -> Unit) {
    // Physics constants
    val gravity = 1.8f // Moon gravity
    val thrustForce = 4.2f
    val dt = 0.05f

    // Lander parameters
    var altitude by remember { mutableStateOf(350f) } // Initial height
    var velocityY by remember { mutableStateOf(10f) } // Descending speed
    var lateralPosition by remember { mutableStateOf(180f) } // X Position
    var velocityX by remember { mutableStateOf(0f) } // X Speed
    var fuel by remember { mutableStateOf(100f) } // Fuel left

    // Environmental factors
    var windDirectionIsLeft by remember { mutableStateOf(Random.nextBoolean()) }
    var windForceLevel by remember { mutableStateOf(Random.nextInt(0, 3)) } // 0: None, 1: Weak, 2: Strong
    val windAcceleration = if (windForceLevel == 0) 0f else {
        (if (windDirectionIsLeft) -0.6f else 0.6f) * windForceLevel
    }

    // Game stats
    var score by remember { mutableStateOf(0) }
    var isLandingActive by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("اضغط ابدأ لبدء الهبوط!") }
    var resultColor by remember { mutableStateOf(Color.White) }
    var isGameOver by remember { mutableStateOf(false) }
    var didCrash by remember { mutableStateOf(false) }

    fun startLanderSim() {
        altitude = 350f
        velocityY = 5f + Random.nextFloat() * 5f
        lateralPosition = 120f + Random.nextFloat() * 120f
        velocityX = (Random.nextFloat() - 0.5f) * 4f
        fuel = 100f
        isGameOver = false
        didCrash = false
        isLandingActive = true
        resultText = "أنت تطير الآن! حافظ على سرعة هبوط عمودية آمنة (أقل من 5.0)!"
        resultColor = Color.White
        windDirectionIsLeft = Random.nextBoolean()
        windForceLevel = Random.nextInt(0, 3)
    }

    // Physical Loop
    LaunchedEffect(isLandingActive) {
        while (isLandingActive) {
            delay(50)
            
            // Apply environment wind
            velocityX += windAcceleration * dt
            
            // Apply gravity
            velocityY += gravity * dt
            
            // Move lander
            altitude -= velocityY * dt * 10f // scaled for visuals
            lateralPosition += velocityX * dt * 15f
            
            // Check boundary
            if (lateralPosition < 0f) lateralPosition = 0f
            if (lateralPosition > 360f) lateralPosition = 360f

            // Land pad checks
            if (altitude <= 30f) {
                altitude = 30f
                isLandingActive = false
                isGameOver = true
                
                // Pad boundary check: Center pad is roughly between 130f and 230f
                val landedOnPad = lateralPosition in 110f..250f
                
                if (!landedOnPad) {
                    didCrash = true
                    resultText = "تحطمت! لقد هبطت خارج المنصة الآمنة. 💥"
                    resultColor = Color(0xFFFF007F)
                } else if (velocityY > 5.0f) {
                    didCrash = true
                    resultText = "تحطمت! سرعة الهبوط كانت عالية جداً: ${"%.1f".format(velocityY)} m/s! 💥"
                    resultColor = Color(0xFFFF007F)
                } else if (kotlin.math.abs(velocityX) > 3.0f) {
                    didCrash = true
                    resultText = "تحطمت! انحرفت المركبة جانبياً بسرعة ${"%.1f".format(velocityX)} m/s! 💥"
                    resultColor = Color(0xFFFF007F)
                } else {
                    didCrash = false
                    score += 100 + fuel.toInt()
                    resultText = "هبوط ناجح ومثالي ومبهر! 🎉 +${100 + fuel.toInt()} نقطة"
                    resultColor = Color(0xFF4CAF50)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cosmic Lander 🚀", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("النقاط: $score", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 14.sp)
        }

        // Live HUD Metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131122)),
            border = BorderStroke(1.dp, Color(0xFF26243A))
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الارتفاع", color = Color.Gray, fontSize = 10.sp)
                    Text("${"%.0f".format(altitude)}m", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("السرعة العمودية", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        "${"%.1f".format(velocityY)} m/s",
                        color = if (velocityY > 5.0f) Color(0xFFFF007F) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("السرعة الأفقية", color = Color.Gray, fontSize = 10.sp)
                    Text("${"%.1f".format(velocityX)} m/s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الوقود المتبقي", color = Color.Gray, fontSize = 10.sp)
                    Text("${"%.0f".format(fuel)}%", color = if (fuel < 20) Color.Red else Color.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Physics Canvas Simulation Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF070512))
                .border(2.dp, Color(0xFF19172B), RoundedCornerShape(20.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw some static starfield background
                val starSeeds = listOf(0.1f to 0.2f, 0.4f to 0.15f, 0.8f to 0.3f, 0.25f to 0.6f, 0.7f to 0.7f, 0.9f to 0.55f, 0.15f to 0.85f, 0.55f to 0.88f)
                starSeeds.forEach { (sx, sy) ->
                    drawCircle(Color.White.copy(alpha = 0.4f), radius = 2f, center = Offset(sx * w, sy * h))
                }

                // Landing Platform Area (Center platform bottom)
                // Drawing landing pad in glow neon line
                val padStartX = 110f / 360f * w
                val padEndX = 250f / 360f * w
                val padY = h - 30f

                // Under-pad structure
                drawRect(
                    color = Color(0xFF1E1A3D),
                    topLeft = Offset(padStartX, padY),
                    size = Size(padEndX - padStartX, 30f)
                )

                // Neon Green safe pad line
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(padStartX, padY),
                    end = Offset(padEndX, padY),
                    strokeWidth = 6f
                )

                // Hazard lights on sides of the pad
                drawCircle(Color.Green, radius = 5f, center = Offset(padStartX, padY))
                drawCircle(Color.Green, radius = 5f, center = Offset(padEndX, padY))

                // DRAW THE SHIP
                val shipX = lateralPosition / 360f * w
                val shipY = h - altitude

                if (isGameOver && didCrash) {
                    // Draw crash explosion
                    drawCircle(
                        color = Color(0xFFFF007F),
                        radius = 25f,
                        center = Offset(shipX, h - 30f)
                    )
                    drawCircle(
                        color = Color(0xFFFF9800),
                        radius = 15f,
                        center = Offset(shipX, h - 30f)
                    )
                } else {
                    // Draw Lander Craft (Triangle body + landing legs + engine flame)
                    val shipPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(shipX, shipY - 18f) // top
                        lineTo(shipX - 12f, shipY + 8f) // bottom left
                        lineTo(shipX + 12f, shipY + 8f) // bottom right
                        close()
                    }
                    drawPath(
                        path = shipPath,
                        color = accentColor
                    )

                    // Landing Legs
                    drawLine(accentColor, Offset(shipX - 12f, shipY + 8f), Offset(shipX - 18f, shipY + 16f), strokeWidth = 3f)
                    drawLine(accentColor, Offset(shipX + 12f, shipY + 8f), Offset(shipX + 18f, shipY + 16f), strokeWidth = 3f)
                    // Leg pads
                    drawCircle(Color.White, radius = 3f, center = Offset(shipX - 18f, shipY + 16f))
                    drawCircle(Color.White, radius = 3f, center = Offset(shipX + 18f, shipY + 16f))

                    // Draw flame if fuel > 0 and user is interacting/applying thrust
                }
            }

            // Wind indicator badge on Canvas
            if (windForceLevel > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF03A9F4).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "الرياح الجانبية: " + (if (windDirectionIsLeft) "⬅️ يسار" else "➡️ يمين") + " (${if (windForceLevel == 1) "خفيفة" else "قوية"})",
                        color = Color(0xFF03A9F4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // System notification / Feedback Text
        Text(
            text = resultText,
            color = resultColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Game Controls Panel
        if (isLandingActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Thrust Left
                Button(
                    onClick = {
                        if (fuel > 0) {
                            fuel = (fuel - 2).coerceAtLeast(0f)
                            velocityX -= 1.8f
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("دفع لليسار ◀️", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // MAIN THRUST UP
                Button(
                    onClick = {
                        if (fuel > 0) {
                            fuel = (fuel - 5).coerceAtLeast(0f)
                            velocityY = (velocityY - thrustForce).coerceAtLeast(-15f)
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = "Thrust", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("دفع للأعلى 🔥", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }

                // Thrust Right
                Button(
                    onClick = {
                        if (fuel > 0) {
                            fuel = (fuel - 2).coerceAtLeast(0f)
                            velocityX += 1.8f
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("▶️ دفع لليمين", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        } else {
            // Start / Restart Simulation Button
            Button(
                onClick = { startLanderSim() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGameOver && !didCrash) Color(0xFF4CAF50) else accentColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isGameOver) "محاكاة هبوط جديدة 🔄" else "ابدأ محاكاة الهبوط 🚀",
                    color = if (isGameOver && !didCrash) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Back out text
        Text(
            text = "خروج إلى ساحة الألعاب",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .clickable { onExit() }
                .padding(8.dp)
        )
    }
}

