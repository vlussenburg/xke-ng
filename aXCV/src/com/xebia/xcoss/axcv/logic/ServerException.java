package com.xebia.xcoss.axcv.logic;

import com.xebia.xcoss.axcv.Messages;
import com.xebia.xcoss.axcv.util.StringUtil;


public class ServerException extends CommException {

	private static final long serialVersionUID = 2056882476603190969L;

	public ServerException(String url) {
		super(Messages.getString("ServerException.0", url));
	}

	public ServerException(String url, Exception e) {
		super(Messages.getString("ServerException.1", url, StringUtil.getExceptionMessage(e)), e);
	}
}
