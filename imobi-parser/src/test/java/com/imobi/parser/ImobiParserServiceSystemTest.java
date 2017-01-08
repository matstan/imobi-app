package com.imobi.parser;

import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imobiapp.parser.ImobiParser;
import com.imobiapp.parser.ImobiParserService;
import com.imobiapp.parser.JSoupDocumentRetriever;

/**
 * @author matics.
 */
public class ImobiParserServiceSystemTest {

    private static final String TEST_URL_1 = "http://nepremicnine.net/oglasi-prodaja/ljubljana-mesto/stanovanje/cena-od-170000-do-270000-eur/{pageNumber}/";
    private static final String TEST_URL_2 = "https://www.nepremicnine.net/oglasi/{pageNumber}/?q=hi%C5%A1a+%C5%A1i%C5%A1ka";
    private static final String TEST_URL_3 = "http://nepremicnine.net/oglasi-prodaja/ljubljana-mesto/stanovanje/cena-od-170000-do-270000-eur/";

    ImobiParserService imobiParserService;

    @Before
    public void setup() throws IOException {
        imobiParserService = new ImobiParserService(new ImobiParser(new JSoupDocumentRetriever()), 60);
    }

    @Test
    public void parseDataJSON_1() throws IOException, ParseException, ExecutionException, InterruptedException {
        String json = imobiParserService.parseDataToJson(Arrays.asList(TEST_URL_1));
        Assert.assertThat(json, notNullValue());

        System.out.println(json);
    }
}
