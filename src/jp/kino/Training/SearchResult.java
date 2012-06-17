package jp.kino.Training;

import java.io.Serializable;

public class SearchResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String _isbn;
	private final String _asin;
	private final String _detail_url;
	private final String _image_url;
	private final String _author;
	private final String _category;
	private final String _media;
	private final String _ean;
	private final String _publish_date;
	private final String _publisher;
	private final String _price;
	private final String _title;
	private final String _formed_title;
	private final String _volume;
	private final String _id;

	private final String _buy_date;
	private final String _buy_price;
	private final byte[] _image;

	public static class Builder {
		private String _isbn;
		private String _asin = "";
		private String _detail_url = "";
		private String _image_url = "";
		private String _author = "";
		private String _category = "";
		private String _media = "";
		private String _ean = "";
		private String _publish_date = "";
		private String _publisher = "";
		private String _price = "";
		private String _title = "";
		private String _formed_title = "";
		private String _volume = "";
		private String _id = "";

		private String _buy_date = "";
		private String _buy_price = "";
		private byte[] _image = new byte[0];

		public Builder() {
		}

		public SearchResult build() {
			return new SearchResult(this);
		}

		public Builder isbn(String val) {
			if (val == null) {
				return this;
			}
			_isbn = val;
			return this;
		}

		public Builder asin(String val) {
			if (val == null) {
				return this;
			}
			_asin = val;
			return this;
		}

		public Builder detail_url(String val) {
			if (val == null) {
				return this;
			}
			_detail_url = val;
			return this;
		}

		public Builder image_url(String val) {
			if (val == null) {
				return this;
			}
			_image_url = val;
			return this;
		}

		public Builder author(String val) {
			if (val == null) {
				return this;
			}
			_author = val;
			return this;
		}

		public Builder category(String val) {
			if (val == null) {
				return this;
			}
			_category = val;
			return this;
		}

		public Builder media(String val) {
			if (val == null) {
				return this;
			}
			_media = val;
			return this;
		}

		public Builder ean(String val) {
			if (val == null) {
				return this;
			}
			_ean = val;
			return this;
		}

		public Builder publish_date(String val) {
			if (val == null) {
				return this;
			}
			_publish_date = val;
			return this;
		}

		public Builder publisher(String val) {
			if (val == null) {
				return this;
			}
			_publisher = val;
			return this;
		}

		public Builder price(String val) {
			if (val == null) {
				return this;
			}
			_price = val;
			return this;
		}

		public Builder title(String val) {
			if (val == null) {
				return this;
			}
			_title = val;
			return this;
		}

		public Builder formed_title(String val) {
			if (val == null) {
				return this;
			}
			_formed_title = val;
			return this;
		}

		public Builder volume(String val) {
			if (val == null) {
				return this;
			}
			_volume = val;
			return this;
		}

		public Builder id(String val) {
			if (val == null) {
				return this;
			}
			_id = val;
			return this;
		}

		public Builder buy_date(String val) {
			if (val == null) {
				return this;
			}
			_buy_date = val;
			return this;
		}

		public Builder buy_price(String val) {
			if (val == null) {
				return this;
			}
			_buy_price = val;
			return this;
		}

		public Builder image(byte[] val) {
			if (val == null) {
				return this;
			}
			_image = val;
			return this;
		}
	}

	private SearchResult(Builder builder) {
		_isbn = builder._isbn;
		_asin = builder._asin;
		_detail_url = builder._detail_url;
		_image_url = builder._image_url;
		_author = builder._author;
		_category = builder._category;
		_media = builder._media;
		_ean = builder._ean;
		_publish_date = builder._publish_date;
		_publisher = builder._publisher;
		_price = builder._price;
		_title = builder._title;
		_formed_title = builder._formed_title;
		_volume = builder._volume;
		_id = builder._id;

		_buy_date = builder._buy_date;
		_buy_price = builder._buy_price;
		_image = builder._image;
	}

	public String getIsbn() {
		return _isbn;
	}

	public String getAsin() {
		return _asin;
	}

	public String getDetail_url() {
		return _detail_url;
	}

	public String getImage_url() {
		return _image_url;
	}

	public String getAuthor() {
		return _author;
	}

	public String getCategory() {
		return _category;
	}

	public String getMedia() {
		return _media;
	}

	public String getEan() {
		return _ean;
	}

	public String getPublish_date() {
		return _publish_date;
	}

	public String getPublisher() {
		return _publisher;
	}

	public String getPrice() {
		return _price;
	}

	public String getTitle() {
		return _title;
	}

	public String getFormed_title() {
		return _formed_title;
	}

	public String getVolume() {
		return _volume;
	}

	public String getId() {
		return _id;
	}

	public String getBuy_date() {
		return _buy_date;
	}

	public String getBuy_price() {
		return _buy_price;
	}

	public byte[] getIamge() {
		return _image;
	}
}
