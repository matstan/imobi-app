package com.imobiapp.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private static final String IMOBI_URL =
            "https://www.nepremicnine.net/oglasi-prodaja/ljubljana-mesto/ljubljana-bezigrad,ljubljana-center,ljubljana-moste-polje,ljubljana-siska,ljubljana-vic-rudnik/stanovanje/cena-od-150000-do-230000-eur,velikost-od-65-do-170-m2,letnik-od-1995-do-2017/";

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

    public List<String> getDataPagesToParse(List<String> listingPages) throws IOException {
        return null;
    }


}
