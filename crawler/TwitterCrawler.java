import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import twitter4j.*;

public class TwitterCrawler {

	public static void main(String[] args) throws TwitterException, IOException {
        if (args.length < 1) {
            System.out.println("java TwitterCrawler [query]");
            System.exit(-1);
		}
		final Format dateFormatOutput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("../data/twitter.log",true)));
		StatusListener listener = new StatusListener(){
			public void onStatus(Status status) {
				out.println("{ 'time' : {$date: '" + dateFormatOutput.format(status.getCreatedAt()) + "'}, 'user_id' : '" + status.getUser().getScreenName() + "', 'user' : '" + status.getUser().getName().replaceAll("'","´") + "', 'twit' : '" + status.getText().replaceAll("'","´").replaceAll("\n","  ") + "' }");
				out.flush();
			}
			public void onStallWarning(StallWarning arg0) {}
			public void onScrubGeo(long userId, long upToStatusId) {}
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		twitterStream.addListener(listener);
		FilterQuery filtre = new FilterQuery();
		String[] keywordsArray = { args[0] };
		filtre.track(keywordsArray);
		twitterStream.filter(filtre);
		//twitterStream.addListener(listener);
		// sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
		//twitterStream.sample();
	}
}