import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class Indexer {

    public static IndexWriter indexWriter;

    public Indexer() {
    }

    public static void main(String[] args) throws Exception {
        String usage = "java Indexer";
        Collection<Item> items = jdbc.getItems();
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
            // IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            // IndexWriterConfig config = new IndexWriterConfig(new EnglishAnalyzer());
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
}
