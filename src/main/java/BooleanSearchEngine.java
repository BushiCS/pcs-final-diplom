import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> wordDatabase = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir, File stopList) {
        Set<String> stopWords = new HashSet<>();
        try (var reader = new BufferedReader(new BufferedReader(new FileReader(stopList)))) {
            if (reader.ready()) {
                stopWords.addAll(reader.lines().collect(Collectors.toSet()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        var listFiles = pdfsDir.listFiles();
        if (listFiles != null) {
            for (var pdf : listFiles) {
                try (var doc = new PdfDocument(new PdfReader(pdf))) {
                    for (int i = 0; i < doc.getNumberOfPages(); i++) {
                        int page = i + 1;
                        var text = PdfTextExtractor.getTextFromPage(doc.getPage(page));
                        var words = text.split("\\P{IsAlphabetic}+");
                        Map<String, Integer> freqs = new HashMap<>();
                        for (var word : words) {
                            word = word.toLowerCase();
                            if (!word.isEmpty()) {
                                if (!stopWords.contains(word)) {
                                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                                }
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
                            wordDatabase.get(entry.getKey()).sort(PageEntry::compareTo);
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
            return wordDatabase.get(word);
        } else {
            return List.of();
        }
    }

    public List<PageEntry> multiplySearch(String[] words) {
        List<PageEntry> list = new ArrayList<>();
        for (String word : words) {
            word = word.toLowerCase();
            if (wordDatabase.containsKey(word)) {
                list.addAll(wordDatabase.get(word));
            }
        }
        for (int i = 0; i < list.size() - 1; i++) {
            for (int k = i + 1; k < list.size(); k++) {
                PageEntry word1 = list.get(i);
                PageEntry word2 = list.get(k);
                if (word1.getPdfName().equals(word2.getPdfName()))
                    if (word1.getPage() == word2.getPage()) {
                        int sum = word2.getCount() + word1.getCount();
                        list.add(new PageEntry(word1.getPdfName(), word1.getPage(), sum));
                        list.remove(word1);
                        list.remove(word2);
                        list.sort(PageEntry::compareTo);
                    }
            }
        }
        return list;
    }
}
