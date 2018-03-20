package com.focustech.common;

import com.focustech.tm.open.sdk.params.FusionField;

import java.io.File;

/**
 * 上传语音工具类
 */
public class UploadVoiceTool extends UploadTool {

	public UploadVoiceTool() {
		requestURL = FusionField.uploadVoiceURL;
	}

	@Override
	public void upload(String picPath) {
		file = new File(picPath);
		//由于图片，暂时决定一次性上传，所以upload_length为文件的大小,并从文件的0处开始上传
		UPDATE_BLOCK = file.length();
		start_position = 0;
		version = null;
		fileId = null;
		super.upload(picPath);
	}
}
