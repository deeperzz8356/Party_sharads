package com.dumbcharades.app

/*
THESIS: A sunlit game-show cue wall makes seventeen games readable at party distance and refuses the dark list-of-cards dashboard.
OWN-WORLD: Warm white stage, ink-black type, square cobalt/pink/yellow/mint/tangerine cue cards, compact radii, and bold category bands.
STORY: Pick a category, recognize a game, learn its rules, add the people in the room, and start without administrative setup.
FIRST VIEWPORT: PARTY MODE sits above category bands and a two-column wall of square game cards; the cards are the navigation.
FORM: Game-show cue wall, assigned grounded direction 6, seed 8b231192.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, and DESIGN.md
*/

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.provider.MediaStore
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dumbcharades.app.game.TiltController
import com.dumbcharades.app.game.TiltGesture
import com.dumbcharades.app.model.GameMode
import com.dumbcharades.app.model.GameType
import com.dumbcharades.app.model.PromptStatus
import com.dumbcharades.app.ui.AppUiState
import com.dumbcharades.app.ui.AppViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DumbCharadesApp() }
    }
}

private object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val GAMES = "games"
    const val RULES = "rules"
    const val DECKS = "decks"
    const val SETUP = "setup"
    const val READY = "ready"
    const val GAME = "game"
    const val SUMMARY = "summary"
    const val FINAL = "final"
    const val CUSTOM = "custom"
    const val SETTINGS = "settings"
}

private val Ink = Color(0xFF111118)
private val InkSoft = Color(0xFF252533)
private val Paper = Color(0xFFFFFCF5)
private val Pink = Color(0xFFFF3D8D)
private val Acid = Color(0xFFFFD629)
private val Sky = Color(0xFF1F6FFF)
private val Tangerine = Color(0xFFFF7A2F)
private val Green = Color(0xFF42D99A)
private val Yellow = Tangerine
private val Purple = Pink
private val Background = Paper
private val PartyShape = RoundedCornerShape(16.dp)
private val PartyTypography = Typography(
    displayLarge = TextStyle(fontSize = 50.sp, lineHeight = 48.sp, fontWeight = FontWeight.Black, letterSpacing = (-1.2).sp),
    headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black, letterSpacing = (-.6).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
)

@Composable
fun DumbCharadesApp(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.dismissMessage() }
    }
    val colors = when (state.settings.theme) {
        "HIGH_CONTRAST" -> darkColorScheme(primary = Color.White, secondary = Color.Yellow, background = Color.Black, surface = Color.Black)
        else -> lightColorScheme(primary = Pink, onPrimary = Color.White, secondary = Sky, onSecondary = Color.White, tertiary = Tangerine, background = Paper, onBackground = Ink, surface = Color.White, onSurface = Ink, surfaceVariant = Color(0xFFF2EEE5), onSurfaceVariant = Color(0xFF5B5963), outline = Color(0xFFCBC5B9))
    }
    MaterialTheme(colorScheme = colors, typography = PartyTypography, shapes = Shapes(small = RoundedCornerShape(12.dp), medium = PartyShape, large = RoundedCornerShape(30.dp))) {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
            NavHost(nav, Routes.SPLASH, Modifier.padding(padding)) {
                composable(Routes.SPLASH) { SplashScreen(state) { nav.replace(if (state.user == null && !state.settings.guestMode) Routes.AUTH else if (state.settings.onboardingComplete) Routes.HOME else Routes.ONBOARDING) } }
                composable(Routes.AUTH) { AuthScreen(state, viewModel) { nav.replace(if (state.settings.onboardingComplete) Routes.HOME else Routes.ONBOARDING) } }
                composable(Routes.ONBOARDING) { OnboardingScreen { viewModel.completeOnboarding(); nav.replace(Routes.HOME) } }
                composable(Routes.HOME) { HomeScreen(state, onPlay = { nav.navigate(Routes.GAMES) }, onCustom = { nav.navigate(Routes.CUSTOM) }, onSettings = { nav.navigate(Routes.SETTINGS) }, onResume = { nav.navigate(Routes.READY) }) }
                composable(Routes.GAMES) {
                    GameHubScreen(
                        choose = { game -> viewModel.chooseGame(game); nav.navigate(Routes.RULES) },
                        partyMix = {
                            viewModel.choosePlaylist(listOf(GameType.CHARADES, GameType.BLANK_AGENT, GameType.CROWD_TRAP, GameType.IMPOSTOR_MIME, GameType.SIGNAL_LINE, GameType.BOMB_DEFUSAL, GameType.REVERSE_CHARADES, GameType.SOUND_EFFECTS))
                            nav.navigate(Routes.DECKS)
                        },
                    )
                }
                composable(Routes.RULES) { GameRulesScreen(state.matchSettings.gameType, onContinue = { nav.navigate(Routes.DECKS) }) }
                composable(Routes.DECKS) { DeckScreen(state, { id -> viewModel.chooseDeck(id); nav.navigate(Routes.SETUP) }, { nav.navigate(Routes.CUSTOM) }) }
                composable(Routes.SETUP) { SetupScreen(state, viewModel) { if (viewModel.prepareMatch()) nav.navigate(Routes.READY) } }
                composable(Routes.READY) { ReadyScreen(state, onStart = { viewModel.startRound(); nav.navigate(Routes.GAME) }, onQuit = { nav.replace(Routes.HOME) }) }
                composable(Routes.GAME) { GameScreen(state, viewModel, finish = { viewModel.finishRound(); nav.replace(Routes.SUMMARY) }, specialFinish = { won -> viewModel.finishSpecialRound(won); nav.replace(Routes.SUMMARY) }, quit = { nav.replace(Routes.HOME) }) }
                composable(Routes.SUMMARY) { SummaryScreen(state, viewModel, onContinue = { if (state.session?.completed == true) nav.replace(Routes.FINAL) else { viewModel.nextRound(); nav.replace(Routes.READY) } }, onEnd = { viewModel.endMatch(); nav.replace(Routes.FINAL) }) }
                composable(Routes.FINAL) { FinalScreen(state, onRematch = { viewModel.rematch(); nav.replace(Routes.READY) }, onHome = { nav.replace(Routes.HOME) }) }
                composable(Routes.CUSTOM) { CustomDeckScreen(viewModel) { id -> viewModel.chooseDeck(id); nav.replace(Routes.SETUP) } }
                composable(Routes.SETTINGS) {
                    SettingsScreen(state, viewModel) {
                        viewModel.signOut()
                        scope.launch { runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) } }
                        nav.replace(Routes.AUTH)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthScreen(state: AppUiState, vm: AppViewModel, onAuthenticated: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var creatingAccount by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Page(if (creatingAccount) "JOIN THE PARTY" else "WELCOME BACK", scrollable = true, eyebrow = "PLAYER PROFILE") {
        Card(Modifier.fillMaxWidth(), shape = PartyShape, colors = CardDefaults.cardColors(containerColor = Sky), border = BorderStroke(2.dp, Ink)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (creatingAccount) "SAVE THE CHAOS." else "ROUND TWO?", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text("Sync custom decks and match history across your devices.", color = Color.White.copy(alpha = .78f))
            }
        }
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, shape = PartyShape, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, shape = PartyShape, modifier = Modifier.fillMaxWidth())
        PartyButton(if (creatingAccount) "CREATE ACCOUNT" else "LET'S GO", onClick = {
            if (creatingAccount) vm.createAccount(email, password, onAuthenticated) else vm.signIn(email, password, onAuthenticated)
        }, enabled = !state.authLoading)
        OutlinedButton(
            onClick = {
                scope.launch {
                    runCatching {
                        val clientIdResource = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                        require(clientIdResource != 0) { "Google Sign-In is not configured" }
                        val googleOption = GetSignInWithGoogleOption.Builder(context.getString(clientIdResource)).build()
                        val request = GetCredentialRequest.Builder().addCredentialOption(googleOption).build()
                        val credential = CredentialManager.create(context).getCredential(context, request).credential
                        require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            "Google did not return an ID token"
                        }
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    }.onSuccess { vm.signInWithGoogle(it, onAuthenticated) }
                        .onFailure { vm.reportAuthError(it.message) }
                }
            },
            enabled = !state.authLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = PartyShape,
        ) { Text("CONTINUE WITH GOOGLE") }
        TextButton(onClick = { vm.continueAsGuest(onAuthenticated) }, modifier = Modifier.fillMaxWidth()) { Text("PLAY OFFLINE AS GUEST") }
        TextButton(onClick = { creatingAccount = !creatingAccount }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (creatingAccount) "Already have an account? Sign in" else "New here? Create an account")
        }
        if (state.authLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

private fun NavController.replace(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { inclusive = true }
    launchSingleTop = true
}

@Composable
private fun SplashScreen(state: AppUiState, ready: () -> Unit) {
    LaunchedEffect(state.loading) { if (!state.loading) ready() }
    Box(Modifier.fillMaxSize().background(Pink), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Acid, size.minDimension * .32f, Offset(size.width * .18f, size.height * .2f))
            drawCircle(Sky, size.minDimension * .26f, Offset(size.width * .86f, size.height * .8f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DUMB\nCHARADES", style = MaterialTheme.typography.displayLarge, color = Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp)); CircularProgressIndicator(color = Acid, strokeWidth = 6.dp)
        }
    }
}

@Composable
private fun OnboardingScreen(onDone: () -> Unit) {
    var page by remember { mutableStateOf(0) }
    val titles = listOf("Hold it high", "Tilt to score", "Mime. Don't blab.")
    val bodies = listOf("Put the phone against your forehead so everyone else can see the prompt.", "Tilt down when they guess it. Tilt up when the clue is hopeless.", "Act, gesture and make a fool of yourself. Just don't say the answer.")
    val symbols = listOf("UP", "TILT", "SHH")
    Page(title = "HOW TO PLAY") {
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = listOf(Sky, Acid, Tangerine)[page], modifier = Modifier.size(150.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(symbols[page], fontSize = 32.sp, color = Ink, fontWeight = FontWeight.Black) }
        }
        Text("${page + 1} OF 3", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
        Text(titles[page], style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Text(bodies[page], style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index -> Box(Modifier.height(8.dp).width(if (index == page) 34.dp else 8.dp).background(if (index == page) Pink else MaterialTheme.colorScheme.outline, CircleShape)) }
        }
        Spacer(Modifier.weight(1f))
        PartyButton(if (page < 2) "GOT IT" else "PLAY AS GUEST", onClick = { if (page < 2) page++ else onDone() })
    }
}

@Composable
private fun HomeScreen(state: AppUiState, onPlay: () -> Unit, onCustom: () -> Unit, onSettings: () -> Unit, onResume: () -> Unit) {
    Page(title = "PARTY MODE", eyebrow = "17 GAMES. ZERO DIGNITY.") {
        Card(Modifier.fillMaxWidth(), shape = PartyShape, colors = CardDefaults.cardColors(containerColor = Pink), border = BorderStroke(2.dp, Color.White.copy(alpha = .75f))) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PICK A GAME.\nCAUSE CHAOS.", style = MaterialTheme.typography.displayLarge, color = Ink)
                Text("Act, bluff, draw, make noise or beat the bomb.", color = Ink, fontSize = 18.sp)
                Button(onClick = onPlay, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = Acid, contentColor = Ink), shape = PartyShape) { Text("OPEN GAME HUB", fontSize = 19.sp, fontWeight = FontWeight.Black) }
            }
        }
        if (state.session?.completed == false) OutlinedButton(onClick = onResume, modifier = Modifier.fillMaxWidth()) { Text("RESUME MATCH") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBadge("${state.stats.gamesPlayed}", "games", Modifier.weight(1f))
            StatBadge("${state.stats.totalGuessed}", "guessed", Modifier.weight(1f))
            StatBadge("${state.stats.accuracyPercent}%", "accuracy", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeCard("PLAY", "DECKS", "${state.decks.size} ready", Sky, onPlay, Modifier.weight(1f))
            HomeCard("NEW", "MAKE ONE", "Your inside jokes", Tangerine, onCustom, Modifier.weight(1f))
        }
        HomeCard("SET", "SETTINGS", "Tilt, sound and themes", Acid, onSettings, Modifier.fillMaxWidth())
    }
}

