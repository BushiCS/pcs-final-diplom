import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private static final Map<String, List<PageEntry>> wordDatabase = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) {
        var listFiles = pdfsDir.listFiles();
        if (listFiles != null) {
            for (var pdf : listFiles) {
                try (var doc = new PdfDocument(new PdfReader(pdf))) {
                    for (int i = 0; i < doc.getNumberOfPages(); i++) { // бежим по всем страницам 1 файла pdf
                        int page = i + 1;
                        var text = PdfTextExtractor.getTextFromPage(doc.getPage(page));
                        var words = text.split("\\P{IsAlphabetic}+");
                        Map<String, Integer> freqs = new HashMap<>();
                        for (var word : words) {
                            if (!word.isEmpty()) {
                                freqs.put(word.toLowerCase(), freqs.getOrDefault(word, 0) + 1);
                            }
                        }
                        for (var entry : freqs.entrySet()) {
                            List<PageEntry> listPageEntries;
                            if (!wordDatabase.containsKey(entry.getKey())) {
                                listPageEntries = new ArrayList<>();
                            } else {
                                listPageEntries = wordDatabase.get(entry.getKey());
                            }
                            listPageEntries.add(new PageEntry(pdf.getName(), page, entry.getValue()));
                            wordDatabase.put(entry.getKey(), listPageEntries);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        word = word.toLowerCase();
        if (wordDatabase.containsKey(word)) {
            wordDatabase.get(word).sort(PageEntry::compareTo);
            return wordDatabase.get(word);
        } else {
            return List.of();
        }
    }
}
