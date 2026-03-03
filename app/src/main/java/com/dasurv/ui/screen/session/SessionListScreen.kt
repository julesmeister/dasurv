package com.dasurv.ui.screen.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.DateNavigationBar
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.screen.client.ClientViewModel
import com.dasurv.ui.screen.client.SessionsList
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.FMT_MONTH_YEAR
import com.dasurv.util.showDatePicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val spacing = DasurvTheme.spacing
    var showNewSessionDialog by remember { mutableStateOf(false) }

    if (showNewSessionDialog) {
        NewSessionDialog(
            clientId = clientId,
            onDismiss = { showNewSessionDialog = false }
        )
    }

    val sessions by viewModel.getSessionsForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var sessionMonth by remember { mutableStateOf(Date()) }
    val sessionMonthLabel = remember(sessionMonth) {
        SimpleDateFormat(FMT_MONTH_YEAR, Locale.getDefault()).format(sessionMonth)
    }
    val filteredSessions = remember(sessions, sessionMonth) {
        val cal = Calendar.getInstance().apply {
            time = sessionMonth
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        sessions.filter { it.date in start until end }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = M3SurfaceContainer,
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Sessions") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { showNewSessionDialog = true },
                contentDescription = "New Session"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            item {
                DateNavigationBar(
                    title = sessionMonthLabel,
                    subtitle = "${filteredSessions.size} session${if (filteredSessions.size != 1) "s" else ""}",
                    accentColor = M3Primary,
                    onPrevious = {
                        sessionMonth = Calendar.getInstance().apply {
                            time = sessionMonth
                            add(Calendar.MONTH, -1)
                        }.time
                    },
                    onNext = {
                        sessionMonth = Calendar.getInstance().apply {
                            time = sessionMonth
                            add(Calendar.MONTH, 1)
                        }.time
                    },
                    onCenterClick = {
                        showDatePicker(context, sessionMonth.time) { millis ->
                            sessionMonth = Date(millis)
                        }
                    }
                )
            }

            if (filteredSessions.isEmpty()) {
                item {
                    DasurvEmptyState(
                        icon = Icons.Default.EventNote,
                        message = "No sessions in $sessionMonthLabel"
                    )
                }
            } else {
                item {
                    SessionsList(
                        sessions = filteredSessions,
                        onNavigateToSession = onNavigateToSession
                    )
                }
            }

            // Bottom spacer for FAB
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}
