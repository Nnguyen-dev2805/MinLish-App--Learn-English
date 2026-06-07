package com.example.minlishapp_learnenglish.data.local.mapper

import com.example.minlishapp_learnenglish.data.local.dao.DailyReviewCount
import com.example.minlishapp_learnenglish.data.local.entity.DeckEntity
import com.example.minlishapp_learnenglish.data.local.entity.NotificationSettingsEntity
import com.example.minlishapp_learnenglish.data.local.entity.ReviewStateEntity
import com.example.minlishapp_learnenglish.data.local.entity.UserEntity
import com.example.minlishapp_learnenglish.data.local.entity.VocabularyWordEntity
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.SubmitReviewResult
import com.example.minlishapp_learnenglish.domain.model.User
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        name = name,
        goal = goal,
        level = level,
        dailyNewWords = dailyNewWords
    )
}

fun User.toEntity(
    password: String = "",
    isLoggedIn: Boolean = false
): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        password = password,
        name = name,
        goal = goal,
        level = level,
        dailyNewWords = dailyNewWords,
        isLoggedIn = isLoggedIn
    )
}

fun DeckEntity.toDomain(
    wordCount: Int = 0,
    learnedCount: Int = 0
): VocabularyDeck {
    return VocabularyDeck(
        id = id,
        name = name,
        description = description,
        tags = tags,
        isPublic = isPublic,
        isSeed = isSeed,
        isReadOnly = isReadOnly,
        sourceName = sourceName,
        sourceUnit = sourceUnit,
        wordCount = wordCount,
        learnedCount = learnedCount
    )
}

fun VocabularyDeck.toEntity(): DeckEntity {
    return DeckEntity(
        id = id,
        name = name,
        description = description,
        tags = tags,
        isPublic = isPublic,
        isSeed = isSeed,
        isReadOnly = isReadOnly,
        sourceName = sourceName,
        sourceUnit = sourceUnit
    )
}

fun VocabularyWordEntity.toDomain(): VocabularyWord {
    return VocabularyWord(
        id = id,
        deckId = deckId,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        description = description,
        example = example,
        collocation = collocation,
        relatedWords = relatedWords,
        note = note,
        suggestion = suggestion,
        imageUrl = imageUrl,
        wordAudioUrl = wordAudioUrl,
        meaningAudioUrl = meaningAudioUrl,
        exampleAudioUrl = exampleAudioUrl
    )
}

fun VocabularyWord.toEntity(): VocabularyWordEntity {
    return VocabularyWordEntity(
        id = id,
        deckId = deckId,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        description = description,
        example = example,
        collocation = collocation,
        relatedWords = relatedWords,
        note = note,
        suggestion = suggestion,
        imageUrl = imageUrl,
        wordAudioUrl = wordAudioUrl,
        meaningAudioUrl = meaningAudioUrl,
        exampleAudioUrl = exampleAudioUrl
    )
}

fun VocabularyWordEntity.toReviewCard(reviewState: ReviewStateEntity?): ReviewCard {
    return ReviewCard(
        id = id,
        deckId = deckId,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        description = description,
        example = example,
        note = note,
        imageUrl = imageUrl,
        wordAudioUrl = wordAudioUrl,
        meaningAudioUrl = meaningAudioUrl,
        exampleAudioUrl = exampleAudioUrl,
        isNew = reviewState == null || reviewState.repetitions == 0,
        dueAt = reviewState?.nextDueAt
    )
}

fun ReviewStateEntity.toSubmitReviewResult(
    rating: ReviewRating,
    isCorrect: Boolean
): SubmitReviewResult {
    return SubmitReviewResult(
        vocabularyItemId = wordId,
        rating = rating,
        isCorrect = isCorrect,
        repetitions = repetitions,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        nextDueAt = nextDueAt.orEmpty()
    )
}

fun NotificationSettingsEntity.toDomain(): NotificationSettings {
    return NotificationSettings(
        dailyTime = dailyTime,
        timezone = timezone,
        emailEnabled = emailEnabled,
        pushEnabled = pushEnabled
    )
}

fun NotificationSettings.toEntity(userId: Long): NotificationSettingsEntity {
    return NotificationSettingsEntity(
        userId = userId,
        dailyTime = dailyTime,
        timezone = timezone,
        emailEnabled = emailEnabled,
        pushEnabled = pushEnabled
    )
}

fun DailyReviewCount.toDomain(): DailyActivity {
    return DailyActivity(
        date = reviewDate,
        reviewCount = reviewCount,
        correctCount = correctCount
    )
}
