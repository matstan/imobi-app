package com.imobiapp.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.imobiapp.parser.dm.AdData;
import com.imobiapp.parser.dm.AdDataHistory;

/**
 * @author matics.
 */
public class ImobiParserService {

    private final ExecutorService executor;

    private final ImobiParser imobiParser;

    private final DateTimeRetriever dateTimeRetriever;

    public ImobiParserService(ImobiParser imobiParser, int numberOfThreads, DateTimeRetriever dateTimeRetriever) {
        this.imobiParser = imobiParser;
        this.dateTimeRetriever = dateTimeRetriever;

        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void syncAds(List<String> baseUrls, Map<String, AdDataHistory> existingAds)
            throws InterruptedException, ExecutionException, IOException {
        List<AdData> newAds = refreshAds(baseUrls);

        for (AdData newAdData : newAds) {
            AdDataHistory existingAdDataHistory = existingAds.get(newAdData.url);

            // if there was no previous entry, create one
            if (existingAdDataHistory == null) {
                AdDataHistory newAdDataHistory = new AdDataHistory();
                newAdDataHistory.lastSynced = dateTimeRetriever.getCurrentTimestamp();
                newAdDataHistory.hash = newAdData.calculateHash();
                newAdDataHistory.seen = false;
                newAdDataHistory.adDataHistory.add(newAdData);
                existingAds.put(newAdData.url, newAdDataHistory);
            } else {
                // if there was already a previous entry existing, compare it with the new entry and add new history entry if needed
                if (!newAdData.calculateHash().equals(existingAdDataHistory.hash)) {
                    existingAdDataHistory.lastSynced = dateTimeRetriever.getCurrentTimestamp();
                    existingAdDataHistory.hash = newAdData.calculateHash();
                    existingAdDataHistory.seen = false;
                    existingAdDataHistory.adDataHistory.add(newAdData);
                }
            }
        }
    }

    public List<AdData> refreshAds(List<String> baseUrls) throws IOException, InterruptedException, ExecutionException {
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

        return adDataList;
    }
}
