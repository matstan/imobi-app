package com.imobi.parser;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imobiapp.parser.DateTimeRetriever;
import com.imobiapp.parser.ImobiParser;
import com.imobiapp.parser.ImobiParserService;
import com.imobiapp.parser.JSoupDocumentRetriever;
import com.imobiapp.parser.dm.AdData;
import com.imobiapp.parser.dm.AdDataHistory;

/**
 * @author matics.
 */
public class ImobiParserServiceSystemTest {

    private static final String TEST_URL_1 = "http://nepremicnine.net/oglasi-prodaja/ljubljana-mesto/stanovanje/cena-od-170000-do-270000-eur/{pageNumber}/";
    private static final String TEST_URL_2 = "https://www.nepremicnine.net/oglasi/{pageNumber}/?q=prodaja+hiša+gunclje";
    private static final String TEST_URL_3 = "http://nepremicnine.net/oglasi-prodaja/ljubljana-mesto/stanovanje/cena-od-170000-do-270000-eur/";

    ImobiParserService imobiParserService;

    @Before
    public void setup() throws IOException {
        imobiParserService = new ImobiParserService(new ImobiParser(new JSoupDocumentRetriever()), 60, new DateTimeRetriever());
    }

    @Test
    public void refreshAds() throws IOException, ParseException, ExecutionException, InterruptedException {
        List<AdData> newAds = imobiParserService.refreshAds(Arrays.asList(TEST_URL_1));
        Assert.assertThat(newAds, notNullValue());
    }

    @Test
    public void syncAds() throws IOException, ParseException, ExecutionException, InterruptedException {
        Map<String, AdDataHistory> ads = new HashMap<>();
        imobiParserService.syncAds(Arrays.asList(TEST_URL_2), ads);
        Assert.assertThat(ads.size(), greaterThan(0));
    }
}
