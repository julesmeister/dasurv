package com.dasurv.ui.screen.client

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.CardPosition
import com.dasurv.ui.component.ClientCard
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DasurvTextField
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClient: (Long) -> Unit,
    onNavigateToAddClient: () -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
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
                onClick = onNavigateToAddClient,
                contentDescription = "Add Client"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DasurvTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                placeholder = { Text("Search clients...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
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
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        items(clients.size, key = { clients[it].id }) { index ->
                            val client = clients[index]
                            val position = when {
                                clients.size == 1 -> CardPosition.Only
                                index == 0 -> CardPosition.First
                                index == clients.lastIndex -> CardPosition.Last
                                else -> CardPosition.Middle
                            }
                            M3ListCard(modifier = Modifier.animateItem()) {
                                ClientCard(
                                    client = client,
                                    onClick = { onNavigateToClient(client.id) },
                                    position = position
                                )
                                if (index < clients.lastIndex) {
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
