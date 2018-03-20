package com.focustech.common;

import com.focustech.tm.open.sdk.params.FusionField;

import java.io.File;

/**
 * 上传图片工具类
 */
public class UploadPictureTool extends UploadTool {

	public UploadPictureTool() {
		requestURL= FusionField.uploadPicURL;
	}

	@Override
	public void upload(String picPath) {
		file = new File(picPath);
		// 暂时决定一次性上传，所以upload_length为文件的大小,并从文件的0处开始上传
		UPDATE_BLOCK = file.length();
		start_position = 0;
		version = null;
		fileId = null;
		super.upload(picPath);
	}
}
