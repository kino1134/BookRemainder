package jp.kino.Training;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDatabaseHelper extends SQLiteOpenHelper {
	/**	SQLite�̃f�[�^�x�[�X�t�@�C���� */
	public static final String DatabaseName = "books.db";
	/** �f�[�^�x�[�X�̃o�[�W���� */
	private static final int DatabaseVersion = 2;

	/** �e�[�u���̖��� */
	public static final String BookTable = "books";
	/** �񖼏�(��L�[) */
	public static final String BookColumnID = "id";
	/** �񖼏�(ASIN) */
	public static final String BookColumnAsin = "asin";
	/** �񖼏�(�ڍ�URL) */
	public static final String BookColumnDetailUrl = "detail_url";
	/** �񖼏�(�摜URL) */
	public static final String BookColumnImageUrl = "image_url";
	/** �񖼏�(����) */
	public static final String BookColumnAuthor = "author";
	/** �񖼏�(���f�B�A) */
	public static final String BookColumnMedia = "media";
	/** �񖼏�(�J�e�S��) */
	public static final String BookColumnCategory = "category";
	/** �񖼏�(EAN) */
	public static final String BookColumnEan = "ean";
	/** �񖼏�(�o�œ�) */
	public static final String BookColumnPublishDate = "publish_date";
	/** �񖼏�(�o�Ŏ�) */
	public static final String BookColumnPublisher = "publisher";
	/** �񖼏�(���i) */
	public static final String BookColumnPrice = "price";
	/** �񖼏�(�^�C�g��(���̂܂�)) */
	public static final String BookColumnTitle = "title";
	/** �񖼏�(�^�C�g��) */
	public static final String BookColumnFormedTitle = "formed_title";
	/** �񖼏�(����) */
	public static final String BookColumnVolume = "volume";
	/** �񖼏�(�w����) */
	public static final String BookColumnBuyDate = "buy_date";
	/** �񖼏�(�w�����i) */
	public static final String BookColumnBuyPrice = "buy_price";
	/** �񖼏�(�摜) */
	public static final String BookColumnImage = "image";
	
	public BookDatabaseHelper(Context context) {
		super(context, DatabaseName, null, DatabaseVersion);
	}

	/**
	 * �f�[�^�x�[�X���쐬����ۂɌĂяo����郁�\�b�h
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder query = new StringBuilder();
		query.append("create table ")
			.append(BookTable).append("(")
			.append(BookColumnID)			.append(" INTEGER").append(" PRIMARY KEY").append(",")
			.append(BookColumnAsin)			.append(" TEXT").append(",")
			.append(BookColumnDetailUrl)	.append(" TEXT").append(",")
			.append(BookColumnImageUrl)		.append(" TEXT").append(",")
			.append(BookColumnAuthor)		.append(" TEXT").append(",")
			.append(BookColumnMedia)		.append(" TEXT").append(",")
			.append(BookColumnCategory)		.append(" TEXT").append(",")
			.append(BookColumnEan)			.append(" TEXT").append(",")
			.append(BookColumnPublishDate)	.append(" TEXT").append(",")
			.append(BookColumnPublisher)	.append(" TEXT").append(",")
			.append(BookColumnPrice)		.append(" TEXT").append(",")
			.append(BookColumnTitle)		.append(" TEXT").append(",")
			.append(BookColumnFormedTitle)	.append(" TEXT").append(",")
			.append(BookColumnVolume)		.append(" TEXT").append(",")
			.append(BookColumnBuyDate)		.append(" TEXT").append(",")
			.append(BookColumnBuyPrice)		.append(" TEXT").append(",")
			.append(BookColumnImage)		.append(" BLOB").append(")");
		db.execSQL(query.toString());
	}

	/**
	 * �f�[�^�x�[�X��`�̍X�V���ɌĂяo����郁�\�b�h
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion == 2) {
			db.execSQL("alter table " + BookTable + " rename to " + BookTable + "_" + oldVersion);
			onCreate(db);
			StringBuilder query = new StringBuilder();
			query.append("insert into ")
				.append(BookTable).append("(")
				.append(BookColumnID)			.append(",")
				.append(BookColumnAsin)			.append(",")
				.append(BookColumnDetailUrl)	.append(",")
				.append(BookColumnImageUrl)		.append(",")
				.append(BookColumnAuthor)		.append(",")
				.append(BookColumnMedia)		.append(",")
				.append(BookColumnCategory)		.append(",")
				.append(BookColumnEan)			.append(",")
				.append(BookColumnPublishDate)	.append(",")
				.append(BookColumnPublisher)	.append(",")
				.append(BookColumnPrice)		.append(",")
				.append(BookColumnTitle)		.append(",")
				.append(BookColumnFormedTitle)	.append(",")
				.append(BookColumnVolume)		.append(",")
				.append(BookColumnBuyDate)		.append(",")
				.append(BookColumnBuyPrice)		.append(",")
				.append(BookColumnImage)		.append(")")
				.append("values( select ")
				.append(BookColumnID)			.append(",")
				.append(BookColumnAsin)			.append(",")
				.append(BookColumnDetailUrl)	.append(",")
				.append(BookColumnImageUrl)		.append(",")
				.append(BookColumnAuthor)		.append(",")
				.append(BookColumnCategory)		.append(",")
				.append("null")					.append(",")
				.append(BookColumnEan)			.append(",")
				.append(BookColumnPublishDate)	.append(",")
				.append(BookColumnPublisher)	.append(",")
				.append(BookColumnPrice)		.append(",")
				.append(BookColumnTitle)		.append(",")
				.append(BookColumnFormedTitle)	.append(",")
				.append(BookColumnVolume)		.append(",")
				.append(BookColumnBuyDate)		.append(",")
				.append(BookColumnBuyPrice)		.append(",")
				.append(BookColumnImage)		.append(" from " + BookTable + "_" + oldVersion + ")");
			db.execSQL("drop table if exists " + BookTable + "_" + oldVersion);
		}
	}
	
	/**
	 * �f�[�^�x�[�X���J���ۂɌĂяo����郁�\�b�h
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
