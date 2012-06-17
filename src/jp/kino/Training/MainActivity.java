package jp.kino.Training;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	/** バーコードスキャン時のIntent遷移ID*/
    public static int REQUEST_CODE_SCAN = 0; 
	/** バーコードスキャン時のIntent遷移ID*/
    public static int REQUEST_CODE_INPUT = 1; 
    
    /** Activity内のDialogID（メッセージ表示）*/
    public static int DIALOG_ID_NORMAL = 0; 
    /** Activity内のDialogID（ISBN入力&表示）*/
    public static int DIALOG_ID_INPUT = 1; 
    /** Activity内のDialogID（処理続行確認）*/
    public static int DIALOG_ID_QUESTION = 2; 
    /** Activity内のDialogID（処理実行中）*/
    public static int DIALOG_ID_PROGRESS = 3; 

    /** DialogのBundleにデータを設定する際のキー（タイトル）*/
    public static String DIALOG_KEY_TITLE = "TITLE";
    /** DialogのBundleにデータを設定する際のキー（本文）*/
    public static String DIALOG_KEY_MESSAGE = "Message";
    /** DialogのBundleにデータを設定する際のキー（ISBN）*/
    public static String DIALOG_KEY_ISBN = "ISBN";

    /** DB接続部品 */
	private BookDatabaseHelper dbhelper;

    // region ボタン押下イベント
    /**
     * スキャンボタン押下メソッド
     */
    private final Button.OnClickListener eButtonScan = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            intent.putExtra("SCAN_WIDTH", 800);
            intent.putExtra("SCAN_HEIGHT", 200);
            intent.putExtra("RESULT_DISPLAY_DURATION_MS", 3000L);
            intent.putExtra("PROMPT_MESSAGE", "バーコードをスキャンします。");
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        }
    };

    /**
     * 手入力ボタン押下メソッド
     */
    private final Button.OnClickListener eButtonInput = new Button.OnClickListener() {
        public void onClick(View v) {
        	removeDialog(DIALOG_ID_INPUT);
        	showDialog(DIALOG_ID_INPUT, new Bundle());
        }
    };

    /**
     * 表示ボタン押下メソッド
     */
    private final Button.OnClickListener eButtonShow = new Button.OnClickListener() {
        public void onClick(View v) {
        	transitShow();
        }
    };

    /**
     * ダイアログのOKボタン等押下時に呼ぶメソッド
     * 自分をdismissする
     */
    private final DialogInterface.OnClickListener eDismissDialog = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
    }; 

    /**
     * ダイアログのCancelボタン等押下時に呼ぶメソッド
     * 自分をdismissする
     */
    private final DialogInterface.OnClickListener eCancelDialog = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
		}
    }; 
    // endregion
    
    // region Activity イベント
    /**
     * 本アプリ起動時に呼ばれるメソッド
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // ボタンにイベントを設定する
        findViewById(R.id.button_scan).setOnClickListener(eButtonScan);
        findViewById(R.id.button_input).setOnClickListener(eButtonInput);
        findViewById(R.id.button_show).setOnClickListener(eButtonShow);
        
        dbhelper = new BookDatabaseHelper(this);
    }
    
    /**
     * Intent遷移後に呼ばれるメソッド
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == REQUEST_CODE_SCAN) {
        	// 結果取得
            IntentResult result = parseActivityResult(resultCode, intent);
            if (result != null) {
            	// ISBN取得
                String contents = result.getContents();
                if (contents != null) {
                	// 取得したISBNを表示した入力ダイアログを表示する
                	Bundle data = new Bundle();
                	data.putString("ISBN", contents);
                	removeDialog(DIALOG_ID_INPUT);
                	showDialog(DIALOG_ID_INPUT, data);
                }
            }
    	} else if (requestCode == REQUEST_CODE_INPUT) {
    		if (resultCode == Activity.RESULT_OK) {
    			Toast toast = Toast.makeText(this, "登録しました", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    }

    /**
     * showDialog時に呼び出されるメソッド
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
    	super.onCreateDialog(id, bundle);
    	
        if (id == DIALOG_ID_INPUT) {
            return createInputDialog(bundle);
        } else if (id == DIALOG_ID_QUESTION) {
        	return createQuestionDialog(bundle);
        } else if (id == DIALOG_ID_PROGRESS) {
        	return createProgressDialog(bundle);
        }
        else {
            return createNormalDialog(bundle);
        }
    }

    /**
     * Dialog表示前に呼び出されるメソッド
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
    	// 入力ダイアログに各種設定を施す
        if (id == DIALOG_ID_INPUT) {
            AlertDialog aDialog = (AlertDialog)dialog;
            EditText textBox = (EditText)dialog.findViewById(123);

            // 初期表示するISBNを設定する
            if (bundle.getString("ISBN") != null) {
            	textBox.setText(bundle.getString("ISBN"));
            }
            
            // ISBN未入力の場合、「OK」ボタンを無効にする
            if (textBox.getText().length() == 0) {
                aDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }
    // endregion
    
    // region ダイアログ作成
    /**
     * メッセージ表示ダイアログを作成する
     * @param bundle タイトル、メッセージ等を入れた引数
     * @return
     */
    private Dialog createNormalDialog(Bundle bundle) {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(bundle.getInt(DIALOG_KEY_TITLE))
            .setMessage(bundle.getString(DIALOG_KEY_MESSAGE))
            .setPositiveButton("OK", eDismissDialog)
            .create();
        
        return dialog;
    }
    
    /**
     * 入力ダイアログを作成する
     * @param bundle 初期表示するISBNなどを入れた引数
     * @return
     */
    private Dialog createInputDialog(Bundle bundle) {
    	// ISBN入力テキストボックス
        final EditText textBox = new EditText(this);
        textBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        textBox.setId(123);
        
        // OKボタン押下時のイベント
        DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (textBox.getText().length() > 0) {
                	if (checkExists(textBox.getText().toString())) {
                		Bundle data = new Bundle();
                		data.putString(DIALOG_KEY_ISBN, textBox.getText().toString());

                    	removeDialog(DIALOG_ID_QUESTION);
                        showDialog(DIALOG_ID_QUESTION, data);
                	} else {
            	    	Bundle bundle = new Bundle();
            	    	bundle.putString(DIALOG_KEY_ISBN, textBox.getText().toString());
            	    	showDialog(DIALOG_ID_PROGRESS, bundle);

            	    	final Handler handler = new Handler();
            	    	new Thread(new Runnable() {
            				public void run() {
            					SearchResult result = search(textBox.getText().toString());
            					if (result == null) {
                					handler.post(new Runnable() {
                						public void run() {
        						        	removeDialog(DIALOG_ID_PROGRESS);
        		
        						        	Bundle data = new Bundle();
        									data.putInt(DIALOG_KEY_TITLE, R.string.result_network_failed);
        									data.putString(DIALOG_KEY_MESSAGE, getString(R.string.result_network_failed_contents));
        									showDialog(DIALOG_ID_NORMAL, data);
                						}
                					});
            					} else {
                	            	transitInput(result);
                			    	
                					handler.post(new Runnable() {
                						public void run() {
                				        	removeDialog(DIALOG_ID_PROGRESS);
                						}
                					});
            					}
            				}
            			}).start();
                	}
                }
            }
        };
        
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("ISBN Input")
            .setView(textBox)
            .setPositiveButton("OK", okClick)
            .setNegativeButton("Cancel", eCancelDialog)
            .create();
        
        // ISBN入力時、OKボタンの使用可否を設定する
        TextWatcher watcher = new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        };
        textBox.addTextChangedListener(watcher);

        // テキストボックスへフォーカスを当て、IMEを初期表示する
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        textBox.requestFocus();
        
        return dialog;
    }
    
    /**
     * 処理続行を確認するダイアログを作成する
     * @param bundle
     * @return
     */
    private Dialog createQuestionDialog(Bundle bundle) {
    	final String isbn = bundle.getString(DIALOG_KEY_ISBN);
    	
        final DialogInterface.OnClickListener eOK = new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    	    	Bundle bundle = new Bundle();
    	    	bundle.putString(DIALOG_KEY_ISBN, isbn);
    	    	showDialog(DIALOG_ID_PROGRESS, bundle);

    	    	final Handler handler = new Handler();
    	    	new Thread(new Runnable() {
    				public void run() {
    					SearchResult result = search(isbn);
    					if (result == null) {
        					handler.post(new Runnable() {
        						public void run() {
						        	removeDialog(DIALOG_ID_PROGRESS);
		
						        	Bundle data = new Bundle();
									data.putInt(DIALOG_KEY_TITLE, R.string.result_network_failed);
									data.putString(DIALOG_KEY_MESSAGE, getString(R.string.result_network_failed_contents));
									showDialog(DIALOG_ID_NORMAL, data);
        						}
        					});
    					} else {
        	            	transitInput(result);
        			    	
        					handler.post(new Runnable() {
        						public void run() {
        				        	removeDialog(DIALOG_ID_PROGRESS);
        						}
        					});
    					}
    				}
    			}).start();
    		}
        }; 

        final AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("確認")
        .setMessage(isbn + "は既に登録されているみたいです。登録を続行しますか？")
        .setPositiveButton("OK", eOK)
        .setNegativeButton("Cancel", eCancelDialog)
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
    	dialog.setMessage("データを取得しています。\r\nISBN:" + bundle.getString(DIALOG_KEY_ISBN));
    	dialog.setCancelable(false);
    	
    	return dialog;
    }
    // endregion

    /**
     * スキャンの結果をまとめる
     * @param requestCode Intent呼び出しコード
     * @param resultCode スキャン結果コード
     * @param intent スキャン結果
     * @return
     */
    private IntentResult parseActivityResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            String contents = intent.getStringExtra("SCAN_RESULT");
            String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
            byte[] rawBytes = intent.getByteArrayExtra("SCAN_RESULT_BYTES");
            int intentOrientation = intent.getIntExtra("SCAN_RESULT_ORIENTATION", Integer.MIN_VALUE);
            Integer orientation = intentOrientation == Integer.MIN_VALUE ? null : intentOrientation;
            String errorCorrectionLevel = intent.getStringExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL");
            return new IntentResult(contents,
                    formatName,
                    rawBytes,
                    orientation,
                    errorCorrectionLevel);
        }
        
        return new IntentResult();
    }
  
    /**
     * ISBNを元に本情報を取得する
     * @param isbn
     * @return
     */
    private SearchResult search(final String isbn) {
    	return BookUtil.search(isbn);
    }
    
    /**
     * 本情報入力画面へ遷移する
     * @param searchRet
     */
    private void transitInput(SearchResult searchRet) {
		Intent intent = new Intent(this, InputActivity.class);
		intent.setType("application/searchResult");
		intent.putExtra("SearchResult", searchRet);
		
		startActivityForResult(intent, REQUEST_CODE_INPUT);
    }

    /**
     * 登録情報表示画面へ遷移する
     * @param searchRet
     */
    private void transitShow() {
    	Intent intent = new Intent(this, ShowActivity.class);
    	startActivity(intent);
    }

    /**
     * 入力ダイアログで入力したISBNが登録済みか調べる
     * @param isbn
     * @return
     */
    private boolean checkExists(String isbn) {
        StringBuilder selection =  new StringBuilder();
        selection.append(BookDatabaseHelper.BookColumnAsin).append("=?")
        		.append(" or ")
        		.append(BookDatabaseHelper.BookColumnEan).append("=?");

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.query(BookDatabaseHelper.BookTable,
        		new String[]{"id"}, selection.toString(), new String[]{ isbn, isbn }, null, null, null);
        int count = c.getCount();
        
        c.close();
        db.close();
        
        return count > 0;
    }
}