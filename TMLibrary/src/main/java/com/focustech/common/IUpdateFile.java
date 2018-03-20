package com.focustech.common;

/**********************************************************
 * @文件名称：IUpdateFile.java
 * @文件作者：huangshuo
 * @创建时间：2014-9-17 上午11:54:29
 * @文件描述：上传接口
 * @修改历史：2014-9-17创建初始版本
 **********************************************************/
public interface IUpdateFile
{
	public void updateFile();
	
	public void onUploadFinish(String str);
	
	public void onUploadError();
}
