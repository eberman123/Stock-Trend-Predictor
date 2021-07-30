import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.json.simple.JSONObject;	
import org.json.simple.*;
import org.json.simple.parser.*;

public class RedditParser {
	private String baseURL; 
	private Document currentDoc;
	private Document newDoc;
	private Map<String, Integer> stockMap; 
	private String topStock;
	private int topScore;

	//user agent field 
	public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36";
	/*
	 * Constructor that initializes the base URL and loads 
	 * the document produced from that URL
	 * Creates the hashmap which includes the sting which are the stock names mapped to the values which are their scores
	 */
	public RedditParser() {
		this.baseURL = "https://www.reddit.com/r/wallstreetbets/search?q=flair_name%3A%22Daily%20Discussion%22&restrict_sr=1&sort=new";
		this.stockMap = new HashMap<String, Integer>();
		try {
			//daily discussion flair searched document
			this.currentDoc = Jsoup.connect(this.baseURL).get();
		} catch (IOException e) {
			System.out.println("Could not connect to specified page");
		}     
	}

	//gets all tickers and adds them to map with each initialized to 0
	public void tickMapMaker() {
		//website that the stock list is taken from (dynamic so that new stock tickers are accounted for)
		String newURL =  "https://stockanalysis.com/stocks/";
		try {
			this.currentDoc = Jsoup.connect(newURL).get();
		} catch (IOException e) {
			System.out.println("Could not connect to the specified page");
		}
		Element ulTags = this.currentDoc.select("ul").get(3);
		Elements liTags = ulTags.select("li");
		for(Element li : liTags) {
			String temp = li.text();
			//placing each ticker into the map with the starting value of 0
			stockMap.put(temp.substring(0,temp.indexOf(" ")), 0);
		}

		//reconnecting currentDoc to the reddit daily discussion page
		try {
			this.currentDoc = Jsoup.connect(baseURL).get();
		} catch (IOException e) {
			System.out.println("N/A");
		}	

	}

	public void wsbScraper(int maxThreads) {
		//URL for daily discussions and moves page
		//    	String ddURL = "https://www.reddit.com/r/wallstreetbets/search?q=flair_name%3A%22Daily%20Discussion%22&restrict_sr=1&sort=new"; 
		//    	try {
		//			Document newDoc = Jsoup.connect(ddURL).get();
		//		} catch (IOException e) {
		//			System.out.println("could not connect to page");
		//			e.printStackTrace();
		//		} 
		//-------------------------------------------------------------
		
		
		// class of table with all discussion links: QBfRw7Rj8UkxybFpX-USO


		Elements table = currentDoc.select("div.QBfRw7Rj8UkxybFpX-USO");   
		//selects the most recent daily discussion element
		Element currentDD = table.select("div").select("span:contains(Daily Discussion Thread for)").parents().parents().parents().get(0);
		//extracts the URL
		String ddURL = currentDD.attr("abs:href");
		Document ddDoc;

		//creating the json link 
		String finalLink = ddURL + ".json";
		
		String fileAsString = "";

		URL jsURL = null;
		try {
			jsURL = new URL(finalLink);
		} catch (MalformedURLException e1) {
			System.out.println("there was a malformed URL exception");
			e1.printStackTrace();
		}
		
		URLConnection c = null;
		try {
			c = jsURL.openConnection();
		} catch (MalformedURLException e) {
			System.out.println("there was a malformed URL exception");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("there was an input output exception");
			e.printStackTrace();
		}

		//in order to not timeout the request and get a 429 error, implemented thread.sleep
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.out.println("there was an interrupted exception");
			e.printStackTrace();
		} 
		//Mimicking a browser by using the User agent 
		c.setRequestProperty("User-Agent", USER_AGENT);

		InputStream inputS = null;
		
		//getting input stream from the opened connection
		try  {
			inputS = c.getInputStream();
		} catch (IOException e) {
			System.out.println("there was an input output exception");
			e.printStackTrace();
		}
		
