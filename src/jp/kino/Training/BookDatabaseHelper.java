package jp.kino.Training;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDatabaseHelper extends SQLiteOpenHelper {
	/**	SQLiteのデータベースファイル名 */
	public static final String DatabaseName = "books.db";
	/** データベースのバージョン */
	private static final int DatabaseVersion = 2;

	/** テーブルの名称 */
	public static final String BookTable = "books";
	/** 列名称(主キー) */
	public static final String BookColumnID = "id";
	/** 列名称(ASIN) */
	public static final String BookColumnAsin = "asin";
	/** 列名称(詳細URL) */
	public static final String BookColumnDetailUrl = "detail_url";
	/** 列名称(画像URL) */
	public static final String BookColumnImageUrl = "image_url";
	/** 列名称(著者) */
	public static final String BookColumnAuthor = "author";
	/** 列名称(メディア) */
	public static final String BookColumnMedia = "media";
	/** 列名称(カテゴリ) */
	public static final String BookColumnCategory = "category";
	/** 列名称(EAN) */
	public static final String BookColumnEan = "ean";
	/** 列名称(出版日) */
	public static final String BookColumnPublishDate = "publish_date";
	/** 列名称(出版社) */
	public static final String BookColumnPublisher = "publisher";
	/** 列名称(価格) */
	public static final String BookColumnPrice = "price";
	/** 列名称(タイトル(そのまま)) */
	public static final String BookColumnTitle = "title";
	/** 列名称(タイトル) */
	public static final String BookColumnFormedTitle = "formed_title";
	/** 列名称(巻数) */
	public static final String BookColumnVolume = "volume";
	/** 列名称(購入日) */
	public static final String BookColumnBuyDate = "buy_date";
	/** 列名称(購入価格) */
	public static final String BookColumnBuyPrice = "buy_price";
	/** 列名称(画像) */
	public static final String BookColumnImage = "image";
	
	public BookDatabaseHelper(Context context) {
		super(context, DatabaseName, null, DatabaseVersion);
	}

	/**
	 * データベースを作成する際に呼び出されるメソッド
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
	 * データベース定義の更新時に呼び出されるメソッド
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
	 * データベースを開く際に呼び出されるメソッド
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
