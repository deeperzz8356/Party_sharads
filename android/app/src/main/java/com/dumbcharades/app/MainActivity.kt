package com.dumbcharades.app

/*
Clay party shelf: warm cream, cocoa lettering, terracotta actions and denim-blue
selection frame the seventeen approved clay cover illustrations. Native controls
keep discovery and pass-the-phone play direct. No login or onboarding gate.
*/

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.provider.MediaStore
import android.provider.Settings
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dumbcharades.app.game.PartyMood
import com.dumbcharades.app.game.guide
import com.dumbcharades.app.game.recommendGames
import com.dumbcharades.app.game.partyTeams
import com.dumbcharades.app.game.TiltController
import com.dumbcharades.app.game.TiltGesture
import com.dumbcharades.app.model.GameMode
import com.dumbcharades.app.model.GameType
import com.dumbcharades.app.model.PromptStatus
import com.dumbcharades.app.model.GameSession
import com.dumbcharades.app.model.Deck
import com.dumbcharades.app.ui.savedScore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.dumbcharades.app.ui.AppUiState
import com.dumbcharades.app.ui.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
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

// Play flow: hub → game page (rules, example, cards) → players → ready → play.
private object Routes {
    const val GAMES = "games"
    const val RULES = "rules"
    const val SETUP = "setup"
    const val CHOOSER = "chooser"
    const val READY = "ready"
    const val GAME = "game"
    const val SUMMARY = "summary"
    const val FINAL = "final"
    const val CREATE_DECK = "create-deck"
    const val CUSTOM = "custom"
    const val SETTINGS = "settings"
}

private val Ink = Color(0xFF35251F)
private val InkSoft = Color(0xFF67514A)
private val Paper = Color(0xFFFFF6E9)
private val Pink = Color(0xFFE7654F)
private val Acid = Color(0xFFF4C75D)
private val Sky = Color(0xFF365E9D)
private val Tangerine = Color(0xFFF2AB7B)
private val Green = Color(0xFF9AC7AA)
private val Yellow = Tangerine
private val Purple = Pink
private val Background = Paper
private val PartyShape = RoundedCornerShape(22.dp)
private fun funkyShape(key: String): Shape = RoundedCornerShape(22.dp)
private val PartyFont = FontFamily(
    Font(R.font.nunito_medium, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black),
)
private val PartyTypography = Typography(
    displayLarge = TextStyle(fontFamily = PartyFont, fontSize = 46.sp, lineHeight = 49.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = PartyFont, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-.5).sp),
    headlineMedium = TextStyle(fontFamily = PartyFont, fontSize = 25.sp, lineHeight = 29.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontFamily = PartyFont, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold),
    titleMedium = TextStyle(fontFamily = PartyFont, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = PartyFont, fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = PartyFont, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = PartyFont, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = PartyFont, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold),
    labelMedium = TextStyle(fontFamily = PartyFont, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontFamily = PartyFont, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold),
)

@Composable
fun DumbCharadesApp(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    SideEffect {
        (context as? Activity)?.let { activity ->
            val light = state.settings.theme != "HIGH_CONTRAST"
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = light
                isAppearanceLightNavigationBars = light
            }
            if (android.os.Build.VERSION.SDK_INT >= 29) activity.window.isNavigationBarContrastEnforced = false
        }
    }
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.dismissMessage() }
    }
    val colors = when (state.settings.theme) {
        "HIGH_CONTRAST" -> darkColorScheme(primary = Color.White, secondary = Color.Yellow, background = Color.Black, surface = Color.Black)
        else -> lightColorScheme(primary = Color(0xFFA43C2D), onPrimary = Color.White, secondary = Sky, onSecondary = Color.White, tertiary = Tangerine, background = Paper, onBackground = Ink, surface = Color(0xFFFFFCF6), onSurface = Ink, surfaceVariant = Color(0xFFF5E6D5), onSurfaceVariant = InkSoft, outline = Color(0xFFD9BFA9))
    }
    MaterialTheme(colorScheme = colors, typography = PartyTypography, shapes = Shapes(small = RoundedCornerShape(12.dp), medium = PartyShape, large = RoundedCornerShape(30.dp))) {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
            NavHost(nav, Routes.GAMES, Modifier.padding(padding)) {
                composable(Routes.GAMES) {
                    GameHubScreen(
                        state = state,
                        onSettings = { nav.navigate(Routes.SETTINGS) },
                        onCustom = { nav.navigate(Routes.CREATE_DECK) },
                        useDeck = { id -> viewModel.chooseGame(GameType.CHARADES); viewModel.chooseDeck(id); nav.navigate(Routes.SETUP) },
                        onResume = { nav.navigate(Routes.READY) },
                        helpChoose = { nav.navigate(Routes.CHOOSER) },
                        choose = { game -> viewModel.chooseGame(game); nav.navigate(Routes.RULES) },
                        partyMix = { viewModel.choosePlaylist(partyMixGames); nav.navigate(Routes.RULES) },
                    )
                }
                composable(Routes.CHOOSER) { GameChooserScreen(onBack = { nav.popBackStack() }) { game, quick ->
                    viewModel.chooseGame(game)
                    viewModel.setTimer(if (quick) 30 else 60)
                    viewModel.setRounds(if (quick) 3 else 5)
                    nav.navigate(Routes.RULES)
                } }
                composable(Routes.RULES) { GameRulesScreen(state, onBack = { nav.popBackStack() }, choosePack = viewModel::chooseDeck, custom = { nav.navigate(Routes.CUSTOM) }, onContinue = { nav.navigate(Routes.SETUP) }) }
                composable(Routes.SETUP) { SetupScreen(state, viewModel, onBack = { nav.popBackStack() }) { if (viewModel.prepareMatch()) nav.playStep(Routes.READY) } }
                composable(Routes.READY) { ReadyScreen(state, onStart = { viewModel.startRound(); nav.playStep(Routes.GAME) }, onQuit = { nav.home() }) }
                composable(Routes.GAME) {
                    var confirmExit by remember { mutableStateOf(false) }
                    BackHandler { confirmExit = true }
                    GameScreen(state, viewModel, finish = { viewModel.finishRound(); nav.playStep(Routes.SUMMARY) }, specialFinish = { won -> viewModel.finishSpecialRound(won); nav.playStep(Routes.SUMMARY) }, quit = { confirmExit = true })
                    if (confirmExit) AlertDialog(
                        onDismissRequest = { confirmExit = false },
                        title = { Text("Leave this round?") },
                        text = { Text("Your game is saved. Tap Continue on the home screen to replay this round.") },
                        confirmButton = { TextButton({
                            if (state.runtime?.paused == false) viewModel.pause()
                            confirmExit = false
                            nav.home()
                        }) { Text("Leave round") } },
                        dismissButton = { TextButton({ confirmExit = false }) { Text("Keep playing") } },
                    )
                }
                composable(Routes.SUMMARY) { SummaryScreen(state, viewModel, onContinue = { if (state.session?.completed == true) nav.playStep(Routes.FINAL) else { viewModel.nextRound(); nav.playStep(Routes.READY) } }, onEnd = { viewModel.endMatch(); nav.playStep(Routes.FINAL) }) }
                composable(Routes.FINAL) { FinalScreen(state, onRematch = { viewModel.rematch(); nav.playStep(Routes.READY) }, onHome = { nav.home() }) }
                composable(Routes.CREATE_DECK) { CustomDeckScreen(viewModel, onBack = { nav.popBackStack() }, useAfterSave = false) { nav.popBackStack() } }
                composable(Routes.CUSTOM) { CustomDeckScreen(viewModel, onBack = { nav.popBackStack() }) { id -> viewModel.chooseDeck(id); nav.popBackStack() } }
                composable(Routes.SETTINGS) { SettingsScreen(state, viewModel, onBack = { nav.popBackStack() }) }

            }
        }
    }
}

// Every in-game screen sits directly above the hub, so system Back always lands on Home.
private fun NavController.playStep(route: String) = navigate(route) {
    popUpTo(Routes.GAMES)
    launchSingleTop = true
}

// A fresh hub entry opens on the Home tab, where the Continue card lives.
private fun NavController.home() = navigate(Routes.GAMES) {
    popUpTo(graph.findStartDestination().id) { inclusive = true }
}

private val partyMixGames = listOf(GameType.CHARADES, GameType.BLANK_AGENT, GameType.CROWD_TRAP, GameType.IMPOSTOR_MIME, GameType.SIGNAL_LINE, GameType.BOMB_DEFUSAL, GameType.REVERSE_CHARADES, GameType.SOUND_EFFECTS)

/** Titles of the bundled packs that suit acting games; matches GameRepository.promptsFor. */
private val actingPackTitles = setOf("Hollywood Movies", "Bollywood Hits", "TV Shows & Anime", "Famous Personalities", "Actions & Verbs", "Pop Songs & Bands", "Brands & Logos", "Wild Scenes", "After Dark")

private fun GameType.builtInPack(): String? = when (this) {
    GameType.SOUND_EFFECTS -> "Sound Lab"
    GameType.BOMB_DEFUSAL -> "Five Second Frenzy"
    GameType.BLANK_AGENT, GameType.INSIDE_VOICE -> "Secret Words"
    GameType.CROWD_TRAP -> "Crowd Questions"
    GameType.SIGNAL_LINE -> "Signal Lines"
    GameType.MOST_LIKELY -> "Most Likely"
    GameType.NEVER_HAVE_I_EVER -> "Never Have I Ever"
    GameType.FAKE_IT -> "Fake It Trivia"
    GameType.DRAW_AND_GUESS -> "Draw & Guess"
    GameType.WOULD_YOU_RATHER -> "Would You Rather"
    GameType.TWIN_WORD_SPY -> "Twin Words"
    GameType.IMPOSTOR_MIME, GameType.MIME_TELEPHONE -> "Wild Scenes"
    else -> null
}

/** Packs the players can actually choose between. Most games use one built-in pack. */
private fun AppUiState.packChoices(): List<Deck> {
    val usable = decks.filter { !it.deleted }
    val game = matchSettings.gameType
    val acting = usable.filter { it.custom || it.title in actingPackTitles }
    val choices = when {
        matchSettings.playlist.isNotEmpty() || game == GameType.CHARADES -> acting
        game == GameType.REVERSE_CHARADES -> usable.filter { it.custom || it.title in setOf("Actions & Verbs", "Wild Scenes") }
        game == GameType.TRUTH_OR_DARE -> usable.filter { it.title in setOf("Truth or Dare", "Truth or Dare After Dark") }
        else -> usable.filter { it.title == game.builtInPack() }
    }
    return choices.ifEmpty { usable }
}

private fun GameType.isTimed(): Boolean = this in setOf(GameType.CHARADES, GameType.SOUND_EFFECTS, GameType.REVERSE_CHARADES, GameType.BOMB_DEFUSAL)

private enum class GameCategory(val tab: String, val title: String, val strapline: String, val color: Color) {
    ACTING("Act it out", "Acting & performance", "Get up. Make a scene.", Sky),
    SOCIAL("Talk & bluff", "Social & secret roles", "Big personalities. Little secrets.", Pink),
    CREATIVE("Think & draw", "Words, trivia & creative", "Quick minds. Wild ideas.", Green),
}

