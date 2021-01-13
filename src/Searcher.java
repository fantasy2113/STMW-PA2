import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Searcher {

  private static String width = null;
  private static String lat = null;
  private static String lon = null;
  private static boolean isGeo = false;

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
      Query query = queryParser.parse(getQuery(searchText));
      TopDocs topDocs = indexSearcher.search(query, 10000);
      System.out.println("Number of Hits: " + topDocs.totalHits);
      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        Document doc = indexSearcher.doc(scoreDoc.doc);
        String out = "id: " + doc.get("id") + ", " + doc.get("name") + ", score: " + scoreDoc.score + ", price: " + doc.get("price") + ", coordinates: " + doc.get("has_coordinates");
        if (isGeo) {
          double dist = getDist(doc.get("id"));
          if (dist < Double.parseDouble(width)) {
            out += ", dist: " + String.format("%.2f", dist);
            System.out.println(out);
          }
        } else {
          System.out.println(out);
        }
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
    if (lon != null && lat != null && width != null) {
      isGeo = true;
    }
  }

  private static String getQuery(String searchText) {
    String[] terms = searchText.split(" ");
    StringBuilder sb = new StringBuilder("line:" + terms[0]);
    for (int i = 1; i < terms.length; i++) {
      sb.append(" OR line:").append(terms[i]);
    }
    if (isGeo) {
      sb.append(" AND has_coordinates:true");
    }
    return sb.toString();
  }

  public static double getDist(String itemId) {
    double dist = -1;
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = DbManager.getConnection(true);
      stmt = conn.createStatement();
      String sql = "SELECT item_id, Distance(Point(" + lon + "," + lat + "), coord) AS dist FROM spatial_index WHERE item_id = " + itemId + " LIMIT 1;";
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
        dist = rs.getDouble("dist");
      }
      rs.close();
      conn.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return dist;
  }
}
