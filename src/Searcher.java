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
import java.util.ArrayList;
import java.util.List;

public class Searcher {
  private static final String SQL_MBR_CONTAINS = "SELECT item_id, MBRContains(LineString(Point(@lon+@width/(111.1/COS(RADIANS(@lat))),@lat+@width/111.1),Point(@lon-@width/(111.1/COS(RADIANS(@lat))),@lat-@width/111.1)),coord) AS is_contains FROM spatial_index WHERE item_id=@id LIMIT 1;";
  private static final String SQL_DIST = "SELECT (6371*acos(cos(radians(@lat))*cos(radians(latitude))*cos(radians(longitude)-radians(@lon))+sin(radians(@lat))*sin(radians(latitude)))) AS distance FROM item_coordinates WHERE item_id=@id LIMIT 1;";
  public static String width = null;
  public static String lat = null;
  public static String lon = null;
  public static boolean isGeo = false;

  public Searcher() {
  }

  public static void main(String[] args) throws Exception {
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
      List<String> result = new ArrayList<>();
      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        Document doc = indexSearcher.doc(scoreDoc.doc);
        String out = "id: " + doc.get("id") + ", " + doc.get("name") + ", score: " + scoreDoc.score + ", price: " + doc.get("price");
        if (isGeo && isMbrContains(doc.get("id"))) {
          double distance = getDistance(doc.get("id"));
          out += ", dist: " + String.format("%.2f", distance);
          result.add(out);
        } else if (!isGeo) {
          result.add(out);
        }
      }
      System.out.println("Number of Hits: " + result.size());
      for (String data : result) {
        System.out.println(data);
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

  public static boolean isMbrContains(String itemId) {
    boolean result = false;
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = DbManager.getConnection(true);
      stmt = conn.createStatement();
      String sql = SQL_MBR_CONTAINS
        .replace("@lat", lat)
        .replace("@lon", lon)
        .replace("@width", width)
        .replace("@id", itemId);
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
        if (rs.getInt("is_contains") == 1) {
          result = true;
        }
      }
      rs.close();
      conn.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return result;
  }

  public static double getDistance(String itemId) {
    double result = -1;
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = DbManager.getConnection(true);
      stmt = conn.createStatement();
      String sql = SQL_DIST
        .replace("@lat", lat)
        .replace("@lon", lon)
        .replace("@id", itemId);
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
        result = rs.getDouble("distance");
      }
      rs.close();
      conn.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return result;
  }
}
