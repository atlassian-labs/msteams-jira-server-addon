package com.microsoft.teams.utils;

import org.junit.Test;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;

public class ExceptionHelpersTest {

        @Test
        public void GetResponseErrorMessage_ErrorMessagePresentErrorsEmpty_ReturnsErrorMessage()
        {
            String errorMessage = "api internal error message";
            String exceptionContent = "{\"errorMessages\":[\"" + errorMessage + "\"], \"errors\":{}}";

            String result = ExceptionHelpers.getResponseErrorMessage(exceptionContent);

            assertNotNull(result);
            assertEquals(errorMessage, result);
        }

        @Test
        public void GetResponseErrorMessage_ErrorMessageEmptyErrorsEmpty_ReturnsEmptyString()
        {
            String exceptionContent = "{\"errorMessages\":[], \"errors\":{}}";

            String result = ExceptionHelpers.getResponseErrorMessage(exceptionContent);

            assertNotNull(result);
            assertEquals("", result);
        }

        @Test
        public void GetResponseErrorMessage_ExceptionContentNull_ReturnsGenericMessage()
        {
            String result = ExceptionHelpers.getResponseErrorMessage(null);

            assertNotNull(result);
            assertEquals(ExceptionHelpers.GENERIC_ERROR_MESSAGE, result);
        }

        @Test
        public void GetResponseErrorMessage_ErrorMessageEmptyErrorsPresent_ReturnsErrors()
        {
            String errorField = "assignee";
            String errorDetail = "User 'johndoe' cannot be assigned issues.";
            String errors = String.format("\"%s\":\"%s\"", errorField, errorDetail);
            String expectedMessage = String.format("%s error: %s", StringUtils.capitalize(errorField), errorDetail);
            String exceptionContent = "{\"errorMessages\":[], \"errors\":{" + errors + "}}";

            String result = ExceptionHelpers.getResponseErrorMessage(exceptionContent);

            assertNotNull(result);
            assertEquals(expectedMessage, result);
        }
}
