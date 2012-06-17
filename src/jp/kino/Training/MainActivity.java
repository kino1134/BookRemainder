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
	/** �o�[�R�[�h�X�L��������Intent�J��ID*/
    public static int REQUEST_CODE_SCAN = 0; 
	/** �o�[�R�[�h�X�L��������Intent�J��ID*/
    public static int REQUEST_CODE_INPUT = 1; 
    
    /** Activity����DialogID�i���b�Z�[�W�\���j*/
    public static int DIALOG_ID_NORMAL = 0; 
    /** Activity����DialogID�iISBN����&�\���j*/
    public static int DIALOG_ID_INPUT = 1; 
    /** Activity����DialogID�i�������s�m�F�j*/
    public static int DIALOG_ID_QUESTION = 2; 
    /** Activity����DialogID�i�������s���j*/
    public static int DIALOG_ID_PROGRESS = 3; 

    /** Dialog��Bundle�Ƀf�[�^��ݒ肷��ۂ̃L�[�i�^�C�g���j*/
    public static String DIALOG_KEY_TITLE = "TITLE";
    /** Dialog��Bundle�Ƀf�[�^��ݒ肷��ۂ̃L�[�i�{���j*/
    public static String DIALOG_KEY_MESSAGE = "Message";
    /** Dialog��Bundle�Ƀf�[�^��ݒ肷��ۂ̃L�[�iISBN�j*/
    public static String DIALOG_KEY_ISBN = "ISBN";

    /** DB�ڑ����i */
	private BookDatabaseHelper dbhelper;

    // region �{�^�������C�x���g
    /**
     * �X�L�����{�^���������\�b�h
     */
    private final Button.OnClickListener eButtonScan = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            intent.putExtra("SCAN_WIDTH", 800);
            intent.putExtra("SCAN_HEIGHT", 200);
            intent.putExtra("RESULT_DISPLAY_DURATION_MS", 3000L);
            intent.putExtra("PROMPT_MESSAGE", "�o�[�R�[�h���X�L�������܂��B");
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        }
    };

    /**
     * ����̓{�^���������\�b�h
     */
    private final Button.OnClickListener eButtonInput = new Button.OnClickListener() {
        public void onClick(View v) {
        	removeDialog(DIALOG_ID_INPUT);
        	showDialog(DIALOG_ID_INPUT, new Bundle());
        }
    };

    /**
     * �\���{�^���������\�b�h
     */
    private final Button.OnClickListener eButtonShow = new Button.OnClickListener() {
        public void onClick(View v) {
        	transitShow();
        }
    };

    /**
     * �_�C�A���O��OK�{�^�����������ɌĂԃ��\�b�h
     * ������dismiss����
     */
    private final DialogInterface.OnClickListener eDismissDialog = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
    }; 

    /**
     * �_�C�A���O��Cancel�{�^�����������ɌĂԃ��\�b�h
     * ������dismiss����
     */
    private final DialogInterface.OnClickListener eCancelDialog = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
		}
    }; 
    // endregion
    
    // region Activity �C�x���g
    /**
     * �{�A�v���N�����ɌĂ΂�郁�\�b�h
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // �{�^���ɃC�x���g��ݒ肷��
        findViewById(R.id.button_scan).setOnClickListener(eButtonScan);
        findViewById(R.id.button_input).setOnClickListener(eButtonInput);
        findViewById(R.id.button_show).setOnClickListener(eButtonShow);
        
        dbhelper = new BookDatabaseHelper(this);
    }
    
    /**
     * Intent�J�ڌ�ɌĂ΂�郁�\�b�h
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == REQUEST_CODE_SCAN) {
        	// ���ʎ擾
            IntentResult result = parseActivityResult(resultCode, intent);
            if (result != null) {
            	// ISBN�擾
                String contents = result.getContents();
                if (contents != null) {
                	// �擾����ISBN��\���������̓_�C�A���O��\������
                	Bundle data = new Bundle();
                	data.putString("ISBN", contents);
                	removeDialog(DIALOG_ID_INPUT);
                	showDialog(DIALOG_ID_INPUT, data);
                }
            }
    	} else if (requestCode == REQUEST_CODE_INPUT) {
    		if (resultCode == Activity.RESULT_OK) {
    			Toast toast = Toast.makeText(this, "�o�^���܂���", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    }

    /**
     * showDialog���ɌĂяo����郁�\�b�h
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
     * Dialog�\���O�ɌĂяo����郁�\�b�h
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
    	// ���̓_�C�A���O�Ɋe��ݒ���{��
        if (id == DIALOG_ID_INPUT) {
            AlertDialog aDialog = (AlertDialog)dialog;
            EditText textBox = (EditText)dialog.findViewById(123);

            // �����\������ISBN��ݒ肷��
            if (bundle.getString("ISBN") != null) {
            	textBox.setText(bundle.getString("ISBN"));
            }
            
            // ISBN�����͂̏ꍇ�A�uOK�v�{�^���𖳌��ɂ���
            if (textBox.getText().length() == 0) {
                aDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }
    // endregion
    
    // region �_�C�A���O�쐬
    /**
     * ���b�Z�[�W�\���_�C�A���O���쐬����
     * @param bundle �^�C�g���A���b�Z�[�W������ꂽ����
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
     * ���̓_�C�A���O���쐬����
     * @param bundle �����\������ISBN�Ȃǂ���ꂽ����
     * @return
     */
    private Dialog createInputDialog(Bundle bundle) {
    	// ISBN���̓e�L�X�g�{�b�N�X
        final EditText textBox = new EditText(this);
        textBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        textBox.setId(123);
        
        // OK�{�^���������̃C�x���g
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
        
        // ISBN���͎��AOK�{�^���̎g�p�ۂ�ݒ肷��
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

        // �e�L�X�g�{�b�N�X�փt�H�[�J�X�𓖂āAIME�������\������
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        textBox.requestFocus();
        
        return dialog;
    }
    
    /**
     * �������s���m�F����_�C�A���O���쐬����
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
        .setTitle("�m�F")
        .setMessage(isbn + "�͊��ɓo�^����Ă���݂����ł��B�o�^�𑱍s���܂����H")
        .setPositiveButton("OK", eOK)
        .setNegativeButton("Cancel", eCancelDialog)
        .create();

        return dialog;
    }
    
    /**
     * �����i�s���ł��邱�Ƃ�\������_�C�A���O���쐬����
     * @param bundle
     * @return
     */
    private Dialog createProgressDialog(Bundle bundle) {
    	final ProgressDialog dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
    	dialog.setIndeterminate(false);
    	dialog.setTitle("������");
    	dialog.setMessage("�f�[�^���擾���Ă��܂��B\r\nISBN:" + bundle.getString(DIALOG_KEY_ISBN));
    	dialog.setCancelable(false);
    	
    	return dialog;
    }
    // endregion

    /**
     * �X�L�����̌��ʂ��܂Ƃ߂�
     * @param requestCode Intent�Ăяo���R�[�h
     * @param resultCode �X�L�������ʃR�[�h
     * @param intent �X�L��������
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
     * ISBN�����ɖ{�����擾����
     * @param isbn
     * @return
     */
    private SearchResult search(final String isbn) {
    	return BookUtil.search(isbn);
    }
    
    /**
     * �{�����͉�ʂ֑J�ڂ���
     * @param searchRet
     */
    private void transitInput(SearchResult searchRet) {
		Intent intent = new Intent(this, InputActivity.class);
		intent.setType("application/searchResult");
		intent.putExtra("SearchResult", searchRet);
		
		startActivityForResult(intent, REQUEST_CODE_INPUT);
    }

    /**
     * �o�^���\����ʂ֑J�ڂ���
     * @param searchRet
     */
    private void transitShow() {
    	Intent intent = new Intent(this, ShowActivity.class);
    	startActivity(intent);
    }

    /**
     * ���̓_�C�A���O�œ��͂���ISBN���o�^�ς݂����ׂ�
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