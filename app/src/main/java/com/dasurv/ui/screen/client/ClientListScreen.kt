package com.dasurv.ui.screen.client

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.CardPosition
import com.dasurv.ui.component.ClientCard
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DasurvSearchField
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClient: (Long) -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    var showClientDialog by remember { mutableStateOf(false) }

    if (showClientDialog) {
        ClientFormDialog(
            clientId = null,
            onDismiss = { showClientDialog = false }
        )
    }
    val clients by viewModel.filteredClients.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val spacing = DasurvTheme.spacing

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle(title = "Clients") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { showClientDialog = true },
                contentDescription = "Add Client"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DasurvSearchField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .padding(start = spacing.lg, end = spacing.lg, top = spacing.lg, bottom = spacing.sm),
                placeholder = "Search clients..."
            )

            AnimatedContent(
                targetState = clients.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "client-list-state"
            ) { isEmpty ->
                if (isEmpty) {
                    DasurvEmptyState(
                        icon = Icons.Default.People,
                        message = if (searchQuery.isBlank()) "No clients yet" else "No results"
                    )
                } else {
                    val grouped = remember(clients) {
                        clients.groupBy {
                            it.name.firstOrNull()?.uppercaseChar() ?: '#'
                        }.toSortedMap()
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = spacing.lg),
                    ) {
                        grouped.forEach { (letter, groupClients) ->
                            item(key = "header_$letter") {
                                Row(
                                    modifier = Modifier
                                        .padding(
                                            start = spacing.lg,
                                            top = spacing.lg,
                                            bottom = spacing.sm
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(M3Primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letter.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = M3Primary
                                        )
                                    }
                                    Text(
                                        text = "${groupClients.size} ${if (groupClients.size == 1) "client" else "clients"}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = M3OnSurfaceVariant
                                    )
                                }
                            }
                            item(key = "group_$letter") {
                                M3ListCard {
                                    groupClients.forEachIndexed { index, client ->
                                        ClientCard(
                                            client = client,
                                            onClick = { onNavigateToClient(client.id) },
                                            position = when {
                                                groupClients.size == 1 -> CardPosition.Only
                                                index == 0 -> CardPosition.First
                                                index == groupClients.lastIndex -> CardPosition.Last
                                                else -> CardPosition.Middle
                                            }
                                        )
                                        if (index < groupClients.lastIndex) {
                                            M3ListDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
