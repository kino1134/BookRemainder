package jp.kino.Training;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

public class InputActivity extends Activity {

	/** 検索結果 */
	private SearchResult _searchRet;
	/** DB接続部品 */
	private BookDatabaseHelper dbhelper;
    /** Activity内のDialogID（メッセージ表示）*/
    public static int DIALOG_ID_NORMAL = 0; 
    /** Activity内のDialogID（処理実行中）*/
    public static int DIALOG_ID_PROGRESS = 3; 
    /** DialogのBundleにデータを設定する際のキー（タイトル）*/
    public static String DIALOG_KEY_TITLE = "TITLE";
    /** DialogのBundleにデータを設定する際のキー（本文）*/
    public static String DIALOG_KEY_MESSAGE = "Message";
	
    // region Activity イベント
    /**
     * 本アプリ起動時に呼ばれるメソッド
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        setContentView(R.layout.input);
		
        // ボタンにイベントを設定する
        findViewById(R.id.button_save).setOnClickListener(eButtonSave);

        Intent intent = getIntent();
        _searchRet = (SearchResult)intent.getSerializableExtra("SearchResult");
        
        dbhelper = new BookDatabaseHelper(this);
        
        if (_searchRet.getId().equals("")) {
        	setTitle(getTitle() + "(新規)");
        } else {
        	setTitle(getTitle() + "(修正)");
        }
	}
	
	/**
	 * 開始時に呼び出されるメソッド
	 */
	@Override
	protected void onStart() {
	    super.onStart();

	    // 検索結果を設定する
	    if (_searchRet != null) {
			((EditText)findViewById(R.id.editTextFormed_title)).setText(_searchRet.getFormed_title());
			((EditText)findViewById(R.id.editTextVolume)).setText(_searchRet.getVolume());
			((EditText)findViewById(R.id.editTextAuthor)).setText(_searchRet.getAuthor());
			
			if (_searchRet.getId().equals("")) {
				((EditText)findViewById(R.id.editTextBuy_date)).setText(DateFormat.format("yyyy-MM-dd", Calendar.getInstance()));
				((EditText)findViewById(R.id.editTextBuy_price)).setText(_searchRet.getPrice());
			} else {
				((EditText)findViewById(R.id.editTextBuy_date)).setText(_searchRet.getBuy_date());
				((EditText)findViewById(R.id.editTextBuy_price)).setText(_searchRet.getBuy_price());
			}
			
			((TextView)findViewById(R.id.valuePrice)).setText(_searchRet.getPrice());
			((TextView)findViewById(R.id.valueCategory)).setText(_searchRet.getCategory());
			((TextView)findViewById(R.id.valueMedia)).setText(_searchRet.getMedia());
			((TextView)findViewById(R.id.valuePublish_date)).setText(_searchRet.getPublish_date());
			((TextView)findViewById(R.id.valuePublisher)).setText(_searchRet.getPublisher());
			((TextView)findViewById(R.id.valueAsin)).setText(_searchRet.getAsin());
			((TextView)findViewById(R.id.valueEan)).setText(_searchRet.getEan());
			//((TextView)findViewById(R.id.valueDetail_url)).setText(_searchRet.getDetail_url());
			//((TextView)findViewById(R.id.valueImage_url)).setText(_searchRet.getImage_url());
			//((TextView)findViewById(R.id.valueTitle)).setText(_searchRet.getTitle());
		}
	}
	
