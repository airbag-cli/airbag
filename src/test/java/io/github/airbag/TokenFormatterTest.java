package io.github.airbag;

import io.github.airbag.token.format.TokenException;
import io.github.airbag.token.format.TokenFormatter;
import io.github.airbag.token.format.TokenFormatterBuilder;
import io.github.airbag.token.format.TokenParseException;
import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenFormatterTest {

    private static final Token TOKEN = Tokens.singleTokenOf()
            .index(0)
            .startIndex(0)
            .stopIndex(2)
            .text("Hello")
            .type(-213)
            .line(1)
            .charPositionInLine(0)
            .get();

    private static final Vocabulary VOCABULARY = new Vocabulary() {


        @Override
        public int getMaxTokenType() {
            return 4;
        }

        @Override
        public String getLiteralName(int tokenType) {
            if (tokenType == 3) {
                return "'='";
            } else if (tokenType == 4){
                return "'=='";
            }
            return null;
        }

        @Override
        public String getSymbolicName(int tokenType) {
            if (tokenType == 1) {
                return "ID";
            } else if (tokenType == 2) {
                return "IDENTIFIER";
            }
            return null;
        }

        @Override
        public String getDisplayName(int tokenType) {
            return "";
        }
    };

    @Test
    void testIntegerFormatter() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.INDEX)
                .toFormatter();
        assertEquals("0", formatter.format(TOKEN));
        var equalizer = Tokens.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("0")));
    }

    @Test
    void testNegativeInteger() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.TYPE)
                .toFormatter();
        assertEquals("-213", formatter.format(TOKEN));
        var equalizer = Tokens.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("-213")));
    }

    @Test
    void invalidNegative() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.TYPE)
                .toFormatter();
        assertThrows(TokenParseException.class, () -> formatter.parse("-"));
    }

    @Test
    void multipleDashes() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.TYPE)
                .toFormatter();
        assertThrows(TokenParseException.class, () -> formatter.parse("--10"));
    }

    @Test
    void testCharacterParserPrinter() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendInteger(TokenField.INDEX)
                .appendLiteral(":")
                .appendInteger(TokenField.TYPE)
                .appendLiteral(")")
                .toFormatter();

        assertEquals("(0:-213)", formatter.format(TOKEN));
        Token parsed = formatter.parse("(0:-213)");
        var equalizer = Tokens.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, parsed));
    }

    @Test
    void testInvalidLiteral() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendInteger(TokenField.INDEX)
                .appendLiteral(":")
                .appendInteger(TokenField.TYPE)
                .appendLiteral(")")
                .toFormatter();
        assertThrows(TokenParseException.class, () -> formatter.parse("(0;-10)"));
    }

    @Test
    void testTextParserWithDelimiter() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendInteger(TokenField.TYPE)
                .appendLiteral(":")
                .appendText()
                .appendLiteral(":")
                .appendInteger(TokenField.INDEX)
                .appendLiteral(")")
                .toFormatter();
        assertEquals("(-213:Hello:0)", formatter.format(TOKEN));
        var equalizer = Tokens.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("(-213:Hello:0)")));
    }

    @Test
    void testTextParserWithoutDelimiter() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendInteger(TokenField.TYPE)
                .appendLiteral(":")
                .appendText().toFormatter();
        assertEquals("-213:Hello", formatter.format(TOKEN));
        var equalizer = Tokens.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("-213:Hello")));
    }

    @Test
    void testSymbolicTypeParser() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendSymbolicType()
                .appendLiteral(":")
                .appendText()
                .appendLiteral(")")
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("number").get();
        assertEquals("(ID:number)", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("(ID:number)"));
        assertEquals(ExpressionLexer.ID, parsed.getType());
        assertEquals("number", parsed.getText());
    }

    @Test
    void testOnlySymbolic() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("number").get();
        assertEquals("ID", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("ID"));
        assertEquals(ExpressionLexer.ID, parsed.getType());
    }

    @Test
    void testSymbolicTypeParserNoVocabulary() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendSymbolicType()
                .toFormatter();
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("id").get();

        // Formatting should fail because there's no vocabulary to find the symbolic name
        assertThrows(TokenException.class, () -> formatter.format(token));

        // Parsing should fail for the same reason
        assertThrows(TokenParseException.class, () -> formatter.parse("ID"));
    }

    @Test
    void testSymbolicTypeParserForLiteral() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendSymbolicType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);

        // EQ corresponds to the '=' literal, which has a literal name but no symbolic name
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.T__0).text("=").get();

        // Formatting should fail because '=' has no symbolic name
        assertThrows(TokenException.class, () -> formatter.format(token));
    }

    @Test
    void testLiteralTypeParser() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendLiteralType()
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.T__0).get();
        assertEquals("'='", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("'='"));
        assertEquals(ExpressionLexer.T__0, parsed.getType());
    }

    @Test
    void testLiteralTypeParserNoVocabulary() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendLiteralType()
                .toFormatter();
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.T__0).text("=").get();

        // Formatting should fail because there's no vocabulary to find the literal name
        assertThrows(TokenException.class, () -> formatter.format(token));

        // Parsing should fail for the same reason
        assertThrows(TokenParseException.class, () -> formatter.parse("="));
    }

    @Test
    void testLiteralTypeParserForSymbolic() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendLiteralType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);

        // ID has a symbolic name but no literal name
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("id").get();

        // Formatting should fail because 'ID' has no literal name
        assertThrows(TokenException.class, () -> formatter.format(token));
    }

    @Test
    void testTakeLongestSymbolicMatch() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token parsed = formatter.parse("IDENTIFIER");
        assertEquals(2, parsed.getType());
    }

        @Test
    void testTakeLongestLiteralMatch() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteralType()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token parsed = formatter.parse("'=='");
        assertEquals(4, parsed.getType());
    }

    @Test
    void testSymbolicPrinterForEOF() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(Token.EOF).get();
        assertEquals("EOF", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("EOF"));
        assertEquals(Token.EOF, parsed.getType());
    }

    @Test
    void testAntlrFormatterStyle() {
        TokenFormatter antlr = TokenFormatter.ANTLR.withVocabulary(ExpressionLexer.VOCABULARY);
        assertEquals("[@0,0:2='Hello',<-213>,1:0]", antlr.format(TOKEN));
        Token literalToken = Tokens.singleTokenOf().type(2).text("*").index(5).startIndex(1).stopIndex(2).line(6).charPositionInLine(9).get();
        assertEquals("[@5,1:2='*',<'*'>,6:9]", antlr.format(literalToken));
        Token symbolicToken = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("var").index(4).startIndex(1).stopIndex(2).line(6).charPositionInLine(9).get();
        assertEquals("[@4,1:2='var',<ID>,6:9]", antlr.format(symbolicToken));
        assertTrue(Tokens.isStrongEqual(TOKEN, antlr.parse("[@0,0:2='Hello',<-213>,1:0]")));
        assertTrue(Tokens.isStrongEqual(literalToken, antlr.parse("[@5,1:2='*',<'*'>,6:9]")));
        assertTrue(Tokens.isStrongEqual(symbolicToken, antlr.parse("[@4,1:2='var',<ID>,6:9]")));
    }

    @Test
    void testAntlrFormatterEscaping() {
        TokenFormatter antlr = TokenFormatter.ANTLR.withVocabulary(ExpressionLexer.VOCABULARY);
        Token newline = Tokens.singleTokenOf()
                .type(ExpressionLexer.NEWLINE)
                .text("\n")
                .index(1)
                .startIndex(10)
                .stopIndex(10)
                .line(2)
                .charPositionInLine(0)
                .get();
        String formatted = "[@1,10:10='\\n',<NEWLINE>,2:0]";
        assertEquals(formatted, antlr.format(newline));
        assertTrue(Tokens.isStrongEqual(newline, antlr.parse(formatted)));
    }

    @Test
    void testOptionalPresent() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendSymbolicType()
                .startOptional()
                .appendLiteral(":")
                .appendInteger(TokenField.CHANNEL, true)
                .endOptional()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token tokenWithChannel = Tokens.singleTokenOf().type(1).channel(5).get();
        assertEquals("ID:5", formatter.format(tokenWithChannel));
        Token parsed = formatter.parse("ID:5");
        assertEquals(1, parsed.getType());
        assertEquals(5, parsed.getChannel());
    }

    @Test
    void testOptionalAbsent() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendSymbolicType()
                .startOptional()
                .appendLiteral(":")
                .appendInteger(TokenField.CHANNEL, true)
                .endOptional()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token tokenWithoutChannel = Tokens.singleTokenOf().type(1).channel(0).get(); // default channel
        assertEquals("ID", formatter.format(tokenWithoutChannel));
        Token parsed = formatter.parse("ID");
        assertEquals(1, parsed.getType());
        assertEquals(0, parsed.getChannel());
    }

    @Test
    void testStrictInteger() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendInteger(TokenField.CHANNEL, true)
                .toFormatter();
        Token tokenWithDefaultChannel = Tokens.singleTokenOf().channel(0).get();
        assertThrows(TokenException.class, () -> formatter.format(tokenWithDefaultChannel));
        Token tokenWithNonDefaultChannel = Tokens.singleTokenOf().channel(3).get();
        assertEquals("3", formatter.format(tokenWithNonDefaultChannel));
    }

    @Test
    void testAppendPattern() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendPattern("N,B:E,C,P,R")
                .toFormatter();
        Token token = Tokens.singleTokenOf()
                .index(1).startIndex(2).stopIndex(3)
                .channel(4).charPositionInLine(5).line(6)
                .get();
        assertEquals("1,2:3,4,5,6", formatter.format(token));
        Token parsed = formatter.parse("1,2:3,4,5,6");
        assertEquals(1, parsed.getTokenIndex());
        assertEquals(2, parsed.getStartIndex());
        assertEquals(3, parsed.getStopIndex());
        assertEquals(4, parsed.getChannel());
        assertEquals(5, parsed.getCharPositionInLine());
        assertEquals(6, parsed.getLine());
    }

    @Test
    void testPatternStrictVsLenient() {
        TokenFormatter strictFormatter = new TokenFormatterBuilder().appendPattern("n").toFormatter();
        TokenFormatter lenientFormatter = new TokenFormatterBuilder().appendPattern("N").toFormatter();
        Token defaultIndex = Tokens.singleTokenOf().index(-1).get();
        Token nonDefaultIndex = Tokens.singleTokenOf().index(10).get();
        assertThrows(TokenException.class, () -> strictFormatter.format(defaultIndex));
        assertEquals("10", strictFormatter.format(nonDefaultIndex));
        assertEquals("-1", lenientFormatter.format(defaultIndex));
        assertEquals("10", lenientFormatter.format(nonDefaultIndex));
    }

    @Test
    void testPatternQuotedLiteral() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendPattern("%hello%").toFormatter();
        assertEquals("hello", formatter.format(TOKEN));
        assertDoesNotThrow(() -> formatter.parse("hello"));
    }

    @Test
    void testPatternEscapedChar() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendPattern("\\N").toFormatter();
        assertEquals("N", formatter.format(TOKEN));
        assertDoesNotThrow(() -> formatter.parse("N"));
    }

    @Test
    void testPatternOptionalSection() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendPattern("s[:c]")
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token tokenWithChannel = Tokens.singleTokenOf().type(1).channel(5).get();
        assertEquals("ID:5", formatter.format(tokenWithChannel));
        Token parsedWith = formatter.parse("ID:5");
        assertEquals(1, parsedWith.getType());
        assertEquals(5, parsedWith.getChannel());

        Token tokenWithoutChannel = Tokens.singleTokenOf().type(1).channel(0).get(); // default channel
        assertEquals("ID", formatter.format(tokenWithoutChannel));
        Token parsedWithout = formatter.parse("ID");
        assertEquals(1, parsedWithout.getType());
        assertEquals(0, parsedWithout.getChannel());
    }

    @Test
    void testAppendTypeSymbolicFirst() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendType(io.github.airbag.token.format.TypeFormat.SYMBOLIC_FIRST)
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token symbolic = Tokens.singleTokenOf().type(1).get();
        Token literal = Tokens.singleTokenOf().type(3).get();
        assertEquals("ID", formatter.format(symbolic));
        assertEquals("'='", formatter.format(literal));
        assertEquals(1, formatter.parse("ID").getType());
        assertEquals(3, formatter.parse("'='" ).getType());
    }

    @Test
    void testAppendTypeLiteralFirst() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendType(io.github.airbag.token.format.TypeFormat.LITERAL_FIRST)
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token symbolic = Tokens.singleTokenOf().type(1).get();
        Token literal = Tokens.singleTokenOf().type(3).get();
        assertEquals("ID", formatter.format(symbolic));
        assertEquals("'='", formatter.format(literal));
        assertEquals(1, formatter.parse("ID").getType());
        assertEquals(3, formatter.parse("'='" ).getType());
    }

    @Test
    void testAppendTypeIntegerOnly() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendType(io.github.airbag.token.format.TypeFormat.INTEGER_ONLY)
                .toFormatter();
        Token token = Tokens.singleTokenOf().type(123).get();
        assertEquals("123", formatter.format(token));
        assertEquals(123, formatter.parse("123").getType());
    }

    @Test
    void testAppendEOF() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendEOF().toFormatter();
        Token eofToken = Tokens.singleTokenOf().type(Token.EOF).text("<EOF>").get();
        assertEquals("EOF", formatter.format(eofToken));
        Token parsed = formatter.parse("EOF");
        assertEquals(Token.EOF, parsed.getType());
        assertEquals("<EOF>", parsed.getText());

        Token notEofToken = Tokens.singleTokenOf().type(1).get();
        assertThrows(TokenException.class, () -> formatter.format(notEofToken));
    }

    @Test
    void testTextFollowedByOptionals() {
        TokenFormatter formatter = TokenFormatter.ofPattern("N:X[l][s][c];")
                .withVocabulary(VOCABULARY);

        // Token 1: Index and Text only
        Token token1 = Tokens.singleTokenOf().index(10).text("some-text").get();
        String formatted1 = "10:some-text;";
        assertEquals(formatted1, formatter.format(token1));
        Token parsed1 = formatter.parse(formatted1);
        assertEquals(10, parsed1.getTokenIndex());
        assertEquals("some-text", parsed1.getText());
        assertEquals(Token.INVALID_TYPE, parsed1.getType());
        assertEquals(0, parsed1.getChannel());

        // Token 2: With Literal Type, which restricts the text.
        Token token2 = Tokens.singleTokenOf().index(11).text("=").type(3).get();
        String formatted2 = "11:='=';";
        assertEquals(formatted2, formatter.format(token2));
        Token parsed2 = formatter.parse(formatted2);
        assertEquals(11, parsed2.getTokenIndex());
        assertEquals("=", parsed2.getText());
        assertEquals(3, parsed2.getType());
        assertEquals(0, parsed2.getChannel());

        // Token 3: With Symbolic Type (does not restrict text)
        Token token3 = Tokens.singleTokenOf().index(12).text("my_var").type(1).get();
        String formatted3 = "12:my_varID;";
        assertEquals(formatted3, formatter.format(token3));
        Token parsed3 = formatter.parse(formatted3);
        assertEquals(12, parsed3.getTokenIndex());
        assertEquals("my_var", parsed3.getText());
        assertEquals(1, parsed3.getType());
        assertEquals(0, parsed3.getChannel());

        // Token 4: With Channel
        Token token4 = Tokens.singleTokenOf().index(13).text("channeled").channel(5).get();
        String formatted4 = "13:channeled5;";
        assertEquals(formatted4, formatter.format(token4));
        Token parsed4 = formatter.parse(formatted4);
        assertEquals(13, parsed4.getTokenIndex());
        assertEquals("channeled", parsed4.getText());
        assertEquals(Token.INVALID_TYPE, parsed4.getType());
        assertEquals(5, parsed4.getChannel());

        // Token 5: With Symbolic and Channel
        Token token5 = Tokens.singleTokenOf().index(14).text("complex").type(2).channel(8).get();
        String formatted5 = "14:complexIDENTIFIER8;";
        assertEquals(formatted5, formatter.format(token5));
        Token parsed5 = formatter.parse(formatted5);
        assertEquals(14, parsed5.getTokenIndex());
        assertEquals("complex", parsed5.getText());
        assertEquals(2, parsed5.getType());
        assertEquals(8, parsed5.getChannel());
    }

    @Test
    void testConflictingFieldThrowsException() {
        TokenFormatter formatter = TokenFormatter.ofPattern("X[l]")
                .withVocabulary(VOCABULARY);

        // Parsing this string will cause a conflict.
        // 'X' will parse "assign" and set the TEXT field.
        // '[l]' will parse "'='" and attempt to set the TEXT field to "=",
        // which conflicts with the existing value, causing an exception.
        String conflictingString = "assign'='";

        assertThrows(TokenParseException.class, () -> formatter.parse(conflictingString));
    }

}