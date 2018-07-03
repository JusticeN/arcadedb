/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.console;

import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;

import java.util.LinkedList;
import java.util.List;

public class TerminalParser extends DefaultParser {

  @Override
  public boolean isDelimiterChar(final CharSequence buffer, final int pos) {
    return buffer.charAt(pos) == ';';
  }

  @Override
  public ParsedLine parse(String line, int cursor, ParseContext context) {
    List<String> words = new LinkedList();
    StringBuilder current = new StringBuilder();
    int wordCursor = -1;
    int wordIndex = -1;
    int quoteStart = -1;
    int rawWordCursor = -1;
    int rawWordLength = -1;
    int rawWordStart = 0;

    for (int i = 0; line != null && i < line.length(); ++i) {
      if (i == cursor) {
        wordIndex = words.size();
        wordCursor = current.length();
        rawWordCursor = i - rawWordStart;
      }

      if (quoteStart < 0 && this.isQuoteChar(line, i)) {
        quoteStart = i;
        current.append(line.charAt(i));
      } else if (quoteStart >= 0) {
        if (line.charAt(quoteStart) == line.charAt(i) && !this.isEscaped(line, i)) {
          current.append(line.charAt(i));
          quoteStart = -1;
          if (rawWordCursor >= 0 && rawWordLength < 0) {
            rawWordLength = i - rawWordStart + 1;
          }
        } else if (!this.isEscapeChar(line, i)) {
          current.append(line.charAt(i));
        }
      } else if (this.isDelimiter(line, i)) {
        if (current.length() > 0) {
          words.add(current.toString());
          current.setLength(0);
          if (rawWordCursor >= 0 && rawWordLength < 0) {
            rawWordLength = i - rawWordStart;
          }
        }

        rawWordStart = i + 1;
      } else if (!this.isEscapeChar(line, i)) {
        current.append(line.charAt(i));
      }
    }

    if (current.length() > 0 || cursor == line.length()) {
      words.add(current.toString());
      if (rawWordCursor >= 0 && rawWordLength < 0) {
        rawWordLength = line.length() - rawWordStart;
      }
    }

    if (cursor == line.length()) {
      wordIndex = words.size() - 1;
      wordCursor = words.get(words.size() - 1).length();
      rawWordCursor = cursor - rawWordStart;
      rawWordLength = rawWordCursor;
    }

    String openingQuote = quoteStart >= 0 ? line.substring(quoteStart, quoteStart + 1) : null;
    return new DefaultParser.ArgumentList(line, words, wordIndex, wordCursor, cursor, openingQuote, rawWordCursor, rawWordLength);
  }
}
