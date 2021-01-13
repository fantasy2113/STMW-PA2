public class Item {
  private long itemId;
  private String line;
  private String name;
  private boolean hasCategory;
  private float price;

  public long getItemId() {
    return itemId;
  }

  public void setItemId(long itemId) {
    this.itemId = itemId;
  }

  public String getLine() {
    return line;
  }

  public void setLine(String line) {
    this.line += (line + " ");
  }

  public boolean isHasCategory() {
    return hasCategory;
  }

  public void setHasCategory(boolean hasCategory) {
    this.hasCategory = hasCategory;
  }

  public float getPrice() {
    return price;
  }

  public void setPrice(float price) {
    this.price = price;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
