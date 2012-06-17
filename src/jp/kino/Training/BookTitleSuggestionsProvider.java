package jp.kino.Training;

import android.content.SearchRecentSuggestionsProvider;

public class BookTitleSuggestionsProvider extends
		SearchRecentSuggestionsProvider {

	public BookTitleSuggestionsProvider(){  
        setupSuggestions("jp.kino.Training", BookTitleSuggestionsProvider.DATABASE_MODE_QUERIES);
	}
}
