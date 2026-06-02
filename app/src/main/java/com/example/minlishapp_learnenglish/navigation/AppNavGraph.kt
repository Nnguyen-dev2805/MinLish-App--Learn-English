package com.example.minlishapp_learnenglish.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.minlishapp_learnenglish.core.AppContainer
import com.example.minlishapp_learnenglish.presentation.viewmodel.viewModelFactory
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.AuthEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.LoginViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.SplashViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.RegisterViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.RegisterEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.RegisterEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.SetupViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.SetupEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.SetupEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.CreateDeckEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.CreateDeckEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.CreateDeckViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.DeckDetailEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.DeckDetailEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.DeckDetailViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.DeckListEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.DeckListEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.DeckListViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.WordEditorEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.WordEditorEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.WordEditorViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.home.HomeEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.home.HomeViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.learning.FlashcardEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.learning.FlashcardEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.learning.FlashcardViewModel
import com.example.minlishapp_learnenglish.domain.model.ReviewSessionSummary
import com.example.minlishapp_learnenglish.presentation.viewmodel.progress.ProgressEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.progress.ProgressEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.progress.ProgressViewModel
import com.example.minlishapp_learnenglish.presentation.viewmodel.profile.ProfileEffect
import com.example.minlishapp_learnenglish.presentation.viewmodel.profile.ProfileEvent
import com.example.minlishapp_learnenglish.presentation.viewmodel.profile.ProfileViewModel
import com.example.minlishapp_learnenglish.ui.screens.auth.LoginScreen
import com.example.minlishapp_learnenglish.ui.screens.auth.SetupScreen
import com.example.minlishapp_learnenglish.ui.screens.auth.SplashScreen
import com.example.minlishapp_learnenglish.ui.screens.auth.RegisterScreen
import com.example.minlishapp_learnenglish.ui.screens.decks.CreateDeckScreen
import com.example.minlishapp_learnenglish.ui.screens.decks.DeckDetailScreen
import com.example.minlishapp_learnenglish.ui.screens.decks.DeckListScreen
import com.example.minlishapp_learnenglish.ui.screens.decks.WordEditorScreen
import com.example.minlishapp_learnenglish.ui.screens.learning.FlashcardLearningScreen
import com.example.minlishapp_learnenglish.ui.screens.learning.ReviewResultsScreen
import com.example.minlishapp_learnenglish.ui.screens.home.HomeScreen
import com.example.minlishapp_learnenglish.ui.screens.progress.ProgressAnalyticsScreen
import com.example.minlishapp_learnenglish.ui.screens.profile.ProfileSettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    var reviewSessionSummary by remember { mutableStateOf<ReviewSessionSummary?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier
    ) {
        composable(Routes.Splash) {
            val viewModel: SplashViewModel = viewModel(
                factory = viewModelFactory {
                    SplashViewModel(appContainer.checkSessionUseCase)
                }
            )
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    navController.handleAuthEffect(effect, currentRoute = Routes.Splash)
                }
            }
            LaunchedEffect(Unit) {
                viewModel.checkSession()
            }

            SplashScreen(uiState = uiState)
        }
        composable(Routes.Login) {
            val viewModel: LoginViewModel = viewModel(
                factory = viewModelFactory {
                    LoginViewModel(
                        loginUseCase = appContainer.loginUseCase,
                        googleLoginUseCase = appContainer.googleLoginUseCase
                    )
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is AuthEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                        else -> navController.handleAuthEffect(effect, currentRoute = Routes.Login)
                    }
                }
            }

            LoginScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLogin = viewModel::loginWithEmailPassword,
                onSignUp = viewModel::navigateRegister,
                onGoogleLogin = viewModel::googleLogin,
                onError = viewModel::showError
            )
        }
        composable(Routes.Register) {
            val viewModel: RegisterViewModel = viewModel(
                factory = viewModelFactory {
                    RegisterViewModel(appContainer.registerUseCase)
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is RegisterEffect.NavigateSetup -> {
                            navController.navigate(Routes.Setup + "/${java.net.URLEncoder.encode(effect.userName, "UTF-8")}") {
                                popUpTo(Routes.Register) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        RegisterEffect.NavigateLogin -> navController.popBackStack()
                        is RegisterEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }

            RegisterScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onNameChange = { value -> viewModel.onEvent(RegisterEvent.NameChanged(value)) },
                onEmailChange = { value -> viewModel.onEvent(RegisterEvent.EmailChanged(value)) },
                onPasswordChange = { value -> viewModel.onEvent(RegisterEvent.PasswordChanged(value)) },
                onConfirmPasswordChange = { value -> viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(value)) },
                onSubmit = { viewModel.onEvent(RegisterEvent.Submit) },
                onBack = { viewModel.onEvent(RegisterEvent.BackToLoginClicked) }
            )
        }
        composable(
            route = Routes.Setup + "/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("userName") ?: "",
                "UTF-8"
            )
            val viewModel: SetupViewModel = viewModel(
                factory = viewModelFactory {
                    SetupViewModel(
                        updateProfileUseCase = appContainer.updateProfileUseCase,
                        userName = userName
                    )
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        SetupEffect.NavigateHome -> {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Login) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        is SetupEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }

            SetupScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onGoalChange = { value -> viewModel.onEvent(SetupEvent.GoalChanged(value)) },
                onLevelChange = { value -> viewModel.onEvent(SetupEvent.LevelChanged(value)) },
                onSubmit = { viewModel.onEvent(SetupEvent.Submit) },
                onSkip = { viewModel.onEvent(SetupEvent.Skip) }
            )
        }

        composable(Routes.Home) {
            val viewModel: HomeViewModel = viewModel(
                factory = viewModelFactory {
                    HomeViewModel(appContainer.loadHomeUseCase)
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        HomeEffect.NavigateLearn -> navController.navigate(Routes.Learn)
                        is HomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }

            Box {
                HomeScreen(
                    uiState = uiState,
                    onStartLearning = viewModel::startLearning,
                    onRetry = viewModel::retry,
                    onRefresh = viewModel::refresh
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        composable(Routes.Decks) {
            val viewModel: DeckListViewModel = viewModel(
                factory = viewModelFactory {
                    DeckListViewModel(appContainer.getDecksUseCase)
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val refreshDecks by it.savedStateHandle
                .getStateFlow("refreshDecks", false)
                .collectAsState()

            LaunchedEffect(refreshDecks) {
                if (refreshDecks) {
                    viewModel.loadDecks(isRefresh = true)
                    it.savedStateHandle["refreshDecks"] = false
                }
            }
            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is DeckListEffect.NavigateDeckDetail -> {
                            navController.navigate(Routes.deckDetail(effect.deckId))
                        }
                        DeckListEffect.NavigateCreateDeck -> {
                            navController.navigate(Routes.CreateDeck)
                        }
                        is DeckListEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            DeckListScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onSearchChange = { query -> viewModel.onEvent(DeckListEvent.SearchChanged(query)) },
                onFilterSelected = { filter -> viewModel.onEvent(DeckListEvent.FilterSelected(filter)) },
                onDeckClick = { deckId -> viewModel.onEvent(DeckListEvent.DeckSelected(deckId)) },
                onCreateDeck = { viewModel.onEvent(DeckListEvent.CreateDeckClicked) },
                onRetry = { viewModel.onEvent(DeckListEvent.Retry) }
            )
        }
        composable(Routes.CreateDeck) {
            val viewModel: CreateDeckViewModel = viewModel(
                factory = viewModelFactory {
                    CreateDeckViewModel(appContainer.createDeckUseCase)
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        CreateDeckEffect.NavigateBack -> navController.popBackStack()
                        is CreateDeckEffect.NavigateDeckDetail -> {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refreshDecks", true)
                            navController.navigate(Routes.deckDetail(effect.deckId)) {
                                popUpTo(Routes.CreateDeck) { inclusive = true }
                            }
                        }
                        is CreateDeckEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            CreateDeckScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onBack = { viewModel.onEvent(CreateDeckEvent.BackClicked) },
                onNameChange = { value -> viewModel.onEvent(CreateDeckEvent.NameChanged(value)) },
                onDescriptionChange = { value ->
                    viewModel.onEvent(CreateDeckEvent.DescriptionChanged(value))
                },
                onTagInputChange = { value ->
                    viewModel.onEvent(CreateDeckEvent.TagInputChanged(value))
                },
                onAddTag = { viewModel.onEvent(CreateDeckEvent.AddTag) },
                onRemoveTag = { tag -> viewModel.onEvent(CreateDeckEvent.RemoveTag(tag)) },
                onSuggestedTag = { tag -> viewModel.onEvent(CreateDeckEvent.SuggestedTagSelected(tag)) },
                onSubmit = { viewModel.onEvent(CreateDeckEvent.Submit) }
            )
        }
        composable(
            route = Routes.DeckDetail,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            val viewModel: DeckDetailViewModel = viewModel(
                key = "deck-detail-$deckId",
                factory = viewModelFactory {
                    DeckDetailViewModel(
                        deckId = deckId,
                        getDeckDetailUseCase = appContainer.getDeckDetailUseCase,
                        getDeckItemsUseCase = appContainer.getDeckItemsUseCase,
                        importDeckItemsUseCase = appContainer.importDeckItemsUseCase
                    )
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val refreshWords by backStackEntry.savedStateHandle
                .getStateFlow("refreshWords", false)
                .collectAsState()
            val deckMessage by backStackEntry.savedStateHandle
                .getStateFlow<String?>("deckMessage", null)
                .collectAsState()

            LaunchedEffect(refreshWords) {
                if (refreshWords) {
                    viewModel.loadDeck()
                    backStackEntry.savedStateHandle["refreshWords"] = false
                }
            }
            LaunchedEffect(deckMessage) {
                val message = deckMessage
                if (message != null) {
                    snackbarHostState.showSnackbar(message)
                    backStackEntry.savedStateHandle["deckMessage"] = null
                }
            }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        DeckDetailEffect.NavigateBack -> navController.popBackStack()
                        is DeckDetailEffect.NavigateAddWord -> {
                            navController.navigate(Routes.addWord(effect.deckId))
                        }
                        is DeckDetailEffect.NavigateEditWord -> {
                            navController.navigate(Routes.editWord(effect.deckId, effect.wordId))
                        }
                        is DeckDetailEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            DeckDetailScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onBack = { viewModel.onEvent(DeckDetailEvent.BackClicked) },
                onRetry = { viewModel.onEvent(DeckDetailEvent.Retry) },
                onLearnDeck = { id -> navController.navigate(Routes.learnDeck(id)) },
                onAddWord = { viewModel.onEvent(DeckDetailEvent.AddWordClicked) },
                onEditWord = { wordId -> viewModel.onEvent(DeckDetailEvent.EditWordClicked(wordId)) },
                onImport = viewModel::importExcel
            )
        }
        composable(
            route = Routes.AddWord,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            WordEditorRoute(
                navController = navController,
                appContainer = appContainer,
                deckId = deckId,
                itemId = null
            )
        }
        composable(
            route = Routes.EditWord,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("itemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            WordEditorRoute(
                navController = navController,
                appContainer = appContainer,
                deckId = deckId,
                itemId = itemId
            )
        }
        composable(Routes.Learn) {
            val viewModel: FlashcardViewModel = viewModel(
                factory = viewModelFactory {
                    FlashcardViewModel(
                        getReviewCardsUseCase = appContainer.getReviewCardsUseCase,
                        submitReviewUseCase = appContainer.submitReviewUseCase
                    )
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        FlashcardEffect.NavigateBack -> navController.popBackStack()
                        is FlashcardEffect.NavigateReviewResults -> {
                            reviewSessionSummary = effect.summary
                            navController.navigate(Routes.ReviewResults) {
                                popUpTo(Routes.Learn) { inclusive = true }
                            }
                        }
                        is FlashcardEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            Box {
                FlashcardLearningScreen(
                    uiState = uiState,
                    onBack = { viewModel.onEvent(FlashcardEvent.BackClicked) },
                    onRetry = { viewModel.onEvent(FlashcardEvent.Retry) },
                    onShowAnswer = { viewModel.onEvent(FlashcardEvent.ShowAnswer) },
                    onPreviousCard = { viewModel.onEvent(FlashcardEvent.PreviousCard) },
                    onNextCard = { viewModel.onEvent(FlashcardEvent.NextCard) },
                    onRating = { rating ->
                        viewModel.onEvent(FlashcardEvent.SubmitRating(rating))
                    }
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        composable(
            route = Routes.LearnDeck,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "deck_all"
            val viewModel: FlashcardViewModel = viewModel(
                key = "learn-deck-$deckId-$mode",
                factory = viewModelFactory {
                    FlashcardViewModel(
                        getReviewCardsUseCase = appContainer.getReviewCardsUseCase,
                        submitReviewUseCase = appContainer.submitReviewUseCase,
                        deckId = deckId,
                        mode = mode
                    )
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        FlashcardEffect.NavigateBack -> navController.popBackStack()
                        is FlashcardEffect.NavigateReviewResults -> {
                            reviewSessionSummary = effect.summary
                            navController.navigate(Routes.ReviewResults) {
                                popUpTo(Routes.LearnDeck) { inclusive = true }
                            }
                        }
                        is FlashcardEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            Box {
                FlashcardLearningScreen(
                    uiState = uiState,
                    onBack = { viewModel.onEvent(FlashcardEvent.BackClicked) },
                    onRetry = { viewModel.onEvent(FlashcardEvent.Retry) },
                    onShowAnswer = { viewModel.onEvent(FlashcardEvent.ShowAnswer) },
                    onPreviousCard = { viewModel.onEvent(FlashcardEvent.PreviousCard) },
                    onNextCard = { viewModel.onEvent(FlashcardEvent.NextCard) },
                    onRating = { rating ->
                        viewModel.onEvent(FlashcardEvent.SubmitRating(rating))
                    }
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        composable(Routes.ReviewResults) {
            ReviewResultsScreen(
                summary = reviewSessionSummary ?: ReviewSessionSummary(),
                onContinueLearning = {
                    reviewSessionSummary = null
                    navController.navigate(Routes.Learn) {
                        popUpTo(Routes.ReviewResults) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackHome = {
                    reviewSessionSummary = null
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.Progress) {
            val viewModel: ProgressViewModel = viewModel(
                factory = viewModelFactory {
                    ProgressViewModel(appContainer.loadProgressAnalyticsUseCase)
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is ProgressEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            Box {
                ProgressAnalyticsScreen(
                    uiState = uiState,
                    onRetry = { viewModel.onEvent(ProgressEvent.Retry) },
                    onRefresh = { viewModel.onEvent(ProgressEvent.Refresh) }
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        composable(Routes.Profile) {
            val viewModel: ProfileViewModel = viewModel(
                factory = viewModelFactory {
                    ProfileViewModel(
                        getProfileUseCase = appContainer.getProfileUseCase,
                        updateProfileUseCase = appContainer.updateProfileUseCase,
                        getNotificationSettingsUseCase = appContainer.getNotificationSettingsUseCase,
                        updateNotificationSettingsUseCase = appContainer.updateNotificationSettingsUseCase,
                        logoutUseCase = appContainer.logoutUseCase,
                        reminderScheduler = appContainer.reminderScheduler
                    )
                }
            )
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        ProfileEffect.NavigateLogin -> {
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Home) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        is ProfileEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            Box {
                ProfileSettingsScreen(
                    uiState = uiState,
                    onRetry = { viewModel.onEvent(ProfileEvent.Retry) },
                    onRefresh = { viewModel.onEvent(ProfileEvent.Refresh) },
                    onNameChange = { value -> viewModel.onEvent(ProfileEvent.NameChanged(value)) },
                    onGoalChange = { value -> viewModel.onEvent(ProfileEvent.GoalChanged(value)) },
                    onLevelChange = { value -> viewModel.onEvent(ProfileEvent.LevelChanged(value)) },
                    onDailyNewWordsChange = { value ->
                        viewModel.onEvent(ProfileEvent.DailyNewWordsChanged(value))
                    },
                    onDailyTimeChange = { value ->
                        viewModel.onEvent(ProfileEvent.DailyTimeChanged(value))
                    },
                    onEmailEnabledChange = { value ->
                        viewModel.onEvent(ProfileEvent.EmailEnabledChanged(value))
                    },
                    onPushEnabledChange = { value ->
                        viewModel.onEvent(ProfileEvent.PushEnabledChanged(value))
                    },
                    onSaveProfile = { viewModel.onEvent(ProfileEvent.SaveProfile) },
                    onSaveNotifications = { viewModel.onEvent(ProfileEvent.SaveNotifications) },
                    onLogout = { viewModel.onEvent(ProfileEvent.Logout) }
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun WordEditorRoute(
    navController: NavHostController,
    appContainer: AppContainer,
    deckId: Long,
    itemId: Long?
) {
    val viewModel: WordEditorViewModel = viewModel(
        key = "word-editor-$deckId-${itemId ?: "new"}",
        factory = viewModelFactory {
            WordEditorViewModel(
                deckId = deckId,
                itemId = itemId,
                getDeckItemsUseCase = appContainer.getDeckItemsUseCase,
                createWordUseCase = appContainer.createWordUseCase,
                updateWordUseCase = appContainer.updateWordUseCase,
                deleteWordUseCase = appContainer.deleteWordUseCase
            )
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WordEditorEffect.NavigateBack -> navController.popBackStack()
                is WordEditorEffect.NavigateBackWithRefresh -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refreshWords", true)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("deckMessage", effect.message)
                    navController.popBackStack()
                }
                is WordEditorEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    WordEditorScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = { viewModel.onEvent(WordEditorEvent.BackClicked) },
        onRetryLoad = { viewModel.onEvent(WordEditorEvent.RetryLoad) },
        onWordChange = { value -> viewModel.onEvent(WordEditorEvent.WordChanged(value)) },
        onPronunciationChange = { value ->
            viewModel.onEvent(WordEditorEvent.PronunciationChanged(value))
        },
        onMeaningChange = { value -> viewModel.onEvent(WordEditorEvent.MeaningChanged(value)) },
        onDescriptionChange = { value ->
            viewModel.onEvent(WordEditorEvent.DescriptionChanged(value))
        },
        onExampleChange = { value -> viewModel.onEvent(WordEditorEvent.ExampleChanged(value)) },
        onCollocationChange = { value ->
            viewModel.onEvent(WordEditorEvent.CollocationChanged(value))
        },
        onRelatedWordsChange = { value ->
            viewModel.onEvent(WordEditorEvent.RelatedWordsChanged(value))
        },
        onNoteChange = { value -> viewModel.onEvent(WordEditorEvent.NoteChanged(value)) },
        onSubmit = { viewModel.onEvent(WordEditorEvent.Submit) },
        onDelete = { viewModel.onEvent(WordEditorEvent.Delete) }
    )
}

private fun NavHostController.handleAuthEffect(effect: AuthEffect, currentRoute: String) {
    when (effect) {
        AuthEffect.NavigateHome -> navigateReplacingCurrentAuth(currentRoute, Routes.Home)
        AuthEffect.NavigateLogin -> navigateReplacingCurrentAuth(currentRoute, Routes.Login)
        AuthEffect.NavigateRegister -> navigate(Routes.Register)
        is AuthEffect.ShowSnackbar -> Unit
    }
}

private fun NavHostController.navigateReplacingCurrentAuth(
    currentRoute: String,
    targetRoute: String
) {
    navigate(targetRoute) {
        popUpTo(currentRoute) { inclusive = true }
        launchSingleTop = true
    }
}
