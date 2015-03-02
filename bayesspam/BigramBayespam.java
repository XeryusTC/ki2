import java.io.*;
import java.util.*;

public class BigramBayespam {
    // This defines the two types of messages we have.
    static enum MessageType {
        NORMAL,
        SPAM
    }

    // This a class with two counters (for regular and for spam)
    static class Multiple_Counter {
        int counter_spam = 0;
        int counter_regular = 0;
        float Pregular = 0;
        float Pspam = 0;

        // Increase one of the counters by one
        public void incrementCounter(MessageType type) {
            if (type == MessageType.NORMAL) {
                ++counter_regular;
            } else {
                ++counter_spam;
            }
        }
    }

    // Listings of the two subdirectories (regular/ and spam/)
    private static File[] listing_regular = new File[0];
    private static File[] listing_spam = new File[0];

    // A hash table for the vocabulary (word searching is very fast in a hash
    // table)
    private static Hashtable<String, Multiple_Counter> vocab =
        new Hashtable<String, Multiple_Counter>();

    private static float Pregular, Pspam;

    // Add a word to the vocabulary
    private static void addWord(String word, MessageType type) {
        Multiple_Counter counter = new Multiple_Counter();

        // if word exists already in the vocabulary..
        if (vocab.containsKey(word)) {
            // get the counter from the hashtable
            counter = vocab.get(word);
        }
        // increase the counter appropriately
        counter.incrementCounter(type);

        // put the word with its counter into the hashtable
        vocab.put(word, counter);
    }

    // List the regular and spam messages
    private static void listDirs(File dir_location) {
        // List all files in the directory passed
        File[] dir_listing = dir_location.listFiles();

        // Check that there are 2 subdirectories
        if (dir_listing.length != 2) {
            System.out.println(
                "- Error: specified directory does not contain " +
                "two subdirectories.\n");
            Runtime.getRuntime().exit(0);
        }

        listing_regular = dir_listing[0].listFiles();
        listing_spam = dir_listing[1].listFiles();
    }

    // Print the current content of the vocabulary
    private static void printVocab() {
        Multiple_Counter counter = new Multiple_Counter();

        for (Enumeration<String> e = vocab.keys(); e.hasMoreElements();) {
            String word;

            word = e.nextElement();
            counter = vocab.get(word);

            System.out.println(word + " | in regular: " +
                               counter.counter_regular + " in spam: " +
                               counter.counter_spam);
        }
    }

