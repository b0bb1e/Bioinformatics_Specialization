package motifs;

// for all kinds of lists
import java.util.*;

// for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
class random {
	// makes it easier than declaring all the bases multiple times
	@SuppressWarnings("serial")
	private static final ArrayList<Character> BASES = new ArrayList<Character>() {
		{
			add('A');
			add('C');
			add('G');
			add('T');
		}
	};
	
	// calculates the number of differing positions in two strings
	// PRECONDITION: strings are equal length
	public static int hammingDistance(String a, String b) {
		// initialize return variable
		int dist = 0;
		
		// loop over each index, incrementing counter if chars are different
		for (int i = 0, n = a.length(); i < n; i++)
			if (a.charAt(i) != b.charAt(i)) dist++;
		
		return dist;
	}
	
	// creates a probability matrix with frequency of each base's appearance at each index in an arraylist of strings
	// PRECONDITION: dna's strings are the same length, which is > 0
	public static float[][] createProfile(String[] dna) {
		// initialize return variable
		float[][] profile = new float[4][dna[0].length()];
		// used to track situation in the loop
		int total = 1;
		
		// for each index in the strings
		for (int i = 0, n = profile[0].length; i < n; i++) {
			// loop over each base
			for (char base : BASES) {
				// loop over each DNA string
				for (String genome : dna) {
					// if THIS string's char at THIS index equals THIS base, increment counter
					if (genome.charAt(i) == base) total++;
				}
				
				//calculate the frequency of this base's appearance
				profile[BASES.indexOf(base)][i] = (float) total / (dna.length + 4);
				// reset counter
				total = 1;
			}
		}
		
		return profile;
	}
	
	// generate a list of random k-mers, one from each string in a list
	// PRECONDITION: dna's strings are the same length, which is >= k, k > 0
	public static String[] randomMotifs(String[] dna, int k) {
		// initialize return variable
		String[] motifs = new String[dna.length];
		// used to save a random number during the loop
		int spot = 0;
		
		// loop through the list of strings
		for (int i = 0, n = dna.length; i < n; i++) {
			// generate a random index where a k-mer could begin
			spot = (int) (Math.random() * (dna[0].length() - k + 1));
			// put the proper k-mer in the list of random motifs
			motifs[i] = dna[i].substring(spot, spot + k);
		}
		
		return motifs;
		}
		
	// calculates the score of a DNA string based on a probability matrix
	// PRECONDITION: pattern is not empty, pattern's length = profile[]'s lengths, profile's length = 4
	// PRECONDITION: all values in profile are on [0, 1]
	public static double scoreMotif(String pattern, float[][] profile) {
		// initialize return variable (probability starts at 1)
		double prob = 1;
		
		// loop through DNA string
		for (int i = 0, n = pattern.length(); i < n; i++) {
			// multiply total probability by appropriate probability from profile
			prob *= profile[BASES.indexOf(pattern.charAt(i))][i];
		}
		
		return prob;
	}
	
	// creates a consensus string from a probability matrix
	// consensus string: string with highest probability
	// PRECONDITION: profile[]'s are of equal length, all values in profile are on [0, 1], profile's length = 4
	public static String consensus(float[][] profile) {
		// initialize return variable
		String con = "";
		// maximum probability set to 0
		float max = 0;
		// used to keep track of which char to add to con in the loop
		char add = 'A';
		
		// loop through the rows (probabilities for one base)
		for (int i = 0, n = profile[0].length; i < n; i++) {
			// loop through the columns (probabilities for each base at an index)
			for (int j = 0, m = profile.length; j < m; j++) {
				// if this base's probability is higher than the column's max
				if (profile[j][i] > max) {
					// set column's max to this probability
					max = profile[j][i];
					
					// set the char to add to the row's corresponding char
					add = BASES.get(j);
				}
			}
			
			// add proper char to the consensus string
			con += add;
			// reset max probability for the column
			max = 0;
		}
		
		return con;
	}
		
	// calculates the score of an array of strings against a probability matrix
	// PRECONDITION: strings in motifs are the same length, which is the length of all profile[]s and > 0
	// CALLS: consensus
	public static int scoreMotifs(String[] motifs, float[][] profile) {
		// initialize return variable
		int score = 0;
		// find the consensus string for this profile
		String consensus = consensus(profile);
		
		// for each string, increment score by how different it is from the consensus
		for (String motif : motifs) score += hammingDistance(motif, consensus);
		
		return score;
	}
	
