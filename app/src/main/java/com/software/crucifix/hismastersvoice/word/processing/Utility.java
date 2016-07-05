package com.software.crucifix.hismastersvoice.word.processing;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Created by MUTTLEY on 27/05/2016.
 */
public class Utility {

    private final static String LOG_TAG = "HisMastersVoice";
    private static final Pattern PATTERN_PUNCTUATION = Pattern.compile("[\\p{Punct}']");
    private static final Pattern PATTERN_ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final char SPACE = ' ';
    private static final String EMPTY = "";
    private static final double PASS_MATCH_SCORE = 0.7;


    /**
     * @param rawText
     * @param rawVoice
     * @return
     */
    public boolean match(final String rawText, final String rawVoice) {

        final String text = cleanseText(rawText);
        final String voice = cleanseText(rawVoice);

        final double score = LetterPairSimilarity.compareStrings(text, voice);

        if (score >= PASS_MATCH_SCORE) {
            return true;
        }

        if (NumberUtils.isNumber(text)) {
            return (LetterPairSimilarity.compareStrings(EnglishNumberToWords.convert(Long.parseLong(text)).trim(), voice) >= PASS_MATCH_SCORE);
        }

        if (StringUtils.containsWhitespace(text)) {
            final String candidateNumber = text.substring(0, text.indexOf(SPACE));
            final String theRemaining = text.substring(text.indexOf(SPACE) + 1);

            if (NumberUtils.isNumber(candidateNumber)) {
                return (LetterPairSimilarity.compareStrings(theRemaining, voice) >= PASS_MATCH_SCORE);
            }
        }

        if (text.length() > voice.length()) {
            if (text.contains(voice)) {
                return true;
            }
        }

        if (text.length() < voice.length()) {
            if (voice.contains(text)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param rawText
     * @return cleansedText
     */
    private String cleanseText(final String rawText) {

        String tempText = rawText.trim().toLowerCase();

        tempText = stripAccents(tempText);
        tempText = PATTERN_PUNCTUATION.matcher(tempText).replaceAll(EMPTY);

        return tempText;
    }

    /**
     * @param rawText
     * @return cleansedText
     */
    public String cleanseText(final String rawText, final String replaceWith) {

        String tempText = rawText.trim().toLowerCase();

        tempText = stripAccents(tempText);
        tempText = PATTERN_PUNCTUATION.matcher(tempText).replaceAll(replaceWith);

        return tempText;
    }

    /**
     * <p>Removes diacritics (~= accents) from a string. The case will not be altered.</p>
     * <p>For instance, '&agrave;' will be replaced by 'a'.</p>
     * <p>Note that ligatures will be left as is.</p>
     * <p>
     * <pre>
     * StringUtils.stripAccents(null)                = null
     * StringUtils.stripAccents("")                  = ""
     * StringUtils.stripAccents("control")           = "control"
     * StringUtils.stripAccents("&eacute;clair")     = "eclair"
     * </pre>
     *
     * @param input String to be stripped
     * @return input text with diacritics removed
     * @since 3.0
     */
    // See also Lucene's ASCIIFoldingFilter (Lucene 2.9) that replaces accented characters by their unaccented equivalent (and uncommitted bug fix: https://issues.apache.org/jira/browse/LUCENE-1343?focusedCommentId=12858907&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_12858907).
    public static String stripAccents(final String input) {

        if (input == null) {
            return null;
        }

        final String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);

        return PATTERN_ACCENTS.matcher(decomposed).replaceAll(EMPTY);
    }
}