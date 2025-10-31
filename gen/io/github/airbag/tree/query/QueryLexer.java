// Generated from /home/agoss/Code/airbag/src/main/java/io/github/airbag/tree/query/QueryLexer.g4 by ANTLR 4.13.2
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
		TOKEN=1, RULE=2, TYPE=3, INDEX=4, ANYWHERE=5, ROOT=6, WILDCARD=7, BANG=8, 
		INT=9, ID=10, STRING=11;
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
			null, null, null, null, null, "'//'", "'/'", "'*'", "'!'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TOKEN", "RULE", "TYPE", "INDEX", "ANYWHERE", "ROOT", "WILDCARD", 
			"BANG", "INT", "ID", "STRING"
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

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 4:
			INT_action((RuleContext)_localctx, actionIndex);
			break;
		case 5:
			ID_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void INT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:

			    if ( _input.LA(1) == EOF ) {
			        setType(TYPE);
			    } else {
			        setType(INDEX);
			    }

			break;
		}
	}
	private void ID_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1:

			    if (Character.isUpperCase(getText().charAt(0))) {
			        setType(TOKEN);
			    } else {
			        setType(RULE);
			    }

			break;
		}
	}

	public static final String _serializedATN =
		"\u0004\u0000\u000bD\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0003\u0004 \b\u0004\u0001\u0004\u0001\u0004\u0005"+
		"\u0004$\b\u0004\n\u0004\f\u0004\'\t\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0005\u0005-\b\u0005\n\u0005\f\u00050\t\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0003\u00066\b\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0005\t>\b\t\n\t\f"+
		"\tA\t\t\u0001\t\u0001\t\u0001?\u0000\n\u0001\u0005\u0003\u0006\u0005\u0007"+
		"\u0007\b\t\t\u000b\n\r\u0000\u000f\u0000\u0011\u0000\u0013\u000b\u0001"+
		"\u0000\u0004\u0001\u000019\u0005\u000009__\u00b7\u00b7\u0300\u036f\u203f"+
		"\u2040\r\u0000AZaz\u00c0\u00d6\u00d8\u00f6\u00f8\u02ff\u0370\u037d\u037f"+
		"\u1fff\u200c\u200d\u2070\u218f\u2c00\u2fef\u3001\u8000\ud7ff\u8000\uf900"+
		"\u8000\ufdcf\u8000\ufdf0\u8000\ufffd\u0001\u000009E\u0000\u0001\u0001"+
		"\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001"+
		"\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000"+
		"\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000"+
		"\u0000\u0000\u0001\u0015\u0001\u0000\u0000\u0000\u0003\u0018\u0001\u0000"+
		"\u0000\u0000\u0005\u001a\u0001\u0000\u0000\u0000\u0007\u001c\u0001\u0000"+
		"\u0000\u0000\t\u001f\u0001\u0000\u0000\u0000\u000b*\u0001\u0000\u0000"+
		"\u0000\r5\u0001\u0000\u0000\u0000\u000f7\u0001\u0000\u0000\u0000\u0011"+
		"9\u0001\u0000\u0000\u0000\u0013;\u0001\u0000\u0000\u0000\u0015\u0016\u0005"+
		"/\u0000\u0000\u0016\u0017\u0005/\u0000\u0000\u0017\u0002\u0001\u0000\u0000"+
		"\u0000\u0018\u0019\u0005/\u0000\u0000\u0019\u0004\u0001\u0000\u0000\u0000"+
		"\u001a\u001b\u0005*\u0000\u0000\u001b\u0006\u0001\u0000\u0000\u0000\u001c"+
		"\u001d\u0005!\u0000\u0000\u001d\b\u0001\u0000\u0000\u0000\u001e \u0005"+
		"-\u0000\u0000\u001f\u001e\u0001\u0000\u0000\u0000\u001f \u0001\u0000\u0000"+
		"\u0000 !\u0001\u0000\u0000\u0000!%\u0007\u0000\u0000\u0000\"$\u0003\u0011"+
		"\b\u0000#\"\u0001\u0000\u0000\u0000$\'\u0001\u0000\u0000\u0000%#\u0001"+
		"\u0000\u0000\u0000%&\u0001\u0000\u0000\u0000&(\u0001\u0000\u0000\u0000"+
		"\'%\u0001\u0000\u0000\u0000()\u0006\u0004\u0000\u0000)\n\u0001\u0000\u0000"+
		"\u0000*.\u0003\u000f\u0007\u0000+-\u0003\r\u0006\u0000,+\u0001\u0000\u0000"+
		"\u0000-0\u0001\u0000\u0000\u0000.,\u0001\u0000\u0000\u0000./\u0001\u0000"+
		"\u0000\u0000/1\u0001\u0000\u0000\u00000.\u0001\u0000\u0000\u000012\u0006"+
		"\u0005\u0001\u00002\f\u0001\u0000\u0000\u000036\u0003\u000f\u0007\u0000"+
		"46\u0007\u0001\u0000\u000053\u0001\u0000\u0000\u000054\u0001\u0000\u0000"+
		"\u00006\u000e\u0001\u0000\u0000\u000078\u0007\u0002\u0000\u00008\u0010"+
		"\u0001\u0000\u0000\u00009:\u0007\u0003\u0000\u0000:\u0012\u0001\u0000"+
		"\u0000\u0000;?\u0005\'\u0000\u0000<>\t\u0000\u0000\u0000=<\u0001\u0000"+
		"\u0000\u0000>A\u0001\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000?=\u0001"+
		"\u0000\u0000\u0000@B\u0001\u0000\u0000\u0000A?\u0001\u0000\u0000\u0000"+
		"BC\u0005\'\u0000\u0000C\u0014\u0001\u0000\u0000\u0000\u0006\u0000\u001f"+
		"%.5?\u0002\u0001\u0004\u0000\u0001\u0005\u0001";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}