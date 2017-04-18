package com.bourne.httprequest.singleThreadDownload;
/**
 *
 *
 *线程信息类，封装线程的ID，线程的url，线程开始位置，结束位置，以及以及完成的位置
 */
public class ThreadInfo {
	private int id;
	private String url;
	private int start;
	private int end;
	private int finished;

	public ThreadInfo() {
		super();
	}
	/**
	 *
	 * @param id 线程的ID
	 * @param url 下载忘记的网络地址
	 * @param start 线程下载的开始位置
	 * @param end 线程下载的结束位置
	 * @param finished	线程已经下载到哪个位置
	 */
	public ThreadInfo(int id, String url, int start, int end, int finished) {
		super();
		this.id = id;
		this.url = url;
		this.start = start;
		this.end = end;
		this.finished = finished;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getFinished() {
		return finished;
	}

	public void setFinished(int finished) {
		this.finished = finished;
	}

	@Override
	public String toString() {
		return "ThreadInfo [id=" + id + ", url=" + url + ", start=" + start + ", end=" + end + ", finished=" + finished
				+ "]";
	}

}
