package jp.kino.Training;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowActivity extends ListActivity {
	/** DB接続部品 */
	private BookDatabaseHelper dbhelper;
	/** アダプタ部品 */
	private BookListAdapter adapter;
	/** バーコードスキャン時のIntent遷移ID*/
    public static int REQUEST_CODE_INPUT = 1; 
	/** Activity切替時のList位置を保存 */
    private int _position = 0;
    /** 検索条件 */
    private String _query;
    
    // region Activity イベント
    /**
     * 本アプリ起動時に呼ばれるメソッド
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show);
		
        dbhelper = new BookDatabaseHelper(this);
        
        registerForContextMenu(getListView());

	    String query = "";
	    // ACTION_SEARCH の Intent で呼び出された場合  
	    if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
	        query = getIntent().getStringExtra(SearchManager.QUERY);  
	    }  
	    
	    searchBook(query);
	}
	
	/**
	 * 開始時に呼び出されるメソッド
	 */
	@Override
	protected void onStart() {
	    super.onStart();
	}
	
	/**
	 * ListItemクリック時に呼び出されるメソッド
	 */
	@Override
	protected void onListItemClick(ListView view, View v, int position, long id){
		SearchResult book = adapter.getItem(position);

		Intent intent = new Intent(this, InputActivity.class);
		intent.setType("application/searchResult");
		intent.putExtra("SearchResult", book);
		
		startActivityForResult(intent, REQUEST_CODE_INPUT);
	}

	/**
	 * ListItem長押し時に呼び出されるメソッド
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
		super.onCreateContextMenu(menu, v, info);
		
		menu.setHeaderTitle("メニュー");

		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
		final Activity activity = this;
		final SearchResult book = (SearchResult)getListView().getItemAtPosition(adapterInfo.position); 
		final String id = book.getId();
		
		menu.add("1件削除").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				SQLiteDatabase db = dbhelper.getWritableDatabase();
				db.delete(BookDatabaseHelper.BookTable, "id = ?", new String[]{ id });
				db.close();
				
				adapter.remove(book);
				
				Toast toast = Toast.makeText(activity, "削除しました", Toast.LENGTH_SHORT);
				toast.show();
				
				return false;
			}
		});
	}
	
    /**
     * Intent遷移後に呼ばれるメソッド
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == REQUEST_CODE_INPUT) {
    		if (resultCode == Activity.RESULT_OK) {
    			Toast toast = Toast.makeText(this, "修正しました", Toast.LENGTH_SHORT);
    			toast.show();
    			
    			searchBook(_query);
    		}
    	}
    }
    
    /**
     * Activity一時停止時に呼び出されるメソッド
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	_position = getListView().getFirstVisiblePosition();
    }
    
    /**
     * Activity再開時に呼び出されるメソッド
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	getListView().setSelection(_position);
    }

    /**
     * 画面回転時などに呼び出されるメソッド（android:configChangedに指定されているイベント）
     */
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
	}
    
    /**
     * オプションメニューを表示する。
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "検索").setIcon(android.R.drawable.ic_menu_search);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				return onSearchRequested();
			}
		});
		
		return true;
	}
    
    @Override
    protected void onNewIntent(Intent intent) {
    	searchBook(intent.getStringExtra(SearchManager.QUERY));
    }
	// endregion

    private void searchBook(String title) {
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        String[] cols = new String[]{
        		BookDatabaseHelper.BookColumnID,
        		BookDatabaseHelper.BookColumnAsin,
        		BookDatabaseHelper.BookColumnDetailUrl,
        		BookDatabaseHelper.BookColumnImageUrl,
        		BookDatabaseHelper.BookColumnAuthor,
        		BookDatabaseHelper.BookColumnCategory,
        		BookDatabaseHelper.BookColumnMedia,
        		BookDatabaseHelper.BookColumnEan,
        		BookDatabaseHelper.BookColumnPublishDate,
        		BookDatabaseHelper.BookColumnPublisher,
        		BookDatabaseHelper.BookColumnPrice,
        		BookDatabaseHelper.BookColumnTitle,
        		BookDatabaseHelper.BookColumnFormedTitle,
        		BookDatabaseHelper.BookColumnVolume,
        		BookDatabaseHelper.BookColumnBuyDate,
        		BookDatabaseHelper.BookColumnBuyPrice,
        		//BookDatabaseHelper.BookColumnImage
        };
        
        String query = null;
        String[] cond = null;
        if (!title.equals("")) {
        	// 検索履歴を記録
        	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, "jp.kino.Training",  
        			BookTitleSuggestionsProvider.DATABASE_MODE_QUERIES);  
        	suggestions.saveRecentQuery(title, null);
        	
        	query = BookDatabaseHelper.BookColumnFormedTitle + " LIKE ? ESCAPE '$'";
        	cond = new String[]{ "%" + title.replace("$", "$$").replace("%", "$%").replace("_", "$_") + "%" };
        }
        
        Cursor c = db.query(BookDatabaseHelper.BookTable, cols, query, cond, null, null, BookDatabaseHelper.BookColumnFormedTitle);
        
        List<SearchResult> list = new ArrayList<SearchResult>(c.getCount());
        if (c.moveToFirst()) {
        	do {
        		SearchResult book = new SearchResult.Builder()
					        		.id(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnID)))
					        		.asin(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnAsin)))
					        		.detail_url(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnDetailUrl)))
					        		.image_url(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnImageUrl)))
					        		.author(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnAuthor)))
					        		.category(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnCategory)))
					        		.media(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnMedia)))
					        		.ean(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnEan)))
					        		.publish_date(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnPublishDate)))
					        		.publisher(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnPublisher)))
					        		.price(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnPrice)))
					        		.title(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnTitle)))
					        		.formed_title(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnFormedTitle)))
					        		.volume(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnVolume)))
					        		.buy_date(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnBuyDate)))
					        		.buy_price(c.getString(c.getColumnIndex(BookDatabaseHelper.BookColumnBuyPrice)))
					        		//.image(c.getBlob(c.getColumnIndex(BookDatabaseHelper.BookColumnImage)))
        							.build();
            	list.add(book);
        	} while (c.moveToNext());
        }
        
        adapter = new BookListAdapter(this, list);
        setListAdapter(adapter);
        
        ((TextView)findViewById(R.id.labelCount)).setText(getText(R.string.search_result) + Integer.toString(c.getCount()) + "件");
        
        _query = title;
        
        c.close();
        db.close();
    }
    
	class BookListAdapter extends ArrayAdapter<SearchResult>{
		
		private LayoutInflater mInflater;
		private ImageView viewImage;
		private TextView viewTitle;
		private TextView viewAuthor;

		public BookListAdapter(Context context, List<SearchResult> objects) {
			super(context, 0, objects);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.show_row, null);
			}
			final SearchResult book = this.getItem(position);
			if(book != null){
				viewImage = (ImageView)convertView.findViewById(R.id.book_img);
				byte[] ba = getImage(book.getId());
				if (ba != null && ba.length > 0) {
					Bitmap bmap = BitmapFactory.decodeStream(new ByteArrayInputStream(ba));
					viewImage.setImageBitmap(bmap);
				} else {
					viewImage.setImageResource(R.drawable.no_image);
				}
					
				
				viewTitle = (TextView)convertView.findViewById(R.id.book_title);
				viewTitle.setText(book.getFormed_title());

				viewAuthor = (TextView)convertView.findViewById(R.id.book_author);
				viewAuthor.setText(book.getAuthor());
			}
			
			return convertView;
		}
		
		private byte[] getImage(String id) {
			byte[] ret = null;
			
	        SQLiteDatabase db = dbhelper.getReadableDatabase();
	        String[] cols = new String[]{
	        		BookDatabaseHelper.BookColumnImage
	        };
	        
	        String query = "id=?";
	        String[] cond = new String[]{ id };
	        
	        Cursor c = db.query(BookDatabaseHelper.BookTable, cols, query, cond, null, null, null);
	        
	        if (c.getCount() > 0 && c.moveToFirst()) {
	        	ret = c.getBlob(c.getColumnIndex(BookDatabaseHelper.BookColumnImage));
	        }
	        
	        c.close();
	        db.close();

	        return ret;
		}
	}
}