    /**
     * showDialog時に呼び出されるメソッド
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
    	super.onCreateDialog(id, bundle);
    	
	    if (id == DIALOG_ID_PROGRESS) {
	    	return createProgressDialog(bundle);
	    }
	    else {
	        return createNormalDialog(bundle);
	    }
    }

    /**
     * オプションメニューを表示する。
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "詳細ページを開く").setIcon(android.R.drawable.ic_menu_gallery);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				if (_searchRet.getDetail_url().equals("")) {
					Bundle data = new Bundle();
					data.putString(DIALOG_KEY_MESSAGE, "詳細URLがありません。");
					
					showDialog(DIALOG_ID_NORMAL, data);
					
					return true;
				}
				
				Uri uri = Uri.parse(_searchRet.getDetail_url());
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				
				return false;
			}
		});
		
		return true;
	}
	// endregion
	
	// region ボタン押下イベント
	/**
	 * 登録ボタン押下メソッド
	 */
	private final Button.OnClickListener eButtonSave = new Button.OnClickListener() {
		public void onClick(View v) {
	    	Bundle bundle = new Bundle();
	    	showDialog(DIALOG_ID_PROGRESS, bundle);
	    	
	    	final Handler handler = new Handler();
	    	final Intent intent = new Intent();
	    	Thread t = new Thread(new Runnable() {
				public void run() {
					SearchResult after = save();
			    	intent.putExtra("SearchResultBefore", _searchRet);
			    	intent.putExtra("SearchResultAfter", after);
			    	
					handler.post(new Runnable() {
						public void run() {
				        	removeDialog(DIALOG_ID_PROGRESS);
						}
					});
				}
			});
	    	t.start();

	    	try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	
	    	intent.putExtra("SearchResultBefore", _searchRet);
        	setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};
	
	/**
	 * DB登録処理
	 */
	private SearchResult save() {
		String id = _searchRet.getId();
		// 登録値マッピング
		ContentValues values = new ContentValues();
		values.put(BookDatabaseHelper.BookColumnFormedTitle, ((EditText)findViewById(R.id.editTextFormed_title)).getText().toString());
		values.put(BookDatabaseHelper.BookColumnVolume, ((EditText)findViewById(R.id.editTextVolume)).getText().toString());
		values.put(BookDatabaseHelper.BookColumnAuthor, ((EditText)findViewById(R.id.editTextAuthor)).getText().toString());
		values.put(BookDatabaseHelper.BookColumnBuyDate, ((EditText)findViewById(R.id.editTextBuy_date)).getText().toString());
		values.put(BookDatabaseHelper.BookColumnBuyPrice, ((EditText)findViewById(R.id.editTextBuy_price)).getText().toString());
		
		values.put(BookDatabaseHelper.BookColumnPrice, _searchRet.getPrice());
		values.put(BookDatabaseHelper.BookColumnCategory, _searchRet.getCategory());
		values.put(BookDatabaseHelper.BookColumnMedia, _searchRet.getMedia());
		values.put(BookDatabaseHelper.BookColumnPublishDate, _searchRet.getPublish_date());
		values.put(BookDatabaseHelper.BookColumnPublisher, _searchRet.getPublisher());
		values.put(BookDatabaseHelper.BookColumnAsin, _searchRet.getAsin());
		values.put(BookDatabaseHelper.BookColumnEan, _searchRet.getEan());
		values.put(BookDatabaseHelper.BookColumnDetailUrl, _searchRet.getDetail_url());
		values.put(BookDatabaseHelper.BookColumnImageUrl, _searchRet.getImage_url());
		values.put(BookDatabaseHelper.BookColumnTitle, _searchRet.getTitle());
		
		if (_searchRet.getId().equals("") && !_searchRet.getImage_url().equals("")) {
			values.put(BookDatabaseHelper.BookColumnImage, BookUtil.getImage(_searchRet.getImage_url()));
		}
		
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		if (_searchRet.getId().equals("")) {
			id = Long.toString(db.insert(BookDatabaseHelper.BookTable, null, values));
		} else {
			db.update(BookDatabaseHelper.BookTable, values, "id = ?", new String[]{ _searchRet.getId() });
		}
		
		db.close();
		
		exportDb();
		
		return new SearchResult.Builder()
							.id(id)
							.asin(_searchRet.getAsin())
							.detail_url(_searchRet.getDetail_url())
							.image_url(_searchRet.getImage_url())
							.author(((EditText)findViewById(R.id.editTextAuthor)).getText().toString())
							.category(_searchRet.getCategory())
							.media(_searchRet.getMedia())
							.ean(_searchRet.getEan())
							.publish_date(_searchRet.getPublish_date())
							.publisher(_searchRet.getPublisher())
							.price(_searchRet.getPrice())
							.title(_searchRet.getTitle())
							.formed_title(((EditText)findViewById(R.id.editTextFormed_title)).getText().toString())
							.volume(((EditText)findViewById(R.id.editTextVolume)).getText().toString())
							.buy_date(((EditText)findViewById(R.id.editTextBuy_date)).getText().toString())
							.buy_price(((EditText)findViewById(R.id.editTextBuy_price)).getText().toString())
							.build();
	}
	
	/**
	 * DBファイルをSDカードへコピーする
	 */
	private void exportDb() {
		// SDカード側格納フォルダ
		String pathDir = new StringBuilder()
							.append(Environment.getExternalStorageDirectory().getPath())
							.append("/Android/data/")
							.append(getPackageName())
							.toString();
		File dir = new File(pathDir);
		// フォルダが無ければ作成する。
		if (!dir.exists() && !dir.mkdirs()) {
			return;
	    }

		// 内部DBファイル
		String dbFile = getDatabasePath(BookDatabaseHelper.DatabaseName).getPath();
		// SDカード側DBファイルパス
		String pathFile = new StringBuilder()
						.append(pathDir)
						.append("/")
						.append(BookDatabaseHelper.DatabaseName)
						.toString();
		
		
		FileChannel channelSource = null;
		FileChannel channelTarget = null;
		// コピー実行
		try {
			channelSource = new FileInputStream(dbFile).getChannel();
			channelTarget = new FileOutputStream(pathFile).getChannel();
			channelSource.transferTo(0, channelSource.size(), channelTarget);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				if (channelSource != null) {
					channelSource.close();
				}
				if (channelTarget != null) {
					channelTarget.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

	}

    /**
     * ダイアログのOKボタン等押下時に呼ぶメソッド
     * 自分をdismissする
     */
    private final DialogInterface.OnClickListener eDismissDialog = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
    }; 
	// endregion

    // region ダイアログ作成
    /**
     * メッセージ表示ダイアログを作成する
     * @param bundle タイトル、メッセージ等を入れた引数
     * @return
     */
    private Dialog createNormalDialog(Bundle bundle) {
        AlertDialog dialog = new AlertDialog.Builder(this)
            //.setTitle(bundle.getInt(DIALOG_KEY_TITLE))
            .setMessage(bundle.getString(DIALOG_KEY_MESSAGE))
            .setPositiveButton("OK", eDismissDialog)
            .create();
        
        return dialog;
    }

    /**
     * 処理進行中であることを表示するダイアログを作成する
     * @param bundle
     * @return
     */
    private Dialog createProgressDialog(Bundle bundle) {
    	final ProgressDialog dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
    	dialog.setIndeterminate(false);
    	dialog.setTitle("処理中");
    	dialog.setMessage("データを登録しています。");
    	dialog.setCancelable(false);
    	
    	return dialog;
    }
// endregion

}
