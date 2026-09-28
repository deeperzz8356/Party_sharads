package com.dumbcharades.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dumbcharades.app.data.GameRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: GameRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = if (repository.syncPending().isSuccess) Result.success() else Result.retry()
}

