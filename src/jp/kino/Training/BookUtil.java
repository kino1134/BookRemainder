package jp.kino.Training;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BookUtil {
	/** 本情報を検索するWebサービスURL　*/
//	public static final String SearchUrl = "http://192.168.12.4:9292";
	public static final String SearchUrl = "http://abook-search.heroku.com";
	/**　JSON取得URL　*/
	public static final String SearchPath = "/json/";
	/**　テスト用　*/
	public static final String SearchPathTest = "/test";
	
	/**
	 * 本情報を検索する
	 * @param isbn
	 * @return
	 */
	public static SearchResult search(String isbn) {
		SearchResult.Builder builder = new SearchResult.Builder();
		builder.isbn(isbn);
		String jsonStr = getJsonString(isbn);
		
		if (jsonStr == null) {
			return null;
		}
		
		try {
			JSONArray jsonArr = new JSONArray(jsonStr);
			JSONObject json = jsonArr.getJSONObject(0);
			builder.asin(json.getString("asin"))
					.detail_url(json.getString("detail_url"))
					.image_url(json.getString("image_url"))
					.author(json.getString("author"))
					.media(json.getString("media"))
					.category(json.getString("category"))
					.ean(json.getString("ean"))
					.publish_date(json.getString("publish_date"))
					.publisher(json.getString("publisher"))
					.price(json.getString("price"))
					.title(json.getString("title"))
					.formed_title(json.getString("formed_title"))
					.volume(json.getString("volume"));
		} catch (JSONException e) {
			return builder.build();
		}
		
		return builder.build();
	}
	
	/**
	 * 本情報を検索して、JSON文字列を取得する
	 * @param isbn
	 * @return
	 */
	private static String getJsonString(String isbn) {
		String result = null;

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(SearchUrl + SearchPath + isbn.trim());

		HttpEntity entity = null;
		
		try {
			HttpResponse response = client.execute(method);
			entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			method.abort();
			e.printStackTrace();
		} catch (IOException e) {
			method.abort();
			e.printStackTrace();
		} finally{
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		client.getConnectionManager().shutdown();

		return result;
	}
	
	/**
	 * 画像取得するために、画像URLを渡して、バイト配列を取得する。
	 * 一応、画像じゃなくても取得は可能だと思われる。
	 * @param url
	 * @return
	 */
	public static byte[] getImage(String url) {
		byte[] result = new byte[0];

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(url);

		HttpEntity entity = null;
		
		try {
			HttpResponse response = client.execute(method);
			entity = response.getEntity();
			result = EntityUtils.toByteArray(entity);
		} catch (ClientProtocolException e) {
			method.abort();
			e.printStackTrace();
		} catch (IOException e) {
			method.abort();
			e.printStackTrace();
		} finally{
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		client.getConnectionManager().shutdown();

		return result;
	}
}
