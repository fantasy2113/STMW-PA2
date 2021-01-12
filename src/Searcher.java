import java.lang.*;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.*;

import java.nio.file.Path;

public class Searcher {

    private static String width;
    private static String lat;
    private static String lon;

    public Searcher() {
    }

    public static void main(String[] args) throws Exception {
        String usage = "java Searcher";
        setInput(args);
        search(args[0], "indexes");
    }

    private static TopDocs search(String searchText, String p) {
        System.out.println("Running search(" + searchText + ")");
        try {
            Path path = Paths.get(p);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser("line", new SimpleAnalyzer());
            Query query = queryParser.parse(searchText);
            TopDocs topDocs = indexSearcher.search(query, 10000);
            System.out.println("Number of Hits: " + topDocs.totalHits);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                System.out.println("doc_id: " + document.get("doc_id")
                        + ", score: " + scoreDoc.score + " [" + document.get("line") + "]");
            }
            return topDocs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void setInput(String... args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-x")) {
                lon = args[i + 1];
            }
            if (args[i].equals("-y")) {
                lat = args[i + 1];
            }
            if (args[i].equals("-w")) {
                width = args[i + 1];
            }
        }
    }
}
