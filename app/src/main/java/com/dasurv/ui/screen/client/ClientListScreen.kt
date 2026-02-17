package com.dasurv.ui.screen.client

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.CardPosition
import com.dasurv.ui.component.ClientCard
import com.dasurv.ui.component.DasurvTextField

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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Clients") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddClient,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(Icons.Default.Add, "Add Client")
            }
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No clients yet" else "No results",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(clients.size, key = { clients[it].id }) { index ->
                            val client = clients[index]
                            val position = when {
                                clients.size == 1 -> CardPosition.Only
                                index == 0 -> CardPosition.First
                                index == clients.lastIndex -> CardPosition.Last
                                else -> CardPosition.Middle
                            }
                            ClientCard(
                                client = client,
                                onClick = { onNavigateToClient(client.id) },
                                position = position,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}
