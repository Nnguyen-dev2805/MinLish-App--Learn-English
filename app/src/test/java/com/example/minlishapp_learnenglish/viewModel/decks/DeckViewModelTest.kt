package com.example.minlishapp_learnenglish.viewModel.decks

import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.DeckRepository
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord
import com.example.minlishapp_learnenglish.domain.usecase.decks.CreateDeckUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.CreateWordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.DeleteWordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.ExportDeckItemsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.GetDeckDetailUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.GetDeckItemsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.GetDecksUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.ImportDeckItemsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.UpdateWordUseCase
import com.example.minlishapp_learnenglish.viewModel.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `deck list success exposes seed decks`() = runTest {
        val viewModel = DeckListViewModel(GetDecksUseCase(FakeDeckRepository()))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(2, state.decks.size)
        assertEquals("Unit 01", state.filteredDecks.first().displayTitle)
    }

    @Test
    fun `deck list search filters by unit and tag`() = runTest {
        val viewModel = DeckListViewModel(GetDecksUseCase(FakeDeckRepository()))
        advanceUntilIdle()

        viewModel.onEvent(DeckListEvent.SearchChanged("unit-02"))

        assertEquals(1, viewModel.uiState.value.filteredDecks.size)
        assertEquals(2L, viewModel.uiState.value.filteredDecks.first().id)
    }

    @Test
    fun `deck list filter shows seed and my decks locally`() = runTest {
        val viewModel = DeckListViewModel(
            GetDecksUseCase(FakeDeckRepository(decksResult = AppResult.Success(sampleMixedDecks)))
        )
        advanceUntilIdle()

        viewModel.onEvent(DeckListEvent.FilterSelected(DeckFilter.Seed))
        assertEquals(2, viewModel.uiState.value.filteredDecks.size)
        assertEquals(true, viewModel.uiState.value.filteredDecks.all { it.isSeed })

        viewModel.onEvent(DeckListEvent.FilterSelected(DeckFilter.Mine))
        assertEquals(1, viewModel.uiState.value.filteredDecks.size)
        assertEquals("My Travel Words", viewModel.uiState.value.filteredDecks.first().name)
    }

    @Test
    fun `deck list combines search and filter`() = runTest {
        val viewModel = DeckListViewModel(
            GetDecksUseCase(FakeDeckRepository(decksResult = AppResult.Success(sampleMixedDecks)))
        )
        advanceUntilIdle()

        viewModel.onEvent(DeckListEvent.FilterSelected(DeckFilter.Seed))
        viewModel.onEvent(DeckListEvent.SearchChanged("travel"))

        assertEquals(0, viewModel.uiState.value.filteredDecks.size)

        viewModel.onEvent(DeckListEvent.FilterSelected(DeckFilter.Mine))
        assertEquals(1, viewModel.uiState.value.filteredDecks.size)
        assertEquals("My Travel Words", viewModel.uiState.value.filteredDecks.first().name)
    }

    @Test
    fun `deck list error exposes message`() = runTest {
        val error = AppError.Network("Không thể kết nối máy chủ.")
        val viewModel = DeckListViewModel(
            GetDecksUseCase(FakeDeckRepository(decksResult = AppResult.Failure(error)))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(error.message, state.errorMessage)
    }

    @Test
    fun `deck list emits navigate detail effect`() = runTest {
        val viewModel = DeckListViewModel(GetDecksUseCase(FakeDeckRepository()))
        val effects = mutableListOf<DeckListEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        viewModel.onEvent(DeckListEvent.DeckSelected(9L))
        advanceUntilIdle()

        assertEquals(listOf(DeckListEffect.NavigateDeckDetail(9L)), effects)
    }

    @Test
    fun `deck detail success loads deck and words`() = runTest {
        val repository = FakeDeckRepository()
        val viewModel = DeckDetailViewModel(
            deckId = 1L,
            getDeckDetailUseCase = GetDeckDetailUseCase(repository),
            getDeckItemsUseCase = GetDeckItemsUseCase(repository),
            importDeckItemsUseCase = ImportDeckItemsUseCase(repository),
            exportDeckItemsUseCase = ExportDeckItemsUseCase(repository)
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Unit 01", state.deck?.displayTitle)
        assertEquals("anxious", state.words.first().word)
    }

    @Test
    fun `create deck validates blank name`() {
        val repository = FakeDeckRepository()
        val viewModel = CreateDeckViewModel(CreateDeckUseCase(repository))

        viewModel.onEvent(CreateDeckEvent.Submit)

        assertEquals(0, repository.createDeckCalls)
        assertNotNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `create deck success emits navigate detail`() = runTest {
        val repository = FakeDeckRepository()
        val viewModel = CreateDeckViewModel(CreateDeckUseCase(repository))
        val effects = mutableListOf<CreateDeckEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        viewModel.onEvent(CreateDeckEvent.NameChanged("Business English"))
        viewModel.onEvent(CreateDeckEvent.SuggestedTagSelected("Business"))
        viewModel.onEvent(CreateDeckEvent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.createDeckCalls)
        assertEquals(listOf(CreateDeckEffect.NavigateDeckDetail(99L)), effects)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `create deck failure exposes api error`() = runTest {
        val error = AppError.Server("Máy chủ đang gặp sự cố.")
        val repository = FakeDeckRepository(createResult = AppResult.Failure(error))
        val viewModel = CreateDeckViewModel(CreateDeckUseCase(repository))

        viewModel.onEvent(CreateDeckEvent.NameChanged("Business English"))
        viewModel.onEvent(CreateDeckEvent.Submit)
        advanceUntilIdle()

        assertEquals(error.message, viewModel.uiState.value.apiError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `word editor validates blank word and meaning before create`() {
        val repository = FakeDeckRepository()
        val viewModel = wordEditorViewModel(repository)

        viewModel.onEvent(WordEditorEvent.Submit)

        assertEquals(0, repository.createWordCalls)
        assertNotNull(viewModel.uiState.value.wordError)
        assertNotNull(viewModel.uiState.value.meaningError)
    }

    @Test
    fun `word editor create success emits refresh effect`() = runTest {
        val repository = FakeDeckRepository()
        val viewModel = wordEditorViewModel(repository)
        val effects = mutableListOf<WordEditorEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        viewModel.onEvent(WordEditorEvent.WordChanged("focus"))
        viewModel.onEvent(WordEditorEvent.MeaningChanged("tập trung"))
        viewModel.onEvent(WordEditorEvent.RelatedWordsChanged("attention, concentration"))
        viewModel.onEvent(WordEditorEvent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.createWordCalls)
        assertEquals(
            listOf(WordEditorEffect.NavigateBackWithRefresh("Đã thêm từ mới.")),
            effects
        )
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `word editor edit loads word and updates successfully`() = runTest {
        val repository = FakeDeckRepository()
        val viewModel = wordEditorViewModel(repository, itemId = 1L)
        val effects = mutableListOf<WordEditorEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        advanceUntilIdle()

        assertEquals("anxious", viewModel.uiState.value.word)
        viewModel.onEvent(WordEditorEvent.MeaningChanged("lo lắng"))
        viewModel.onEvent(WordEditorEvent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.updateWordCalls)
        assertEquals(
            listOf(WordEditorEffect.NavigateBackWithRefresh("Đã cập nhật từ vựng.")),
            effects
        )
    }

    @Test
    fun `word editor delete success emits refresh effect`() = runTest {
        val repository = FakeDeckRepository()
        val viewModel = wordEditorViewModel(repository, itemId = 1L)
        val effects = mutableListOf<WordEditorEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        advanceUntilIdle()

        viewModel.onEvent(WordEditorEvent.Delete)
        advanceUntilIdle()

        assertEquals(1, repository.deleteWordCalls)
        assertEquals(
            listOf(WordEditorEffect.NavigateBackWithRefresh("Đã xoá từ vựng.")),
            effects
        )
    }
}

private class FakeDeckRepository(
    private val decksResult: AppResult<List<VocabularyDeck>> = AppResult.Success(sampleDecks),
    private val deckResult: AppResult<VocabularyDeck> = AppResult.Success(sampleDecks.first()),
    private val itemsResult: AppResult<List<VocabularyWord>> = AppResult.Success(sampleWords),
    private val createResult: AppResult<VocabularyDeck> = AppResult.Success(sampleCreatedDeck),
    private val createWordResult: AppResult<VocabularyWord> = AppResult.Success(sampleCreatedWord),
    private val updateWordResult: AppResult<VocabularyWord> = AppResult.Success(sampleCreatedWord),
    private val deleteWordResult: AppResult<Unit> = AppResult.Success(Unit)
) : DeckRepository {
    var createDeckCalls = 0
        private set
    var createWordCalls = 0
        private set
    var updateWordCalls = 0
        private set
    var deleteWordCalls = 0
        private set

    override suspend fun getDecks(): AppResult<List<VocabularyDeck>> = decksResult

    override suspend fun getDeck(deckId: Long): AppResult<VocabularyDeck> = deckResult

    override suspend fun getDeckItems(deckId: Long): AppResult<List<VocabularyWord>> = itemsResult

    override suspend fun createDeck(
        name: String,
        description: String?,
        tags: List<String>
    ): AppResult<VocabularyDeck> {
        createDeckCalls += 1
        return createResult
    }

    override suspend fun createWord(
        deckId: Long,
        word: String,
        pronunciation: String?,
        meaning: String,
        description: String?,
        example: String?,
        collocation: String?,
        relatedWords: List<String>,
        note: String?
    ): AppResult<VocabularyWord> {
        createWordCalls += 1
        return createWordResult
    }

    override suspend fun updateWord(
        itemId: Long,
        word: String,
        pronunciation: String?,
        meaning: String,
        description: String?,
        example: String?,
        collocation: String?,
        relatedWords: List<String>,
        note: String?
    ): AppResult<VocabularyWord> {
        updateWordCalls += 1
        return updateWordResult
    }

    override suspend fun deleteWord(itemId: Long): AppResult<Unit> {
        deleteWordCalls += 1
        return deleteWordResult
    }

    override suspend fun importDeckItems(
        deckId: Long,
        fileName: String,
        fileBytes: ByteArray
    ): AppResult<Int> = AppResult.Success(0)

    override suspend fun exportDeckItems(deckId: Long): AppResult<ByteArray> {
        return AppResult.Success(ByteArray(0))
    }
}

private fun wordEditorViewModel(
    repository: DeckRepository,
    itemId: Long? = null
): WordEditorViewModel {
    return WordEditorViewModel(
        deckId = 99L,
        itemId = itemId,
        getDeckItemsUseCase = GetDeckItemsUseCase(repository),
        createWordUseCase = CreateWordUseCase(repository),
        updateWordUseCase = UpdateWordUseCase(repository),
        deleteWordUseCase = DeleteWordUseCase(repository)
    )
}

private val sampleDecks = listOf(
    VocabularyDeck(
        id = 1L,
        name = "Unit 01",
        description = "4000 Essential English Words - Book 2",
        tags = listOf("4000-essential", "book-2", "unit-01", "seed"),
        isPublic = true,
        isSeed = true,
        isReadOnly = true,
        sourceName = "4000 Essential English Words - Book 2",
        sourceUnit = "Unit 01",
        wordCount = 20
    ),
    VocabularyDeck(
        id = 2L,
        name = "Unit 02",
        description = "4000 Essential English Words - Book 2",
        tags = listOf("4000-essential", "book-2", "unit-02", "seed"),
        isPublic = true,
        isSeed = true,
        isReadOnly = true,
        sourceName = "4000 Essential English Words - Book 2",
        sourceUnit = "Unit 02",
        wordCount = 20
    )
)

private val sampleMixedDecks = sampleDecks + VocabularyDeck(
    id = 3L,
    name = "My Travel Words",
    description = "Personal deck",
    tags = listOf("travel", "personal"),
    isPublic = false,
    isSeed = false,
    isReadOnly = false,
    sourceName = null,
    sourceUnit = null,
    wordCount = 3
)

private val sampleCreatedDeck = VocabularyDeck(
    id = 99L,
    name = "Business English",
    description = null,
    tags = listOf("Business"),
    isPublic = false,
    isSeed = false,
    isReadOnly = false,
    sourceName = null,
    sourceUnit = null,
    wordCount = 0
)

private val sampleWords = listOf(
    VocabularyWord(
        id = 1L,
        deckId = 1L,
        word = "anxious",
        pronunciation = "['æŋ(k)ʃəs]",
        meaning = "lo âu, băn khoăn",
        description = "When a person is anxious, they worry that something bad will happen.",
        example = "She was anxious about not making her appointment on time.",
        collocation = null,
        relatedWords = emptyList(),
        note = "tính từ",
        suggestion = "a__x__ __ __ __",
        imageUrl = "/static/media/anki/book2/4000B2_601.jpg",
        wordAudioUrl = "/static/media/anki/book2/4000B2_anxious.mp3",
        meaningAudioUrl = null,
        exampleAudioUrl = null
    )
)

private val sampleCreatedWord = VocabularyWord(
    id = 7L,
    deckId = 99L,
    word = "focus",
    pronunciation = null,
    meaning = "tập trung",
    description = null,
    example = null,
    collocation = null,
    relatedWords = listOf("attention", "concentration"),
    note = null,
    suggestion = null,
    imageUrl = null,
    wordAudioUrl = null,
    meaningAudioUrl = null,
    exampleAudioUrl = null
)