private data class HubGame(val type: GameType, val title: String, val subtitle: String, val color: Color, val category: GameCategory)

private val offlineGameCatalog = listOf(
    HubGame(GameType.CHARADES, "DUMB CHARADES", "Act it out.", Acid, GameCategory.ACTING),
    HubGame(GameType.IMPOSTOR_MIME, "IMPOSTOR MIME", "Blend into the action.", Green, GameCategory.ACTING),
    HubGame(GameType.REVERSE_CHARADES, "REVERSE CHARADES", "The room performs.", Tangerine, GameCategory.ACTING),
    HubGame(GameType.SOUND_EFFECTS, "SOUND EFFECTS", "Make noise, not words.", Color(0xFFE7DFFF), GameCategory.ACTING),
    HubGame(GameType.BLANK_AGENT, "BLANK AGENT", "Clue without knowing.", Color(0xFFE7DFFF), GameCategory.SOCIAL),
    HubGame(GameType.INSIDE_VOICE, "INSIDE VOICE", "Find the hidden influence.", Acid, GameCategory.SOCIAL),
    HubGame(GameType.CROWD_TRAP, "CROWD TRAP", "Move with the majority.", Green, GameCategory.SOCIAL),
    HubGame(GameType.SIGNAL_LINE, "SIGNAL LINE", "Read the room.", Tangerine, GameCategory.SOCIAL),
    HubGame(GameType.TRUTH_OR_DARE, "TRUTH OR DARE", "Answer or do it.", Sky, GameCategory.SOCIAL),
    HubGame(GameType.MOST_LIKELY, "MOST LIKELY", "Pick who fits.", Acid, GameCategory.SOCIAL),
    HubGame(GameType.NEVER_HAVE_I_EVER, "NEVER HAVE I EVER", "Five hearts to survive.", Color(0xFFE7DFFF), GameCategory.SOCIAL),
    HubGame(GameType.WOULD_YOU_RATHER, "WOULD YOU RATHER", "Choose your side.", Green, GameCategory.SOCIAL),
    HubGame(GameType.MIME_TELEPHONE, "MIME TELEPHONE", "Pass the meaning.", Acid, GameCategory.CREATIVE),
    HubGame(GameType.FAKE_IT, "FAKE IT", "Bluff the room.", Color(0xFFE7DFFF), GameCategory.CREATIVE),
    HubGame(GameType.DRAW_AND_GUESS, "DRAW & GUESS", "Sketch against time.", Tangerine, GameCategory.CREATIVE),
    HubGame(GameType.BOMB_DEFUSAL, "5-SECOND BOMB", "Name three. Pass fast.", Sky, GameCategory.CREATIVE),
    HubGame(GameType.TWIN_WORD_SPY, "TWIN WORD SPY", "Spot the odd word.", Pink, GameCategory.CREATIVE),
)
private data class FakeOption(val text: String, val author: Int? = null, val correct: Boolean = false)
private data class DrawMark(val point: Offset?, val color: Color, val width: Float)

private enum class HubTab(val label: String, val icon: Int) {
    HOME("Home", R.drawable.nav_home), GAMES("Games", R.drawable.nav_games),
    MY_DECKS("My decks", R.drawable.nav_decks), SCORES("Scores", R.drawable.nav_scores),
}

