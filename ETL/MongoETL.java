import com.mongodb.DBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import java.io.*;

public class MongoETL {
 
	public static void main(String[] args) throws FileNotFoundException, IOException {
 
		MongoClient client = new MongoClient("localhost", 27017);
		DB database = client.getDB("twitter_db");
		DBCollection collection = database.getCollection("tweets");
		BufferedReader br = new BufferedReader(new FileReader("../data/twitter.log"));
		String  line = null;
		while ((line = br.readLine()) != null) {
			DBObject dbObject = (DBObject)JSON.parse(line);
			collection.insert(dbObject);
		}
		client.close();
	}
 
}