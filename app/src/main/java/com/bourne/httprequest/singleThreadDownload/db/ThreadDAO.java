package com.bourne.httprequest.singleThreadDownload.db;



import com.bourne.httprequest.singleThreadDownload.ThreadInfo;

import java.util.List;

/**
 * 数据库操作的接口类
 *
 */
public interface ThreadDAO {
	// 插入线程
	public void insertThread(ThreadInfo info);
	// 刪除线程
	public void deleteThread(String url, int thread_id);
	// 更新线程
	public void updateThread(String url, int thread_id, int finished);
	//查询线程
	public List<ThreadInfo> queryThreads(String url);
	// 判断线程是否存在
	public boolean isExists(String url, int threadId);
}
