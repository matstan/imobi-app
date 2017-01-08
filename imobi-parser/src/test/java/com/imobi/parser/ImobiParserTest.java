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
import java.text.ParseException;
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
    private static final String TEST_URL_AD_CONTENT_1 = "https://www.nepremicnine.net/oglasi-prodaja/smartno-pod-smarno-goro-hisa_6066634/";


    @Before
    public void setup() throws IOException {
        // mock JSoupDocumentRetriever so it doesn't retrieve URLs, but reads HTML responses from files
        REQUEST_FILE_MAPPING.put(TEST_URL_PAGINATION_1, "/pagination_1.html");
        REQUEST_FILE_MAPPING.put(TEST_URL_PAGINATION_2, "/pagination_2.html");
        REQUEST_FILE_MAPPING.put(TEST_URL_PAGINATION_ABSENT, "/pagination_3.html");
        REQUEST_FILE_MAPPING.put(TEST_URL_AD_CONTENT_1, "/ad_content_1.html");


        JSoupDocumentRetriever jSoupDocumentRetriever = mock(JSoupDocumentRetriever.class);
        when(jSoupDocumentRetriever.getDocument(anyString())).thenAnswer(new Answer<Document>() {
            @Override
            public Document answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String url = (String) args[0];

                return getDocument(REQUEST_FILE_MAPPING.get(url));
            }
        });

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

    @Test
    public void getDataPages() throws IOException {
        List<String> listingsPages = imobiParser.getAdPages(TEST_URL_PAGINATION_1);

        Assert.assertThat(listingsPages, notNullValue());
        Assert.assertThat(listingsPages.size(), is(30));
        Assert.assertThat(listingsPages, hasItem(ImobiParser.IMOBI_BASE_URL + "/oglasi-prodaja/crnuce-spodnje-okrogarjeva-2-stanovanje_5966303/"));
        Assert.assertThat(listingsPages, hasItem(ImobiParser.IMOBI_BASE_URL + "/oglasi-prodaja/lj-center-vrtaca-stanovanje_6053889/"));
    }

    @Test
    public void getAdData() throws IOException, ParseException {
        ImobiParser.AdData adData = imobiParser.getAdData(TEST_URL_AD_CONTENT_1);

        Assert.assertThat(adData, notNullValue());
        Assert.assertThat(adData.url, is(TEST_URL_AD_CONTENT_1));
        Assert.assertThat(adData.summary, is("Prodaja, hiša, dvojček: ŠMARTNO POD ŠMARNO GORO, 138 m2"));
        Assert.assertThat(adData.price, is("350.000,00 €"));
        Assert.assertThat(adData.size, is("138"));
        Assert.assertThat(adData.shortDescripton, is("ŠMARTNO POD ŠMARNO GORO, 138 m2, dvojček, zgrajen l. 2016, 386 m2 zemljišča, prodamo. Cena: 350.000,00 EUR"));
        Assert.assertThat(adData.longDescription, is("V spodnjih Pirničah, ob robu gozda prodamo energetsko varčni dvojček bivalne površine 138m2. Zraven je območje Nature 2000, zaradi česar naprej ne bo več novogradenj. Hiša ima tri etaže. V spodnji je dnevni prostor z velikimi steklenimi površinami, orieintiran na jug. Vsa okna in vrata na fasadi so ALU, zastekljena s 3-slojnim izolacijskim steklom polnjenim s plinom. Spodaj so še vetrolov, garderoba in stranišče. V prvem nadstropju sta dve sobi, stena med njima ni nosilna ter kopalnica. V vrhnjem nadstropju sta prav tako dve sobi v katerih je strop na enem delu visok 3 metre. Ogrevanje je na toplotno črpalko, streha je prekrita z vlaknocementnimi ploščami. Stene v pritličju in nadstropju so klasično zidane z modularno opeko debeline 25cm, notranje stene 20cm. Plošča med pritličjem in nadstropjem je debela 16cm. Nepremičnini pripadata dve parkirni mesti, predvidoma bo končana julija 2016."));
        Assert.assertThat(adData.contact, is("LJUBLJANA NEPREMIČNINE d.o.o. Cesta na Brdo 69 1000 Ljubljana 01/244-50-00 Nejc Šink 030/313-004 http://www.ljubljananepremicnine.si"));
    }
}
