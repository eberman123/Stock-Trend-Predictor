import java.util.ArrayList;
import java.util.Scanner;

public class WsbScraperMain {
    
    public static void main (String[] args) {
    	System.out.println("Type the number of threads you want the system to parse (note that each thread takes ~ 2 seconds to parse)");
    	Scanner sc = new Scanner(System.in);
    	int threadsNum = Integer.parseInt(sc.nextLine());
    	System.out.println("How many reccomendations do you want? (type an integer)");
    	int numRecs = Integer.parseInt(sc.nextLine());
        RedditParser rp= new RedditParser(); 
        rp.tickMapMaker();
        rp.wsbScraper(threadsNum);
        ArrayList<String> recs = new ArrayList<>();
        recs = rp.getTopRecs(numRecs);
        System.out.println("These are the top " + numRecs + " recomendations in order:");
        for(String s : recs) {
        	System.out.println(s);
        }
    }
}
