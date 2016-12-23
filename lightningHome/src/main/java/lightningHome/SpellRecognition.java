package lightningHome;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;

public class SpellRecognition {

	private static final String ACOUSTIC_MODEL = "resource:/edu/cmu/sphinx/models/en-us/en-us";
	private static final String DICTIONARY_PATH = "src/main/resources/spells.dict";
	private static final String GRAMMAR_PATH = "src/main/resources/";
	private static final String GRAMMAR_NAME = "hpgrammar";
	// private static final String LANGUAGE_MODEL = "resource:/edu/cmu/sphinx/demo/dialog/weather.lm";
	public static Configuration getConfiguration() {
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath(ACOUSTIC_MODEL);
		configuration.setDictionaryPath(DICTIONARY_PATH);
		configuration.setGrammarPath(GRAMMAR_PATH);
		configuration.setUseGrammar(true);
		configuration.setGrammarName(GRAMMAR_NAME);
		return configuration;
	}
	/*
	public static void main(String[] args) throws Exception {
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath(ACOUSTIC_MODEL);
		configuration.setDictionaryPath(DICTIONARY_PATH);
		configuration.setGrammarPath(GRAMMAR_PATH);
		configuration.setUseGrammar(true);
		configuration.setGrammarName(GRAMMAR_NAME);
		
		LiveSpeechRecognizer spellRecognizer = new LiveSpeechRecognizer(configuration);

		spellRecognizer.startRecognition(true);
		while (true) {
			String utterance = spellRecognizer.getResult().getHypothesis();
			System.out.println(utterance);
		}
	}*/
}