/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.logsniffer.web.controller.exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error handler for REST validation.
 * 
 * @author mbok
 * 
 */
@ControllerAdvice
public class RestErrorHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MessageSource messageSource;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorResponse processValidationError(
			final MethodArgumentNotValidException ex,
			final HttpServletResponse response) throws IOException {
		return processFieldErrors(
				processExceptionResponse(ex, response,
						org.apache.http.HttpStatus.SC_BAD_REQUEST), ex
						.getBindingResult().getFieldErrors());
	}

	@ExceptionHandler(HttpMessageConversionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorResponse processHttpMessageConversionError(
			final HttpMessageConversionException ex,
			final HttpServletResponse response) throws IOException {
		return processExceptionResponse(ex, response,
				org.apache.http.HttpStatus.SC_BAD_REQUEST);
	}

	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public ErrorResponse processAllExceptions(final Throwable ex,
			final HttpServletResponse response) throws IOException {
		return processExceptionResponse(ex, response,
				org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	private ErrorResponse processExceptionResponse(final Throwable ex,
			final HttpServletResponse response, final int status)
			throws IOException {
		logger.info("Catched exception", ex);
		ErrorResponse er = new ErrorResponse();
		er.setException(ex);
		response.setStatus(status, er.getExceptionMessage());
		return er;
	}

	private ErrorResponse processFieldErrors(final ErrorResponse er,
			final List<FieldError> fieldErrors) {
		Map<String, String> errors = new HashMap<String, String>();
		for (FieldError fieldError : fieldErrors) {
			String localizedErrorMessage = resolveLocalizedErrorMessage(fieldError);
			errors.put(fieldError.getField(), localizedErrorMessage);
		}
		er.setBindErrors(errors);
		return er;
	}

	private String resolveLocalizedErrorMessage(final FieldError fieldError) {
		Locale currentLocale = LocaleContextHolder.getLocale();
		String localizedErrorMessage = messageSource.getMessage(fieldError,
				currentLocale);
		return localizedErrorMessage;
	}
}