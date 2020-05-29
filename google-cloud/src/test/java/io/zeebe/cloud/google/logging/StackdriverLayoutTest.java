package io.zeebe.cloud.google.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class StackdriverLayoutTest {
  private static final ObjectReader READER = new ObjectMapper().reader();

  private Logger logger;
  private Writer logTarget;
  private WriterAppender appender;

  @BeforeEach
  public void before() {
    logger = (Logger) LogManager.getLogger();
    logTarget = new StringWriter();
    appender = createAndStartAppender(new StackdriverLayout(), logTarget);
    logger.addAppender(appender);
  }

  @AfterEach
  public void tearDown() {
    logger.removeAppender(appender);
  }

  @Test
  void testJSONOutput() throws IOException {
    // when
    logger.error("Should appear as JSON formatted output");

    // then
    final var jsonMap = writerToMap(logTarget);

    SoftAssertions.assertSoftly(
        softly ->
            softly
                .assertThat(jsonMap)
                .containsKeys(
                    "message",
                    "severity",
                    "thread",
                    "time",
                    "context")
                .containsEntry("message", "Should appear as JSON formatted output")
                .containsEntry("severity", "ERROR")
                .containsEntry("time", )
                .containsEntry("context", Map.of("loggerName", logger.getName())));
  }

  private Map<String, Object> writerToMap(final Writer logTarget) throws JsonProcessingException {
    return READER.withValueToUpdate(new HashMap<String, Object>()).readValue(logTarget.toString());
  }

  private WriterAppender createAndStartAppender(final StringLayout layout, final Writer logTarget) {
    final var writerAppender =
        WriterAppender.createAppender(layout, null, logTarget, "test", false, false);
    writerAppender.start();
    return writerAppender;
  }
}
