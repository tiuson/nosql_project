import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;

import java.util.Scanner; 

public class MongoReport {
 
	public static void main(String[] args) {
 
		MongoClient client = new MongoClient("localhost", 27017);
		DB database = client.getDB("twitter_db");
		DBCollection collection = database.getCollection("tweets");
		int choice = 10;
		while (choice != 0) {
			printMenu();
			Scanner reader = new Scanner(System.in);
			choice = reader.nextInt();
			long startTime = System.currentTimeMillis();
			switch (choice) {
				case 1:
					printTotal(collection);
					break;
				case 2:
					printTotalByDay(collection);
					break;
				case 3:
					printTotalByDayHour(collection);
					break;
				case 4:
					printFrequentTerms(collection, "2");
					break;
				case 5:
					printFrequentTerms(collection, "3");
					break;
			}
			long finishTime = System.currentTimeMillis();
			System.out.println("Tempo total da consulta: " + (finishTime - startTime) + " milissegundos");

		}
		client.close();
	}

	private static void printMenu() {
		System.out.println("Selecione o número do relatório desejado ou 0 (zero) para finalizar: ");
		System.out.println("[1] Total de Tweets ");
		System.out.println("[2] Volume de Tweets por dia ");
		System.out.println("[3] Volume de Tweets por hora do dia ");
		System.out.println("[4] Termos mais frequentes (min 2 caracteres)");
		System.out.println("[5] Termos mais frequentes (min 3 caracteres)");
	}
 
	private static void printTotal(DBCollection collection) {
		System.out.println("Total de tweets: " + collection.getCount());
	}
 
	private static void printTotalByDay(DBCollection collection) {
		DBObject id = new BasicDBObject();
		id.put("year", new BasicDBObject("$year", "$time"));
		id.put("month", new BasicDBObject("$month", "$time"));
		id.put("day", new BasicDBObject("$dayOfMonth", "$time"));

		DBObject group = new BasicDBObject();
		group.put("_id", id);
		group.put("cnt", new BasicDBObject("$sum", 1));

		AggregationOutput output = collection.aggregate(new BasicDBObject("$group", group), new BasicDBObject("$sort", new BasicDBObject("_id", 1)));

		if (output != null) {
			for (DBObject result : output.results()) {
				Integer count = (Integer) result.get("cnt");

				DBObject idObj = (DBObject) result.get("_id");
				Integer day = (Integer) idObj.get("day");
				Integer month = (Integer) idObj.get("month");
				Integer year = (Integer) idObj.get("year");
				System.out.println(day + "/" + month + "/" + year + ": " + count);
			}
		}
	}

	private static void printTotalByDayHour(DBCollection collection) {
		DBObject id = new BasicDBObject();
		id.put("year", new BasicDBObject("$year", "$time"));
		id.put("month", new BasicDBObject("$month", "$time"));
		id.put("day", new BasicDBObject("$dayOfMonth", "$time"));
		id.put("hour", new BasicDBObject("$hour", "$time"));

		DBObject group = new BasicDBObject();
		group.put("_id", id);
		group.put("cnt", new BasicDBObject("$sum", 1));

		AggregationOutput output = collection.aggregate(new BasicDBObject("$group", group), new BasicDBObject("$sort", new BasicDBObject("_id", 1)));

		if (output != null) {
			for (DBObject result : output.results()) {
				Integer count = (Integer) result.get("cnt");

				DBObject idObj = (DBObject) result.get("_id");
				Integer hour = (Integer) idObj.get("hour");
				Integer day = (Integer) idObj.get("day");
				Integer month = (Integer) idObj.get("month");
				Integer year = (Integer) idObj.get("year");
				System.out.println(day + "/" + month + "/" + year + " " + hour + "h00 ~ " + (hour+1) + "h00: " + count);
			}
		}
	}

	private static void printFrequentTerms(DBCollection collection, String min) {
		String map = "function map() { " +
					 "    var cnt = this.twit; " +
					 "    var words = cnt.match(/\\w+/g); " +
					 "    if (words == null) { " +
					 "        return; " +
					 "    } " +
					 "    for (var i = 0; i < words.length; i++) { " +
					 "        emit({ word:words[i] }, { count:1 }); " +
					 "    } " +
					 "}";
		String reduce = "function reduce(key, counts) { " +
						 "    var cnt = 0; " +
						 "    for (var i = 0; i < counts.length; i++) { " +
						 "        cnt = cnt + counts[i].count; " +
						 "    } " +
						 "    return { count:cnt }; " +
						 "}";
        MapReduceCommand cmd = new MapReduceCommand(collection, map, reduce,
			"wordcounts", MapReduceCommand.OutputType.REPLACE, null);
		MapReduceOutput output = collection.mapReduce(cmd);
		DBCollection wordcounts = output.getOutputCollection();
		BasicDBObject query = new BasicDBObject();
		query.put("$where","this._id.word.length >= " + min);
		com.mongodb.DBCursor cursor = wordcounts.find(query)
			.sort(new BasicDBObject("value.count", -1)).limit(50);
		System.out.println("Termo ==> Nº de ocorrências");
		while(cursor.hasNext()){
			DBObject result = cursor.next();
			DBObject idObj = (DBObject) result.get("_id");
			DBObject idVal = (DBObject) result.get("value");
			System.out.println(idObj.get("word") + " ==> " + idVal.get("count"));
		}
		/*for (DBObject o : output.results()) {
		    System.out.println(o.toString());
	   }*/
	}

}