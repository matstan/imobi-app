package com.imobiapp.parser;

import static com.imobiapp.parser.ImobiParser.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author matics.
 */
public class ImobiParserService {

    final ExecutorService executor;

    final ImobiParser imobiParser;

    public ImobiParserService(ImobiParser imobiParser, int numberOfThreads) {
        this.imobiParser = imobiParser;
        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    public String parseDataToJson(List<String> baseUrls) throws IOException, InterruptedException, ExecutionException {
        // first retrieve all listing pages in single thread execution
        List<AdData> adDataList = new ArrayList<>();
        List<String> listingPages = new ArrayList<>();
        for (String baseUrl : baseUrls) {
            listingPages.addAll(imobiParser.getListingPages(baseUrl));
        }

        try {
            /*
             *  retrieve all ad pages in multithread execution
             */
            List<Future<List<String>>> adPagesFutures = new ArrayList<>();
            for (final String listingPage : listingPages) {
                Future<List<String>> adPagesFuture = executor.submit(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        return imobiParser.getAdPages(listingPage);
                    }
                });
                adPagesFutures.add(adPagesFuture);
            }

            List<String> adPages = new ArrayList<>();
            for (Future<List<String>> adPagesFuture : adPagesFutures) {
                try {
                    adPages.addAll(adPagesFuture.get());
                } catch (InterruptedException e) {
                    String msg = "The method was interrupted when waiting on result for getting ad pages response.";
                    throw new InterruptedException(msg);
                } catch (ExecutionException e) {
                    String msg = "The method for getting ad pages threw exception.";
                    throw new ExecutionException(msg, e);
                }
            }

            /*
             *  retrieve all ad data in multithread execution
             */
            List<Future<AdData>> adDataFutures = new ArrayList<>();
            for (final String adPage : adPages) {
                Future<AdData> adDataFuture = executor.submit(new Callable<AdData>() {
                    @Override
                    public AdData call() throws Exception {
                        return imobiParser.getAdData(adPage);
                    }
                });
                adDataFutures.add(adDataFuture);
            }

            for (Future<AdData> adDataFuture : adDataFutures) {
                try {
                    adDataList.add(adDataFuture.get());
                } catch (InterruptedException e) {
                    String msg = "The method was interrupted when waiting on result for parsing ad pages response.";
                    throw new InterruptedException(msg);
                } catch (ExecutionException e) {
                    String msg = "The method for parsing ad pages threw exception.";
                    throw new ExecutionException(msg, e);
                }
            }
        } finally {
            executor.shutdown();
        }

        return new ObjectMapper().writeValueAsString(adDataList);
    }
}
