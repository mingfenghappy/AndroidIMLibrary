package com.focustech.common;

import android.util.Log;

import com.focustech.tm.open.sdk.params.FusionField;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 上传工具类
 */
public class UploadTool implements Runnable, IUploadTool {
	/**
	 * 边界标识 随机生成
	 */
	private static final String BOUNDARY = UUID.randomUUID().toString();
	/**
	 * 内容类型
	 */
	private static final String CONTENT_TYPE = "multipart/form-data";
	private static final String PREFIX = "--";
	private static final String LINE_END = "\r\n";

	HttpURLConnection conn;
	String requestURL;
	File file;

	//断点续传，每块碎片的最大值
	long UPDATE_BLOCK;
	//断点续传，每次碎片上传的大小
	public long upload_length;
	//断点续传，已经上传的大小
	public long already_update;
	//断点续传开始的位置
	public long start_position;
	
	public int statement_and_boundary_length;
	public String version;
	public String fileId;
	
	private IUpdateFile listener;
	
	private ExecutorService executorService;

	/**
	 * 下载中途退出需要设置退出标志
	 */
	public boolean quit = false;

	public UploadTool() {
		executorService= Executors.newFixedThreadPool(1);
	}
	
	@Override
	public void upload(String picPath) {
		executorService.submit(new Thread(this));
	}

	@Override
	public void run()
	{
		startUpload();
	}

	/**
	 * Http开始上传
	 */
	private void startUpload() {
		// upload_length用于计算本次上传的大小，UPDATE_BLOCK是上传最大值。
		upload_length = file.length() - start_position;
		if (upload_length > UPDATE_BLOCK) {
			upload_length = UPDATE_BLOCK;
		}
		
		try {
			URL url = new URL(requestURL);
			conn = (HttpURLConnection) url.openConnection();
			// 由于需要上传中需要指明数据块大小，先计算除文件上传外的数据的大小。
			// 声明部分。和边界部分
			statement_and_boundary_length = getStatement().length() + getBoundary().length();
			// 设置连接参数
			setConnectionParams();
			// 当文件不为空，把文件包装并且上传
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			// 写入声明
			dos.write(getStatement().getBytes());
			// 上传文件
			already_update = writeFile(file, dos);
			if (!quit) {
				// 获取响应字符串，并返回，此时是中途没有强制退出的情况
				getResponseString(conn);
			}
		}
		catch (MalformedURLException e) {
			listener.onUploadError();
			e.printStackTrace();
		}
		catch (IOException e) {
			listener.onUploadError();
			e.printStackTrace();
		}
	}

	/**
	 * 设置连接参数
	 */
	public void setConnectionParams() {
		try {
			conn.setReadTimeout(FusionField.readTimeOut);
			conn.setConnectTimeout(FusionField.connectTimeout);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Charset", FusionField.CHARSET); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			// Data-Range 是文件流中部分上传的范围
			conn.setRequestProperty("Data-Range",
					String.valueOf(start_position) + "-" + String.valueOf(start_position + upload_length - 1));
			// Data-Length 是文件的大小
			conn.setRequestProperty("Data-Length", String.valueOf(file.length()));
			if (start_position > 0) {
				// 当start_position大于0时，说明是续传，这是要加上需要的version和Id
				Log.d("owen", "Data-version" + version);
				Log.d("owen", "Data-Id" + fileId);
				conn.setRequestProperty("Data-Version", version);
				conn.setRequestProperty("Data-Id", fileId);
			}
			// Content_length是本次上传的总大小，部分文件大小+声明部分的大小
			conn.setRequestProperty("Content-Length", String.valueOf(statement_and_boundary_length + upload_length));
			Log.d("owen", "Content-Length = " + String.valueOf(statement_and_boundary_length + upload_length));
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
		}
		catch (ProtocolException e) {
			e.printStackTrace();
		}
	}

	public String getStatement() {
		try {
			StringBuffer sb = new StringBuffer();
			/**
			 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
			 * filename是文件的名字，包含后缀名的 比如:abc.png
			 */
			sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
			sb.append("Content-Disposition:form-data; name=\"" + FusionField.FILEKEY + "\"; filename=\""
					+ URLEncoder.encode(file.getName(), "UTF-8") + "\"" + LINE_END);
			sb.append("Content-Type:application/octet-stream" + LINE_END); // 这里配置的Content-type很重要的 ，用于服务器端辨别文件的类型的
			sb.append(LINE_END);
			Log.d("owen", "getStatement = " + sb.toString());
			return sb.toString();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 向服务器写文件
	 * @param file
	 * @param dos
	 */
	public long writeFile(File file, DataOutputStream dos) {
		// 一共读取的长度
		int curLen = 0;
		RandomAccessFile is;
		try {
			is = new RandomAccessFile(file, "r");
			is.seek(start_position);
			byte[] bytes = new byte[1024];
			// 单次读取的长度
			int len = 0;
			while ((len = is.read(bytes)) != -1) {
				if (curLen + len <= UPDATE_BLOCK) {
					curLen += len;
					dos.write(bytes, 0, len);
				}
				else {
					break;
				}
			}
			Thread.sleep(1000);
			is.close();
			// 写结束边界 --bound--
			dos.write(LINE_END.getBytes());
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
			dos.write(end_data);
			dos.flush();
			dos.close();
			return curLen + start_position;
		}
		catch (FileNotFoundException e) {
			listener.onUploadError();
			e.printStackTrace();
		}
		catch (IOException e) {
			listener.onUploadError();
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			listener.onUploadError();
			e.printStackTrace();
		}
		return curLen;
	}

	/**
	 * 获取返回字符串
	 */
	private void getResponseString(HttpURLConnection conn) {
		int res;
		try {
			res = conn.getResponseCode();
			Log.d("owen", "response code = " + res);
			if (res == 200) {
				InputStream input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}
				JSONObject object = new JSONObject(sb1.toString());
				String fileId = object.getString("fileId");
				String version = object.getString("version");
				listener.onUploadFinish(fileId);
				if (file.length() == already_update) {
					//已经上传完成
				}
				else {
					start_position = already_update;
					this.version = version;
					this.fileId = fileId;
					//进行断点续传
					startUpload();
				}
				return;
			}
			else {
				listener.onUploadError();
				return;
			}
		}
		catch (IOException e)
		{
			conn.disconnect();
			listener.onUploadError();
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			conn.disconnect();
			listener.onUploadError();
			e.printStackTrace();
		}
	}

	public String getBoundary()
	{
		StringBuffer sb = null;
		sb = new StringBuffer();
		sb.append(LINE_END);
		sb.append(PREFIX + BOUNDARY + PREFIX + LINE_END);
		return sb.toString();
	}

	public void setOnUpdataListener(IUpdateFile listener)
	{
		this.listener = listener;
	}
}