private data class HubGame(val type: GameType, val mark: String, val title: String, val subtitle: String, val color: Color, val category: String)
private data class FakeOption(val text: String, val author: Int? = null, val correct: Boolean = false)
private data class DrawMark(val point: Offset?, val color: Color, val width: Float)

@Composable
private fun GameHubScreen(choose: (GameType) -> Unit, partyMix: () -> Unit) {
    val games = listOf(
        HubGame(GameType.CHARADES, "ACT", "DUMB CHARADES", "Act it out.", Sky, "ACT"),
        HubGame(GameType.IMPOSTOR_MIME, "MIM", "IMPOSTOR MIME", "Blend into the action.", Pink, "ACT"),
        HubGame(GameType.REVERSE_CHARADES, "REV", "REVERSE", "The room performs.", Acid, "ACT"),
        HubGame(GameType.SOUND_EFFECTS, "SFX", "SOUND ONLY", "Make noise, not words.", Green, "ACT"),
        HubGame(GameType.BLANK_AGENT, "BLK", "BLANK AGENT", "Clue without knowing.", Color(0xFFE7DFFF), "TALK"),
        HubGame(GameType.INSIDE_VOICE, "YES?", "INSIDE VOICE", "Find the hidden influence.", Sky, "TALK"),
        HubGame(GameType.CROWD_TRAP, "VOTE", "CROWD TRAP", "Move with the majority.", Tangerine, "TALK"),
        HubGame(GameType.SIGNAL_LINE, "LINE", "SIGNAL LINE", "Read the room.", Green, "TALK"),
        HubGame(GameType.TRUTH_OR_DARE, "T/D", "TRUTH OR DARE", "Answer or do it.", Pink, "TALK"),
        HubGame(GameType.MOST_LIKELY, "WHO", "MOST LIKELY", "Pick who fits.", Acid, "TALK"),
        HubGame(GameType.NEVER_HAVE_I_EVER, "5X", "NEVER HAVE I EVER", "Five hearts to survive.", Tangerine, "TALK"),
        HubGame(GameType.WOULD_YOU_RATHER, "A/B", "WOULD YOU RATHER", "Choose your side.", Sky, "TALK"),
        HubGame(GameType.MIME_TELEPHONE, "CHAIN", "MIME TELEPHONE", "Pass the meaning.", Tangerine, "CREATE"),
        HubGame(GameType.FAKE_IT, "LIE", "FAKE IT", "Bluff the room.", Pink, "CREATE"),
        HubGame(GameType.DRAW_AND_GUESS, "DRAW", "DRAW & GUESS", "Sketch against time.", Green, "CREATE"),
        HubGame(GameType.BOMB_DEFUSAL, "5S", "5-SECOND BOMB", "Name three. Pass fast.", Acid, "CREATE"),
        HubGame(GameType.TWIN_WORD_SPY, "SPY", "TWIN WORD SPY", "Spot the odd word.", Sky, "CREATE"),
    )
    val categories = listOf(Triple("ACT", "ACTING GAMES", Sky), Triple("TALK", "SOCIAL + TALK", Pink), Triple("CREATE", "TRIVIA + DRAWING", Acid))
    var selectedCategory by remember { mutableStateOf("ACT") }
    Page("PARTY MODE") {
        Card(Modifier.fillMaxWidth().clickable(onClick = partyMix), shape = PartyShape, colors = CardDefaults.cardColors(containerColor = Ink)) {
            Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(Acid, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("MIX", color = Ink, fontWeight = FontWeight.Black) }
                Column { Text("PARTY MIX", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black); Text("Eight modes. One playlist.", color = Color.White) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { (id, title, color) ->
                FilterChip(
                    selected = selectedCategory == id,
                    onClick = { selectedCategory = id },
                    label = { Text(title.substringBefore(" "), fontWeight = FontWeight.Black) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color, selectedLabelColor = gameForeground(color)),
                )
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val (_, title, color) = categories.first { it.first == selectedCategory }
            item(key = "header-$selectedCategory") { Surface(color = color, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) { Text(title, Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = gameForeground(color), fontWeight = FontWeight.Black, fontSize = 19.sp) } }
            items(games.filter { it.category == selectedCategory }.chunked(2), key = { row -> row.joinToString { it.type.name } }) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { game -> GameTile(game, { choose(game.type) }, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GameTile(game: HubGame, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .96f else 1f, label = "game-tile-press")
    val content = gameForeground(game.color)
    Card(
        modifier.aspectRatio(1f).graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = PartyShape,
        colors = CardDefaults.cardColors(containerColor = game.color),
    ) {
        Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
            GameGlyph(game.type, content)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(game.title, color = content, fontSize = 20.sp, lineHeight = 20.sp, fontWeight = FontWeight.Black)
                Text(game.type.playerLabel(), color = content, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text(game.subtitle, color = content, fontSize = 13.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun GameGlyph(game: GameType, color: Color) {
    Canvas(Modifier.size(48.dp)) {
        val stroke = size.minDimension * .10f
        val c = Offset(size.width / 2f, size.height / 2f)
        val variant = game.ordinal
        drawCircle(color, radius = size.minDimension * .38f, center = c, style = Stroke(stroke))
        when (game) {
            GameType.CHARADES, GameType.REVERSE_CHARADES, GameType.IMPOSTOR_MIME, GameType.MIME_TELEPHONE -> {
                drawCircle(color, size.minDimension * .08f, Offset(size.width * .35f, size.height * .42f))
                drawCircle(color, size.minDimension * .08f, Offset(size.width * .65f, size.height * .42f))
                drawArc(color, 20f, 140f, false, topLeft = Offset(size.width * .27f, size.height * .40f), size = androidx.compose.ui.geometry.Size(size.width * .46f, size.height * .34f), style = Stroke(stroke))
            }
            GameType.SOUND_EFFECTS -> repeat(3) { index ->
                val x = size.width * (.32f + index * .18f)
                val half = size.height * (.12f + index * .055f)
                drawLine(color, Offset(x, c.y - half), Offset(x, c.y + half), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
            GameType.BLANK_AGENT, GameType.INSIDE_VOICE, GameType.TWIN_WORD_SPY -> {
                drawCircle(color, size.minDimension * .10f, Offset(size.width * .38f, size.height * .43f))
                drawLine(color, Offset(size.width * .54f, size.height * .38f), Offset(size.width * .69f, size.height * .60f), stroke)
                drawCircle(color, size.minDimension * .045f, Offset(size.width * .69f, size.height * .69f))
            }
            GameType.CROWD_TRAP, GameType.MOST_LIKELY, GameType.SIGNAL_LINE -> repeat(3) { index ->
                drawCircle(color, size.minDimension * .075f, Offset(size.width * (.32f + index * .18f), size.height * (.42f + (index % 2) * .15f)))
            }
            GameType.TRUTH_OR_DARE, GameType.WOULD_YOU_RATHER -> {
                drawLine(color, Offset(size.width * .32f, size.height * .30f), Offset(size.width * .68f, size.height * .70f), stroke)
                drawLine(color, Offset(size.width * .68f, size.height * .30f), Offset(size.width * .32f, size.height * .70f), stroke)
            }
            GameType.NEVER_HAVE_I_EVER -> repeat(3) { index -> drawCircle(color, size.minDimension * .07f, Offset(size.width * (.32f + index * .18f), c.y)) }
            GameType.FAKE_IT -> {
                drawLine(color, Offset(size.width * .29f, c.y), Offset(size.width * .71f, c.y), stroke)
                drawCircle(color, size.minDimension * .07f, Offset(size.width * .68f, size.height * .36f))
            }
            GameType.DRAW_AND_GUESS -> {
                drawLine(color, Offset(size.width * .30f, size.height * .67f), Offset(size.width * .68f, size.height * .29f), stroke)
                drawCircle(color, size.minDimension * .065f, Offset(size.width * .30f, size.height * .67f))
            }
            GameType.BOMB_DEFUSAL -> {
                drawCircle(color, size.minDimension * .17f, Offset(c.x, size.height * .57f))
                drawLine(color, Offset(c.x, size.height * .38f), Offset(size.width * .65f, size.height * .27f), stroke)
            }
        }
        repeat(variant % 3) { index -> drawCircle(color, size.minDimension * .025f, Offset(size.width * (.38f + index * .12f), size.height * .82f)) }
    }
}

@Composable
private fun GameRulesScreen(game: GameType, onContinue: () -> Unit) {
    val color = game.gameColor()
    val content = gameForeground(color)
    Page(game.displayName(), scrollable = true) {
        Surface(Modifier.fillMaxWidth(), color = color, shape = PartyShape) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(game.playerLabel(), color = content, fontWeight = FontWeight.Black)
                Text(game.teamRule(), color = content, fontSize = 27.sp, lineHeight = 31.sp, fontWeight = FontWeight.Black)
            }
        }
        Text("HOW TO PLAY", Modifier.fillMaxWidth(), fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
        game.rules().forEachIndexed { index, rule ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                Surface(color = color, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Text("${index + 1}", color = content, fontWeight = FontWeight.Black) } }
                Text(rule, Modifier.weight(1f).padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = PartyShape) {
            Text("The app handles the order, timers, private reveals and scoring. You only need the people in the room.", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        PartyButton("CHOOSE A CARD PACK", onContinue)
    }
}

@Composable private fun HomeCard(symbol: String, title: String, subtitle: String, color: Color, click: () -> Unit, modifier: Modifier) {
    Card(modifier.clickable(onClick = click), shape = PartyShape, colors = CardDefaults.cardColors(containerColor = color), border = BorderStroke(2.dp, Ink)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(symbol, fontSize = 28.sp)
            Text(title, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(subtitle, color = Ink.copy(alpha = .68f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun DeckScreen(state: AppUiState, choose: (String) -> Unit, custom: () -> Unit) {
    val game = state.matchSettings.gameType
    val decks = if (state.matchSettings.playlist.isNotEmpty()) state.decks else when (game) {
        GameType.SOUND_EFFECTS -> state.decks.filter { it.title == "Sound Lab" }
        GameType.BOMB_DEFUSAL -> state.decks.filter { it.title == "Five Second Frenzy" }
        GameType.BLANK_AGENT, GameType.INSIDE_VOICE -> state.decks.filter { it.title == "Secret Words" }
        GameType.CROWD_TRAP -> state.decks.filter { it.title == "Crowd Questions" }
        GameType.SIGNAL_LINE -> state.decks.filter { it.title == "Signal Lines" }
        GameType.TRUTH_OR_DARE -> state.decks.filter { it.title in setOf("Truth or Dare", "Truth or Dare After Dark") }
        GameType.MOST_LIKELY -> state.decks.filter { it.title == "Most Likely" }
        GameType.NEVER_HAVE_I_EVER -> state.decks.filter { it.title == "Never Have I Ever" }
        GameType.FAKE_IT -> state.decks.filter { it.title == "Fake It Trivia" }
        GameType.DRAW_AND_GUESS -> state.decks.filter { it.title == "Draw & Guess" }
        GameType.WOULD_YOU_RATHER -> state.decks.filter { it.title == "Would You Rather" }
        GameType.TWIN_WORD_SPY -> state.decks.filter { it.title == "Twin Words" }
        GameType.IMPOSTOR_MIME, GameType.MIME_TELEPHONE -> state.decks.filter { it.title == "Wild Scenes" }
        GameType.REVERSE_CHARADES -> state.decks.filter { it.title in setOf("Actions & Verbs", "Wild Scenes") }
        else -> state.decks
    }.ifEmpty { state.decks }
    Page("PICK A CARD PACK", eyebrow = "${game.displayName()} · ${decks.size} PACKS") {
        OutlinedButton(custom, Modifier.fillMaxWidth().height(54.dp), shape = PartyShape) { Text("MAKE YOUR OWN PACK") }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(decks, key = { it.id }) { deck ->
                val color = when (kotlin.math.abs(deck.id.hashCode()) % 4) { 0 -> Sky; 1 -> Tangerine; 2 -> Acid; else -> Pink }
                Card(Modifier.fillMaxWidth().clickable { choose(deck.id) }, shape = PartyShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(2.dp, color.copy(alpha = .7f))) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.size(54.dp).background(color, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                            Text(deck.title.take(1).uppercase(), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(deck.title, style = MaterialTheme.typography.titleLarge)
                            Text(deck.description, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            if (deck.custom) Text("YOUR DECK", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Text("${deck.cardCount}\nCARDS", textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupScreen(state: AppUiState, vm: AppViewModel, onReady: () -> Unit) {
    val selectedGame = state.matchSettings.playlist.firstOrNull() ?: state.matchSettings.gameType
    val roster = remember { mutableStateListOf<String>().also { list -> list.addAll(state.matchSettings.teams.flatMap { it.players }.distinct()) } }
    var newPlayer by remember { mutableStateOf("") }
    var teamMode by remember { mutableStateOf(selectedGame.prefersTeams()) }
    val accent = selectedGame.gameColor()
    Page("WHO'S PLAYING?") {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Surface(Modifier.fillMaxWidth(), color = accent, shape = PartyShape) {
                    Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (state.matchSettings.playlist.isEmpty()) selectedGame.displayName() else "PARTY MIX", color = gameForeground(accent), fontWeight = FontWeight.Black, fontSize = 23.sp)
                            Text("${roster.size} PLAYERS", color = gameForeground(accent), fontWeight = FontWeight.Bold)
                        }
                        GameGlyph(selectedGame, gameForeground(accent))
                    }
                }
            }
            item {
                PartyPanel("ADD PLAYERS", Sky) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(newPlayer, { newPlayer = it.take(30) }, label = { Text("Player name") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                        Button({ if (newPlayer.isNotBlank() && roster.none { it.equals(newPlayer.trim(), true) }) { roster.add(newPlayer.trim()); newPlayer = "" } }, modifier = Modifier.height(56.dp), enabled = newPlayer.isNotBlank()) { Text("ADD") }
                    }
                    roster.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { player ->
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { roster.remove(player) },
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(player, Modifier.weight(1f), fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("×", color = Pink, fontSize = 22.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    if (roster.isNotEmpty()) Text("Tap a name to remove it", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            item {
                PartyPanel("GAME OPTIONS", Acid) {
                    if (selectedGame.prefersTeams()) {
                        MiniLabel("PLAY FORMAT")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartyChip("ONE GROUP", !teamMode) { teamMode = false }
                            PartyChip("2 AUTO TEAMS", teamMode) { teamMode = true }
                        }
                        if (teamMode && roster.size >= 2) Text("Blue Crew ${(roster.size + 1) / 2}  ·  Pink Crew ${roster.size / 2}", fontWeight = FontWeight.Bold)
                    }
                    if (selectedGame in setOf(GameType.CHARADES, GameType.SOUND_EFFECTS)) {
                        MiniLabel("CONTROLS")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartyChip("FOREHEAD", state.matchSettings.mode == GameMode.FOREHEAD_TILT) { vm.chooseMode(GameMode.FOREHEAD_TILT) }
                            PartyChip("TOUCH", state.matchSettings.mode == GameMode.CLASSIC_TOUCH) { vm.chooseMode(GameMode.CLASSIC_TOUCH) }
                        }
                    }
                    if (selectedGame in setOf(GameType.CHARADES, GameType.REVERSE_CHARADES, GameType.SOUND_EFFECTS)) {
                        MiniLabel("SECONDS")
                        ChipRow(listOf(30, 60, 90, 120), state.matchSettings.timerSeconds, vm::setTimer)
                    }
                    MiniLabel("ROUNDS")
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf(3, 5, 10).forEach { value -> PartyChip("$value", state.matchSettings.totalRounds == value) { vm.setRounds(value) } }
                        PartyChip("∞", state.matchSettings.totalRounds == null) { vm.setRounds(null) }
                    }
                    if (selectedGame in setOf(GameType.CHARADES, GameType.REVERSE_CHARADES, GameType.SOUND_EFFECTS)) {
                        MiniLabel("SKIPS")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartyChip("3", state.matchSettings.skipLimit == 3) { vm.setSkipLimit(3) }
                            PartyChip("UNLIMITED", state.matchSettings.skipLimit == null) { vm.setSkipLimit(null) }
                        }
                    }
                }
            }
        }
        PartyButton("READY TO PLAY", onClick = {
            val finalRoster = (roster + newPlayer.trim()).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
            vm.setRoster(finalRoster, teamMode && selectedGame.prefersTeams())
            onReady()
        }, enabled = roster.size + (if (newPlayer.isBlank()) 0 else 1) >= selectedGame.minimumPlayers())
    }
}

@Composable private fun ChipRow(values: List<Int>, selected: Int, set: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { values.forEach { value -> PartyChip("${value}s", selected == value) { set(value) } } }
}

@Composable
private fun ReadyScreen(state: AppUiState, onStart: () -> Unit, onQuit: () -> Unit) {
    val session = state.session
    val team = session?.settings?.teams?.getOrNull(session.activeTeamIndex)
    val turns = session?.rounds?.count { it.activeTeamId == team?.id } ?: 0
    val actor = team?.players?.getOrNull(turns % (team.players.size.coerceAtLeast(1))) ?: "Player"
    val game = session?.activeGameType ?: GameType.CHARADES
    var countdown by remember { mutableStateOf<Int?>(null) }
    var counting by remember { mutableStateOf(false) }
    LaunchedEffect(counting) {
        if (counting) {
            for (value in 3 downTo 1) { countdown = value; delay(700) }
            countdown = null
            counting = false
            onStart()
        }
    }
    Page(game.displayName(), eyebrow = "ROUND ${(session?.currentRound ?: 0) + 1}") {
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = Acid, modifier = Modifier.size(170.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(countdown?.toString() ?: "PASS", fontSize = if (countdown == null) 34.sp else 84.sp, fontWeight = FontWeight.Black, color = Ink) }
        }
        Text("${team?.name ?: "Team"}'s turn", color = MaterialTheme.colorScheme.secondary, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(actor, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
        Text(game.readyInstruction(), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        PartyButton("START  3 · 2 · 1", { countdown = 3; counting = true }, enabled = !counting)
        TextButton(onQuit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun GameScreen(state: AppUiState, vm: AppViewModel, finish: () -> Unit, specialFinish: (Boolean) -> Unit, quit: () -> Unit) {
    when (state.session?.activeGameType) {
        GameType.IMPOSTOR_MIME -> ImpostorGameScreen(state, specialFinish, quit)
        GameType.BLANK_AGENT -> BlankAgentGameScreen(state, specialFinish, quit)
        GameType.INSIDE_VOICE -> InsideVoiceGameScreen(state, specialFinish, quit)
        GameType.CROWD_TRAP -> CrowdTrapGameScreen(state, specialFinish, quit)
        GameType.SIGNAL_LINE -> SignalLineGameScreen(state, specialFinish, quit)
        GameType.TRUTH_OR_DARE -> TruthOrDareGameScreen(state, specialFinish, quit)
        GameType.MOST_LIKELY -> MostLikelyGameScreen(state, specialFinish, quit)
        GameType.NEVER_HAVE_I_EVER -> NeverHaveIEverGameScreen(state, specialFinish, quit)
        GameType.FAKE_IT -> FakeItGameScreen(state, specialFinish, quit)
        GameType.DRAW_AND_GUESS -> DrawAndGuessGameScreen(state, specialFinish, quit)
        GameType.WOULD_YOU_RATHER -> WouldYouRatherGameScreen(state, specialFinish, quit)
        GameType.TWIN_WORD_SPY -> TwinWordSpyGameScreen(state, specialFinish, quit)
        GameType.MIME_TELEPHONE -> TelephoneGameScreen(state, specialFinish, quit)
        else -> TimedGameScreen(state, vm, finish, quit)
    }
}

@Composable
private fun TimedGameScreen(state: AppUiState, vm: AppViewModel, finish: () -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val game = runtime.session.activeGameType
    val reversePrep = game == GameType.REVERSE_CHARADES && runtime.paused && runtime.results.isEmpty()
    val context = LocalContext.current
    val activity = context as Activity
    val tilt = remember { TiltController(context.applicationContext) }
    val tone = remember(state.settings.soundVolume) { ToneGenerator(AudioManager.STREAM_MUSIC, (state.settings.soundVolume * 100).roundToInt()) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    fun respond(status: PromptStatus) {
        vm.answer(status)
        if (status == PromptStatus.GUESSED) {
            tone.startTone(if (state.settings.soundPack == "ARCADE") ToneGenerator.TONE_DTMF_9 else ToneGenerator.TONE_PROP_ACK, 120)
            vibrator.vibrate(VibrationEffect.createOneShot(70, 180))
        } else {
            tone.startTone(if (state.settings.soundPack == "ARCADE") ToneGenerator.TONE_DTMF_1 else ToneGenerator.TONE_PROP_NACK, 160)
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 60, 70), -1))
        }
    }
    DisposableEffect(runtime.session.settings.mode, state.settings.tiltThreshold) {
        val usesTilt = runtime.session.settings.mode == GameMode.FOREHEAD_TILT || game == GameType.SOUND_EFFECTS
        activity.requestedOrientation = if (usesTilt) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        if (usesTilt && tilt.available) tilt.start(state.settings.tiltThreshold) { gesture ->
            activity.runOnUiThread { respond(if (gesture == TiltGesture.CORRECT) PromptStatus.GUESSED else PromptStatus.SKIPPED) }
        }
        onDispose { tilt.stop(); tone.release(); activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    LaunchedEffect(runtime.remainingSeconds) {
        if (runtime.remainingSeconds <= 0) {
            if (game == GameType.BOMB_DEFUSAL) vm.markBombExplosion()
            finish()
        }
    }
    val urgent = runtime.remainingSeconds <= 10
    Box(Modifier.fillMaxSize().background(if (urgent) Color(0xFF28151B) else MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(if (urgent) Pink.copy(alpha = .18f) else Sky.copy(alpha = .10f), size.minDimension * .42f, Offset(size.width * .84f, size.height * .18f))
        }
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LinearProgressIndicator(
                progress = { runtime.remainingSeconds / runtime.durationSeconds.toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp), color = if (urgent) Pink else Acid,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = if (urgent) Pink else MaterialTheme.colorScheme.surface, shape = CircleShape) {
                    Text("${runtime.remainingSeconds}s", Modifier.padding(horizontal = 18.dp, vertical = 10.dp), fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                Text("${runtime.score} POINTS", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Green)
                TextButton(onClick = vm::pause) { Text(if (runtime.paused) "RESUME" else "PAUSE", fontWeight = FontWeight.Black) }
            }
            Box(Modifier.weight(1f).fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp)).padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (game == GameType.BOMB_DEFUSAL) Text("BOOM", fontSize = 44.sp, fontWeight = FontWeight.Black, color = Pink)
                    Text(if (game == GameType.REVERSE_CHARADES && !reversePrep) "EVERYONE ACT!" else runtime.currentPrompt?.text ?: "All done!", fontSize = 48.sp, lineHeight = 50.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    runtime.currentPrompt?.hint?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
                    if (reversePrep) {
                        Text("Show this card to every actor. The guesser must look away.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Button(vm::pause, colors = ButtonDefaults.buttonColors(containerColor = Acid, contentColor = Ink)) { Text("HAND TO THE GUESSER") }
                    } else if (runtime.paused) Surface(color = Acid, shape = CircleShape) { Text("PAUSED", Modifier.padding(horizontal = 18.dp, vertical = 8.dp), color = Ink, fontWeight = FontWeight.Black) }
                }
            }
            if (!reversePrep) {
                if (runtime.session.settings.penaltyEnabled) TextButton({ respond(PromptStatus.PENALTY) }, Modifier.align(Alignment.CenterHorizontally)) { Text("−1 RULE BREAK") }
                Row(Modifier.fillMaxWidth().height(94.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button({ respond(PromptStatus.SKIPPED) }, Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Tangerine, contentColor = Ink), shape = PartyShape, enabled = !runtime.paused) { Text(if (game == GameType.BOMB_DEFUSAL) "CAN'T NAME 3" else "SKIP  ↑", fontSize = 19.sp, fontWeight = FontWeight.Black) }
                    Button({ respond(PromptStatus.GUESSED) }, Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink), shape = PartyShape, enabled = !runtime.paused) { Text(if (game == GameType.BOMB_DEFUSAL) "PASS BOMB  →" else "GOT IT  ↓", fontSize = 19.sp, fontWeight = FontWeight.Black) }
                }
            }
        }
    }
    if (runtime.paused && !reversePrep) AlertDialog(
        onDismissRequest = vm::pause,
        title = { Text("Round paused") },
        text = { Column { TextButton(vm::pause) { Text("RESUME") }; TextButton({ vm.startRound() }) { Text("RESTART ROUND") }; TextButton(quit) { Text("QUIT TO HOME") } } },
        confirmButton = {},
    )
}

@Composable
private fun ImpostorGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(10)
    val impostorIndex = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(players.size.coerceAtLeast(1)) }
    var playerIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(30) }
    var votedIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(phase) {
        if (phase == 1) {
            while (seconds > 0) { delay(1_000); seconds-- }
            phase = 2
        }
    }
    Page("IMPOSTOR MIME", eyebrow = when (phase) { 0 -> "SECRET CARDS"; 1 -> "EVERYONE MIME"; 2 -> "CAST YOUR VOTE"; else -> "THE REVEAL" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f))
                Text("Pass to", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(players.getOrElse(playerIndex) { "Player" }, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, shape = PartyShape, colors = CardDefaults.cardColors(containerColor = if (revealed && playerIndex == impostorIndex) Pink else Acid)) {
                    Box(Modifier.fillMaxWidth().height(190.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (!revealed) "TAP TO REVEAL" else if (playerIndex == impostorIndex) "YOU ARE THE\nIMPOSTOR" else runtime.currentPrompt?.text.orEmpty(),
                            color = Ink, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        )
                    }
                }
                Text(if (revealed) "Memorize it, then hide the screen." else "Keep this away from everyone else.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton(if (playerIndex == players.lastIndex) "START THE MIME" else "HIDE & PASS", {
                    if (!revealed) return@PartyButton
                    if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false }
                }, enabled = revealed)
            }
            1 -> {
                Spacer(Modifier.weight(1f))
                Text("$seconds", fontSize = 108.sp, lineHeight = 112.sp, fontWeight = FontWeight.Black, color = if (seconds <= 10) Pink else Acid)
                Text("Everyone mimes at the same time. The impostor watches and blends in.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                OutlinedButton({ phase = 2 }, Modifier.fillMaxWidth()) { Text("VOTE NOW") }
            }
            2 -> {
                Text("Who looked like they were guessing?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(players.withIndex().toList(), key = { it.index }) { item ->
                        Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, shape = RoundedCornerShape(18.dp), border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) {
                            Text(item.value, Modifier.padding(18.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                PartyButton("LOCK THE VOTE", { phase = 3 }, enabled = votedIndex != null)
            }
            else -> {
                val caught = votedIndex == impostorIndex
                Spacer(Modifier.weight(1f))
                Text(if (caught) "CAUGHT!" else "ESCAPED!", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink)
                Text("${players.getOrElse(impostorIndex) { "The impostor" }} was the impostor.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Text(if (caught) "The crew earns the point." else "The impostor fooled the room.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton("SEE ROUND RESULT", { complete(caught) })
            }
        }
        if (phase != 3) TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun BlankAgentGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(12)
    val whiteIndex = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(players.size.coerceAtLeast(1)) }
    val clueOrder = remember(runtime.session.id, runtime.session.currentRound) { players.indices.shuffled() }
    val clueConstraint = remember(runtime.session.id, runtime.session.currentRound) { listOf("ONE WORD ONLY", "NO LETTER E", "PERSONAL MEMORY", "SOUND EFFECT ONLY", "GESTURE ONLY").random() }
    var phase by remember { mutableStateOf(0) }
    var playerIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var clueTurn by remember { mutableStateOf(0) }
    var votedIndex by remember { mutableStateOf<Int?>(null) }
    var finalGuess by remember { mutableStateOf("") }
    var crewWon by remember { mutableStateOf(false) }
    val secret = runtime.currentPrompt?.text.orEmpty()
    Page("BLANK AGENT", eyebrow = when (phase) { 0 -> "SECRET WORDS"; 1 -> "ONE CLUE EACH"; 2 -> "FIND THE AGENT"; 3 -> "LAST CHANCE"; else -> "THE REVEAL" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f))
                Text("Pass to", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(players.getOrElse(playerIndex) { "Player" }, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, shape = PartyShape, colors = CardDefaults.cardColors(containerColor = if (revealed && playerIndex == whiteIndex) Color.White else Sky)) {
                    Box(Modifier.fillMaxWidth().height(190.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (!revealed) "TAP TO REVEAL" else if (playerIndex == whiteIndex) "YOU ARE THE\nBLANK AGENT\n\nListen carefully." else "THE WORD IS\n$secret",
                            color = Ink, fontSize = 29.sp, lineHeight = 33.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        )
                    }
                }
                Text("Do not react. Remember your role, hide it, and pass.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton(if (playerIndex == players.lastIndex) "START CLUES" else "HIDE & PASS", {
                    if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false }
                }, enabled = revealed)
            }
            1 -> {
                Spacer(Modifier.weight(1f))
                Text("CLUE ${clueTurn + 1} OF ${players.size}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Surface(color = Tangerine, shape = CircleShape) { Text(clueConstraint, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Ink, fontWeight = FontWeight.Black) }
                Text(players.getOrElse(clueOrder.getOrElse(clueTurn) { 0 }) { "Player" }, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Text("Say one hint about the word. Be useful—but not so obvious that the Blank Agent learns it.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f))
                PartyButton(if (clueTurn == players.lastIndex) "ALL CLUES DONE · VOTE" else "CLUE GIVEN · NEXT", {
                    if (clueTurn == players.lastIndex) phase = 2 else clueTurn++
                })
            }
            2 -> {
                Text("Who never knew the word?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(players.withIndex().toList(), key = { it.index }) { item ->
                        Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, shape = RoundedCornerShape(18.dp), border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) {
                            Text(item.value, Modifier.padding(18.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                PartyButton("LOCK THE VOTE", {
                    if (votedIndex == whiteIndex) phase = 3 else { crewWon = false; phase = 4 }
                }, enabled = votedIndex != null)
            }
            3 -> {
                Text("BLANK AGENT CAUGHT", style = MaterialTheme.typography.headlineMedium, color = Pink)
                Text("${players.getOrElse(whiteIndex) { "Blank Agent" }}, guess the secret word to steal the win.", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(finalGuess, { finalGuess = it.take(60) }, label = { Text("Secret word") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f))
                PartyButton("FINAL ANSWER", {
                    crewWon = !finalGuess.trim().equals(secret.trim(), ignoreCase = true)
                    phase = 4
                }, enabled = finalGuess.isNotBlank())
            }
            else -> {
                val escaped = votedIndex != whiteIndex
                Spacer(Modifier.weight(1f))
                Text(if (crewWon) "CREW WINS" else "AGENT WINS", style = MaterialTheme.typography.displayLarge, color = if (crewWon) Green else Pink, textAlign = TextAlign.Center)
                Text("${players.getOrElse(whiteIndex) { "The agent" }} was the Blank Agent.", fontSize = 21.sp, textAlign = TextAlign.Center)
                Text("The secret word was $secret.", fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                if (!crewWon) Text(if (escaped) "The vote missed." else "The final guess was correct.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton("SEE ROUND RESULT", { complete(crewWon) })
            }
        }
        if (phase < 4) TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun InsideVoiceGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(20)
    val host = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(players.size) }
    val navigator = remember(host) { players.indices.filter { it != host }.random() }
    val distorter = remember(host, navigator) { players.indices.filter { it != host && it != navigator }.random() }
    val secret = runtime.currentPrompt?.text.orEmpty()
    var phase by remember { mutableStateOf(0) }
    var playerIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(90) }
    var solved by remember { mutableStateOf(false) }
    var votedIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(phase) {
        if (phase == 1) {
            while (seconds > 0 && !solved) { delay(1_000); seconds-- }
            phase = 2
        }
    }
    Page("INSIDE VOICE", eyebrow = when (phase) { 0 -> "PRIVATE ROLES"; 1 -> "YES / NO SEARCH"; 2 -> "FIND THE DISTORTER"; else -> "THE REVEAL" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass to", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(players[playerIndex], style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, shape = PartyShape, colors = CardDefaults.cardColors(containerColor = when (playerIndex) { host -> Sky; navigator -> Green; distorter -> Pink; else -> Acid })) {
                    Box(Modifier.fillMaxWidth().height(210.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                        val role = when (playerIndex) { host -> "HOST\nAnswer yes or no.\nWORD: $secret"; navigator -> "NAVIGATOR\nQuietly guide the group.\nWORD: $secret"; distorter -> "DISTORTER\nSecretly steer them away.\nWORD: $secret"; else -> "SEEKER\nAsk sharp yes/no questions." }
                        Text(if (revealed) role else "TAP TO REVEAL", color = Ink, fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.weight(1f)); PartyButton(if (playerIndex == players.lastIndex) "BEGIN SEARCH" else "HIDE & PASS", {
                    if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false }
                }, enabled = revealed)
            }
            1 -> {
                Text("${players[host]} is the Host", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text("$seconds", fontSize = 92.sp, lineHeight = 96.sp, fontWeight = FontWeight.Black, color = if (seconds <= 15) Pink else Acid)
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Sky), shape = PartyShape) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("HOST'S WORD", color = Ink.copy(alpha = .65f), fontWeight = FontWeight.Black); Text(secret, color = Ink, fontSize = 34.sp, fontWeight = FontWeight.Black) } }
                Text("Everyone asks yes/no questions. The Navigator helps subtly; the Distorter misleads.", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("THE GROUP SOLVED IT", { solved = true; phase = 2 })
                OutlinedButton({ solved = false; phase = 2 }, Modifier.fillMaxWidth()) { Text("TIME UP · VOTE") }
            }
            2 -> {
                Text(if (solved) "Who tried to mislead you?" else "Who was secretly helping?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(players.withIndex().filter { it.index != host }, key = { it.index }) { item ->
                        Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, shape = RoundedCornerShape(18.dp), border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontSize = 19.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                PartyButton("LOCK THE VOTE", { phase = 3 }, enabled = votedIndex != null)
            }
            else -> {
                val target = if (solved) distorter else navigator
                val caught = votedIndex == target
                Spacer(Modifier.weight(1f)); Text(if (caught) "VOICE FOUND" else "VOICE ESCAPED", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink, textAlign = TextAlign.Center)
                Text("${players[target]} was the ${if (solved) "Distorter" else "Navigator"}.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Text("The secret word was $secret.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f)); PartyButton("SEE ROUND RESULT", { complete(caught) })
            }
        }
        if (phase < 3) TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun CrowdTrapGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(20)
    val trendsetter = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(players.size) }
    val firstAnswers = remember { mutableStateListOf<String>().also { list -> repeat(players.size) { list.add("") } } }
    val finalAnswers = remember { mutableStateListOf<String>().also { list -> repeat(players.size) { list.add("") } } }
    var phase by remember { mutableStateOf(0) }
    var playerIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var votedIndex by remember { mutableStateOf<Int?>(null) }
    val question = runtime.currentPrompt?.text.orEmpty()
    fun groups(values: List<String>) = values.filter { it.isNotBlank() }.groupingBy { it.trim().lowercase() }.eachCount().entries.sortedByDescending { it.value }
    Page("CROWD TRAP", eyebrow = when (phase) { 0 -> "SECRET ROLE"; 1 -> "PRIVATE ANSWERS"; 2 -> "THE CROWD SPEAKS"; 3 -> "FINAL ANSWERS"; 4 -> "RESULTS"; 5 -> "FIND THE TRENDSETTER"; else -> "REVEAL" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass to ${players[playerIndex]}")
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, colors = CardDefaults.cardColors(containerColor = if (playerIndex == trendsetter) Pink else Acid), shape = PartyShape) {
                    Box(Modifier.height(190.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(if (!revealed) "TAP TO REVEAL" else if (playerIndex == trendsetter) "YOU ARE THE TRENDSETTER\n\nMake at least one person switch answers." else "YOU ARE THE CROWD\n\nMatch the final majority.", Modifier.padding(20.dp), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
                }
                Spacer(Modifier.weight(1f)); PartyButton(if (playerIndex == players.lastIndex) "START QUESTION" else "HIDE & PASS", { if (playerIndex == players.lastIndex) { phase = 1; playerIndex = 0 } else { playerIndex++; revealed = false } }, enabled = revealed)
            }
            1, 3 -> {
                Text(question, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text("${players[playerIndex]} answers privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                if (phase == 3) Text("First answer: ${firstAnswers[playerIndex]}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(input, { input = it.take(40) }, label = { Text(if (phase == 1) "Short answer" else "Keep or change") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("LOCK & PASS", {
                    if (phase == 1) firstAnswers[playerIndex] = input.trim() else finalAnswers[playerIndex] = input.trim()
                    if (playerIndex == players.lastIndex) { input = ""; phase = if (phase == 1) 2 else 4; playerIndex = 0 }
                    else { playerIndex++; input = if (phase == 3) firstAnswers[playerIndex] else "" }
                }, enabled = input.isNotBlank())
            }
            2 -> {
                Text(question, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text("Anonymous first answers", color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(groups(firstAnswers).toList()) { item -> PartyPanel(item.key.uppercase(), Sky) { Text("${item.value} player${if (item.value == 1) "" else "s"}") } } }
                Text("Debate for 30 seconds. The Trendsetter is trying to move the crowd.", textAlign = TextAlign.Center)
                PartyButton("ANSWER AGAIN", { phase = 3; input = firstAnswers[0] })
            }
            4 -> {
                val changed = players.indices.count { !firstAnswers[it].equals(finalAnswers[it], true) }
                Text("FINAL CROWD", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(groups(finalAnswers).toList()) { item -> PartyPanel(item.key.uppercase(), Tangerine) { Text("${item.value} vote${if (item.value == 1) "" else "s"}") } } }
                Text("$changed player${if (changed == 1) "" else "s"} switched.", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                PartyButton("VOTE FOR THE TRENDSETTER", { phase = 5 })
            }
            5 -> {
                Text("Who steered the discussion?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("REVEAL", { phase = 6 }, enabled = votedIndex != null)
            }
            else -> {
                val caught = votedIndex == trendsetter
                Spacer(Modifier.weight(1f)); Text(if (caught) "CROWD WINS" else "TRENDSETTER WINS", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink, textAlign = TextAlign.Center)
                Text("${players[trendsetter]} was the Trendsetter.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("SEE ROUND RESULT", { complete(caught) })
            }
        }
        if (phase < 6) TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun SignalLineGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(20)
    val caller = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(players.size) }
    val jammer = remember(caller) { players.indices.filter { it != caller }.random() }
    val target = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(5, 96) }
    val ends = runtime.currentPrompt?.text.orEmpty().split("|").map { it.trim() }
    val left = ends.getOrElse(0) { "LOW" }; val right = ends.getOrElse(1) { "HIGH" }
    val guessers = remember(caller) { players.indices.filter { it != caller } }
    val guesses = remember { mutableStateListOf<Float>().also { list -> repeat(players.size) { list.add(50f) } } }
    var phase by remember { mutableStateOf(0) }
    var playerIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var scenario by remember { mutableStateOf("") }
    var guessTurn by remember { mutableStateOf(0) }
    var slider by remember { mutableStateOf(50f) }
    var groupMarker by remember { mutableStateOf(50f) }
    var votedIndex by remember { mutableStateOf<Int?>(null) }
    Page("SIGNAL LINE", eyebrow = when (phase) { 0 -> "SECRET ROLES"; 1 -> "CALL THE SIGNAL"; 2 -> "PRIVATE READS"; 3 -> "GROUP DECISION"; 4 -> "FIND THE JAMMER"; else -> "REVEAL" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass to ${players[playerIndex]}")
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, colors = CardDefaults.cardColors(containerColor = when (playerIndex) { caller -> Sky; jammer -> Pink; else -> Acid }), shape = PartyShape) { Box(Modifier.height(210.dp).fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    val zoneStart = (target / 25) * 25
                    val role = when (playerIndex) { caller -> "CALLER\nTarget: $target / 100\nGive a scenario that lands there."; jammer -> "JAMMER\nTarget zone: $zoneStart–${(zoneStart + 25).coerceAtMost(100)}\nPush the group away from it."; else -> "READER\nPlace the Caller's scenario on the line." }
                    Text(if (revealed) role else "TAP TO REVEAL", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                } }
                Spacer(Modifier.weight(1f)); PartyButton(if (playerIndex == players.lastIndex) "CALL THE SIGNAL" else "HIDE & PASS", { if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false } }, enabled = revealed)
            }
            1 -> {
                Text("$left  ↔  $right", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text("${players[caller]}, your hidden target is $target", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                OutlinedTextField(scenario, { scenario = it.take(100) }, label = { Text("A scenario, not a number") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("LOCK CLUE & PASS", { phase = 2 }, enabled = scenario.isNotBlank())
            }
            2 -> {
                val current = guessers[guessTurn]
                Text("$left  ↔  $right", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                PartyPanel("THE SCENARIO", Sky) { Text(scenario, fontSize = 21.sp, fontWeight = FontWeight.Bold) }
                Text("${players[current]} places it privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text("${slider.roundToInt()}", fontSize = 54.sp, fontWeight = FontWeight.Black)
                Slider(slider, { slider = it }, valueRange = 0f..100f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(left); Text(right) }
                Spacer(Modifier.weight(1f)); PartyButton("LOCK & PASS", { guesses[current] = slider; slider = 50f; if (guessTurn == guessers.lastIndex) phase = 3 else guessTurn++ })
            }
            3 -> {
                val spread = guessers.map { guesses[it] }
                Text("READ THE ROOM", style = MaterialTheme.typography.headlineMedium)
                Text("Private guesses spread from ${spread.min().roundToInt()} to ${spread.max().roundToInt()}.", textAlign = TextAlign.Center)
                Text("Debate, then place one shared marker.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${groupMarker.roundToInt()}", fontSize = 64.sp, fontWeight = FontWeight.Black)
                Slider(groupMarker, { groupMarker = it }, valueRange = 0f..100f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(left); Text(right) }
                Spacer(Modifier.weight(1f)); PartyButton("LOCK GROUP MARKER", { phase = 4 })
            }
            4 -> {
                Text("Who jammed the discussion?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().filter { it.index != caller }) { item -> Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("REVEAL SIGNAL", { phase = 5 }, enabled = votedIndex != null)
            }
            else -> {
                val accurate = kotlin.math.abs(groupMarker - target) <= 10f
                val caught = votedIndex == jammer
                val crewWon = accurate && caught
                Spacer(Modifier.weight(1f)); Text(if (crewWon) "SIGNAL FOUND" else "SIGNAL JAMMED", style = MaterialTheme.typography.displayLarge, color = if (crewWon) Green else Pink, textAlign = TextAlign.Center)
                Text("Target $target · Group ${groupMarker.roundToInt()}", fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("${players[jammer]} was the Jammer.${if (!caught) " The vote missed." else ""}", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("SEE ROUND RESULT", { complete(crewWon) })
            }
        }
        if (phase < 5) TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun TruthOrDareGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }
    val pair = runtime.currentPrompt?.text.orEmpty().split("|", limit = 2)
    var selected by remember { mutableStateOf(0) }
    var spinning by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(0) }
    var choice by remember { mutableStateOf("TRUTH") }
    LaunchedEffect(spinning) {
        if (spinning) {
            repeat(16) { selected = (selected + 1) % players.size; delay(45L + it * 6L) }
            selected = kotlin.random.Random.nextInt(players.size); spinning = false; phase = 1
        }
    }
    Page("TRUTH OR DARE", eyebrow = when (phase) { 0 -> "SPIN THE PLAYER"; 1 -> "MAKE THE CHOICE"; else -> choice }) {
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = if (spinning) Tangerine else Pink, modifier = Modifier.size(190.dp)) { Box(contentAlignment = Alignment.Center) { Text(players[selected], Modifier.padding(18.dp), color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) } }
        when (phase) {
            0 -> PartyButton(if (spinning) "SPINNING…" else "SPIN THE WHEEL", { spinning = true }, enabled = !spinning)
            1 -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button({ choice = "TRUTH"; phase = 2 }, Modifier.weight(1f).height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Sky, contentColor = Ink)) { Text("TRUTH", fontWeight = FontWeight.Black) }
                Button({ choice = "DARE"; phase = 2 }, Modifier.weight(1f).height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Color.White)) { Text("DARE", fontWeight = FontWeight.Black) }
            }
            else -> {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (choice == "TRUTH") Sky else Pink), shape = PartyShape) { Text(if (choice == "TRUTH") pair.firstOrNull().orEmpty() else pair.getOrElse(1) { pair.firstOrNull().orEmpty() }, Modifier.padding(24.dp), color = if (choice == "TRUTH") Ink else Color.White, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
                PartyButton("CHALLENGE COMPLETE", { complete(true) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun MostLikelyGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }
    val votes = remember { mutableStateListOf<Int>() }
    var voter by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var reveal by remember { mutableStateOf(false) }
    Page("MOST LIKELY", eyebrow = if (reveal) "ROOM VERDICT" else "PRIVATE VOTE ${voter + 1} OF ${players.size}") {
        Text(runtime.currentPrompt?.text.orEmpty(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        if (!reveal) {
            Text("Pass to ${players[voter]}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable { selected = item.index }, border = BorderStroke(2.dp, if (selected == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
            PartyButton("LOCK & PASS", { votes.add(selected!!); selected = null; if (voter == players.lastIndex) reveal = true else voter++ }, enabled = selected != null)
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(players.withIndex().sortedByDescending { candidate -> votes.count { it == candidate.index } }) { item ->
                    val count = votes.count { it == item.index }; val percent = count * 100 / votes.size.coerceAtLeast(1)
                    PartyPanel(item.value.uppercase(), if (percent == votes.groupingBy { it }.eachCount().values.maxOrNull()?.times(100)?.div(votes.size.coerceAtLeast(1))) Pink else Sky) { LinearProgressIndicator({ percent / 100f }, Modifier.fillMaxWidth()); Text("$percent% · $count votes", fontWeight = FontWeight.Bold) }
                }
            }
            PartyButton("ROUND COMPLETE", { complete(true) })
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun NeverHaveIEverGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }
    val hearts = remember { mutableStateListOf<Int>().also { list -> repeat(players.size) { list.add(5) } } }
    val guilty = remember { mutableStateListOf<Int>() }
    var card by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    val prompt = runtime.prompts[card % runtime.prompts.size].text
    Page("NEVER HAVE I EVER", eyebrow = if (finished) "LAST HEARTS STANDING" else "CONFESSION ${card + 1}") {
        if (!finished) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Pink), shape = PartyShape) { Text(prompt, Modifier.padding(24.dp), color = Color.White, fontSize = 28.sp, lineHeight = 33.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
            Text("If you have, tap your name once.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable(enabled = hearts[item.index] > 0 && item.index !in guilty) { guilty.add(item.index); hearts[item.index]-- }, border = BorderStroke(2.dp, if (item.index in guilty) Pink else MaterialTheme.colorScheme.outline)) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(item.value, fontWeight = FontWeight.Bold); Text("♥".repeat(hearts[item.index]), color = Pink, fontSize = 20.sp) } } } }
            PartyButton("NEXT CONFESSION", { val alive = hearts.count { it > 0 }; if (alive <= 1 || card >= 9) finished = true else { card++; guilty.clear() } })
        } else {
            Spacer(Modifier.weight(1f)); val best = hearts.maxOrNull() ?: 0
            Text("SURVIVORS", style = MaterialTheme.typography.displayLarge, color = Green)
            Text(players.indices.filter { hearts[it] == best }.joinToString { players[it] }, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text("$best hearts remaining", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f)); PartyButton("SEE ROUND RESULT", { complete(true) })
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun WouldYouRatherGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }
    val options = runtime.currentPrompt?.text.orEmpty().split("|", limit = 2)
    val votes = remember { mutableStateListOf<Boolean>() }
    var voter by remember { mutableStateOf(0) }
    var reveal by remember { mutableStateOf(false) }
    Page("WOULD YOU RATHER", eyebrow = if (reveal) "THE ROOM CHOSE" else "${players[voter]}'S PRIVATE CHOICE") {
        Spacer(Modifier.weight(1f))
        if (!reveal) {
            Button({ votes.add(false); if (voter == players.lastIndex) reveal = true else voter++ }, Modifier.fillMaxWidth().weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Sky, contentColor = Ink), shape = PartyShape) { Text("A\n${options.firstOrNull().orEmpty()}", fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
            Text("OR", fontWeight = FontWeight.Black)
            Button({ votes.add(true); if (voter == players.lastIndex) reveal = true else voter++ }, Modifier.fillMaxWidth().weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Pink), shape = PartyShape) { Text("B\n${options.getOrElse(1) { "" }}", fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
        } else {
            val b = votes.count { it }; val a = votes.size - b
            PartyPanel("A · ${a * 100 / votes.size}%", Sky) { Text(options.firstOrNull().orEmpty(), fontSize = 22.sp, fontWeight = FontWeight.Bold); LinearProgressIndicator({ a / votes.size.toFloat() }, Modifier.fillMaxWidth()) }
            PartyPanel("B · ${b * 100 / votes.size}%", Pink) { Text(options.getOrElse(1) { "" }, fontSize = 22.sp, fontWeight = FontWeight.Bold); LinearProgressIndicator({ b / votes.size.toFloat() }, Modifier.fillMaxWidth(), color = Pink) }
            Spacer(Modifier.weight(1f)); PartyButton("ROUND COMPLETE", { complete(true) })
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun FakeItGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }
    val parts = runtime.currentPrompt?.text.orEmpty().split("|", limit = 2)
    val question = parts.firstOrNull().orEmpty(); val answer = parts.getOrElse(1) { "Unknown" }
    val fakes = remember { mutableStateListOf<String>().also { list -> repeat(players.size) { list.add("") } } }
    val options = remember { mutableStateListOf<FakeOption>() }
    val scores = remember { mutableStateListOf<Int>().also { list -> repeat(players.size) { list.add(0) } } }
    var phase by remember { mutableStateOf(0) }
    var playerIndex by remember { mutableStateOf(0) }
    var input by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Int?>(null) }
    Page("FAKE IT", eyebrow = when (phase) { 0 -> "WRITE A LIE"; 1 -> "FIND THE TRUTH"; else -> "SCORES" }) {
        Text(question, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        when (phase) {
            0 -> {
                Text("${players[playerIndex]} writes privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text("Invent a believable wrong answer.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(input, { input = it.take(60) }, label = { Text("Fake answer") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("LOCK & PASS", {
                    fakes[playerIndex] = input.trim(); input = ""
                    if (playerIndex == players.lastIndex) { options.addAll((fakes.mapIndexed { index, text -> FakeOption(text, index) } + FakeOption(answer, correct = true)).shuffled()); playerIndex = 0; phase = 1 } else playerIndex++
                }, enabled = input.isNotBlank() && !input.trim().equals(answer, true))
            }
            1 -> {
                Text("${players[playerIndex]} votes privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(options.withIndex().filter { it.value.author != playerIndex }, key = { it.index }) { item -> Card(Modifier.fillMaxWidth().clickable { selected = item.index }, border = BorderStroke(2.dp, if (selected == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value.text, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("LOCK & PASS", {
                    val chosen = options[selected!!]
                    if (chosen.correct) scores[playerIndex] += 2 else chosen.author?.let { scores[it]++ }
                    selected = null
                    if (playerIndex == players.lastIndex) phase = 2 else playerIndex++
                }, enabled = selected != null)
            }
            else -> {
                PartyPanel("REAL ANSWER", Green) { Text(answer, fontSize = 25.sp, fontWeight = FontWeight.Black) }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) { items(players.indices.sortedByDescending { scores[it] }) { index -> Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(players[index], fontWeight = FontWeight.Bold); Text("${scores[index]} pts", color = Green, fontWeight = FontWeight.Black) } } }
                PartyButton("ROUND COMPLETE", { complete(true) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun DrawAndGuessGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val marks = remember { mutableStateListOf<DrawMark>() }
    var color by remember { mutableStateOf(Ink) }
    var width by remember { mutableStateOf(10f) }
    var phase by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(60) }
    var solved by remember { mutableStateOf(false) }
    LaunchedEffect(phase) { if (phase == 1) { while (seconds > 0 && !solved) { delay(1_000); seconds-- }; phase = 2 } }
    Page("DRAW & GUESS", eyebrow = when (phase) { 0 -> "ARTIST ONLY"; 1 -> "$seconds SECONDS"; else -> "ROUND OVER" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Secret word", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(runtime.currentPrompt?.text.orEmpty(), style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Text("Show only the artist, then hide the word.", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("START DRAWING", { phase = 1 })
            }
            1 -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(Ink, Pink, Sky, Green, Tangerine).forEach { value -> Box(Modifier.size(34.dp).background(value, CircleShape).border(if (color == value) 3.dp else 1.dp, Color.White, CircleShape).clickable { color = value }) }
                    TextButton({ color = Paper }) { Text("ERASER") }
                    TextButton({ marks.clear() }) { Text("CLEAR") }
                }
                Slider(width, { width = it }, valueRange = 4f..28f)
                GameDrawingCanvas(marks, color, width, Modifier.fillMaxWidth().weight(1f))
                PartyButton("GUESSED IT!", { solved = true; phase = 2 })
            }
            else -> {
                Spacer(Modifier.weight(1f)); Text(if (solved) "MASTERPIECE!" else "TIME'S UP!", style = MaterialTheme.typography.displayLarge, color = if (solved) Green else Pink)
                Text("The word was ${runtime.currentPrompt?.text.orEmpty()}.", fontSize = 23.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("SEE ROUND RESULT", { complete(solved) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun GameDrawingCanvas(marks: MutableList<DrawMark>, color: Color, width: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.background(Paper, RoundedCornerShape(24.dp)).border(2.dp, Sky, RoundedCornerShape(24.dp)).pointerInput(color, width) {
        detectDragGestures(onDragStart = { marks.add(DrawMark(null, color, width)); marks.add(DrawMark(it, color, width)) }, onDrag = { change, _ -> change.consume(); marks.add(DrawMark(change.position, color, width)) })
    }) {
        var previous: DrawMark? = null
        marks.forEach { mark ->
            val old = previous
            if (mark.point == null) previous = null
            else { if (old?.point != null) drawLine(mark.color, old.point, mark.point, mark.width); previous = mark }
        }
    }
}

@Composable
private fun TwinWordSpyGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(20)
    val words = runtime.currentPrompt?.text.orEmpty().split("|", limit = 2)
    val spy = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(players.size) }
    val order = remember(runtime.session.id, runtime.session.currentRound) { players.indices.shuffled() }
    var phase by remember { mutableStateOf(0) }; var player by remember { mutableStateOf(0) }; var revealed by remember { mutableStateOf(false) }; var clue by remember { mutableStateOf(0) }; var vote by remember { mutableStateOf<Int?>(null) }
    Page("TWIN WORD SPY", eyebrow = when (phase) { 0 -> "SECRET WORDS"; 1 -> "ONE CLUE EACH"; 2 -> "FIND THE SPY"; else -> "REVEAL" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass to ${players[player]}")
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, colors = CardDefaults.cardColors(containerColor = if (player == spy) Pink else Acid), shape = PartyShape) { Box(Modifier.height(190.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(if (revealed) "YOUR WORD\n${if (player == spy) words.getOrElse(1) { "?" } else words.firstOrNull().orEmpty()}" else "TAP TO REVEAL", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) } }
                Spacer(Modifier.weight(1f)); PartyButton(if (player == players.lastIndex) "START CLUES" else "HIDE & PASS", { if (player == players.lastIndex) phase = 1 else { player++; revealed = false } }, enabled = revealed)
            }
            1 -> {
                Spacer(Modifier.weight(1f)); Text("CLUE ${clue + 1} OF ${players.size}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text(players[order[clue]], style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Text("Describe your word with one short clue. Do not say the word.", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton(if (clue == players.lastIndex) "VOTE" else "NEXT CLUE", { if (clue == players.lastIndex) phase = 2 else clue++ })
            }
            2 -> {
                Text("Who received the related word?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable { vote = item.index }, border = BorderStroke(2.dp, if (vote == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("REVEAL", { phase = 3 }, enabled = vote != null)
            }
            else -> {
                val caught = vote == spy; Spacer(Modifier.weight(1f)); Text(if (caught) "SPY CAUGHT" else "SPY ESCAPED", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink, textAlign = TextAlign.Center)
                Text("${players[spy]} had “${words.getOrElse(1) { "?" }}”. Everyone else had “${words.firstOrNull().orEmpty()}”.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("SEE ROUND RESULT", { complete(caught) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun TelephoneGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }
    var stage by remember { mutableStateOf(0) }
    var phrase by remember { mutableStateOf(runtime.currentPrompt?.text.orEmpty()) }
    var recorded by remember { mutableStateOf(false) }
    var clipUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var guess by remember { mutableStateOf("") }
    val strokes = remember { mutableStateListOf<Offset?>() }
    val context = LocalContext.current
    val recorder = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) { recorded = true; clipUri = result.data?.data }
    }
    fun record() {
        recorder.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE).putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15))
    }
    Page("MIME TELEPHONE", eyebrow = "${stage + 1} OF 6") {
        when (stage) {
            0 -> {
                Text("${players.getOrElse(0) { "Player 1" }} writes the secret", style = MaterialTheme.typography.headlineMedium)
                Text("Nobody else should see it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(phrase, { phrase = it.take(120) }, label = { Text("A weird scene") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("LOCK PHRASE & PASS", { stage = 1 }, enabled = phrase.isNotBlank())
            }
            1 -> {
                Text("${players.getOrElse(1) { "Player 2" }} records the mime", style = MaterialTheme.typography.headlineMedium)
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Acid), shape = PartyShape) { Text(phrase, Modifier.padding(22.dp), color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
                Text("You have up to 15 seconds. No spoken words.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f)); OutlinedButton(::record, Modifier.fillMaxWidth().height(58.dp)) { Text(if (recorded) "RECORD AGAIN" else "RECORD 15s CLIP") }
                PartyButton("HIDE PHRASE & PASS", { stage = 2 }, enabled = recorded)
            }
            2 -> {
                Text("${players.getOrElse(2) { "Player 3" }} watches the clip", style = MaterialTheme.typography.headlineMedium)
                Text("Watch once, then draw only what you remember.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = Sky, modifier = Modifier.size(150.dp)) { Box(contentAlignment = Alignment.Center) { Text("PLAY", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black) } }
                Spacer(Modifier.weight(1f))
                OutlinedButton({ clipUri?.let { context.startActivity(Intent(Intent.ACTION_VIEW, it).setDataAndType(it, "video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) } }, Modifier.fillMaxWidth(), enabled = clipUri != null) { Text(if (clipUri == null) "CLIP CAPTURED" else "WATCH CLIP") }
                PartyButton("DRAW WHAT YOU SAW", { stage = 3 })
            }
            3 -> {
                Text("Draw it. No letters or words.", style = MaterialTheme.typography.headlineMedium)
                DrawingPad(strokes, editable = true, modifier = Modifier.fillMaxWidth().weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ strokes.clear() }, Modifier.weight(1f)) { Text("CLEAR") }
                    Button({ stage = 4 }, Modifier.weight(1f), enabled = strokes.isNotEmpty()) { Text("PASS IT") }
                }
            }
            4 -> {
                Text("${players.getOrElse(3) { "Player 4" }} guesses the original", style = MaterialTheme.typography.headlineMedium)
                DrawingPad(strokes, editable = false, modifier = Modifier.fillMaxWidth().height(270.dp))
                OutlinedTextField(guess, { guess = it.take(120) }, label = { Text("Your final guess") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("REVEAL THE CHAIN", { stage = 5 }, enabled = guess.isNotBlank())
            }
            else -> {
                Text("THE CHAIN", style = MaterialTheme.typography.headlineMedium)
                PartyPanel("1 · ORIGINAL", Pink) { Text(phrase, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                PartyPanel("2 · VIDEO", Sky) { Text(if (recorded) "15-second mime captured" else "No clip") }
                PartyPanel("3 · DRAWING", Tangerine) { DrawingPad(strokes, editable = false, modifier = Modifier.fillMaxWidth().height(130.dp)) }
                PartyPanel("4 · FINAL GUESS", Acid) { Text(guess, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Text("Did the meaning survive?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ complete(false) }, Modifier.weight(1f)) { Text("NOT EVEN CLOSE") }
                    Button({ complete(true) }, Modifier.weight(1f)) { Text("NAILED IT") }
                }
            }
        }
        if (stage < 5) TextButton(quit, Modifier.fillMaxWidth()) { Text("Quit match") }
    }
}

@Composable
private fun DrawingPad(strokes: MutableList<Offset?>, editable: Boolean, modifier: Modifier = Modifier) {
    Canvas(
        modifier.background(Paper, RoundedCornerShape(24.dp)).border(2.dp, Tangerine, RoundedCornerShape(24.dp))
            .then(if (editable) Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { point -> strokes.add(null); strokes.add(point) },
                    onDrag = { change, _ -> change.consume(); strokes.add(change.position) },
                )
            } else Modifier),
    ) {
        var path = Path()
        var started = false
        strokes.forEach { point ->
            if (point == null) { path = Path(); started = false }
            else if (!started) { path.moveTo(point.x, point.y); started = true }
            else { path.lineTo(point.x, point.y); drawPath(path, Ink, style = Stroke(width = 8f)) }
        }
    }
}

@Composable
private fun SummaryScreen(state: AppUiState, vm: AppViewModel, onContinue: () -> Unit, onEnd: () -> Unit) {
    val runtime = state.runtime
    Page("ROUND WRAPPED", eyebrow = "NICE MIME WORK") {
        Card(Modifier.fillMaxWidth(), shape = PartyShape, colors = CardDefaults.cardColors(containerColor = Green)) {
            Column(Modifier.padding(20.dp)) {
                Text("${runtime?.score ?: 0}", fontSize = 68.sp, lineHeight = 68.sp, fontWeight = FontWeight.Black, color = Ink)
                Text("POINTS THIS ROUND", color = Ink.copy(alpha = .7f), fontWeight = FontWeight.Black)
            }
        }
        Text("Tap any answer to fix the score.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(runtime?.results?.withIndex()?.toList() ?: emptyList(), key = { it.value.promptId }) { indexed ->
                val guessed = indexed.value.status == PromptStatus.GUESSED
                Card(Modifier.fillMaxWidth().clickable { vm.override(indexed.index) }, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(indexed.value.text, Modifier.weight(1f)); Text(if (guessed) "GOT IT" else "SKIP", color = if (guessed) Green else Yellow, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        PartyButton(if (state.session?.completed == true) "SEE FINAL SCORE" else "NEXT TURN", onContinue)
        if (state.session?.settings?.totalRounds == null) OutlinedButton(onEnd, Modifier.fillMaxWidth()) { Text("END MATCH") }
    }
}

@Composable
private fun FinalScreen(state: AppUiState, onRematch: () -> Unit, onHome: () -> Unit) {
    val teams = state.session?.settings?.teams.orEmpty().sortedByDescending { it.score }
    val winner = teams.firstOrNull()
    val context = LocalContext.current
    Page("THAT'S A WRAP", eyebrow = "FINAL SCORE") {
        Spacer(Modifier.height(10.dp))
        Surface(shape = CircleShape, color = Acid, modifier = Modifier.size(126.dp)) { Box(contentAlignment = Alignment.Center) { Text("WIN", color = Ink, fontSize = 38.sp, fontWeight = FontWeight.Black) } }
        Text(winner?.name ?: "Winner", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
        Text("ABSOLUTE MIME LEGENDS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
        teams.forEachIndexed { index, team ->
            Surface(Modifier.fillMaxWidth(), color = if (index == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("${index + 1}.  ${team.name}", fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("${team.score}", fontSize = 26.sp, fontWeight = FontWeight.Black) }
            }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton({
            val text = teams.joinToString(prefix = "Party Mode results\n", separator = "\n") { "${it.name}: ${it.score}" }
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Share results"))
        }, Modifier.fillMaxWidth()) { Text("SHARE RESULTS") }
        PartyButton("REMATCH", onRematch)
        TextButton(onHome, Modifier.fillMaxWidth()) { Text("Back home") }
    }
}

@Composable
private fun CustomDeckScreen(vm: AppViewModel, saved: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var prompts by remember { mutableStateOf("") }
    val context = LocalContext.current
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) prompts = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
    }
    Page("MAKE IT PERSONAL", eyebrow = "CUSTOM DECK") {
        Card(Modifier.fillMaxWidth(), shape = PartyShape, colors = CardDefaults.cardColors(containerColor = Sky), border = BorderStroke(2.dp, Ink)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("INSIDE JOKES WELCOME.", style = MaterialTheme.typography.headlineMedium, color = Ink)
                Text("Add one prompt per line or import a file.", color = Ink.copy(alpha = .7f))
            }
        }
        OutlinedButton({ importer.launch(arrayOf("text/plain", "text/csv", "application/csv")) }, Modifier.fillMaxWidth().height(54.dp), shape = PartyShape) { Text("IMPORT TXT OR CSV") }
        OutlinedTextField(title, { title = it }, label = { Text("Deck name") }, shape = PartyShape, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(prompts, { prompts = it }, label = { Text("Prompts") }, minLines = 10, shape = PartyShape, modifier = Modifier.fillMaxWidth().weight(1f))
        PartyButton("SAVE AND USE", onClick = { vm.saveCustomDeck(title, prompts, saved) })
    }
}

@Composable
private fun SettingsScreen(state: AppUiState, vm: AppViewModel, onSignOut: () -> Unit) {
    var showAgeGate by remember { mutableStateOf(false) }
    Page("TUNE THE CHAOS", scrollable = true, eyebrow = "SETTINGS") {
        PartyPanel("TILT SENSITIVITY", Sky) {
            Text("${state.settings.tiltThreshold.roundToInt()}° gesture threshold", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(state.settings.tiltThreshold, vm::setThreshold, valueRange = 30f..75f)
        }
        PartyPanel("SOUND", Tangerine) {
            Text("Volume ${(state.settings.soundVolume * 100).roundToInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(state.settings.soundVolume, vm::setSoundVolume, valueRange = 0f..1f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CHIME", "ARCADE").forEach { pack -> PartyChip(pack, state.settings.soundPack == pack) { vm.setSoundPack(pack) } }
            }
        }
        PartyPanel("PARTY LIGHTING", Acid) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("SUNNY", "CONTRAST").forEachIndexed { index, label ->
                    val theme = listOf("LIGHT", "HIGH_CONTRAST")[index]
                    PartyChip(label, state.settings.theme == theme) { vm.setTheme(theme) }
                }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = PartyShape, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("After Dark deck", fontWeight = FontWeight.Bold); Text("Hidden unless age-confirmed", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Checkbox(state.settings.adultUnlocked, { enabled -> if (enabled) showAgeGate = true else vm.unlockAdult(false) })
            }
        }
        PartyPanel("ACCOUNT", Pink) {
            Text(state.user?.email ?: state.user?.displayName ?: "Guest player · saved on this phone", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), shape = PartyShape) { Text(if (state.user == null) "EXIT GUEST MODE" else "SIGN OUT") }
        }
        Text("The game works offline. Deck and match changes synchronize automatically when a connection returns.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (showAgeGate) AlertDialog(onDismissRequest = { showAgeGate = false }, title = { Text("Adults only") }, text = { Text("Confirm that you are 18 or older to reveal the After Dark deck on this device.") }, confirmButton = { Button({ vm.unlockAdult(true); showAgeGate = false }) { Text("I AM 18+") } }, dismissButton = { TextButton({ showAgeGate = false }) { Text("Cancel") } })
}

@Composable
private fun Page(title: String, scrollable: Boolean = false, eyebrow: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val scrollState = rememberScrollState()
    val accentWash = MaterialTheme.colorScheme.primary.copy(alpha = .08f)
    val secondaryWash = MaterialTheme.colorScheme.secondary.copy(alpha = .06f)
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(accentWash, size.minDimension * .38f, Offset(size.width * 1.02f, -20f))
            drawCircle(secondaryWash, size.minDimension * .22f, Offset(-20f, size.height * .82f))
        }
        val baseModifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 22.dp)
        val pageModifier = if (scrollable) baseModifier.verticalScroll(scrollState) else baseModifier
        Column(pageModifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.headlineLarge)
                eyebrow?.let {
                    Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = .12f), shape = CircleShape) {
                        Text(it, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun PartyButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(60.dp), shape = PartyShape) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatBadge(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PartyPanel(title: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = PartyShape,
        border = BorderStroke(2.dp, accent.copy(alpha = .72f)),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(14.dp).background(accent, RoundedCornerShape(5.dp)))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = .8.sp)
            }
            content()
        }
    }
}

@Composable
private fun PartyChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Black, fontSize = 12.sp) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
        ),
    )
}

@Composable
private fun MiniLabel(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
}

private fun GameType.displayName(): String = when (this) {
    GameType.CHARADES -> "DUMB CHARADES"
    GameType.IMPOSTOR_MIME -> "IMPOSTOR MIME"
    GameType.BLANK_AGENT -> "BLANK AGENT"
    GameType.INSIDE_VOICE -> "INSIDE VOICE"
    GameType.CROWD_TRAP -> "CROWD TRAP"
    GameType.SIGNAL_LINE -> "SIGNAL LINE"
    GameType.TRUTH_OR_DARE -> "TRUTH OR DARE"
    GameType.MOST_LIKELY -> "MOST LIKELY"
    GameType.NEVER_HAVE_I_EVER -> "NEVER HAVE I EVER"
    GameType.FAKE_IT -> "FAKE IT"
    GameType.DRAW_AND_GUESS -> "DRAW & GUESS"
    GameType.WOULD_YOU_RATHER -> "WOULD YOU RATHER"
    GameType.TWIN_WORD_SPY -> "TWIN WORD SPY"
    GameType.REVERSE_CHARADES -> "REVERSE CHARADES"
    GameType.MIME_TELEPHONE -> "MIME TELEPHONE"
    GameType.SOUND_EFFECTS -> "SOUND EFFECTS ONLY"
    GameType.BOMB_DEFUSAL -> "5-SECOND BOMB"
}

private fun GameType.readyInstruction(): String = when (this) {
    GameType.CHARADES -> "Phone on forehead. Friends at the ready."
    GameType.IMPOSTOR_MIME -> "Gather everyone close. Each person will secretly reveal a role."
    GameType.BLANK_AGENT -> "Each player sees the secret word—except one Blank Agent. Then everyone gives one clue."
    GameType.INSIDE_VOICE -> "Ask yes/no questions, solve the word, then expose the hidden Distorter."
    GameType.CROWD_TRAP -> "Answer privately, debate the crowd, then catch who tried to steer it."
    GameType.SIGNAL_LINE -> "A Caller gives a scenario. Place it on the spectrum and expose the Jammer."
    GameType.TRUTH_OR_DARE -> "Spin for a random player, then choose a truth or a dare."
    GameType.MOST_LIKELY -> "Everyone votes privately for the player who best fits the prompt."
    GameType.NEVER_HAVE_I_EVER -> "Everyone starts with five hearts. Guilty players lose one."
    GameType.FAKE_IT -> "Write convincing fake answers, then find the real one without being fooled."
    GameType.DRAW_AND_GUESS -> "One artist gets the word and draws while the room races the clock."
    GameType.WOULD_YOU_RATHER -> "Choose A or B privately, then reveal how the room split."
    GameType.TWIN_WORD_SPY -> "Most players share one word; the spy receives a related word."
    GameType.REVERSE_CHARADES -> "One guesser looks away while the whole room learns the prompt."
    GameType.MIME_TELEPHONE -> "Four players needed: writer, mime, artist and guesser."
    GameType.SOUND_EFFECTS -> "No words or gestures—mouth sounds, claps and taps only."
    GameType.BOMB_DEFUSAL -> "Name three, tap pass, and do not be holding the phone when it blows."
}

private fun GameType.minimumPlayers(): Int = when (this) {
    GameType.INSIDE_VOICE -> 5
    GameType.IMPOSTOR_MIME, GameType.CROWD_TRAP, GameType.SIGNAL_LINE, GameType.MIME_TELEPHONE -> 4
    GameType.BLANK_AGENT, GameType.MOST_LIKELY, GameType.FAKE_IT, GameType.TWIN_WORD_SPY -> 3
    else -> 2
}

private fun GameType.playerLabel(): String = "${minimumPlayers()}–20 PLAYERS"

private fun GameType.prefersTeams(): Boolean = this in setOf(GameType.CHARADES, GameType.REVERSE_CHARADES, GameType.SOUND_EFFECTS, GameType.DRAW_AND_GUESS, GameType.BOMB_DEFUSAL)

private fun GameType.gameColor(): Color = when (this) {
    GameType.CHARADES, GameType.SIGNAL_LINE, GameType.WOULD_YOU_RATHER, GameType.TWIN_WORD_SPY -> Sky
    GameType.IMPOSTOR_MIME, GameType.TRUTH_OR_DARE, GameType.FAKE_IT -> Pink
    GameType.REVERSE_CHARADES, GameType.MOST_LIKELY, GameType.BOMB_DEFUSAL -> Acid
    GameType.SOUND_EFFECTS, GameType.INSIDE_VOICE, GameType.DRAW_AND_GUESS -> Green
    GameType.CROWD_TRAP, GameType.NEVER_HAVE_I_EVER, GameType.MIME_TELEPHONE -> Tangerine
    GameType.BLANK_AGENT -> Color(0xFFE7DFFF)
}

private fun gameForeground(background: Color): Color = if (background == Ink) Color.White else Ink

private fun GameType.teamRule(): String = when (this) {
    GameType.CHARADES -> "One actor per turn. Play as one group or let the app split two teams."
    GameType.REVERSE_CHARADES -> "One guesser faces the whole group acting together."
    GameType.SOUND_EFFECTS -> "One clue-maker uses noises only while everyone else guesses."
    GameType.DRAW_AND_GUESS -> "One artist draws. Their group races to name the word."
    GameType.BOMB_DEFUSAL -> "The whole room plays in one passing circle—or two crews can compete."
    GameType.MIME_TELEPHONE -> "Four roles form one chain: writer, mime, artist and guesser."
    GameType.IMPOSTOR_MIME -> "Everyone performs together; one player never saw the prompt."
    GameType.BLANK_AGENT -> "Everyone gives a clue; one player received no word."
    GameType.INSIDE_VOICE -> "The room solves together while hidden roles influence the search."
    GameType.CROWD_TRAP -> "Everyone answers alone, then the room negotiates as one crowd."
    GameType.SIGNAL_LINE -> "One Caller leads the room while a hidden Jammer pushes it off target."
    GameType.TRUTH_OR_DARE -> "The spinner chooses one player for a truth or a dare."
    GameType.MOST_LIKELY -> "Everyone votes privately for someone else—or themselves."
    GameType.NEVER_HAVE_I_EVER -> "Every player protects five hearts. No teams and no early spectators."
    GameType.FAKE_IT -> "Everyone writes a lie, votes, and scores individually."
    GameType.WOULD_YOU_RATHER -> "Everyone chooses A or B privately before the room split appears."
    GameType.TWIN_WORD_SPY -> "Everyone gives a clue; one player has a related but different word."
}

private fun GameType.rules(): List<String> = when (this) {
    GameType.CHARADES -> listOf("Choose touch controls or hold the phone to your forehead.", "Act the prompt without speaking while the group guesses.", "Mark it correct, skip it, or take a penalty before time runs out.")
    GameType.REVERSE_CHARADES -> listOf("One guesser looks away while every actor sees the prompt.", "The whole room acts at the same time without speaking.", "The guesser names as many prompts as possible before time ends.")
    GameType.IMPOSTOR_MIME -> listOf("Pass the phone so every player privately sees a role.", "Everyone mimes together; the impostor copies the room to blend in.", "Vote for the suspicious performer. The crew scores only if the impostor is caught.")
    GameType.BLANK_AGENT -> listOf("Everyone except one Blank Agent sees the same secret word.", "Give one clue in the randomized order and obey the round's clue restriction.", "Vote for the Agent. If caught, they can steal the win by guessing the word.")
    GameType.INSIDE_VOICE -> listOf("The Host, Navigator and Distorter privately learn the secret word.", "Ask the Host yes/no questions while hidden roles help or mislead.", "After solving—or timing out—vote for the hidden influence.")
    GameType.CROWD_TRAP -> listOf("Everyone answers a subjective question privately.", "Reveal anonymous answers, debate, then answer again.", "Score with the final crowd and vote for the hidden Trendsetter.")
    GameType.SIGNAL_LINE -> listOf("The Caller sees a hidden 0–100 target on a funny spectrum.", "The room places the scenario privately, then agrees on one marker.", "Reveal the target and vote for the Jammer who steered the debate.")
    GameType.TRUTH_OR_DARE -> listOf("Spin to select a random player.", "That player chooses Truth or Dare.", "Reveal the card and complete it before moving on.")
    GameType.MOST_LIKELY -> listOf("Read the scenario aloud.", "Pass the phone so everyone votes privately for a player.", "Reveal the room's vote percentages together.")
    GameType.NEVER_HAVE_I_EVER -> listOf("Every player starts with five hearts.", "If a statement is true for you, tap your name and lose one heart.", "Play ten confessions or until one player has hearts left.")
    GameType.FAKE_IT -> listOf("Read the obscure question, then everyone writes a convincing fake answer.", "The real answer is shuffled into the lies and everyone votes privately.", "Earn two points for the truth and one whenever your lie fools someone.")
    GameType.DRAW_AND_GUESS -> listOf("Only the artist sees the secret word.", "Draw with colors, brush sizes and the eraser—never write letters.", "The group has 60 seconds to guess the word.")
    GameType.WOULD_YOU_RATHER -> listOf("Read both impossible options.", "Pass the phone so every player chooses A or B privately.", "Reveal the room's percentages and defend your choice.")
    GameType.TWIN_WORD_SPY -> listOf("Most players receive one word; the spy receives a related word.", "Give one short clue each without saying either word.", "Vote for the player whose clue came from the odd word.")
    GameType.MIME_TELEPHONE -> listOf("Player one writes a strange scene and player two records a silent mime.", "Player three watches only the clip and draws what they understood.", "Player four guesses, then reveal the full text-to-video-to-drawing chain.")
    GameType.SOUND_EFFECTS -> listOf("The clue-maker may use mouth sounds, claps or nearby objects.", "No words, lip spelling, pointing or body gestures.", "Tilt or tap correct and skip while the timer runs.")
    GameType.BOMB_DEFUSAL -> listOf("Read a 'name three' challenge and answer within five seconds.", "Tap pass and hand the phone to the next player immediately.", "The fuse explodes randomly between 30 and 90 seconds; the holder takes the penalty.")
}
