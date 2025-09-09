package io.github.airbag;

import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.symbol.*;
import org.antlr.v4.runtime.Vocabulary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolFormatterTest {

    private static final Symbol TOKEN = Symbol.of()
            .index(0)
            .start(0)
            .stop(2)
            .text("Hello")
            .type(-213)
            .line(1)
            .position(0)
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
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX)
                .toFormatter();
        assertEquals("0", formatter.format(TOKEN));
        var equalizer = SymbolField.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("0")));
    }

    @Test
    void testNegativeInteger() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE)
                .toFormatter();
        assertEquals("-213", formatter.format(TOKEN));
        var equalizer = SymbolField.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("-213")));
    }

    @Test
    void invalidNegative() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE)
                .toFormatter();
        assertThrows(SymbolParseException.class, () -> formatter.parse("-"));
    }

    @Test
    void multipleDashes() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE)
                .toFormatter();
        assertThrows(SymbolParseException.class, () -> formatter.parse("--10"));
    }

    @Test
    void testCharacterParserPrinter() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("(")
                .appendInteger(SymbolField.INDEX)
                .appendLiteral(":")
                .appendInteger(SymbolField.TYPE)
                .appendLiteral(")")
                .toFormatter();

        assertEquals("(0:-213)", formatter.format(TOKEN));
        Symbol parsed = formatter.parse("(0:-213)");
        var equalizer = SymbolField.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, parsed));
    }

    @Test
    void testInvalidLiteral() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("(")
                .appendInteger(SymbolField.INDEX)
                .appendLiteral(":")
                .appendInteger(SymbolField.TYPE)
                .appendLiteral(")")
                .toFormatter();
        assertThrows(SymbolParseException.class, () -> formatter.parse("(0;-10)"));
    }

    @Test
    void testTextParserWithDelimiter() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("(")
                .appendInteger(SymbolField.TYPE)
                .appendLiteral(":")
                .appendText()
                .appendLiteral(":")
                .appendInteger(SymbolField.INDEX)
                .appendLiteral(")")
                .toFormatter();
        assertEquals("(-213:Hello:0)", formatter.format(TOKEN));
        var equalizer = SymbolField.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("(-213:Hello:0)")));
    }

    @Test
    void testTextParserWithoutDelimiter() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendInteger(SymbolField.TYPE)
                .appendLiteral(":")
                .appendText().toFormatter();
        assertEquals("-213:Hello", formatter.format(TOKEN));
        var equalizer = SymbolField.equalizer(formatter.getFields());
        assertTrue(equalizer.test(TOKEN, formatter.parse("-213:Hello")));
    }

    @Test
    void testSymbolicTypeParser() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("(")
                .appendSymbolicType()
                .appendLiteral(":")
                .appendText()
                .appendLiteral(")")
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Symbol token = Symbol.of().type(ExpressionLexer.ID).text("number").get();
        assertEquals("(ID:number)", formatter.format(token));
        Symbol parsed = assertDoesNotThrow(() -> formatter.parse("(ID:number)"));
        assertEquals(ExpressionLexer.ID, parsed.type());
        assertEquals("number", parsed.text());
    }

    @Test
    void testOnlySymbolic() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);
        Symbol token = Symbol.of().type(ExpressionLexer.ID).text("number").get();
        assertEquals("ID", formatter.format(token));
        Symbol parsed = assertDoesNotThrow(() -> formatter.parse("ID"));
        assertEquals(ExpressionLexer.ID, parsed.type());
    }

    @Test
    void testSymbolicTypeParserNoVocabulary() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendSymbolicType()
                .toFormatter();
        Symbol token = Symbol.of().type(ExpressionLexer.ID).text("id").get();

        // Formatting should fail because there's no vocabulary to find the symbolic name
        assertThrows(SymbolException.class, () -> formatter.format(token));

        // Parsing should fail for the same reason
        assertThrows(SymbolParseException.class, () -> formatter.parse("ID"));
    }

    @Test
    void testSymbolicTypeParserForLiteral() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendSymbolicType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);

        // EQ corresponds to the '=' literal, which has a literal name but no symbolic name
        Symbol token = Symbol.of().type(ExpressionLexer.T__0).text("=").get();

        // Formatting should fail because '=' has no symbolic name
        assertThrows(SymbolException.class, () -> formatter.format(token));
    }

    @Test
    void testLiteralTypeParser() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendLiteralType()
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Symbol token = Symbol.of().type(ExpressionLexer.T__0).get();
        assertEquals("'='", formatter.format(token));
        Symbol parsed = assertDoesNotThrow(() -> formatter.parse("'='"));
        assertEquals(ExpressionLexer.T__0, parsed.type());
    }

    @Test
    void testLiteralTypeParserNoVocabulary() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendLiteralType()
                .toFormatter();
        Symbol token = Symbol.of().type(ExpressionLexer.T__0).text("=").get();

        // Formatting should fail because there's no vocabulary to find the literal name
        assertThrows(SymbolException.class, () -> formatter.format(token));

        // Parsing should fail for the same reason
        assertThrows(SymbolParseException.class, () -> formatter.parse("="));
    }

    @Test
    void testLiteralTypeParserForSymbolic() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendLiteralType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);

        // ID has a symbolic name but no literal name
        Symbol token = Symbol.of().type(ExpressionLexer.ID).text("id").get();

        // Formatting should fail because 'ID' has no literal name
        assertThrows(SymbolException.class, () -> formatter.format(token));
    }

    @Test
    void testTakeLongestSymbolicMatch() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Symbol parsed = formatter.parse("IDENTIFIER");
        assertEquals(2, parsed.type());
    }

        @Test
    void testTakeLongestLiteralMatch() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteralType()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Symbol parsed = formatter.parse("'=='");
        assertEquals(4, parsed.type());
    }

    @Test
    void testSymbolicPrinterForEOF() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Symbol token = Symbol.of().type(Symbol.EOF).get();
        assertEquals("EOF", formatter.format(token));
        Symbol parsed = assertDoesNotThrow(() -> formatter.parse("EOF"));
        assertEquals(Symbol.EOF, parsed.type());
    }

