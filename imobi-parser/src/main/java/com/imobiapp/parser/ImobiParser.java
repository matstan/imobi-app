package com.imobiapp.parser;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    /**
     * Retrieves all the pages for {@param baseUrl}.
     *
     * The {@param baseUrl} must be a valid GET query for "nepremicnine.net" with the {page_number}
     * placeholder within. For the purpose of retrieving all the possible pages, the number "1" is
     * placed for {page_number} so that the actual number of pages can be retrieved.
     * The method searches for "pagination" section and parses out all the pages
     * that the query returns.
     *
     * @param baseUrl
     * @return
     * @throws IOException
     */
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

    /**
     * Searches through all the {@param listingPages} and retrieves all the links to
     * the ad pages.
     *
     * @param listingPages
     * @return
     * @throws IOException
     */
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

    /**
     * Reads and parses a single ad page and returns consolidate data.
     *
     * @param url
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public AdData getAdData(String url) throws IOException, ParseException {
        AdData adData = new AdData();
        adData.url = url;
        Document doc = jSoupDocumentRetriever.getDocument(url);

        Element content980 = doc.getElementById("vsebina980");
        Elements summaryElements = content980.getElementsByTag("h1");
        if (summaryElements.size() == 1) {
            adData.summary = summaryElements.get(0).html();

            String[] summarySplit = adData.summary.split(",");
            String sizeString = summarySplit[summarySplit.length - 1].replace("m2", "").trim();
            adData.size = Double.parseDouble(sizeString);

        }

        Elements priceElements = doc.getElementById("podrobnosti").getElementsByClass("cena clearfix");
        if (priceElements.size() == 1) {
            String currencyString = priceElements.get(0).getElementsByTag("span").get(0).html();
            adData.price = (Long) NumberFormat.getCurrencyInstance(Locale.GERMANY).parse(currencyString);
        }

        Elements shortDescriptionElements = doc.getElementById("opis").getElementsByClass("kratek");
        if (shortDescriptionElements.size() > 0) {
            Element shortDescriptionElement = shortDescriptionElements.get(0);
            if (shortDescriptionElement.childNodes().size() == 2) {
                adData.shortDescripton = shortDescriptionElement.childNodes().get(0).childNode(0).toString() + shortDescriptionElement.childNodes().get(1).toString();
            }
        }

        Elements longDescriptionConatiner = doc.getElementById("opis").getElementsByClass("web-opis");
        if (longDescriptionConatiner.size() > 0) {
            Elements longDescriptionElements = longDescriptionConatiner.get(0).getElementsByTag("p");
            if (longDescriptionElements.size() == 1) {
                adData.longDescription = longDescriptionElements.get(0).text();
            }
        }

        Elements contactElements = doc.getElementById("get-info").getElementsByClass("wrapper-prodajalec");
        if (contactElements.size() > 0) {
            adData.contact = contactElements.get(0).text();
        }

        return adData;
    }


    public static class AdData {
        public String url;
        public Long price;
        public Double size;
        public String summary;
        public String shortDescripton;
        public String longDescription;
        public String contact;

    }
}
