package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.model.SearchResults
import tj.umar.navoplayer.core.domain.search.LibrarySearchIndex
import tj.umar.navoplayer.core.domain.search.SearchQuery
import javax.inject.Inject

class SearchLibraryUseCase @Inject constructor(
    private val observeLibrary: ObserveLibraryUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(queries: Flow<String>): Flow<SearchResults> = combine(
        observeLibrary().map(::LibrarySearchIndex),
        queries.map(SearchQuery::parse).distinctUntilChangedBy { it.text },
    ) { index, query ->
        if (query.isBlank) SearchResults.empty(query.text) else index.search(query)
    }.flowOn(defaultDispatcher)
}
