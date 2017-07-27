package com.tenfen.cache.modules;

import java.util.List;

public class BookPageVO {

	private int bookId;
	private int chargeMode;
	private String author;
	private List<CatalogVO> chapters;

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public int getChargeMode() {
		return chargeMode;
	}

	public void setChargeMode(int chargeMode) {
		this.chargeMode = chargeMode;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<CatalogVO> getChapters() {
		return chapters;
	}

	public void setChapters(List<CatalogVO> chapters) {
		this.chapters = chapters;
	}

}