@Composable
private fun GameHubScreen(
    state: AppUiState, choose: (GameType) -> Unit, partyMix: () -> Unit,
    onSettings: () -> Unit, onCustom: () -> Unit, onResume: () -> Unit, useDeck: (String) -> Unit, helpChoose: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(HubTab.HOME) }
    var selectedCategory by rememberSaveable { mutableStateOf(GameCategory.ACTING) }
    val homeScroll = rememberLazyListState()
    val gamesScroll = rememberLazyListState()
    val decksScroll = rememberLazyListState()
    val scoresScroll = rememberLazyListState()
    val colors = MaterialTheme.colorScheme
    BackHandler(enabled = tab != HubTab.HOME) { tab = HubTab.HOME }
    Column(Modifier.fillMaxSize().background(colors.background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(tab.label, Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            IconButton(onSettings, Modifier.size(48.dp)) { Icon(painterResource(R.drawable.nav_settings), contentDescription = "Settings", tint = colors.primary) }
        }
        when (tab) {
            HubTab.HOME -> LazyColumn(
                Modifier.weight(1f).padding(horizontal = 18.dp), state = homeScroll,
                verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            ) {
                item(key = "welcome") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Good times\nstart here.", fontSize = 34.sp, lineHeight = 37.sp, fontWeight = FontWeight.Black)
                        Text("Pick a game, add your friends, pass the phone.", color = colors.onSurfaceVariant, fontSize = 15.sp)
                    }
                }
                if (state.session?.completed == false) item(key = "resume") {
                    Surface(onClick = onResume, color = colors.secondary, contentColor = colors.onSecondary, shape = PartyShape, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            GameCover(state.session.activeGameType, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Continue ${if (state.session.settings.playlist.isNotEmpty()) "Party mix" else state.session.activeGameType.displayName().friendlyTitle()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text("Round ${state.session.currentRound + 1}${state.session.settings.totalRounds?.let { " of $it" } ?: ""} · ${state.session.settings.teams.sumOf { it.players.size }} players", fontSize = 13.sp)
                            }
                            Text("→", fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                item(key = "start") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StartChoice("Help us pick a game", "Answer 3 quick questions", colors.primary, colors.onPrimary, helpChoose)
                        StartChoice("Browse all ${offlineGameCatalog.size} games", "Sorted by the kind of fun", colors.surface, colors.onSurface) { tab = HubTab.GAMES }
                    }
                }
                item(key = "quick") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Easy first games", style = MaterialTheme.typography.titleLarge)
                            Text("No explaining needed. Tap one to start.", color = colors.onSurfaceVariant, fontSize = 13.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(GameType.CHARADES, GameType.WOULD_YOU_RATHER, GameType.DRAW_AND_GUESS).forEach { game ->
                                val name = game.displayName().friendlyTitle()
                                Column(
                                    Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).clickable(role = Role.Button) { choose(game) },
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    GameCover(game, Modifier.fillMaxWidth().aspectRatio(.9f).clip(RoundedCornerShape(18.dp)))
                                    Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, lineHeight = 18.sp, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                item(key = "mix") {
                    Surface(onClick = partyMix, color = Acid, contentColor = Ink, shape = PartyShape, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Party mix", fontSize = 21.sp, fontWeight = FontWeight.Black)
                                Text("A different game every round. ${partyMixGames.size} games, one scoreboard.", fontSize = 13.sp)
                            }
                            Text("→", fontWeight = FontWeight.Black, fontSize = 22.sp)
                        }
                    }
                }
            }
            HubTab.GAMES -> LazyColumn(
                Modifier.weight(1f).padding(horizontal = 18.dp), state = gamesScroll,
                verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            ) {
                item(key = "categories") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GameCategory.entries.forEach { category ->
                            val active = selectedCategory == category
                            Surface(
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { selected = active }.clickable(role = Role.Tab) { selectedCategory = category },
                                color = if (active) colors.secondary else colors.surfaceVariant,
                                contentColor = if (active) colors.onSecondary else colors.onSurfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Box(Modifier.padding(horizontal = 4.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text(category.tab, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
                item(key = "category-heading") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(selectedCategory.title, style = MaterialTheme.typography.titleLarge)
                        Text("${selectedCategory.strapline} Tap a game to see how it works.", color = colors.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
                items(offlineGameCatalog.filter { it.category == selectedCategory }.sortedBy { if (it.type.guide().easy) 0 else 1 }.chunked(2), key = { row -> row.joinToString { it.type.name } }) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { game -> GameTile(game, { choose(game.type) }, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            HubTab.MY_DECKS -> LazyColumn(
                Modifier.weight(1f).padding(horizontal = 18.dp), state = decksScroll,
                verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            ) {
                item(key = "decks-heading") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Make it\npersonal.", fontSize = 34.sp, lineHeight = 37.sp, fontWeight = FontWeight.Black)
                        Text("Inside jokes make the best cards.", color = colors.onSurfaceVariant)
                        Surface(color = Acid, contentColor = Ink, shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("A deck only your people could make.", fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Black)
                                Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    PromptPostcard("Dance like\na robot", -5f, Modifier.weight(1f))
                                    PromptPostcard("Your friend's\nsignature move", 5f, Modifier.weight(1f))
                                }
                                PartyButton("Create a deck", onCustom)
                            }
                        }
                    }
                }
                val customDecks = state.decks.filter { it.custom && !it.deleted }
                item(key = "saved-heading") { Text("Your collection", style = MaterialTheme.typography.titleLarge) }
                if (customDecks.isEmpty()) item(key = "empty-decks") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nothing here yet. Make the first one.", fontWeight = FontWeight.Bold)
                        Text("Name it, add your prompts, then bring it to the party.", color = colors.onSurfaceVariant)
                    }
                }
                items(customDecks, key = { it.id }) { deck ->
                    Surface(color = colors.surface, shape = PartyShape, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(deck.title, style = MaterialTheme.typography.titleLarge)
                            Text("${deck.cardCount} cards", color = colors.onSurfaceVariant, fontSize = 13.sp)
                            if (deck.description.isNotBlank()) Text(deck.description, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton({ useDeck(deck.id) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Play charades") }
                        }
                    }
                }
            }
            HubTab.SCORES -> LazyColumn(
                Modifier.weight(1f).padding(horizontal = 18.dp), state = scoresScroll,
                verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            ) {
                item(key = "scoreboard-heading") {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(color = Acid, contentColor = Ink, shape = RoundedCornerShape(22.dp), modifier = Modifier.size(72.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.nav_scores), null, Modifier.size(40.dp)) }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Keep the bragging rights.", style = MaterialTheme.typography.titleLarge)
                            Text("Your saved matches, newest first.", color = colors.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }
                if (state.history.isEmpty()) item(key = "empty-scores") {
                    Column(Modifier.padding(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("The scoreboard is yours to start.", style = MaterialTheme.typography.headlineMedium)
                        Text("Finish a round and its saved score will appear here. Completed matches show the winner or a tie.", color = colors.onSurfaceVariant)
                        PartyButton("Browse games", { tab = HubTab.GAMES })
                    }
                }
                items(state.history, key = { it.id }) { session -> SavedScoreCard(session) }
            }
        }
        Row(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HubTab.entries.forEach { destination ->
                val active = tab == destination
                Surface(
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics { selected = active }.clickable(role = Role.Tab) { tab = destination },
                    color = if (active) colors.secondary else Color.Transparent,
                    contentColor = if (active) colors.onSecondary else colors.onSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(Modifier.padding(horizontal = 3.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(painterResource(destination.icon), contentDescription = null, modifier = Modifier.size(24.dp))
                        Text(destination.label, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun StartChoice(title: String, detail: String, background: Color, content: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick, color = background, contentColor = content, shape = PartyShape,
        border = if (background == MaterialTheme.colorScheme.surface) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.Black)
                Text(detail, fontSize = 13.sp, lineHeight = 17.sp)
            }
            Text("→", fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun PromptPostcard(text: String, angle: Float, modifier: Modifier = Modifier) {
    Surface(modifier.graphicsLayer { rotationZ = angle }, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp).heightIn(min = 100.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(painterResource(R.drawable.nav_decks), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun SavedScoreCard(session: GameSession) {
    val colors = MaterialTheme.colorScheme
    val score = session.savedScore()
    var expanded by rememberSaveable(session.id) { mutableStateOf(false) }
    val date = remember(session.updatedAt) {
        runCatching { DateTimeFormatter.ofPattern("d MMM yyyy · h:mm a", Locale.getDefault()).withZone(ZoneId.systemDefault()).format(Instant.parse(session.updatedAt)) }.getOrDefault("Date unavailable")
    }
    Surface(color = colors.surface, shape = PartyShape, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GameCover(session.settings.gameType, Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(if (session.settings.playlist.isNotEmpty()) "Party mix" else session.settings.gameType.displayName().friendlyTitle(), style = MaterialTheme.typography.titleMedium)
                    Text(date, color = colors.onSurfaceVariant, fontSize = 12.sp)
                    Text("${session.rounds.size} ${if (session.rounds.size == 1) "round" else "rounds"} · ${if (session.completed) "Finished" else "In progress"}", color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Surface(color = if (session.completed) Acid else colors.surfaceVariant, contentColor = if (session.completed) Ink else colors.onSurfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Text(score.result, Modifier.fillMaxWidth().padding(12.dp), fontWeight = FontWeight.ExtraBold)
            }
            score.teams.forEach { team ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(team.name, fontWeight = FontWeight.Bold)
                        Text(team.players.joinToString(", "), color = colors.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    Text("${team.points} ${if (kotlin.math.abs(team.points) == 1) "pt" else "pts"}", color = colors.primary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
            if (session.rounds.isNotEmpty()) TextButton({ expanded = !expanded }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(if (expanded) "Hide rounds" else "Round details", fontWeight = FontWeight.Bold)
            }
            if (expanded) session.rounds.forEachIndexed { index, round ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${index + 1}. ${round.actorName} · ${round.gameType.displayName().friendlyTitle()}", Modifier.weight(1f), fontSize = 13.sp, lineHeight = 18.sp)
                    Text("${round.score} ${if (kotlin.math.abs(round.score) == 1) "pt" else "pts"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun String.friendlyTitle(): String = lowercase().split(" ").withIndex().joinToString(" ") { (index, word) ->
    if (index > 0 && word in setOf("or", "of", "and")) word else word.replaceFirstChar { it.titlecase() }
}

@Composable
private fun GameTile(game: HubGame, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .97f else 1f, label = "game-tile-press")
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        color = colors.surface, shape = RoundedCornerShape(22.dp),
    ) {
        Column {
            GameCover(game.type, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(22.dp)))
            Column(Modifier.padding(horizontal = 11.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(game.title.friendlyTitle(), color = colors.onSurface, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.Black, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(game.type.guide().premise, color = colors.onSurfaceVariant, fontSize = 13.sp, lineHeight = 17.sp, minLines = 3, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text("${game.type.minimumPlayers()}+ players · ${game.type.difficultyLabel()}", color = colors.secondary, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun GameType.coverResource(): Int = when (this) {
    GameType.CHARADES -> R.drawable.cover_charades
    GameType.IMPOSTOR_MIME -> R.drawable.cover_impostor_mime
    GameType.BLANK_AGENT -> R.drawable.cover_blank_agent
    GameType.INSIDE_VOICE -> R.drawable.cover_inside_voice
    GameType.CROWD_TRAP -> R.drawable.cover_crowd_trap
    GameType.SIGNAL_LINE -> R.drawable.cover_signal_line
    GameType.TRUTH_OR_DARE -> R.drawable.cover_truth_or_dare
    GameType.MOST_LIKELY -> R.drawable.cover_most_likely
    GameType.NEVER_HAVE_I_EVER -> R.drawable.cover_never_have_i_ever
    GameType.FAKE_IT -> R.drawable.cover_fake_it
    GameType.DRAW_AND_GUESS -> R.drawable.cover_draw_and_guess
    GameType.WOULD_YOU_RATHER -> R.drawable.cover_would_you_rather
    GameType.TWIN_WORD_SPY -> R.drawable.cover_twin_word_spy
    GameType.REVERSE_CHARADES -> R.drawable.cover_reverse_charades
    GameType.MIME_TELEPHONE -> R.drawable.cover_mime_telephone
    GameType.SOUND_EFFECTS -> R.drawable.cover_sound_effects
    GameType.BOMB_DEFUSAL -> R.drawable.cover_bomb_defusal
}

@Composable
private fun GameCover(game: GameType, modifier: Modifier = Modifier) {
    Image(painterResource(game.coverResource()), contentDescription = null,
        modifier = modifier, contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
}

@Composable
private fun GameChooserScreen(onBack: () -> Unit, choose: (GameType, Boolean) -> Unit) {
    var step by rememberSaveable { mutableStateOf(0) }
    var count by rememberSaveable { mutableStateOf("4") }
    var mood by rememberSaveable { mutableStateOf(PartyMood.ACT) }
    var quick by rememberSaveable { mutableStateOf(true) }
    var alternative by rememberSaveable { mutableStateOf(0) }
    val chooserScroll = rememberLazyListState()
    LaunchedEffect(step, alternative) { chooserScroll.scrollToItem(0) }
    val players = count.toIntOrNull()
    val valid = players != null && players in 2..20
    val recommendations = if (valid) recommendGames(players!!, mood, quick) else emptyList()
    val recommendation = recommendations.getOrNull(alternative % recommendations.size.coerceAtLeast(1))
    val colors = MaterialTheme.colorScheme
    fun back() { if (step > 0) step-- else onBack() }
    BackHandler { back() }
    Page(if (step < 3) "Help us choose" else "Your game pick", onBack = { back() }) {
        LazyColumn(Modifier.weight(1f), state = chooserScroll, verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            if (step < 3) item { Text("${step + 1} of 3", color = colors.onSurfaceVariant, fontSize = 13.sp) }
            when (step) {
                0 -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("How many people are playing?", style = MaterialTheme.typography.headlineMedium)
                        Text("We'll suggest games that fit. Add names after choosing.", color = colors.onSurfaceVariant)
                        OutlinedTextField(count, { count = it.filter(Char::isDigit).take(2) }, label = { Text("Number of players") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            isError = !valid, supportingText = { Text("Choose 2–20 people") }, shape = PartyShape)
                    }
                }
                1 -> {
                    item { Text("What's the room in the mood for?", style = MaterialTheme.typography.headlineMedium) }
                    items(PartyMood.entries, key = { it.name }) { option ->
                        ChoiceRow(option.label, when (option) { PartyMood.ACT -> "Act, mime and make silly sounds."; PartyMood.TALK -> "Share answers, vote or spot a bluff."; PartyMood.THINK -> "Draw, guess and think on your feet." }, mood == option) { mood = option }
                    }
                }
                2 -> {
                    item { Text("How much time have you got?", style = MaterialTheme.typography.headlineMedium) }
                    item { ChoiceRow("A quick round", "Easy to jump into, with fewer rounds.", quick) { quick = true } }
                    item { ChoiceRow("A longer game", "More rounds and room to learn something new.", !quick) { quick = false } }
                }
                else -> if (recommendation != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            GameCover(recommendation, Modifier.fillMaxWidth().aspectRatio(1.5f).clip(PartyShape))
                            Text(recommendation.displayName().friendlyTitle(), style = MaterialTheme.typography.headlineLarge)
                            Text(recommendation.guide().premise, style = MaterialTheme.typography.bodyLarge)
                            Text("Fits $players players · ${if (recommendation.guide().easy) "Easy to start" else "Needs explaining"}", color = colors.secondary, fontWeight = FontWeight.Bold)
                            Text(if (quick) "A simple pick for a quick round." else "A good fit for your mood, with more rounds to enjoy.", color = colors.onSurfaceVariant)
                        }
                    }
                    if (recommendations.size > 1) item {
                        TextButton({ alternative++ }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Show another") }
                    }
                }
            }
        }
        PartyButton(when (step) { 0, 1 -> "Next"; 2 -> "Find our game"; else -> "Play this" }, {
            if (step < 3) { alternative = 0; step++ } else recommendation?.let { choose(it, quick) }
        }, enabled = valid && (step < 3 || recommendation != null))
    }
}

@Composable
private fun ChoiceRow(title: String, detail: String, active: Boolean, select: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { selected = active }.clickable(role = Role.RadioButton, onClick = select),
        color = if (active) colors.secondary else colors.surface, contentColor = if (active) colors.onSecondary else colors.onSurface,
        shape = PartyShape,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(detail, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameRulesScreen(state: AppUiState, onBack: () -> Unit, choosePack: (String) -> Unit, custom: () -> Unit, onContinue: () -> Unit) {
    val game = state.matchSettings.gameType
    val mix = state.matchSettings.playlist.isNotEmpty()
    val packs = state.packChoices()
    val chosen = packs.firstOrNull { it.id == state.matchSettings.deckIds.firstOrNull() } ?: packs.firstOrNull()
    val colors = MaterialTheme.colorScheme
    Page(if (mix) "Party mix" else game.displayName().friendlyTitle(), onBack = onBack) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            item(key = "intro") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!mix) GameCover(game, Modifier.fillMaxWidth().aspectRatio(1.8f).clip(RoundedCornerShape(24.dp)))
                    Text(if (mix) "A different game every round, one shared scoreboard. Best once your group knows a few games." else game.guide().premise, style = MaterialTheme.typography.bodyLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val minimum = state.matchSettings.playlist.ifEmpty { listOf(game) }.maxOf { it.minimumPlayers() }
                        InfoBadge("$minimum+ players")
                        if (!mix) InfoBadge(game.difficultyLabel())
                    }
                }
            }
            if (mix) {
                item(key = "mix-heading") { Text("Games in the mix", style = MaterialTheme.typography.titleLarge) }
                items(state.matchSettings.playlist.distinct(), key = { it.name }) { mixGame ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        GameCover(mixGame, Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)))
                        Column(Modifier.weight(1f)) {
                            Text(mixGame.displayName().friendlyTitle(), fontWeight = FontWeight.ExtraBold)
                            Text(mixGame.guide().premise, color = colors.onSurfaceVariant, fontSize = 13.sp, lineHeight = 17.sp)
                        }
                    }
                }
                item(key = "mix-note") { Text("Before every round the app shows which game is next, its quick rules and who holds the phone.", color = colors.onSurfaceVariant) }
            } else {
                item(key = "rules-heading") { Text("How to play", style = MaterialTheme.typography.titleLarge) }
                items(game.rules().withIndex().toList(), key = { "rule-${it.index}" }) { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                        Surface(color = Acid, contentColor = Ink, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("${item.index + 1}", fontWeight = FontWeight.Black, fontSize = 16.sp) }
                        }
                        Text(item.value, Modifier.weight(1f).padding(top = 3.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                item(key = "example") {
                    Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("For example", fontWeight = FontWeight.ExtraBold, color = colors.primary)
                            Text(game.guide().example, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (packs.size > 1) {
                item(key = "packs-heading") {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Pick your cards", style = MaterialTheme.typography.titleLarge)
                        Text("You can change this next time.", color = colors.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
                items(packs, key = { it.id }) { deck ->
                    val active = deck.id == chosen?.id
                    Surface(
                        modifier = Modifier.fillMaxWidth().semantics { selected = active }.clickable(role = Role.RadioButton) { choosePack(deck.id) },
                        color = if (active) colors.secondary else colors.surface, contentColor = if (active) colors.onSecondary else colors.onSurface,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(deck.title, fontWeight = FontWeight.ExtraBold)
                                Text(if (deck.custom) "Your deck · ${deck.cardCount} cards" else deck.description, fontSize = 13.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            if (active) Text("✓", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                if (mix || game == GameType.CHARADES || game == GameType.REVERSE_CHARADES) item(key = "custom-deck") {
                    TextButton(custom, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("+ Make your own deck") }
                }
            }
        }
        PartyButton("Next: add players", { chosen?.let { choosePack(it.id); onContinue() } }, enabled = chosen != null)
    }
}

@Composable
private fun InfoBadge(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupScreen(state: AppUiState, vm: AppViewModel, onBack: () -> Unit, onReady: () -> Unit) {
    val mix = state.matchSettings.playlist.isNotEmpty()
    val game = state.matchSettings.playlist.firstOrNull() ?: state.matchSettings.gameType
    val roster = remember { mutableStateListOf<String>().also { it.addAll(state.matchSettings.teams.flatMap { team -> team.players }.distinct().filterNot { name -> name in listOf("Player 1", "Player 2") }) } }
    var newPlayer by rememberSaveable { mutableStateOf("") }
    var twoTeams by rememberSaveable { mutableStateOf(false) }
    var assignments by rememberSaveable { mutableStateOf(mapOf<String, Int>()) }
    var rename by rememberSaveable { mutableStateOf(false) }
    var blueName by rememberSaveable { mutableStateOf("") }
    var coralName by rememberSaveable { mutableStateOf("") }
    var options by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val minimum = state.matchSettings.playlist.ifEmpty { listOf(game) }.maxOf { it.minimumPlayers() }
    val pending = newPlayer.trim()
    val duplicate = pending.isNotBlank() && roster.any { it.equals(pending, true) }
    val canAdd = pending.isNotBlank() && !duplicate && roster.size < 20
    val readyCount = roster.size + if (canAdd) 1 else 0
    val missing = (minimum - readyCount).coerceAtLeast(0)
    val teamsAllowed = game.prefersTeams() && roster.size >= 2
    val splitTeams = twoTeams && teamsAllowed
    val groups = (0..1).map { group -> roster.filterIndexed { index, name -> (assignments[name] ?: index % 2) == group } }
    val validTeams = !splitTeams || groups.all { it.isNotEmpty() }
    fun addPlayer() {
        if (canAdd) { roster.add(pending); newPlayer = ""; vm.setRoster(roster.toList(), false) }
    }
    fun removePlayer(player: String) { roster.remove(player); assignments = assignments - player; vm.setRoster(roster.toList(), false) }
    Page("Who's playing?", onBack = onBack) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            item(key = "selected-game") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GameCover(game, Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
                    Column {
                        Text(if (mix) "Party mix" else game.displayName().friendlyTitle(), fontWeight = FontWeight.Bold)
                        Text("Needs at least $minimum players", color = colors.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
            item(key = "add-players") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(newPlayer, { newPlayer = it.take(30) }, label = { Text("Player name") }, singleLine = true,
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), isError = duplicate,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { addPlayer() }))
                        Button({ addPlayer() }, Modifier.heightIn(min = 56.dp).padding(top = 6.dp), enabled = canAdd, shape = RoundedCornerShape(14.dp)) { Text("Add") }
                    }
                    Text(when {
                        duplicate -> "That name is already here. Try a different name."
                        roster.size == 20 -> "Party full: 20 players."
                        roster.isEmpty() -> "Type a name and tap Add. Add everyone in the room."
                        else -> "${roster.size} ${if (roster.size == 1) "player" else "players"} added"
                    }, color = if (duplicate) colors.error else colors.onSurfaceVariant, fontSize = 13.sp)
                }
            }
            if (roster.isNotEmpty()) item(key = "roster") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    roster.forEach { player ->
                        Surface(color = colors.surface, shape = CircleShape, border = BorderStroke(1.dp, colors.outline)) {
                            Row(Modifier.padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(player, fontWeight = FontWeight.Bold)
                                IconButton({ removePlayer(player) }, Modifier.size(44.dp).semantics { contentDescription = "Remove $player" }) {
                                    Text("✕", color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            if (teamsAllowed) item(key = "play-format") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Play together or in teams?", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoicePill("Together", !twoTeams, Modifier.weight(1f)) { twoTeams = false }
                        ChoicePill("Two teams", twoTeams, Modifier.weight(1f)) { twoTeams = true }
                    }
                    Text(if (twoTeams) "We split everyone evenly. Tap Move to swap someone." else "Everyone plays as one group and shares the score.", color = colors.onSurfaceVariant, fontSize = 13.sp)
                }
            }
            if (splitTeams) {
                item(key = "team-actions") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton({ assignments = roster.shuffled().withIndex().associate { it.value to it.index % 2 } }) { Text("Shuffle teams") }
                            TextButton({ rename = !rename }) { Text(if (rename) "Done naming" else "Rename teams") }
                        }
                        if (rename) {
                            OutlinedTextField(blueName, { blueName = it.take(30) }, label = { Text("First team name") }, placeholder = { Text("Team Blue") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(coralName, { coralName = it.take(30) }, label = { Text("Second team name") }, placeholder = { Text("Team Coral") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                (0..1).forEach { group ->
                    item(key = "team-$group") {
                        Surface(color = colors.surface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text((if (group == 0) blueName else coralName).ifBlank { if (group == 0) "Team Blue" else "Team Coral" }, style = MaterialTheme.typography.titleLarge, color = if (group == 0) Sky else colors.primary)
                                if (groups[group].isEmpty()) Text("Move at least one player here.", color = colors.error)
                                groups[group].forEach { player ->
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(player, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                        TextButton({ assignments = roster.withIndex().associate { it.value to (assignments[it.value] ?: it.index % 2) }.toMutableMap().apply { put(player, 1 - group) } }, Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Move $player to the other team" }) { Text("Move") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "options-toggle") {
                Surface(onClick = { options = !options }, color = colors.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Game settings", fontWeight = FontWeight.ExtraBold)
                            Text(listOfNotNull(
                                if (game.isTimed() && game != GameType.BOMB_DEFUSAL) "${state.matchSettings.timerSeconds}s turns" else null,
                                state.matchSettings.totalRounds?.let { "$it rounds" } ?: "Play until you stop",
                                if (game in setOf(GameType.CHARADES, GameType.SOUND_EFFECTS)) (if (state.matchSettings.mode == GameMode.FOREHEAD_TILT) "forehead tilt" else "tap buttons") else null,
                            ).joinToString(" · "), color = colors.onSurfaceVariant, fontSize = 13.sp)
                        }
                        Text(if (options) "Hide" else "Change", color = colors.primary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            if (options) item(key = "options") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (game in setOf(GameType.CHARADES, GameType.SOUND_EFFECTS)) {
                        MiniLabel("HOW TO MARK ANSWERS")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartyChip("Tap buttons", state.matchSettings.mode == GameMode.CLASSIC_TOUCH) { vm.chooseMode(GameMode.CLASSIC_TOUCH) }
                            PartyChip("Phone on forehead", state.matchSettings.mode == GameMode.FOREHEAD_TILT) { vm.chooseMode(GameMode.FOREHEAD_TILT) }
                        }
                    }
                    if (game.isTimed() && game != GameType.BOMB_DEFUSAL) {
                        MiniLabel("TIME PER TURN")
                        ChipRow(listOf(30, 60, 90, 120), state.matchSettings.timerSeconds, vm::setTimer)
                    }
                    MiniLabel("ROUNDS")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 10).forEach { n -> PartyChip("$n", state.matchSettings.totalRounds == n) { vm.setRounds(n) } }
                        PartyChip("Until we stop", state.matchSettings.totalRounds == null) { vm.setRounds(null) }
                    }
                    if (game.isTimed() && game != GameType.BOMB_DEFUSAL) {
                        MiniLabel("SKIPS PER TURN")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartyChip("3", state.matchSettings.skipLimit == 3) { vm.setSkipLimit(3) }
                            PartyChip("Unlimited", state.matchSettings.skipLimit == null) { vm.setSkipLimit(null) }
                        }
                    }
                }
            }
        }
        PartyButton(if (missing > 0) "Add $missing more ${if (missing == 1) "player" else "players"}" else "Start game", {
            addPlayer()
            vm.setTeams(partyTeams(roster.toList(), splitTeams, assignments, listOf(blueName, coralName)))
            onReady()
        }, enabled = missing == 0 && readyCount <= 20 && !duplicate && validTeams)
    }
}

@Composable
private fun ChoicePill(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.heightIn(min = 48.dp).semantics { selected = active }.clickable(role = Role.RadioButton, onClick = onClick),
        color = if (active) colors.secondary else colors.surface, contentColor = if (active) colors.onSecondary else colors.onSurface,
        border = if (active) null else BorderStroke(1.dp, colors.outline), shape = RoundedCornerShape(14.dp),
    ) {
        Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) { Text(label, fontWeight = FontWeight.ExtraBold) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun ChipRow(values: List<Int>, selected: Int, set: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { values.forEach { value -> PartyChip("${value}s", selected == value) { set(value) } } }
}

@Composable
private fun ReadyScreen(state: AppUiState, onStart: () -> Unit, onQuit: () -> Unit) {
    val session = state.session
    val teams = session?.settings?.teams.orEmpty()
    val team = teams.getOrNull(session?.activeTeamIndex ?: 0)
    val turns = session?.rounds?.count { it.activeTeamId == team?.id } ?: 0
    val turnActor = team?.players?.getOrNull(turns % (team.players.size.coerceAtLeast(1))) ?: "Player"
    val game = session?.activeGameType ?: GameType.CHARADES
    val privateHandoff = game in setOf(GameType.IMPOSTOR_MIME, GameType.BLANK_AGENT, GameType.INSIDE_VOICE, GameType.CROWD_TRAP, GameType.SIGNAL_LINE, GameType.TWIN_WORD_SPY, GameType.MOST_LIKELY, GameType.WOULD_YOU_RATHER, GameType.FAKE_IT)
    val actor = if (privateHandoff) teams.flatMap { it.players }.firstOrNull() ?: turnActor else turnActor
    val mix = session?.settings?.playlist?.isNotEmpty() == true
    val total = session?.settings?.totalRounds
    var countdown by remember { mutableStateOf<Int?>(null) }
    var counting by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    LaunchedEffect(counting) {
        if (counting) {
            for (value in 3 downTo 1) { countdown = value; delay(700) }
            countdown = null
            counting = false
            onStart()
        }
    }
    Page(game.displayName().friendlyTitle(), eyebrow = "Round ${(session?.currentRound ?: 0) + 1}${total?.let { " of $it" } ?: ""}${if (mix) " · Party mix" else ""}") {
        if (teams.size > 1) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            teams.forEach { candidate ->
                val active = candidate.id == team?.id
                Surface(Modifier.weight(1f), color = if (active) colors.secondary else colors.surface, contentColor = if (active) colors.onSecondary else colors.onSurface, shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(candidate.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${candidate.score} pts", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (countdown != null) {
            Surface(shape = CircleShape, color = Acid, modifier = Modifier.size(170.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("$countdown", fontSize = 84.sp, fontWeight = FontWeight.Black, color = Ink) }
            }
        } else GameCover(game, Modifier.size(150.dp).clip(RoundedCornerShape(32.dp)))
        Text(if (privateHandoff || teams.size <= 1) "Hand the phone to" else "${team?.name}'s turn · hand the phone to", color = colors.secondary, fontSize = 17.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(actor, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
        Text(game.readyInstruction(session?.settings?.mode ?: GameMode.CLASSIC_TOUCH), color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        PartyButton(if (game.isTimed() && game != GameType.REVERSE_CHARADES) "Start the timer" else "Start", {
            if (game.isTimed() && game != GameType.REVERSE_CHARADES) { countdown = 3; counting = true } else onStart()
        }, enabled = !counting)
        TextButton(onQuit, Modifier.fillMaxWidth()) { Text("Back to home (game is saved)") }
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
    val reversePrep = game == GameType.REVERSE_CHARADES && runtime.paused
    val context = LocalContext.current
    val activity = context as Activity
    val tilt = remember { TiltController(context.applicationContext) }
    val tone = remember(state.settings.soundVolume) { ToneGenerator(AudioManager.STREAM_MUSIC, (state.settings.soundVolume * 100).roundToInt()) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    fun respond(status: PromptStatus) {
        vm.answer(status)
        if (game == GameType.REVERSE_CHARADES && !runtime.paused) vm.pause()
        if (status == PromptStatus.GUESSED) {
            tone.startTone(if (state.settings.soundPack == "Arcade") ToneGenerator.TONE_DTMF_9 else ToneGenerator.TONE_PROP_ACK, 120)
            vibrator.vibrate(VibrationEffect.createOneShot(70, 180))
        } else {
            tone.startTone(if (state.settings.soundPack == "Arcade") ToneGenerator.TONE_DTMF_1 else ToneGenerator.TONE_PROP_NACK, 160)
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
    Box(Modifier.fillMaxSize().background(if (urgent && state.settings.theme != "High_contrast") Color(0xFFFFE8EF) else MaterialTheme.colorScheme.background)) {
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
                Surface(color = if (urgent) Pink else MaterialTheme.colorScheme.surface, contentColor = if (urgent) Ink else MaterialTheme.colorScheme.onSurface, shape = CircleShape) {
                    Text("${runtime.remainingSeconds}s", Modifier.padding(horizontal = 18.dp, vertical = 10.dp), fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                Text("Score: ${runtime.score}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = vm::pause) { Text(if (runtime.paused) "Resume" else "Pause", fontWeight = FontWeight.Black) }
            }
            Box(Modifier.weight(1f).fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp)).padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (game == GameType.BOMB_DEFUSAL) Text("Boom", fontSize = 44.sp, fontWeight = FontWeight.Black, color = Pink)
                    Text(if (game == GameType.REVERSE_CHARADES && !reversePrep) "Everyone act!" else runtime.currentPrompt?.text ?: "All done!", fontSize = 48.sp, lineHeight = 50.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    if (game != GameType.REVERSE_CHARADES || reversePrep) runtime.currentPrompt?.hint?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
                    if (reversePrep) {
                        Text("Show this card to every actor. The guesser must look away.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Button(vm::pause, colors = ButtonDefaults.buttonColors(containerColor = Acid, contentColor = Ink)) { Text("Hand to the guesser") }
                    } else if (runtime.paused) Surface(color = Acid, shape = CircleShape) { Text("Paused", Modifier.padding(horizontal = 18.dp, vertical = 8.dp), color = Ink, fontWeight = FontWeight.Black) }
                }
            }
            if (!reversePrep) {
                if (runtime.session.settings.penaltyEnabled) TextButton({ respond(PromptStatus.PENALTY) }, Modifier.align(Alignment.CenterHorizontally)) { Text("−1 Rule break") }
                Row(Modifier.fillMaxWidth().height(94.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button({ respond(PromptStatus.SKIPPED) }, Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Tangerine, contentColor = Ink), shape = PartyShape, enabled = !runtime.paused) { Text(if (game == GameType.BOMB_DEFUSAL) "Can't name 3" else "Skip  ↑", fontSize = 19.sp, fontWeight = FontWeight.Black) }
                    Button({ respond(PromptStatus.GUESSED) }, Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink), shape = PartyShape, enabled = !runtime.paused) { Text(if (game == GameType.BOMB_DEFUSAL) "Pass bomb  →" else "Got it  ↓", fontSize = 19.sp, fontWeight = FontWeight.Black) }
                }
            }
        }
    }
    if (runtime.paused && !reversePrep) AlertDialog(
        onDismissRequest = vm::pause,
        title = { Text("Round paused") },
        text = { Column { TextButton(vm::pause) { Text("Resume") }; TextButton({ vm.startRound() }) { Text("Restart round") }; TextButton(quit) { Text("Quit to home") } } },
        confirmButton = {},
    )
}

@Composable
private fun ImpostorGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(20)
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
    Page("Impostor Mime", eyebrow = when (phase) { 0 -> "Secret cards"; 1 -> "Everyone mime"; 2 -> "Cast your vote"; else -> "The reveal" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f))
                Text("Pass the phone to", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(players.getOrElse(playerIndex) { "Player" }, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, shape = PartyShape, colors = CardDefaults.cardColors(containerColor = if (revealed && playerIndex == impostorIndex) Pink else Acid)) {
                    Box(Modifier.fillMaxWidth().height(190.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (!revealed) "Tap to reveal" else if (playerIndex == impostorIndex) "YOU ARE THE\nIMPOSTOR" else runtime.currentPrompt?.text.orEmpty(),
                            color = Ink, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        )
                    }
                }
                Text(if (revealed) "Memorize it, then hide the screen." else "Keep this away from everyone else.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton(if (playerIndex == players.lastIndex) "Start the mime" else "Hide & pass", {
                    if (!revealed) return@PartyButton
                    if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false }
                }, enabled = revealed)
            }
            1 -> {
                Spacer(Modifier.weight(1f))
                Text("$seconds", fontSize = 108.sp, lineHeight = 112.sp, fontWeight = FontWeight.Black, color = if (seconds <= 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                Text("Everyone mimes at the same time. The impostor watches and blends in.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                OutlinedButton({ phase = 2 }, Modifier.fillMaxWidth()) { Text("Vote now") }
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
                PartyButton("Lock the vote", { phase = 3 }, enabled = votedIndex != null)
            }
            else -> {
                val caught = votedIndex == impostorIndex
                Spacer(Modifier.weight(1f))
                Text(if (caught) "Caught!" else "Escaped!", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink)
                Text("${players.getOrElse(impostorIndex) { "The impostor" }} was the impostor.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Text(if (caught) "The crew earns the point." else "The impostor fooled the room.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton("See round result", { complete(caught) })
            }
        }
        if (phase != 3) TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
    }
}

@Composable
private fun BlankAgentGameScreen(state: AppUiState, complete: (Boolean) -> Unit, quit: () -> Unit) {
    val runtime = state.runtime ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    val players = runtime.session.settings.teams.flatMap { it.players }.take(12)
    val whiteIndex = remember(runtime.session.id, runtime.session.currentRound) { kotlin.random.Random.nextInt(players.size.coerceAtLeast(1)) }
    val clueOrder = remember(runtime.session.id, runtime.session.currentRound) { players.indices.shuffled() }
    val clueConstraint = remember(runtime.session.id, runtime.session.currentRound) { listOf("One word only", "No letter e", "Personal memory", "Sound effect only", "Gesture only").random() }
    var phase by remember { mutableStateOf(0) }
    var playerIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var clueTurn by remember { mutableStateOf(0) }
    var votedIndex by remember { mutableStateOf<Int?>(null) }
    var finalGuess by remember { mutableStateOf("") }
    var crewWon by remember { mutableStateOf(false) }
    val secret = runtime.currentPrompt?.text.orEmpty()
    Page("Blank Agent", eyebrow = when (phase) { 0 -> "Secret words"; 1 -> "One clue each"; 2 -> "Find the agent"; 3 -> "Last chance"; else -> "The reveal" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f))
                Text("Pass the phone to", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(players.getOrElse(playerIndex) { "Player" }, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, shape = PartyShape, colors = CardDefaults.cardColors(containerColor = Acid)) {
                    Box(Modifier.fillMaxWidth().height(190.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (!revealed) "Tap to reveal" else if (playerIndex == whiteIndex) "YOU ARE THE\nBLANK AGENT\n\nListen carefully." else "THE WORD IS\n$secret",
                            color = Ink, fontSize = 29.sp, lineHeight = 33.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        )
                    }
                }
                Text("Do not react. Remember your role, hide it, and pass.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton(if (playerIndex == players.lastIndex) "Start clues" else "Hide & pass", {
                    if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false }
                }, enabled = revealed)
            }
            1 -> {
                Spacer(Modifier.weight(1f))
                Text("Clue ${clueTurn + 1} of ${players.size}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Surface(color = Tangerine, shape = CircleShape) { Text(clueConstraint, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Ink, fontWeight = FontWeight.Black) }
                Text(players.getOrElse(clueOrder.getOrElse(clueTurn) { 0 }) { "Player" }, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Text("Say one hint about the word. Be useful—but not so obvious that the Blank Agent learns it.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f))
                PartyButton(if (clueTurn == players.lastIndex) "All clues done · Vote" else "Clue given · Next", {
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
                PartyButton("Lock the vote", {
                    if (votedIndex == whiteIndex) phase = 3 else { crewWon = false; phase = 4 }
                }, enabled = votedIndex != null)
            }
            3 -> {
                Text("Blank agent caught", style = MaterialTheme.typography.headlineMedium, color = Pink)
                Text("${players.getOrElse(whiteIndex) { "Blank Agent" }}, guess the secret word to steal the win.", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(finalGuess, { finalGuess = it.take(60) }, label = { Text("Secret word") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f))
                PartyButton("Final answer", {
                    crewWon = !finalGuess.trim().equals(secret.trim(), ignoreCase = true)
                    phase = 4
                }, enabled = finalGuess.isNotBlank())
            }
            else -> {
                val escaped = votedIndex != whiteIndex
                Spacer(Modifier.weight(1f))
                Text(if (crewWon) "Crew wins" else "Agent wins", style = MaterialTheme.typography.displayLarge, color = if (crewWon) Green else Pink, textAlign = TextAlign.Center)
                Text("${players.getOrElse(whiteIndex) { "The agent" }} was the Blank Agent.", fontSize = 21.sp, textAlign = TextAlign.Center)
                Text("The secret word was $secret.", fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                if (!crewWon) Text(if (escaped) "The vote missed." else "The final guess was correct.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                PartyButton("See round result", { complete(crewWon) })
            }
        }
        if (phase < 4) TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    var showHostWord by remember { mutableStateOf(false) }
    if (phase == 1 && showHostWord) {
        AlertDialog(
            onDismissRequest = { showHostWord = false },
            title = { Text("HOST ONLY · ${players[host]}") },
            text = { Text(secret, style = MaterialTheme.typography.headlineLarge) },
            confirmButton = { TextButton({ showHostWord = false }) { Text("Hide word") } },
        )
    }
    LaunchedEffect(phase) {
        if (phase == 1) {
            while (seconds > 0 && !solved) { delay(1_000); seconds-- }
            phase = 2
        }
    }
    Page("Inside Voice", eyebrow = when (phase) { 0 -> "Private roles"; 1 -> "Yes / no search"; 2 -> "Find the distorter"; else -> "The reveal" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass the phone to", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(players[playerIndex], style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, shape = PartyShape, colors = CardDefaults.cardColors(containerColor = Acid)) {
                    Box(Modifier.fillMaxWidth().height(210.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                        val role = when (playerIndex) { host -> "HOST\nAnswer yes or no.\nWORD: $secret"; navigator -> "NAVIGATOR\nQuietly guide the group.\nWORD: $secret"; distorter -> "DISTORTER\nSecretly steer them away.\nWORD: $secret"; else -> "SEEKER\nAsk sharp yes/no questions." }
                        Text(if (revealed) role else "Tap to reveal", color = Ink, fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.weight(1f)); PartyButton(if (playerIndex == players.lastIndex) "Begin search" else "Hide & pass", {
                    if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false }
                }, enabled = revealed)
            }
            1 -> {
                Text("${players[host]} is the Host", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text("$seconds", fontSize = 92.sp, lineHeight = 96.sp, fontWeight = FontWeight.Black, color = if (seconds <= 15) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                PartyPanel("Word hidden", Sky) {
                    Text("Keep guessing. Only the Host can check the word privately.")
                    OutlinedButton({ showHostWord = true }, Modifier.fillMaxWidth()) { Text("Host: check word privately") }
                }
                Text("Everyone asks yes/no questions. The Navigator helps subtly; the Distorter misleads.", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("The group solved it", { solved = true; phase = 2 })
                OutlinedButton({ solved = false; phase = 2 }, Modifier.fillMaxWidth()) { Text("Time up · Vote") }
            }
            2 -> {
                Text(if (solved) "Who tried to mislead you?" else "Who was secretly helping?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(players.withIndex().filter { it.index != host }, key = { it.index }) { item ->
                        Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, shape = RoundedCornerShape(18.dp), border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontSize = 19.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                PartyButton("Lock the vote", { phase = 3 }, enabled = votedIndex != null)
            }
            else -> {
                val target = if (solved) distorter else navigator
                val caught = votedIndex == target
                Spacer(Modifier.weight(1f)); Text(if (caught) "Voice found" else "Voice escaped", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink, textAlign = TextAlign.Center)
                Text("${players[target]} was the ${if (solved) "Distorter" else "Navigator"}.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Text("The secret word was $secret.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f)); PartyButton("See round result", { complete(caught) })
            }
        }
        if (phase < 3) TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Crowd Trap", eyebrow = when (phase) { 0 -> "Secret role"; 1 -> "Private answers"; 2 -> "The crowd speaks"; 3 -> "Final answers"; 4 -> "Results"; 5 -> "Find the trendsetter"; else -> "Reveal" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass the phone to ${players[playerIndex]}")
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, colors = CardDefaults.cardColors(containerColor = Acid), shape = PartyShape) {
                    Box(Modifier.height(190.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(if (!revealed) "Tap to reveal" else if (playerIndex == trendsetter) "YOU ARE THE TRENDSETTER\n\nMake at least one person switch answers." else "YOU ARE THE CROWD\n\nMatch the final majority.", Modifier.padding(20.dp), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
                }
                Spacer(Modifier.weight(1f)); PartyButton(if (playerIndex == players.lastIndex) "Start question" else "Hide & pass", { if (playerIndex == players.lastIndex) { phase = 1; playerIndex = 0 } else { playerIndex++; revealed = false } }, enabled = revealed)
            }
            1, 3 -> {
                Text(question, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text("${players[playerIndex]} answers privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                if (phase == 3) Text("First answer: ${firstAnswers[playerIndex]}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(input, { input = it.take(40) }, label = { Text(if (phase == 1) "Short answer" else "Keep or change") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("Lock & pass", {
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
                PartyButton("Answer again", { phase = 3; input = firstAnswers[0] })
            }
            4 -> {
                val changed = players.indices.count { !firstAnswers[it].equals(finalAnswers[it], true) }
                Text("Final crowd", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(groups(finalAnswers).toList()) { item -> PartyPanel(item.key.uppercase(), Tangerine) { Text("${item.value} vote${if (item.value == 1) "" else "s"}") } } }
                Text("$changed player${if (changed == 1) "" else "s"} switched.", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                PartyButton("Vote for the trendsetter", { phase = 5 })
            }
            5 -> {
                Text("Who steered the discussion?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("Reveal", { phase = 6 }, enabled = votedIndex != null)
            }
            else -> {
                val caught = votedIndex == trendsetter
                Spacer(Modifier.weight(1f)); Text(if (caught) "Crowd wins" else "Trendsetter wins", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink, textAlign = TextAlign.Center)
                Text("${players[trendsetter]} was the Trendsetter.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("See round result", { complete(caught) })
            }
        }
        if (phase < 6) TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    val left = ends.getOrElse(0) { "Low" }; val right = ends.getOrElse(1) { "High" }
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
    Page("Signal Line", eyebrow = when (phase) { 0 -> "Secret roles"; 1 -> "Call the signal"; 2 -> "Private reads"; 3 -> "Group decision"; 4 -> "Find the jammer"; else -> "Reveal" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass the phone to ${players[playerIndex]}")
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, colors = CardDefaults.cardColors(containerColor = Acid), shape = PartyShape) { Box(Modifier.height(210.dp).fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    val zoneStart = (target / 25) * 25
                    val role = when (playerIndex) { caller -> "CALLER\nTarget: $target / 100\nGive a scenario that lands there."; jammer -> "JAMMER\nTarget zone: $zoneStart–${(zoneStart + 25).coerceAtMost(100)}\nPush the group away from it."; else -> "READER\nPlace the Caller's scenario on the line." }
                    Text(if (revealed) role else "Tap to reveal", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                } }
                Spacer(Modifier.weight(1f)); PartyButton(if (playerIndex == players.lastIndex) "Call the signal" else "Hide & pass", { if (playerIndex == players.lastIndex) phase = 1 else { playerIndex++; revealed = false } }, enabled = revealed)
            }
            1 -> {
                Text("$left  ↔  $right", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text("${players[caller]}, your hidden target is $target", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                OutlinedTextField(scenario, { scenario = it.take(100) }, label = { Text("A scenario, not a number") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("Lock clue & pass", { phase = 2 }, enabled = scenario.isNotBlank())
            }
            2 -> {
                val current = guessers[guessTurn]
                Text("$left  ↔  $right", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                PartyPanel("The scenario", Sky) { Text(scenario, fontSize = 21.sp, fontWeight = FontWeight.Bold) }
                Text("${players[current]} places it privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text("${slider.roundToInt()}", fontSize = 54.sp, fontWeight = FontWeight.Black)
                Slider(slider, { slider = it }, valueRange = 0f..100f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(left); Text(right) }
                Spacer(Modifier.weight(1f)); PartyButton("Lock & pass", { guesses[current] = slider; slider = 50f; if (guessTurn == guessers.lastIndex) phase = 3 else guessTurn++ })
            }
            3 -> {
                val spread = guessers.map { guesses[it] }
                Text("Read the room", style = MaterialTheme.typography.headlineMedium)
                Text("Private guesses spread from ${spread.min().roundToInt()} to ${spread.max().roundToInt()}.", textAlign = TextAlign.Center)
                Text("Debate, then place one shared marker.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${groupMarker.roundToInt()}", fontSize = 64.sp, fontWeight = FontWeight.Black)
                Slider(groupMarker, { groupMarker = it }, valueRange = 0f..100f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(left); Text(right) }
                Spacer(Modifier.weight(1f)); PartyButton("Lock group marker", { phase = 4 })
            }
            4 -> {
                Text("Who jammed the discussion?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().filter { it.index != caller }) { item -> Card(Modifier.fillMaxWidth().clickable { votedIndex = item.index }, border = BorderStroke(2.dp, if (votedIndex == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("Reveal signal", { phase = 5 }, enabled = votedIndex != null)
            }
            else -> {
                val accurate = kotlin.math.abs(groupMarker - target) <= 10f
                val caught = votedIndex == jammer
                val crewWon = accurate && caught
                Spacer(Modifier.weight(1f)); Text(if (crewWon) "Signal found" else "Signal jammed", style = MaterialTheme.typography.displayLarge, color = if (crewWon) Green else Pink, textAlign = TextAlign.Center)
                Text("Target $target · Group ${groupMarker.roundToInt()}", fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("${players[jammer]} was the Jammer.${if (!caught) " The vote missed." else ""}", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("See round result", { complete(crewWon) })
            }
        }
        if (phase < 5) TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    var choice by remember { mutableStateOf("Truth") }
    LaunchedEffect(spinning) {
        if (spinning) {
            repeat(16) { selected = (selected + 1) % players.size; delay(45L + it * 6L) }
            selected = kotlin.random.Random.nextInt(players.size); spinning = false; phase = 1
        }
    }
    Page("Truth or Dare", eyebrow = when (phase) { 0 -> "Spin the player"; 1 -> "Make the choice"; else -> choice }) {
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = if (spinning) Tangerine else Pink, modifier = Modifier.size(190.dp)) { Box(contentAlignment = Alignment.Center) { Text(players[selected], Modifier.padding(18.dp), color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) } }
        when (phase) {
            0 -> PartyButton(if (spinning) "Spinning…" else "Spin the wheel", { spinning = true }, enabled = !spinning)
            1 -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button({ choice = "Truth"; phase = 2 }, Modifier.weight(1f).height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Sky, contentColor = Color.White)) { Text("Truth", fontWeight = FontWeight.Black) }
                Button({ choice = "Dare"; phase = 2 }, Modifier.weight(1f).height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Ink)) { Text("Dare", fontWeight = FontWeight.Black) }
            }
            else -> {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (choice == "Truth") Sky else Pink), shape = PartyShape) { Text(if (choice == "Truth") pair.firstOrNull().orEmpty() else pair.getOrElse(1) { pair.firstOrNull().orEmpty() }, Modifier.padding(24.dp), color = if (choice == "Truth") Color.White else Ink, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
                PartyButton("Challenge complete", { complete(true) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Most Likely", eyebrow = if (reveal) "Room verdict" else "Private vote ${voter + 1} of ${players.size}") {
        Text(runtime.currentPrompt?.text.orEmpty(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        if (!reveal) {
            Text("Pass the phone to ${players[voter]}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable { selected = item.index }, border = BorderStroke(2.dp, if (selected == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
            PartyButton("Lock & pass", { votes.add(selected!!); selected = null; if (voter == players.lastIndex) reveal = true else voter++ }, enabled = selected != null)
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(players.withIndex().sortedByDescending { candidate -> votes.count { it == candidate.index } }) { item ->
                    val count = votes.count { it == item.index }; val percent = count * 100 / votes.size.coerceAtLeast(1)
                    PartyPanel(item.value.uppercase(), if (percent == votes.groupingBy { it }.eachCount().values.maxOrNull()?.times(100)?.div(votes.size.coerceAtLeast(1))) Pink else Sky) { LinearProgressIndicator({ percent / 100f }, Modifier.fillMaxWidth()); Text("$percent% · $count votes", fontWeight = FontWeight.Bold) }
                }
            }
            PartyButton("Round complete", { complete(true) })
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Never Have I Ever", eyebrow = if (finished) "Last hearts standing" else "Confession ${card + 1} of 10") {
        if (!finished) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Pink), shape = PartyShape) { Text(prompt, Modifier.padding(24.dp), color = Color.White, fontSize = 28.sp, lineHeight = 33.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
            Text("If you have, tap your name once.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable(enabled = hearts[item.index] > 0 && item.index !in guilty) { guilty.add(item.index); hearts[item.index]-- }, border = BorderStroke(2.dp, if (item.index in guilty) Pink else MaterialTheme.colorScheme.outline)) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(item.value, fontWeight = FontWeight.Bold); Text("♥".repeat(hearts[item.index]), color = Pink, fontSize = 20.sp) } } } }
            PartyButton("Next confession", { val alive = hearts.count { it > 0 }; if (alive <= 1 || card >= 9) finished = true else { card++; guilty.clear() } })
        } else {
            Spacer(Modifier.weight(1f)); val best = hearts.maxOrNull() ?: 0
            Text("Survivors", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.secondary)
            Text(players.indices.filter { hearts[it] == best }.joinToString { players[it] }, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text("$best hearts remaining", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f)); PartyButton("See round result", { complete(true) })
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Would You Rather", eyebrow = if (reveal) "The room chose" else "${players[voter]} chooses privately") {
        Spacer(Modifier.weight(1f))
        if (!reveal) {
            Button({ votes.add(false); if (voter == players.lastIndex) reveal = true else voter++ }, Modifier.fillMaxWidth().weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Sky, contentColor = Color.White), shape = PartyShape) { Text("A\n${options.firstOrNull().orEmpty()}", fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
            Text("OR", fontWeight = FontWeight.Black)
            Button({ votes.add(true); if (voter == players.lastIndex) reveal = true else voter++ }, Modifier.fillMaxWidth().weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Pink), shape = PartyShape) { Text("B\n${options.getOrElse(1) { "" }}", fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
        } else {
            val b = votes.count { it }; val a = votes.size - b
            PartyPanel("A · ${a * 100 / votes.size}%", Sky) { Text(options.firstOrNull().orEmpty(), fontSize = 22.sp, fontWeight = FontWeight.Bold); LinearProgressIndicator({ a / votes.size.toFloat() }, Modifier.fillMaxWidth()) }
            PartyPanel("B · ${b * 100 / votes.size}%", Pink) { Text(options.getOrElse(1) { "" }, fontSize = 22.sp, fontWeight = FontWeight.Bold); LinearProgressIndicator({ b / votes.size.toFloat() }, Modifier.fillMaxWidth(), color = Pink) }
            Spacer(Modifier.weight(1f)); PartyButton("Round complete", { complete(true) })
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Fake It", eyebrow = when (phase) { 0 -> "Write a lie"; 1 -> "Find the truth"; else -> "Scores" }) {
        Text(question, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        when (phase) {
            0 -> {
                Text("${players[playerIndex]} writes privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text("Invent a believable wrong answer.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(input, { input = it.take(60) }, label = { Text("Fake answer") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("Lock & pass", {
                    fakes[playerIndex] = input.trim(); input = ""
                    if (playerIndex == players.lastIndex) { options.addAll((fakes.mapIndexed { index, text -> FakeOption(text, index) } + FakeOption(answer, correct = true)).shuffled()); playerIndex = 0; phase = 1 } else playerIndex++
                }, enabled = input.isNotBlank() && !input.trim().equals(answer, true))
            }
            1 -> {
                Text("${players[playerIndex]} votes privately", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(options.withIndex().filter { it.value.author != playerIndex }, key = { it.index }) { item -> Card(Modifier.fillMaxWidth().clickable { selected = item.index }, border = BorderStroke(2.dp, if (selected == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value.text, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("Lock & pass", {
                    val chosen = options[selected!!]
                    if (chosen.correct) scores[playerIndex] += 2 else chosen.author?.let { scores[it]++ }
                    selected = null
                    if (playerIndex == players.lastIndex) phase = 2 else playerIndex++
                }, enabled = selected != null)
            }
            else -> {
                PartyPanel("Real answer", Green) { Text(answer, fontSize = 25.sp, fontWeight = FontWeight.Black) }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) { items(players.indices.sortedByDescending { scores[it] }) { index -> Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(players[index], fontWeight = FontWeight.Bold); Text("${scores[index]} pts", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black) } } }
                PartyButton("Round complete", { complete(true) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Draw & Guess", eyebrow = when (phase) { 0 -> "Artist only"; 1 -> "$seconds seconds left"; else -> "ROUND OVER" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Secret word", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(runtime.currentPrompt?.text.orEmpty(), style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Text("Show only the artist, then hide the word.", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("Start drawing", { phase = 1 })
            }
            1 -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(Ink, Pink, Sky, Green, Tangerine).forEach { value -> Box(Modifier.size(34.dp).background(value, CircleShape).border(if (color == value) 3.dp else 1.dp, Color.White, CircleShape).clickable { color = value }) }
                    TextButton({ color = Paper }) { Text("Eraser") }
                    TextButton({ marks.clear() }) { Text("Clear") }
                }
                Slider(width, { width = it }, valueRange = 4f..28f)
                GameDrawingCanvas(marks, color, width, Modifier.fillMaxWidth().weight(1f))
                PartyButton("Guessed it!", { solved = true; phase = 2 })
            }
            else -> {
                Spacer(Modifier.weight(1f)); Text(if (solved) "Masterpiece!" else "Time's up!", style = MaterialTheme.typography.displayLarge, color = if (solved) Green else Pink)
                Text("The word was ${runtime.currentPrompt?.text.orEmpty()}.", fontSize = 23.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("See round result", { complete(solved) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Twin Word Spy", eyebrow = when (phase) { 0 -> "Secret words"; 1 -> "One clue each"; 2 -> "Find the spy"; else -> "Reveal" }) {
        when (phase) {
            0 -> {
                Spacer(Modifier.weight(1f)); Text("Pass the phone to ${players[player]}")
                Card(Modifier.fillMaxWidth().clickable { revealed = true }, colors = CardDefaults.cardColors(containerColor = Acid), shape = PartyShape) { Box(Modifier.height(190.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(if (revealed) "YOUR WORD\n${if (player == spy) words.getOrElse(1) { "?" } else words.firstOrNull().orEmpty()}" else "TAP TO REVEAL", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) } }
                Spacer(Modifier.weight(1f)); PartyButton(if (player == players.lastIndex) "Start clues" else "Hide & pass", { if (player == players.lastIndex) phase = 1 else { player++; revealed = false } }, enabled = revealed)
            }
            1 -> {
                Spacer(Modifier.weight(1f)); Text("Clue ${clue + 1} of ${players.size}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                Text(players[order[clue]], style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Text("Describe your word with one short clue. Do not say the word.", textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton(if (clue == players.lastIndex) "Vote" else "Next clue", { if (clue == players.lastIndex) phase = 2 else clue++ })
            }
            2 -> {
                Text("Who received the related word?", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(players.withIndex().toList()) { item -> Card(Modifier.fillMaxWidth().clickable { vote = item.index }, border = BorderStroke(2.dp, if (vote == item.index) Pink else MaterialTheme.colorScheme.outline)) { Text(item.value, Modifier.padding(17.dp), fontWeight = FontWeight.Bold) } } }
                PartyButton("Reveal", { phase = 3 }, enabled = vote != null)
            }
            else -> {
                val caught = vote == spy; Spacer(Modifier.weight(1f)); Text(if (caught) "Spy caught" else "Spy escaped", style = MaterialTheme.typography.displayLarge, color = if (caught) Green else Pink, textAlign = TextAlign.Center)
                Text("${players[spy]} had “${words.getOrElse(1) { "?" }}”. Everyone else had “${words.firstOrNull().orEmpty()}”.", fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f)); PartyButton("See round result", { complete(caught) })
            }
        }
        TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    Page("Mime Telephone", eyebrow = "${stage + 1} OF 6") {
        when (stage) {
            0 -> {
                Text("${players.getOrElse(0) { "Player 1" }} writes the secret", style = MaterialTheme.typography.headlineMedium)
                Text("Nobody else should see it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(phrase, { phrase = it.take(120) }, label = { Text("A weird scene") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("Lock phrase & pass", { stage = 1 }, enabled = phrase.isNotBlank())
            }
            1 -> {
                Text("${players.getOrElse(1) { "Player 2" }} records the mime", style = MaterialTheme.typography.headlineMedium)
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Acid), shape = PartyShape) { Text(phrase, Modifier.padding(22.dp), color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
                Text("You have up to 15 seconds. No spoken words.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f)); OutlinedButton(::record, Modifier.fillMaxWidth().height(58.dp)) { Text(if (recorded) "Record again" else "RECORD 15s CLIP") }
                PartyButton("Hide phrase & pass", { stage = 2 }, enabled = recorded)
            }
            2 -> {
                Text("${players.getOrElse(2) { "Player 3" }} watches the clip", style = MaterialTheme.typography.headlineMedium)
                Text("Watch once, then draw only what you remember.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = Sky, modifier = Modifier.size(150.dp)) { Box(contentAlignment = Alignment.Center) { Text("Play", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black) } }
                Spacer(Modifier.weight(1f))
                OutlinedButton({ clipUri?.let { context.startActivity(Intent(Intent.ACTION_VIEW, it).setDataAndType(it, "video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) } }, Modifier.fillMaxWidth(), enabled = clipUri != null) { Text(if (clipUri == null) "Clip captured" else "Watch clip") }
                PartyButton("Draw what you saw", { stage = 3 })
            }
            3 -> {
                Text("Draw it. No letters or words.", style = MaterialTheme.typography.headlineMedium)
                DrawingPad(strokes, editable = true, modifier = Modifier.fillMaxWidth().weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ strokes.clear() }, Modifier.weight(1f)) { Text("Clear") }
                    Button({ stage = 4 }, Modifier.weight(1f), enabled = strokes.isNotEmpty()) { Text("Pass it") }
                }
            }
            4 -> {
                Text("${players.getOrElse(3) { "Player 4" }} guesses the original", style = MaterialTheme.typography.headlineMedium)
                DrawingPad(strokes, editable = false, modifier = Modifier.fillMaxWidth().height(270.dp))
                OutlinedTextField(guess, { guess = it.take(120) }, label = { Text("Your final guess") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f)); PartyButton("Reveal the chain", { stage = 5 }, enabled = guess.isNotBlank())
            }
            else -> {
                Text("The chain", style = MaterialTheme.typography.headlineMedium)
                PartyPanel("1 · Original", Pink) { Text(phrase, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                PartyPanel("2 · Video", Sky) { Text(if (recorded) "15-second mime captured" else "No clip") }
                PartyPanel("3 · Drawing", Tangerine) { DrawingPad(strokes, editable = false, modifier = Modifier.fillMaxWidth().height(130.dp)) }
                PartyPanel("4 · Final guess", Acid) { Text(guess, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Text("Did the meaning survive?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ complete(false) }, Modifier.weight(1f)) { Text("Not even close") }
                    Button({ complete(true) }, Modifier.weight(1f)) { Text("Nailed it") }
                }
            }
        }
        if (stage < 5) TextButton(quit, Modifier.fillMaxWidth()) { Text("Leave game") }
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
    val session = state.session
    val lastRound = session?.rounds?.lastOrNull()
    val game = lastRound?.gameType ?: session?.activeGameType ?: GameType.CHARADES
    val teams = session?.settings?.teams.orEmpty()
    val scorer = teams.firstOrNull { it.id == lastRound?.activeTeamId }
    val points = runtime?.score ?: lastRound?.score ?: 0
    val completed = session?.completed == true
    val colors = MaterialTheme.colorScheme
    Page("Round ${lastRound?.index ?: 1} done", eyebrow = game.displayName().friendlyTitle()) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            item(key = "result") {
                Surface(color = if (points > 0) Green else colors.surfaceVariant, contentColor = Ink, shape = PartyShape, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (game.isTimed()) {
                            Text("$points", fontSize = 64.sp, lineHeight = 66.sp, fontWeight = FontWeight.Black)
                            Text("${if (kotlin.math.abs(points) == 1) "point" else "points"} for ${if (teams.size > 1) scorer?.name ?: "this team" else "the group"}", fontWeight = FontWeight.ExtraBold)
                        } else {
                            Text(if (points > 0) "+1 point" else "No point this round", fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black)
                            Text(if (points > 0) "Nicely played, ${if (teams.size > 1) scorer?.name ?: "team" else "everyone"}!" else "Shake it off. Next round is a fresh start.", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (game.isTimed() && !runtime?.results.isNullOrEmpty()) {
                item(key = "fix-hint") { Text("Marked something wrong? Tap a card to switch it.", color = colors.onSurfaceVariant, fontSize = 13.sp) }
                items(runtime?.results?.withIndex()?.toList() ?: emptyList(), key = { it.index }) { indexed ->
                    val status = indexed.value.status
                    Surface(onClick = { if (status != PromptStatus.PENALTY) vm.override(indexed.index) }, color = colors.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(indexed.value.text, Modifier.weight(1f))
                            Text(when (status) { PromptStatus.GUESSED -> "✓ Got it"; PromptStatus.SKIPPED -> "Skipped"; PromptStatus.PENALTY -> "−1" },
                                color = if (status == PromptStatus.GUESSED) Color(0xFF2F6B45) else colors.onSurfaceVariant, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            item(key = "standings") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Text(if (teams.size > 1) "Scores so far" else "Group total", style = MaterialTheme.typography.titleMedium)
                    teams.sortedByDescending { it.score }.forEach { team ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (teams.size > 1) team.name else team.players.joinToString(", "), Modifier.weight(1f), fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${team.score} pts", color = colors.primary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            if (!completed && session != null) item(key = "next") {
                val nextTeam = teams.getOrNull(session.activeTeamIndex)
                Text("Up next: ${session.activeGameType.displayName().friendlyTitle()}${if (teams.size > 1 && nextTeam != null) " · ${nextTeam.name}" else ""}", color = colors.secondary, fontWeight = FontWeight.ExtraBold)
            }
        }
        PartyButton(if (completed) "See final results" else "Next round", onContinue)
        if (!completed) TextButton(onEnd, Modifier.fillMaxWidth()) { Text("Finish the game now") }
    }
}

@Composable
private fun FinalScreen(state: AppUiState, onRematch: () -> Unit, onHome: () -> Unit) {
    val session = state.session
    val teams = session?.settings?.teams.orEmpty().sortedByDescending { it.score }
    val winner = teams.firstOrNull()
    val together = teams.size <= 1
    val tie = teams.size > 1 && teams[0].score == teams[1].score
    val gameName = if (session?.settings?.playlist?.isNotEmpty() == true) "Party mix" else session?.settings?.gameType?.displayName()?.friendlyTitle() ?: "Game"
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    BackHandler(onBack = onHome)
    Page("Game over", eyebrow = gameName) {
        Spacer(Modifier.weight(1f))
        session?.settings?.let { GameCover(it.gameType, Modifier.size(120.dp).clip(RoundedCornerShape(28.dp))) }
        Text(when { together -> "${winner?.score ?: 0} points together!"; tie -> "It's a tie!"; else -> "${winner?.name} wins!" },
            style = MaterialTheme.typography.headlineLarge, color = colors.secondary, textAlign = TextAlign.Center)
        if (!together) teams.forEachIndexed { index, team ->
            Surface(Modifier.fillMaxWidth(), color = if (index == 0 && !tie) Acid.copy(alpha = .35f) else colors.surface, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(team.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(team.players.joinToString(", "), color = colors.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    Text("${team.score}", fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
            }
        } else Text(winner?.players?.joinToString(", ").orEmpty(), color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        PartyButton("Play again", onRematch)
        OutlinedButton({
            val text = teams.joinToString(prefix = "$gameName results\n", separator = "\n") { "${it.name}: ${it.score}" }
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Share results"))
        }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Share results") }
        TextButton(onHome, Modifier.fillMaxWidth()) { Text("Choose another game") }
    }
}

@Composable
private fun CustomDeckScreen(vm: AppViewModel, onBack: () -> Unit, useAfterSave: Boolean = true, saved: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var prompts by remember { mutableStateOf("") }
    val context = LocalContext.current
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) prompts = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
    }
    val count = prompts.lineSequence().flatMap { it.split(',').asSequence() }.count { it.isNotBlank() }
    Page("New card deck", eyebrow = "Write your own charades cards: inside jokes, friends' names, family moments.", onBack = onBack) {
        OutlinedTextField(title, { title = it.take(60) }, label = { Text("Deck name") }, placeholder = { Text("e.g. Family trip 2026") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(prompts, { prompts = it }, label = { Text("Cards: one per line") }, placeholder = { Text("Dad's dance moves\nThe lost suitcase\nAunty's famous biryani") },
            minLines = 6, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().weight(1f),
            supportingText = { Text("$count ${if (count == 1) "card" else "cards"}") })
        TextButton({ importer.launch(arrayOf("text/plain", "text/csv", "application/csv")) }, Modifier.fillMaxWidth()) { Text("Or import a .txt / .csv file") }
        PartyButton(if (useAfterSave) "Save and use this deck" else "Save deck", onClick = { vm.saveCustomDeck(title, prompts, saved) }, enabled = title.isNotBlank() && count > 0)
    }
}

@Composable
private fun SettingsScreen(state: AppUiState, vm: AppViewModel, onBack: () -> Unit) {
    var showAgeGate by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Page("Settings", scrollable = true, onBack = onBack) {
        PartyPanel("Sound", Tangerine) {
            Text("Volume ${(state.settings.soundVolume * 100).roundToInt()}%", color = colors.onSurfaceVariant)
            Slider(state.settings.soundVolume, vm::setSoundVolume, valueRange = 0f..1f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CHIME" to "Soft chime", "ARCADE" to "Arcade beeps").forEach { (pack, label) -> PartyChip(label, state.settings.soundPack == pack) { vm.setSoundPack(pack) } }
            }
        }
        PartyPanel("Forehead tilt sensitivity", Sky) {
            Text("How far you tilt the phone to mark an answer in forehead mode: ${state.settings.tiltThreshold.roundToInt()}°. Lower is more sensitive.", color = colors.onSurfaceVariant)
            Slider(state.settings.tiltThreshold, vm::setThreshold, valueRange = 30f..75f)
        }
        PartyPanel("Display", Acid) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("LIGHT" to "Light", "HIGH_CONTRAST" to "High contrast").forEach { (theme, label) ->
                    PartyChip(label, state.settings.theme == theme) { vm.setTheme(theme) }
                }
            }
        }
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.surfaceVariant) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Show 18+ cards", fontWeight = FontWeight.Bold); Text("Adds the After Dark decks. Off by default.", color = colors.onSurfaceVariant, fontSize = 13.sp) }
                Switch(state.settings.adultUnlocked, { enabled -> if (enabled) showAgeGate = true else vm.unlockAdult(false) })
            }
        }
        Text("Everything is saved on this phone and works offline. No account needed — your games and decks back up automatically when you're online.", color = colors.onSurfaceVariant, fontSize = 13.sp)
    }
    if (showAgeGate) AlertDialog(onDismissRequest = { showAgeGate = false }, title = { Text("Adults only") }, text = { Text("Confirm that you are 18 or older to show the After Dark decks on this phone.") }, confirmButton = { Button({ vm.unlockAdult(true); showAgeGate = false }) { Text("I am 18+") } }, dismissButton = { TextButton({ showAgeGate = false }) { Text("Cancel") } })
}

@Composable
private fun Page(title: String, scrollable: Boolean = false, eyebrow: String? = null, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme
    val baseModifier = Modifier.fillMaxSize().background(colors.background).imePadding().padding(horizontal = 20.dp, vertical = 14.dp)
    val pageModifier = if (scrollable) baseModifier.verticalScroll(scrollState) else baseModifier
    Column(pageModifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (onBack != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onBack, Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Back" }, contentPadding = PaddingValues(start = 0.dp, end = 12.dp)) {
                    Canvas(Modifier.size(20.dp)) {
                        val stroke = size.width * .1f
                        drawLine(colors.primary, Offset(size.width * .8f, size.height * .5f), Offset(size.width * .2f, size.height * .5f), strokeWidth = stroke)
                        drawLine(colors.primary, Offset(size.width * .2f, size.height * .5f), Offset(size.width * .5f, size.height * .2f), strokeWidth = stroke)
                        drawLine(colors.primary, Offset(size.width * .2f, size.height * .5f), Offset(size.width * .5f, size.height * .8f), strokeWidth = stroke)
                    }
                    Spacer(Modifier.width(7.dp))
                    Text("Back")
                }
                Spacer(Modifier.weight(1f))
            }
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            eyebrow?.let { Text(it, color = colors.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold) }
        }
        content()
    }
}

@Composable
private fun PartyButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp), shape = PartyShape) {
        Text(label, fontSize = 15.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PartyPanel(title: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title.friendlyTitle(), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun PartyChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
        modifier = Modifier.heightIn(min = 48.dp),
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

private fun GameType.readyInstruction(mode: GameMode): String = when (this) {
    GameType.CHARADES -> if (mode == GameMode.FOREHEAD_TILT) "Hold the phone on your forehead, screen facing out. Tilt down when they guess it, tilt up to skip." else "Only you look at the screen. Act out the card without talking; tap Got it when someone guesses."
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

private fun GameType.minimumPlayers(): Int = guide().minimumPlayers

private fun GameType.difficultyLabel(): String = if (guide().easy) "Easy to learn" else "Takes a minute to learn"

private fun GameType.playerLabel(): String = "${minimumPlayers()}–20 PLAYERS"

private fun GameType.prefersTeams(): Boolean = this in setOf(GameType.CHARADES, GameType.REVERSE_CHARADES, GameType.SOUND_EFFECTS, GameType.DRAW_AND_GUESS, GameType.BOMB_DEFUSAL)

private fun GameType.gameColor(): Color = offlineGameCatalog.first { it.type == this }.color

private fun gameForeground(background: Color): Color = if (background == Ink || background == Sky) Color.White else Ink

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