		try {
            int contentChecker;
            //while there is still content to be added from the input stream
            while ((contentChecker = inputS.read()) != -1) {
            	//adds the character to the string we are building of the json file
                fileAsString += ((char)contentChecker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		//System.out.println(fileAsString.indexOf("\"children\":"));
		
		//need to navigate to the last children tag in the json file 
		//while there is another children tag, we get a substring leaving the json file content which comes after the tag
		while(fileAsString.indexOf("\"children\":") > 0) {
			fileAsString = fileAsString.substring(fileAsString.indexOf("\"children\":")+11);
		}
		//System.out.println(fileAsString);
		
		//array list of all the unique ending codes to each comment thread extracted from json file
		ArrayList<String> commentCodes = new ArrayList<>();
		
		//want to gather all the unique codes that are included in the last children tag
		//note that the end of the tag is denoted by a ]
		//note that each tag has a length of 7 that is unique to only that comment thread
		while(!fileAsString.substring(0,1).equals("]")) {
			commentCodes.add(fileAsString.substring(fileAsString.indexOf("\"")+1, fileAsString.indexOf("\"")+8));
			fileAsString = fileAsString.substring(fileAsString.indexOf("\"")+9);
		}
		
		//System.out.println(commentCodes.size());
		
		//loop through each code adding it to the daily discussion base URL: ddURL
		//count will allow us to keep track of how many different comment threads were parsed
		int count = 0;
		int maxValue = 0;
		String maxKey = null;
		for(String s : commentCodes) {
			//the max number of threads to look at (each thread will take ~ 2 seconds)
			if(count == maxThreads) { 
				break;
			}
			//creates the URL to navigate to by adding the unique code to the end of the daily discussion link
			String ccURL = ddURL + s;
			Document ccDoc = null;
			
			try {
				ccDoc = Jsoup.connect(ccURL).get();
			} catch (IOException e) {
				System.out.println("could not connect to page");
			}
			
			//class of comment box : _1ump7uMrSA43cqok14tPrG _1oTUrVtKJk1ue0r3fe31kJ
			//System.out.println(ccDoc.select("p"));
			
			Elements pTags = ccDoc.select("p");
			for(Element p : pTags) {
				//System.out.println(p.text());
				int sentiment = sentiment(p.text());
				String [] pArr = p.text().split(" ");
				//the scoring system is as follows:
				//bullish mention of the stock = +2
				//bearish mentions of the stock = -2
				//neutral mention of the stock = +1
				
				
				for (String aR : pArr) {
					if (stockMap.containsKey(aR)) {
						//System.out.println("success");
						//System.out.println("BEFORE " + stockMap.get(aR) + " " + aR);
						stockMap.put(aR, (stockMap).get(aR) + sentiment);
						//System.out.println("AFTER " + stockMap.get(aR) + " " + aR);
						
						//keeps track of the highest scoring stock for easy retrieval 
						if (stockMap.get(aR) > maxValue) {
							maxValue = stockMap.get(aR);
							maxKey = aR;
						}

					}
				}
				
				
			}
			count++;
			
		}
		topStock = maxKey;
		topScore = maxValue;
		//System.out.println("FINAL BEST: " + maxKey + ": " + maxValue);
	}
	
	//sentiment analysis function
	//returns if a given string includes:
	//bullish key words = 2
	//neutral = 1
	//bearish = -2
	public int sentiment(String s) {
		//lists of the respective sentiment keywords
		ArrayList<String> bear = new ArrayList<>();
		ArrayList<String> bull = new ArrayList<>();
		
		BufferedReader brBear;
		BufferedReader brBull;
		
		try {

			FileReader frBear = new FileReader("bear.txt");
			FileReader frBull = new FileReader("bull.txt");
			brBear = new BufferedReader(frBear);
			brBull = new BufferedReader(frBull);
			String bearLine = brBear.readLine();
			String bullLine = brBull.readLine();
			
			
			while(bearLine != null) {
				bear.add(bearLine.toUpperCase());
				bearLine = brBear.readLine();
			}
			while(bullLine != null) {
				bull.add(bullLine.toUpperCase());
				bullLine = brBull.readLine();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		int bullScore = 0;
		int bearScore = 0;
		
		//increments the respective bull and bear score depending on how many of the key terms appear in the string
		for(String kw : bull) {
			if(s.toUpperCase().contains(kw)) {
				bullScore ++;
			}
		}
		for(String kw : bear) {
			if(s.toUpperCase().contains(kw)) {
				bearScore ++;
			}
		}
		
		//System.out.println("bull score:" + bullScore);
		//System.out.println("bear score:" + bearScore);
		//System.out.println("phrase:" + s);
		
		//determines over all sentiment 
		if(bullScore > bearScore) {
			return 2;
		}
		if(bullScore < bearScore) {
			return -2;
		}
		return 1;
	}
	
	public ArrayList<String> getTopRecs(int num){

		ArrayList<String> recs = new ArrayList<>();
		for(int i=0 ; i<num ; i++) {
			int maxScore = 0;
			//list of all tickers
			Set<String> tickers = stockMap.keySet();
			//account for the removal of common words
			tickers.remove("A");
			tickers.remove("IT");
			tickers.remove("I");
			String maxTicker = "no recomendation; there were no mentions of tickers";
			for(String s : tickers) {
				if(stockMap.get(s) > maxScore) {
					maxScore = stockMap.get(s);
					maxTicker = s;
				}
			}
			recs.add(maxTicker);
			//remove a ticker once it is recommended 
			tickers.remove(maxTicker);
			
		}
		return recs;
		
	}
}
