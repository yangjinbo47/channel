package com.tenfen.cache.modules;

import java.util.List;

public class ChapterVO {

	private int bookId;
	private int pageWords;
	private List<String> chapterContent;

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public int getPageWords() {
		return pageWords;
	}

	public void setPageWords(int pageWords) {
		this.pageWords = pageWords;
	}

	public List<String> getChapterContent() {
		return chapterContent;
	}

	public void setChapterContent(List<String> chapterContent) {
		this.chapterContent = chapterContent;
	}

}
