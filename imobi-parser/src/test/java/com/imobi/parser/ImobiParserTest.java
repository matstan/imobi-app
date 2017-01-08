package com.imobi.parser;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.imobiapp.parser.ImobiParser;
import com.imobiapp.parser.JSoupDocumentRetriever;

/**
 * @author matics.
 */
public class ImobiParserTest {

    private static final String RESOURCES_PATH = "/com/imobi/parser/";

    ImobiParser imobiParser;

    private static final Map<String, String> REQUEST_FILE_MAPPING = new HashMap<>();
    private static final String TEST_URL_PAGINATION_1 = "http://nepremicnine.net/oglasi-prodaja/ljubljana-mesto/stanovanje/cena-od-170000-do-270000-eur/{pageNumber}";
    private static final String TEST_URL_PAGINATION_2 = "https://www.nepremicnine.net/oglasi/{pageNumber}/?q=hi%C5%A1a+%C5%A1i%C5%A1ka";
    private static final String TEST_URL_PAGINATION_ABSENT = "https://www.nepremicnine.net/nepremicnine.html?q=hi%C5%A1a+vi%C5%BEmarje";


    @Before
    public void setup() throws IOException {
        // mock JSoupDocumentRetriever so it doesn't retrieve URLs, but reads HTML responses from files
        REQUEST_FILE_MAPPING.put(TEST_URL_PAGINATION_1, "/pagination_1.html");
        REQUEST_FILE_MAPPING.put(TEST_URL_PAGINATION_2, "/pagination_2.html");
        REQUEST_FILE_MAPPING.put(TEST_URL_PAGINATION_ABSENT, "/pagination_3.html");


        JSoupDocumentRetriever jSoupDocumentRetriever = mock(JSoupDocumentRetriever.class);
        when(jSoupDocumentRetriever.getDocument(anyString(), anyInt())).thenAnswer(new Answer<Document>() {
            @Override
            public Document answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String url = (String) args[0];

                return getDocument(REQUEST_FILE_MAPPING.get(url));
            }
        });

        when(jSoupDocumentRetriever.getNumberedPage(anyString(), anyInt())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String url = (String) args[0];
                int pageNumber = (Integer) args[1];

                return new JSoupDocumentRetriever().getNumberedPage(url, pageNumber);
            }
        });

        imobiParser = new ImobiParser(jSoupDocumentRetriever);
    }

    private Document getDocument(String filename) throws IOException {
        File file = new File(this.getClass().getResource(RESOURCES_PATH + filename).getFile());
        return Jsoup.parse(file, "UTF-8");
    }

    /**
     * Should return 10 results/pages.
     */
    @Test
    public void getListingsPages_1() throws IOException {
        List<String> listingsPages = imobiParser.getListingPages(TEST_URL_PAGINATION_1);

        Assert.assertThat(listingsPages, notNullValue());
        Assert.assertThat(listingsPages.size(), is(10));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_1.replaceFirst("\\{pageNumber\\}", String.valueOf(1)))));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_1.replaceFirst("\\{pageNumber\\}", String.valueOf(5)))));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_1.replaceFirst("\\{pageNumber\\}", String.valueOf(10)))));
    }

    /**
     * Should return 16 results/pages.
     */
    @Test
    public void getListingsPages_2() throws IOException {
        List<String> listingsPages = imobiParser.getListingPages(TEST_URL_PAGINATION_2);

        Assert.assertThat(listingsPages, notNullValue());
        Assert.assertThat(listingsPages.size(), is(16));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_2.replaceFirst("\\{pageNumber\\}", String.valueOf(1)))));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_2.replaceFirst("\\{pageNumber\\}", String.valueOf(5)))));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_2.replaceFirst("\\{pageNumber\\}", String.valueOf(10)))));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_2.replaceFirst("\\{pageNumber\\}", String.valueOf(16)))));
    }

    /**
     * Should return 1 results/pages.
     */
    @Test
    public void getListingsPages_3() throws IOException {
        List<String> listingsPages = imobiParser.getListingPages(TEST_URL_PAGINATION_ABSENT);

        Assert.assertThat(listingsPages, notNullValue());
        Assert.assertThat(listingsPages.size(), is(1));
        Assert.assertThat(listingsPages, hasItem(is(TEST_URL_PAGINATION_ABSENT.replaceFirst("\\{pageNumber\\}", String.valueOf(1)))));
    }
}
