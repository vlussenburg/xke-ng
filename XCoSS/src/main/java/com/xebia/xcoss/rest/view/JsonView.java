package com.xebia.xcoss.rest.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.gson.Gson;

public class JsonView extends AbstractView {

	private static final String CONTENT_TYPE = "Content-type";
	private final String json;

	public JsonView(final Object object) {
		this.json = new Gson().toJson(object);
	}

	@Override
	protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
			final HttpServletResponse response) {
		response.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			CharStreams.copy(CharStreams.newReaderSupplier(json), writer);
		}
		catch (final IOException e) {
			throw new IllegalStateException(e);
		}
		finally {
			Closeables.closeQuietly(writer);
		}
	}

}
