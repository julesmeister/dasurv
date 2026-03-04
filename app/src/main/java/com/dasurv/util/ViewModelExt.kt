package com.dasurv.util

import kotlinx.coroutines.flow.SharingStarted

val DefaultSubscribePolicy: SharingStarted = SharingStarted.WhileSubscribed(5_000)