//TODO
//    @Test
//    void testAntlrFormatterStyle() {
//        SymbolFormatter antlr = SymbolFormatter.ANTLR.withVocabulary(ExpressionLexer.VOCABULARY);
//        assertEquals("[@0,0:2='Hello',<-213>,1:0]", antlr.format(TOKEN));
//        Symbol literalSymbol = Symbol.of().type(2).text("*").index(5).startIndex(1).stopIndex(2).line(6).charPositionInLine(9).get();
//        assertEquals("[@5,1:2='*',<'*'>,6:9]", antlr.format(literalSymbol));
//        Symbol symbolicSymbol = Symbol.of().type(ExpressionLexer.ID).text("var").index(4).startIndex(1).stopIndex(2).line(6).charPositionInLine(9).get();
//        assertEquals("[@4,1:2='var',<ID>,6:9]", antlr.format(symbolicSymbol));
//        assertTrue(Symbols.isStrongEqual(TOKEN, antlr.parse("[@0,0:2='Hello',<-213>,1:0]")));
//        assertTrue(Symbols.isStrongEqual(literalSymbol, antlr.parse("[@5,1:2='*',<'*'>,6:9]")));
//        assertTrue(Symbols.isStrongEqual(symbolicSymbol, antlr.parse("[@4,1:2='var',<ID>,6:9]")));
//    }

