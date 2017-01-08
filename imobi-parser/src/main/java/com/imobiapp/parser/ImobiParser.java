package com.imobiapp.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author matics.
 */
public class ImobiParser {

    public static class ImobiData {
        public String title;
    }

    public static final String IMOBI_BASE_URL = "https://www.nepremicnine.net";

    private JSoupDocumentRetriever jSoupDocumentRetriever;

    public ImobiParser(JSoupDocumentRetriever jSoupDocumentRetriever) {
        this.jSoupDocumentRetriever = jSoupDocumentRetriever;
    }

    public List<ImobiData> parseDataFromPage(Document doc) throws IOException {
        List<ImobiData> result = new ArrayList<ImobiData>();

        Element paginationElement = doc.getElementById("pagination");
        Elements anchorElements = paginationElement.getElementsByTag("a");

        //        String title = doc.title();
        //
        //        Elements adds = doc.getElementsByClass("oglas_container");
        //        for (Element add : adds) {
        //            ImobiData imobiData = new ImobiData();
        //            imobiData.title = add.nodeName();
        //
        //            result.add(imobiData);
        //        }
        //
        //
        //        return result;

        return null;
    }

    public List<String> getListingPages(String baseUrl) throws IOException {
        List<String> resultPages = new ArrayList<String>();

        // retrieve first page result
        Document doc = jSoupDocumentRetriever.getDocument(baseUrl, 1);

        Elements lastPageAnchor = doc.getElementById("pagination").getElementsByClass("last");
        if (lastPageAnchor.size() == 1) {
            String lastPageUrl = lastPageAnchor.get(0).attr("href");

            // find the number of the last page
            String pattern = "(.*)(/)(\\d+)(/)(.*)";
            Pattern r = Pattern.compile(pattern);

            int lastNumberPage = -1;
            Matcher m = r.matcher(lastPageUrl);
            if (m.find() && m.groupCount() == 5) {
                lastNumberPage = Integer.valueOf(m.group(3));
            }

            // create a listings page link for each page
            if (lastNumberPage > 0) {
                for (int page = 1; page <= lastNumberPage; page++) {
                    resultPages.add(jSoupDocumentRetriever.getNumberedPage(baseUrl, page));
                }
            }
        } else if (lastPageAnchor.size() == 0) {
            // if there is no pagination div available, return only the first page
            resultPages.add(jSoupDocumentRetriever.getNumberedPage(baseUrl, 1));
        }

        return resultPages;
    }

    public List<String> getDataPages(List<String> listingPages) throws IOException {
        Set<String> resultPages = new HashSet<>();

        // iterate over all the listing pages and store all the ads to be parsed in the next stage
        for (String url : listingPages) {

            Document doc = jSoupDocumentRetriever.getDocument(url);
            String pattern = "\\/oglasi-prodaja\\/.*?\\/";
            Pattern r = Pattern.compile(pattern);

            // parse only those links which come from "oglas_container" div
            Elements adContainerElements = doc.getElementsByClass("oglas_container");
            if (adContainerElements != null && adContainerElements.size() > 0) {
                for (Element adContainerElement : adContainerElements) {
                    Elements linksList = adContainerElement.getElementsByAttributeValueMatching("href", r);
                    if (linksList.size() > 0) {
                        for (Element link : linksList) {
                            resultPages.add(IMOBI_BASE_URL + link.attr("href"));
                        }
                    }
                }
            }
        }

        return new ArrayList<>(resultPages);
    }
}
