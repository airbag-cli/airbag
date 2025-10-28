// Generated from QueryLexer.g4 by ANTLR 4.13.2
package io.github.airbag.tree.query;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class QueryLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TOKEN_REF=1, RULE_REF=2, ANYWHERE=3, ROOT=4, WILDCARD=5, BANG=6, INT=7, 
		ID=8, STRING=9;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"ANYWHERE", "ROOT", "WILDCARD", "BANG", "INT", "ID", "NameChar", "NameStartChar", 
			"Digit", "STRING"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, "'//'", "'/'", "'*'", "'!'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TOKEN_REF", "RULE_REF", "ANYWHERE", "ROOT", "WILDCARD", "BANG", 
			"INT", "ID", "STRING"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public QueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "QueryLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\t@\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0003\u0004 \b\u0004\u0001\u0004\u0001\u0004\u0005"+
		"\u0004$\b\u0004\n\u0004\f\u0004\'\t\u0004\u0001\u0005\u0001\u0005\u0005"+
		"\u0005+\b\u0005\n\u0005\f\u0005.\t\u0005\u0001\u0006\u0001\u0006\u0003"+
		"\u00062\b\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001"+
		"\t\u0005\t:\b\t\n\t\f\t=\t\t\u0001\t\u0001\t\u0001;\u0000\n\u0001\u0003"+
		"\u0003\u0004\u0005\u0005\u0007\u0006\t\u0007\u000b\b\r\u0000\u000f\u0000"+
		"\u0011\u0000\u0013\t\u0001\u0000\u0004\u0001\u000019\u0005\u000009__\u00b7"+
		"\u00b7\u0300\u036f\u203f\u2040\r\u0000AZaz\u00c0\u00d6\u00d8\u00f6\u00f8"+
		"\u02ff\u0370\u037d\u037f\u1fff\u200c\u200d\u2070\u218f\u2c00\u2fef\u3001"+
		"\u8000\ud7ff\u8000\uf900\u8000\ufdcf\u8000\ufdf0\u8000\ufffd\u0001\u0000"+
		"09A\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000"+
		"\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000"+
		"\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000"+
		"\u0000\u0013\u0001\u0000\u0000\u0000\u0001\u0015\u0001\u0000\u0000\u0000"+
		"\u0003\u0018\u0001\u0000\u0000\u0000\u0005\u001a\u0001\u0000\u0000\u0000"+
		"\u0007\u001c\u0001\u0000\u0000\u0000\t\u001f\u0001\u0000\u0000\u0000\u000b"+
		"(\u0001\u0000\u0000\u0000\r1\u0001\u0000\u0000\u0000\u000f3\u0001\u0000"+
		"\u0000\u0000\u00115\u0001\u0000\u0000\u0000\u00137\u0001\u0000\u0000\u0000"+
		"\u0015\u0016\u0005/\u0000\u0000\u0016\u0017\u0005/\u0000\u0000\u0017\u0002"+
		"\u0001\u0000\u0000\u0000\u0018\u0019\u0005/\u0000\u0000\u0019\u0004\u0001"+
		"\u0000\u0000\u0000\u001a\u001b\u0005*\u0000\u0000\u001b\u0006\u0001\u0000"+
		"\u0000\u0000\u001c\u001d\u0005!\u0000\u0000\u001d\b\u0001\u0000\u0000"+
		"\u0000\u001e \u0005-\u0000\u0000\u001f\u001e\u0001\u0000\u0000\u0000\u001f"+
		" \u0001\u0000\u0000\u0000 !\u0001\u0000\u0000\u0000!%\u0007\u0000\u0000"+
		"\u0000\"$\u0003\u0011\b\u0000#\"\u0001\u0000\u0000\u0000$\'\u0001\u0000"+
		"\u0000\u0000%#\u0001\u0000\u0000\u0000%&\u0001\u0000\u0000\u0000&\n\u0001"+
		"\u0000\u0000\u0000\'%\u0001\u0000\u0000\u0000(,\u0003\u000f\u0007\u0000"+
		")+\u0003\r\u0006\u0000*)\u0001\u0000\u0000\u0000+.\u0001\u0000\u0000\u0000"+
		",*\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-\f\u0001\u0000\u0000"+
		"\u0000.,\u0001\u0000\u0000\u0000/2\u0003\u000f\u0007\u000002\u0007\u0001"+
		"\u0000\u00001/\u0001\u0000\u0000\u000010\u0001\u0000\u0000\u00002\u000e"+
		"\u0001\u0000\u0000\u000034\u0007\u0002\u0000\u00004\u0010\u0001\u0000"+
		"\u0000\u000056\u0007\u0003\u0000\u00006\u0012\u0001\u0000\u0000\u0000"+
		"7;\u0005\'\u0000\u00008:\t\u0000\u0000\u000098\u0001\u0000\u0000\u0000"+
		":=\u0001\u0000\u0000\u0000;<\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000"+
		"\u0000<>\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000>?\u0005\'\u0000"+
		"\u0000?\u0014\u0001\u0000\u0000\u0000\u0006\u0000\u001f%,1;\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}