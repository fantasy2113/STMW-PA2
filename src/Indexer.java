import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Indexer {

  public static IndexWriter indexWriter;

  public Indexer() {
  }

  public static void main(String[] args) throws Exception {
    String usage = "java Indexer";
    Collection<Item> items = getItems();
    rebuildIndexes("indexes", items);
  }


  public static void insertDoc(IndexWriter indexWriter, Item item) {
    Document doc = new Document();
    doc.add(new LongField("id", item.getItemId(), Field.Store.YES));
    doc.add(new TextField("line", item.getLine().trim(), Field.Store.YES));
    doc.add(new TextField("name", item.getName().trim(), Field.Store.YES));
    doc.add(new FloatField("price", item.getPrice(), Field.Store.YES));
    doc.add(new TextField("has_coordinates", item.isHasCategory() ? "true" : "false", Field.Store.YES));
    try {
      indexWriter.addDocument(doc);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void rebuildIndexes(String indexPath, Collection<Item> items) {
    try {
      Path path = Paths.get(indexPath);
      System.out.println("Indexing to directory '" + indexPath + "'...\n");
      Directory directory = FSDirectory.open(path);
      IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
      IndexWriter indexWriter = new IndexWriter(directory, config);
      indexWriter.deleteAll();
      for (Item item : items) {
        insertDoc(indexWriter, item);
      }
      indexWriter.close();
      directory.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Collection<Item> getItems() throws SQLException {
    System.out.println("Fetch items...");
    Map<Long, Item> itemMap = new HashMap<>();
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = DbManager.getConnection(true);
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT item.*,  item_coordinates.latitude, buy_price.buy_price, has_category.category_name FROM item LEFT JOIN item_coordinates ON item.item_id = item_coordinates.item_id LEFT JOIN has_category ON item.item_id = has_category.item_id LEFT JOIN buy_price ON item.item_id = buy_price.item_id ORDER BY item.item_id ASC;");
      while (rs.next()) {
        final long item_id = rs.getLong("item_id");
        if (!itemMap.containsKey(item_id)) {
          Item item = new Item();
          item.setItemId(item_id);
          item.setName(rs.getString("item_name"));
          item.setLine(rs.getString("item_name"));
          item.setLine(rs.getString("description"));
          item.setLine(rs.getString("category_name"));
          if (rs.getObject("latitude") != null) {
            item.setHasCategory(true);
          }
          if (rs.getObject("buy_price") != null) {
            item.setPrice(rs.getFloat("buy_price"));
          }
          itemMap.put(item_id, item);
        } else {
          Item item = itemMap.get(item_id);
          item.setLine(rs.getString("category_name"));
        }
      }
    } catch (
      SQLException ex) {
      ex.printStackTrace();
    } finally {
      if (stmt != null) stmt.close();
      if (conn != null) conn.close();
    }
    return itemMap.values();
  }
}
