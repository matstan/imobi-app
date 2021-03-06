package com.imobiapp.parser;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author matics.
 */
public class JSoupDocumentRetriever {

    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).timeout(0).get();
    }

    public Document getDocument(String url, int pageNumber) throws IOException {
        return Jsoup.connect(getNumberedPage(url, pageNumber)).timeout(0).get();
    }

    public String getNumberedPage(String url, int pageNumber) {
        return url.replaceFirst("\\{pageNumber\\}", String.valueOf(pageNumber));
    }
}
