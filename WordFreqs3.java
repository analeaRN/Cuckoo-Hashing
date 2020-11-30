import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.exit;

/**
 * Word counter. After loading the file passed through the command line.
 * Users can type any word they would like to get the frequency of.
 * If they would like to delete a word. add a dash to the beginning of
 * the to-be-soon deleted word. (e.g. -bye). To exit, just press enter.
 *
 * This a cuckcoo hashTable
 *
 * All considered different words: word word's words
 *
 * @student Ana-Lea N; class CSCI 361; 11/20/2020
 */
public class WordFreqs3 {

    CuckooHash<String, Integer> wordCount;

    /**
     * @param args takes in the file to fill this word counter with.
     */
    public static void main(String[] args) {
        // File checking
        if (args.length < 1) {
            System.out.println("No input detected. Please enter a file " +
                                "through command-line args...");
            return;
        }

        // init
        WordFreqs3 wf = new WordFreqs3();
        readFile(args[0], wf);

        // Welcome messages
        System.out.printf("This text contains %d distinct words.\n", wf.wordCount.size());
        System.out.println("Please enter a word to get its frequency, or hit enter to leave.");

        // Setting up house for user input
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();

        // Get user input till they enter nothing
        while(!input.isBlank()) {
            input = input.trim();

            if (input.length() > 1 && input.charAt(0) == '-') {
                wf.printDeleteWord(input.substring(1).toLowerCase());
            } else {
                wf.printWordCountOf(input.toLowerCase());
            }
            input = scan.nextLine();
        }

        //goodbye
        scan.close();
        System.out.print("Terminating Program... Goodbye!");
    }

    /**
     * WordFreqs uses a red-black tree to keep the frequency of words.
     * Key holds the word, while Value holds the frequency of that word.
     */
    public WordFreqs3() {
        wordCount = new CuckooHash<>();
    }

    /**
     * Opens file and puts words found in file into WordFreq.
     * @param filePath the file that this wordFreq will be filling.
     *                 Only takes in a text file.
     */
    public static void readFile(String filePath, WordFreqs3 wf) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File does not exist. Please check the name for file: " + file.getName());
            System.out.println("Path: " + file.getAbsolutePath());
            exit(-1);
        } else if (!file.canRead()) {
            System.out.println("No read permission allowed for this file. Please try another file");
            exit(-1);
        }

        // add words to hashTable
        try (Scanner scan = new Scanner(file)) {

            scan.useDelimiter("[\\W\\p{Punct}&&[^'_]]+");
            String token;

            while (scan.hasNext()) {
                token = scan.next().toLowerCase();
                token = remove39(token);
                if (!token.isBlank()) { wf.updateWordCount(token); }
            }

        } catch (Exception e) {
            System.out.println("Check your file");
            System.out.println(e.getMessage());
            e.printStackTrace();
            exit(-1);
        }
    }


    /**
     * Adds the passed through String into this dictionary. If it already
     * exists then update it's word count.
     *
     * @param word the word to add to the wordCount
     */
    public void updateWordCount(String word) {
        if (this.wordCount.contains(word)) {
            int oldCount = this.wordCount.get(word);
            this.wordCount.put(word, oldCount + 1);
        } else {
            this.wordCount.put(word, 1);
        }
    }

    /**
     * Gets the word count of the passed through String, counted from the loaded file.
     *
     * @param getWordCount the word desired to count appearance in text.
     */
    public void printWordCountOf(String getWordCount) {
        if (this.wordCount.contains(getWordCount)) {
            System.out.printf("\"%s\" appears %d times. %n", getWordCount, this.wordCount.get(getWordCount));
        } else {
            System.out.printf("\"%s\" does not appear. %n", getWordCount);
        }
    }

    /**
     * Given a String, delete that word to from this dictionary, if found.
     *
     * @param wordToDelete which word to delete from this dictionary.
     */
    public void printDeleteWord(String wordToDelete) {
        if (this.wordCount.contains(wordToDelete)) {
            this.wordCount.delete(wordToDelete);
            System.out.printf("\"%s\" has been deleted. %n", wordToDelete);
        } else {
            System.out.printf("\"%s\" does not appear. Cannot delete :(. %n", wordToDelete);
        }
    }

    /**
     * This method takes in a String and it returns a string a copy of that String with out
     * any leading or trailing zeros. If there are internal apostrophes,
     * then those are still kept.
     *
     * @param str the desired string to take off leading and trailing apostrophes.
     * @return the string without trailing or leading apostrophes. If
     *          the string is made up of all apostrophes or is blank, then this method
     *          will return an empty string "".
     */
    static public String remove39(String str) {
        final String EMPTY_STRING = "";

        // Check if string is empty or if string is only a one letter.
        if (str == null || str.isBlank()) { return EMPTY_STRING; }
        else if (str.length() == 1) {
            if (str.charAt(0) == 39) { return EMPTY_STRING; }
            else { return str; }
        }

        char[] splitStr = str.toCharArray();

        int sindex = 0;
        int eindex = splitStr.length - 1;

        // remove leading 's
        for (; sindex < splitStr.length; sindex++) {
            if(splitStr[sindex] != 39) {
                break;
            }
        }

        // remove trailing 's
        for (; eindex >= 0 ; eindex--) {
            if(splitStr[eindex] != 39) {
                break;
            } else if (eindex < sindex) {
                return EMPTY_STRING;
            }
        }

        char[] newString = Arrays.copyOfRange(splitStr, sindex, eindex + 1);
        return new String(newString);
    }
}