    /// Read a file to a stream of tokens
    private static String[] readFile(File f) throws IOException {
        HashSet<String> ret = new HashSet<String>();
        FileInputStream in = new FileInputStream(f);
        BufferedReader buf = new BufferedReader(new InputStreamReader(in));
        String line, word, prevWord = "";

        while ((line = buf.readLine()) != null) {
            StringTokenizer tok = new StringTokenizer(line);
            while (tok.hasMoreTokens()) {
                word =
                    tok.nextToken().replaceAll("[^a-zA-Z]", "").toLowerCase();
                if (word.length() >= 4) {
                    if (prevWord != "") {
                        ret.add(prevWord + " " + word);
                    }
                    prevWord = word;
                } else {
					prevWord = "";
				}
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    // Read the words from messages and add them to your vocabulary. The boolean
    // type determines whether the messages are regular or not
    private static void readMessages(MessageType type) throws IOException {
        File[] messages = new File[0];

        if (type == MessageType.NORMAL) {
            messages = listing_regular;
        } else {
            messages = listing_spam;
        }

        for (int i = 0; i < messages.length; ++i) {
            FileInputStream i_s = new FileInputStream(messages[i]);
            BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
            String line;
            String word, prevWord = "";

            // read a line
            while ((line = in.readLine()) != null) {
                // parse it into words
                StringTokenizer st = new StringTokenizer(line);

                // while there are stille words left..
                while (st.hasMoreTokens()) {
                    word = st.nextToken();
                    word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    // add them to the vocabulary
                    if (word.length() >= 4) {
                        if (prevWord != "") {
                            addWord(prevWord + " " + word, type);
                        }
                        prevWord = word;
                    } else {
						prevWord = "";
					}
                }
            }

            in.close();
        }
    }

    public static void main(String[] args) throws IOException {
        float epsilon = 1f;
        // Location of the directory (the path) taken from the cmd line (first
        // arg)
        File dir_location = new File(args[0]);

        // Check if the cmd line arg is a directory
        if (!dir_location.isDirectory()) {
            System.out.println("- Error: cmd line arg not a directory.\n");
            Runtime.getRuntime().exit(0);
        }

        // Initialize the regular and spam lists
        listDirs(dir_location);

        // Read the e-mail messages
        readMessages(MessageType.NORMAL);
        readMessages(MessageType.SPAM);

        // Print out the hash table
        /// printVocab();

        /// Remove bigrams that occur only a couple of times
        Hashtable<String, Multiple_Counter> tmp =
            new Hashtable<String, Multiple_Counter>();
        for (String it : vocab.keySet()) {
            Multiple_Counter bigram = vocab.get(it);
            if (bigram.counter_regular + bigram.counter_spam >= 2) {
                tmp.put(it, bigram);
            }
        }
        vocab = tmp;
        // Now all students must continue from here:
        //
        // 1) A priori class probabilities must be computed from the number of
        // regular and spam messages
        /// Count total words
        int nMessagesRegular, nMessagesSpam, nMessagesTotal, nWordsRegular = 0,
                                                             nWordsSpam = 0;

        nMessagesRegular = listing_regular.length;
        nMessagesSpam = listing_spam.length;
        nMessagesTotal = nMessagesRegular + nMessagesSpam;

        Pregular = (float)Math.log10(nMessagesRegular / (float)nMessagesTotal);
        Pspam = (float)Math.log10(nMessagesSpam / (float)nMessagesTotal);

        System.out.println("P(regular):\t" + Pregular + "\nP(spam):\t" + Pspam);

        for (String it : vocab.keySet()) {
            Multiple_Counter word = vocab.get(it);
            nWordsRegular += word.counter_regular;
            nWordsSpam += word.counter_spam;
        }

        for (String it : vocab.keySet()) {
            Multiple_Counter word = vocab.get(it);
            /// Calculate probability of word being in regular
            if (word.counter_regular > 0) {
                word.Pregular = (float)Math.log10(word.counter_regular /
                                                  (float)nWordsRegular);
            } else {
                word.Pregular = (float)Math.log10(
                    epsilon / (float)(nWordsRegular + nWordsSpam));
            }
            /// Calculate probability of word being in spam
            if (word.counter_spam > 0) {
                word.Pspam =
                    (float)Math.log10(word.counter_spam / (float)nWordsSpam);
            } else {
                word.Pspam = (float)Math.log10(
                    epsilon / (float)(nWordsRegular + nWordsSpam));
            }
        }

        // 2) The vocabulary must be clean: punctuation and digits must be
        // removed, case insensitive
        /// See readMessages()
        // 3) Conditional probabilities must be computed for every word
        // 4) A priori probabilities must be computed for every word
        // 5) Zero probabilities must be replaced by a small estimated value
        // 6) Bayes rule must be applied on new messages, followed by argmax
        // classification
        /// Start reading the test data
        int correctRegular = 0, falseRegular = 0, correctSpam = 0,
            falseSpam = 0;
        File test = new File(args[1]);
        if (!test.isDirectory()) {
            System.out.println("-Error: " + args[1] + " not a directory.\n");
            Runtime.getRuntime().exit(0);
        }

        listDirs(test);

        float allMsg = listing_regular.length + listing_spam.length;
        for (File f : listing_regular) {
            /// Initialize Pregmsg and Pspammsg to Pregular/Pspam + alpha
            float Pregmsg = Pregular + (1 / allMsg),
                  Pspammsg = Pspam + (1 / allMsg);
            for (String str : readFile(f)) {
                if (vocab.containsKey(str)) {
                    Pregmsg += vocab.get(str).Pregular;
                    Pspammsg += vocab.get(str).Pspam;
                }
            }
            if (Pregmsg > Pspammsg) {
                correctRegular++;
            } else {
                falseSpam++;
            }
        }

        for (File f : listing_spam) {
            /// Initialize Pregmsg and Pspammsg to Pregular/Pspam + alpha
            float Pregmsg = Pregular + (1 / allMsg),
                  Pspammsg = Pspam + (1 / allMsg);
            for (String str : readFile(f)) {
                if (vocab.containsKey(str)) {
                    Pregmsg += vocab.get(str).Pregular;
                    Pspammsg += vocab.get(str).Pspam;
                }
            }
            if (Pregmsg > Pspammsg) {
                falseRegular++;
            } else {
                correctSpam++;
            }
        }

        float car = correctRegular / (float)allMsg;
        float crr = correctSpam / (float)allMsg;
        float far = falseRegular / (float)allMsg;
        float frr = falseSpam / (float)allMsg;
        System.out.println("Correct accept rate:\t" + car);
        System.out.println("Correct reject rate:\t" + crr);
        System.out.println("False   accept rate:\t" + far);
        System.out.println("False   reject rate:\t" + frr);
        System.out.println("\n\tPredicted\nActual\tRegular\tSpam\nRegular\t" +
                           correctRegular + "\t" + falseSpam + "\nSpam\t" +
                           falseRegular + "\t" + correctSpam + "\n");
        System.out.println("Performance:\t" +
                           (correctRegular + correctSpam) / allMsg * 100);

        // 7) Errors must be computed on the test set (FAR = false accept rate
        // (misses), FRR = false reject rate (false alarms))
        // 8) Improve the code and the performance (speed, accuracy)
        //
        // Use the same steps to create a class BigramBayespam which implements
        // a classifier using a vocabulary consisting of bigrams
    }
}