//    @Test
//    void testAntlrFormatterEscaping() {
//        SymbolFormatter antlr = SymbolFormatter.ANTLR.withVocabulary(ExpressionLexer.VOCABULARY);
//        Symbol newline = Symbol.of()
//                .type(ExpressionLexer.NEWLINE)
//                .text("\n")
//                .index(1)
//                .start(10)
//                .stop(10)
//                .line(2)
//                .position(0)
//                .get();
//        String formatted = "[@1,10:10='\\n',<NEWLINE>,2:0]";
//        assertEquals(formatted, antlr.format(newline));
//        assertTrue(Symbols.isStrongEqual(newline, antlr.parse(formatted)));
//    }

    @Test
    void testOptionalPresent() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendSymbolicType()
                .startOptional()
                .appendLiteral(":")
                .appendInteger(SymbolField.CHANNEL, true)
                .endOptional()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Symbol tokenWithChannel = Symbol.of().type(1).channel(5).get();
        assertEquals("ID:5", formatter.format(tokenWithChannel));
        Symbol parsed = formatter.parse("ID:5");
        assertEquals(1, parsed.type());
        assertEquals(5, parsed.channel());
    }

    @Test
    void testOptionalAbsent() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendSymbolicType()
                .startOptional()
                .appendLiteral(":")
                .appendInteger(SymbolField.CHANNEL, true)
                .endOptional()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Symbol tokenWithoutChannel = Symbol.of().type(1).channel(0).get(); // default channel
        assertEquals("ID", formatter.format(tokenWithoutChannel));
        Symbol parsed = formatter.parse("ID");
        assertEquals(1, parsed.type());
        assertEquals(0, parsed.channel());
    }

    @Test
    void testStrictInteger() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendInteger(SymbolField.CHANNEL, true)
                .toFormatter();
        Symbol tokenWithDefaultChannel = Symbol.of().channel(0).get();
        assertThrows(SymbolException.class, () -> formatter.format(tokenWithDefaultChannel));
        Symbol tokenWithNonDefaultChannel = Symbol.of().channel(3).get();
        assertEquals("3", formatter.format(tokenWithNonDefaultChannel));
    }

    @Test
    void testAppendPattern() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendPattern("N,B:E,C,P,R")
                .toFormatter();
        Symbol token = Symbol.of()
                .index(1).start(2).stop(3)
                .channel(4).position(5).line(6)
                .get();
        assertEquals("1,2:3,4,5,6", formatter.format(token));
        Symbol parsed = formatter.parse("1,2:3,4,5,6");
        assertEquals(1, parsed.index());
        assertEquals(2, parsed.start());
        assertEquals(3, parsed.stop());
        assertEquals(4, parsed.channel());
        assertEquals(5, parsed.position());
        assertEquals(6, parsed.line());
    }

    @Test
    void testPatternStrictVsLenient() {
        SymbolFormatter strictFormatter = new SymbolFormatterBuilder().appendPattern("n").toFormatter();
        SymbolFormatter lenientFormatter = new SymbolFormatterBuilder().appendPattern("N").toFormatter();
        Symbol defaultIndex = Symbol.of().index(-1).get();
        Symbol nonDefaultIndex = Symbol.of().index(10).get();
        assertThrows(SymbolException.class, () -> strictFormatter.format(defaultIndex));
        assertEquals("10", strictFormatter.format(nonDefaultIndex));
        assertEquals("-1", lenientFormatter.format(defaultIndex));
        assertEquals("10", lenientFormatter.format(nonDefaultIndex));
    }

    @Test
    void testPatternQuotedLiteral() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendPattern("%hello%").toFormatter();
        assertEquals("hello", formatter.format(TOKEN));
        assertDoesNotThrow(() -> formatter.parse("hello"));
    }

    @Test
    void testPatternEscapedChar() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendPattern("\\N").toFormatter();
        assertEquals("N", formatter.format(TOKEN));
        assertDoesNotThrow(() -> formatter.parse("N"));
    }

    @Test
    void testPatternOptionalSection() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendPattern("s[:c]")
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Symbol tokenWithChannel = Symbol.of().type(1).channel(5).get();
        assertEquals("ID:5", formatter.format(tokenWithChannel));
        Symbol parsedWith = formatter.parse("ID:5");
        assertEquals(1, parsedWith.type());
        assertEquals(5, parsedWith.channel());

        Symbol tokenWithoutChannel = Symbol.of().type(1).channel(0).get(); // default channel
        assertEquals("ID", formatter.format(tokenWithoutChannel));
        Symbol parsedWithout = formatter.parse("ID");
        assertEquals(1, parsedWithout.type());
        assertEquals(0, parsedWithout.channel());
    }

    @Test
    void testAppendTypeSymbolicFirst() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendType(TypeFormat.SYMBOLIC_FIRST)
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Symbol symbolic = Symbol.of().type(1).get();
        Symbol literal = Symbol.of().type(3).get();
        assertEquals("ID", formatter.format(symbolic));
        assertEquals("'='", formatter.format(literal));
        assertEquals(1, formatter.parse("ID").type());
        assertEquals(3, formatter.parse("'='" ).type());
    }

    @Test
    void testAppendTypeLiteralFirst() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendType(TypeFormat.LITERAL_FIRST)
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Symbol symbolic = Symbol.of().type(1).get();
        Symbol literal = Symbol.of().type(3).get();
        assertEquals("ID", formatter.format(symbolic));
        assertEquals("'='", formatter.format(literal));
        assertEquals(1, formatter.parse("ID").type());
        assertEquals(3, formatter.parse("'='" ).type());
    }

    @Test
    void testAppendTypeIntegerOnly() {
        SymbolFormatter formatter = new SymbolFormatterBuilder()
                .appendType(TypeFormat.INTEGER_ONLY)
                .toFormatter();
        Symbol token = Symbol.of().type(123).get();
        assertEquals("123", formatter.format(token));
        assertEquals(123, formatter.parse("123").type());
    }

    @Test
    void testAppendEOF() {
        SymbolFormatter formatter = new SymbolFormatterBuilder().appendEOF().toFormatter();
        Symbol eofSymbol = Symbol.of().type(Symbol.EOF).text("<EOF>").get();
        assertEquals("EOF", formatter.format(eofSymbol));
        Symbol parsed = formatter.parse("EOF");
        assertEquals(Symbol.EOF, parsed.type());
        assertEquals("<EOF>", parsed.text());

        Symbol notEofSymbol = Symbol.of().type(1).get();
        assertThrows(SymbolException.class, () -> formatter.format(notEofSymbol));
    }

    @Test
    void testTextFollowedByOptionals() {
        SymbolFormatter formatter = SymbolFormatter.ofPattern("N:X[l][s][c];")
                .withVocabulary(VOCABULARY);

        // Symbol 1: Index and Text only
        Symbol token1 = Symbol.of().index(10).text("some-text").get();
        String formatted1 = "10:some-text;";
        assertEquals(formatted1, formatter.format(token1));
        Symbol parsed1 = formatter.parse(formatted1);
        assertEquals(10, parsed1.index());
        assertEquals("some-text", parsed1.text());
        assertEquals(Symbol.INVALID_TYPE, parsed1.type());
        assertEquals(0, parsed1.channel());

        // Symbol 2: With Literal Type, which restricts the text.
        Symbol token2 = Symbol.of().index(11).text("=").type(3).get();
        String formatted2 = "11:='=';";
        assertEquals(formatted2, formatter.format(token2));
        Symbol parsed2 = formatter.parse(formatted2);
        assertEquals(11, parsed2.index());
        assertEquals("=", parsed2.text());
        assertEquals(3, parsed2.type());
        assertEquals(0, parsed2.channel());

        // Symbol 3: With Symbolic Type (does not restrict text)
        Symbol token3 = Symbol.of().index(12).text("my_var").type(1).get();
        String formatted3 = "12:my_varID;";
        assertEquals(formatted3, formatter.format(token3));
        Symbol parsed3 = formatter.parse(formatted3);
        assertEquals(12, parsed3.index());
        assertEquals("my_var", parsed3.text());
        assertEquals(1, parsed3.type());
        assertEquals(0, parsed3.channel());

        // Symbol 4: With Channel
        Symbol token4 = Symbol.of().index(13).text("channeled").channel(5).get();
        String formatted4 = "13:channeled5;";
        assertEquals(formatted4, formatter.format(token4));
        Symbol parsed4 = formatter.parse(formatted4);
        assertEquals(13, parsed4.index());
        assertEquals("channeled", parsed4.text());
        assertEquals(Symbol.INVALID_TYPE, parsed4.type());
        assertEquals(5, parsed4.channel());

        // Symbol 5: With Symbolic and Channel
        Symbol token5 = Symbol.of().index(14).text("complex").type(2).channel(8).get();
        String formatted5 = "14:complexIDENTIFIER8;";
        assertEquals(formatted5, formatter.format(token5));
        Symbol parsed5 = formatter.parse(formatted5);
        assertEquals(14, parsed5.index());
        assertEquals("complex", parsed5.text());
        assertEquals(2, parsed5.type());
        assertEquals(8, parsed5.channel());
    }

    @Test
    void testConflictingFieldThrowsException() {
        SymbolFormatter formatter = SymbolFormatter.ofPattern("X[l]")
                .withVocabulary(VOCABULARY);

        // Parsing this string will cause a conflict.
        // 'X' will parse "assign" and set the TEXT field.
        // '[l]' will parse "'='" and attempt to set the TEXT field to "=",
        // which conflicts with the existing value, causing an exception.
        String conflictingString = "assign'='";

        assertThrows(SymbolParseException.class, () -> formatter.parse(conflictingString));
    }

}