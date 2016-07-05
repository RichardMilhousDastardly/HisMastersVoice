package com.software.crucifix.hismastersvoice.word.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Dr. Simon White
 */
public class LetterPairSimilarity {

    /**
     * This public method computes the character pairs from the words of each of
     * the two input strings, then iterates through the ArrayLists to find the
     * size of the intersection. Note that whenever a match is found, that
     * character pair is removed from the second array list to prevent us from
     * matching against the same character pair multiple times. (Otherwise,
     * 'GGGGG' would score a perfect match against 'GG'.)
     *
     * @param compare
     * @param with
     * @return double
     */
    public static double compareStrings(final String compare, final String with) {

        final List<String> firstPair = wordLetterPairs(compare.toUpperCase());
        final List<String> secondPair = wordLetterPairs(with.toUpperCase());

        int intersection = 0;
        final int union = firstPair.size() + secondPair.size();

        for (int outerIndex = 0; outerIndex < firstPair.size(); outerIndex++) {
            final String primaryPair = firstPair.get(outerIndex);
            for (int innerIndex = 0; innerIndex < secondPair.size(); innerIndex++) {

                final String secondaryPair = secondPair.get(innerIndex);
                if (primaryPair.equals(secondaryPair)) {
                    intersection++;
                    secondPair.remove(innerIndex);
                    break;
                }
            }
        }

        return (2.0 * intersection) / union;
    }

    /**
     * This method uses the split() method of the String class to split the
     * input string into separate words, or tokens. It then iterates through
     * each of the words, computing the character pairs for each word. The
     * character pairs are added to an ArrayList, which is returned from the
     * method. An ArrayList is used, rather than an array, because we do not
     * know in advance how many character pairs will be returned. (At this
     * point, the program doesn?t know how much white space the input string
     * contains).
     *
     * @param text
     * @return List<String>
     */
    private static List<String> wordLetterPairs(final String text) {

        final List<String> allPairs = new ArrayList<>();
        final StringTokenizer stringTokenizer = new StringTokenizer(text.trim());

        // For each word
        while (stringTokenizer.hasMoreElements()) {
            // Find the pairs of characters

            final String[] pairsInWord = letterPairs(stringTokenizer.nextToken());

            for (int p = 0; p < pairsInWord.length; p++) {
                allPairs.add(pairsInWord[p]);
            }
        }
        return allPairs;
    }

    /**
     * The basis of the algorithm is the method that computes the pairs of
     * characters contained in the input string. This method creates an array of
     * Strings to contain its result. It then iterates through the input string,
     * to extract character pairs and store them in the array. Finally, the
     * array is returned.
     *
     * @param word
     * @return String[]
     */
    private static String[] letterPairs(final String word) {

        final int numPairs = word.length() - 1;

        final String[] pairs = new String[numPairs];

        for (int i = 0; i < numPairs; i++) {
            pairs[i] = word.substring(i, i + 2);
        }

        return pairs;
    }
}