	// finds the k-mer in a string that is the most probable given a probability matrix for each position and base
	// PRECONDITION: genome is not empty, genome's length >= profile[]'s lengths
	// CALLS: scoreMotif
	public static String profProbKmer(String genome, float[][] profile) {
		// calculate how long the k-mer should be
		int k = profile[0].length;
		// initialize return variable to first k-mer
		String mostProb = genome.substring(0, k);
		// maximum probability so far is nothing
		double maxProb = 0;
		// used to track the situation inside the loop
		double prob = 0;
		
		// loop through every k-mer in the string
		for (int i = 0, n = genome.length() - k; i <= n; i++) {
			// calculate the probability of this k-mer
			prob = scoreMotif(genome.substring(i, i + k), profile);
			
			// if the probability exceeds the maximum probability so far
			if (prob > maxProb) {
				// set maximum probability to this probability, and most likely k-mer to this k-mer
				maxProb = prob;
				mostProb = genome.substring(i, i + k);
			}
		}
		return mostProb;
	}
		
	// generate the best-scoring k-mers, one from each string in a list, given a probability matrix
	// PRECONDITION: dna's strings are the same length, which equals profile[]'s, profile's length = 4
	// CALLS: profProbKmer
	public static String[] bestMotifs(String[] dna, float[][] profile) {
		// initialize return variable
		String[] bestMotifs = new String[dna.length];
		
		// for each string in the list
		for (int i = 0, n = bestMotifs.length; i < n; i++) {
			// add the most likely k-mer to the list of good motifs
			bestMotifs[i] = profProbKmer(dna[i], profile);
		}
		
		return bestMotifs;
	}
	
	// tries to find an array of k-mers, from an array of strings, which are the most similar to each other
	// PRECONDITION: dna's strings are the same length, which is >= k, k > 0
	// CALLS: randomMotifs, createProfile, bestMotifs, scoreMotifs
	public static String[] randomizedMotifSearch(String[] dna, int k) {
		// initialize return variable
		String[] bestMotifs = new String[dna.length];
		// initial collection of motifs is random
		String[] motifs = randomMotifs(dna, k);
		// minimum recorded score is maximum possible (every letter of every k-mer is different)
		int minScore = k * dna.length;
		// use to track situation in the loop
		int score;
		float[][] profile = new float[4][dna[0].length()];
		
		// loop for as many times as necessary
		while (true) {
			// create a profile for the current motifs
			profile = createProfile(motifs);
			// try to select better motifs, based on that profile
			motifs = bestMotifs(dna, profile);
			// calculate (hopefully improved score)
			score = scoreMotifs(motifs, createProfile(motifs));
			
			// if the score does continue to improve
			if (score < minScore) {
				// update minimum score & best motifs
				minScore = score;
				bestMotifs = motifs.clone();
			}
			
			// otherwise, bestMotifs is the local minimum, return & exit
			else return bestMotifs;
		}
	}
	
	// tries to find an array of k-mers, from an array of strings, which are the most similar to each other
	// PRECONDITION: dna's strings are the same length, which is >= k, k > 0, times > 0;
	// CALLS: randomizedMotifSearch, scoreMotifs, createProfile
	public static String[] bestRandomMotifs(String[] dna, int k, int t) {
		// initialize return variable to some good motifs
		String[] bestMotifs = randomizedMotifSearch(dna, k);
		// set current collection of motifs
		String[] motifs = bestMotifs.clone();
		// the minimum recorded score is score of those motifs
		int minScore = scoreMotifs(motifs, createProfile(motifs));
		// keep track of current score in the loop
		int score;
		
		// loop 1 - t times (already ran once)
		for (int i = 1; i < t; i++) {
			// get some new "good" motifs
			motifs = randomizedMotifSearch(dna, k);
			// score these motifs
			score = scoreMotifs(motifs, createProfile(motifs));
			
			// if the new motifs are better than the current best ones
			if (score < minScore) {
				// update minimum score & best motifs
				minScore = score;
				bestMotifs = motifs.clone();
			}
		}
		
		return bestMotifs;
	}
	
	// reads a file in as a string, getting rid of line breaks
	// PRECONDITION: fileName is a valid location
	public static String readFileAsString(String fileName) {
		// i got no forking idea how this works
		String text = "";
		try {
			text = new String(Files.readAllBytes(Paths.get(fileName)));
			text = text.replace("\r", "").replace("*", "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}
	
	// where I call methods to test them
	public static void main(String[] args) {
		String[] dna = {"AAGCCAAA", "AATCCTGG", "GCTACTTG", "ATGTTTTG"};
		String[] motifs = bestMotifs(dna, createProfile(new String[] {"CCA", "CCT", "CTT", "TTG"}));
		for (String motif : motifs) System.out.print(motif + " ");
	}
}