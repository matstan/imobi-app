package com.imobi.parser;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imobiapp.parser.ImobiParser;
import com.imobiapp.parser.JSoupDocumentRetriever;

/**
 * @author matics.
 */
public class ImobiParserSystemTest {

    private static final String TEST_URL_1 = "http://nepremicnine.net/oglasi-prodaja/ljubljana-mesto/stanovanje/cena-od-170000-do-270000-eur/{pageNumber}/";
    private static final String TEST_URL_2 = "https://www.nepremicnine.net/oglasi/{pageNumber}/?q=hi%C5%A1a+%C5%A1i%C5%A1ka";
    private static final String TEST_URL_3 = "http://nepremicnine.net/oglasi-prodaja/ljubljana-mesto/stanovanje/cena-od-170000-do-270000-eur/";

    ImobiParser imobiParser;

    @Before
    public void setup() throws IOException {
        imobiParser = new ImobiParser(new JSoupDocumentRetriever());
    }

    @Test
    public void parseData_1() throws IOException, ParseException {
        List<ImobiParser.AdData> adDataList = imobiParser.parseData(TEST_URL_1);
        Assert.assertThat(adDataList, notNullValue());
        Assert.assertThat(adDataList.size(), greaterThan(0));

        System.out.println(adDataList.size());
    }

    @Test
    public void parseData_2() throws IOException, ParseException {
        List<ImobiParser.AdData> adDataList = imobiParser.parseData(TEST_URL_2);
        Assert.assertThat(adDataList, notNullValue());
        Assert.assertThat(adDataList.size(), greaterThan(0));

        System.out.println(adDataList.size());
    }

    @Test
    public void parseData_3() throws IOException, ParseException {
        List<ImobiParser.AdData> adDataList = imobiParser.parseData(TEST_URL_3);
        Assert.assertThat(adDataList, notNullValue());
        Assert.assertThat(adDataList.size(), greaterThan(0));

        System.out.println(adDataList.size());
    }
}
