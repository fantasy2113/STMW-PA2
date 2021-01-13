import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class jdbc {

  public static String width = null;
  public static String lat = null;
  public static String lon = null;
  public static boolean isGeo = false;

  public jdbc() {
  }

  public static void main(String[] args) {
    String usage = "java jdbc";
    rebuildIndexes("indexes");
  }

  public static void rebuildIndexes(String indexPath) {
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = DbManager.getConnection(true);
      stmt = conn.createStatement();
      //String sql = "SELECT * from item limit 3;";
      String sql = "SELECT count(*) as count from item;";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        String count = rs.getString("count");
        System.out.println("count: " + count);
      }
      rs.close();
      conn.close();
    } catch (SQLException ex) {
      System.out.println(ex);
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
      ResultSet rs = stmt.executeQuery("SELECT item.*,  item_coordinates.latitude, buy_price.buy_price, has_category_idx.category_name FROM item LEFT JOIN item_coordinates ON item.item_id = item_coordinates.item_id LEFT JOIN has_category_idx ON item.item_id = has_category_idx.item_id LEFT JOIN buy_price ON item.item_id = buy_price.item_id ORDER BY item.item_id ASC;");
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

