package ru.devmark.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final int requestLimit;
    private final long timeInterval;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger requestCount;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestLimit = requestLimit;
        this.timeInterval = timeUnit.toMillis(1);
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.requestCount = new AtomicInteger(0);

        // Reset semaphore permits at the end of each time interval
        this.scheduler.scheduleAtFixedRate(() -> {
            semaphore.release(requestLimit - semaphore.availablePermits());
            requestCount.set(0);
        }, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
    }

    public void createDocument(Document document, String signature) throws InterruptedException, IOException {
        semaphore.acquire();
        try {
            String json = objectMapper.writeValueAsString(document);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .addHeader("Signature", signature)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                // Handle response if needed
            }
        } finally {
            semaphore.release();
        }
    }

    public static class Document {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;

        // Inner classes for nested JSON objects
        public static class Description {
            public String participantInn;
        }

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }

    public static void main(String[] args) {
        // Example usage
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);
        Document document = new Document();
        document.description = new Document.Description();
        document.description.participantInn = "string";
        document.doc_id = "string";
        document.doc_status = "string";
        document.doc_type = "LP_INTRODUCE_GOODS";
        document.importRequest = true;
        document.owner_inn = "string";
        document.participant_inn = "string";
        document.producer_inn = "string";
        document.production_date = "2020-01-23";
        document.production_type = "string";
        document.products = new Document.Product[1];
        document.products[0] = new Document.Product();
        document.products[0].certificate_document = "string";
        document.products[0].certificate_document_date = "2020-01-23";
        document.products[0].certificate_document_number = "string";
        document.products[0].owner_inn = "string";
        document.products[0].producer_inn = "string";
        document.products[0].production_date = "2020-01-23";
        document.products[0].tnved_code = "string";
        document.products[0].uit_code = "string";
        document.products[0].uitu_code = "string";
        document.reg_date = "2020-01-23";
        document.reg_number = "string";

        try {
            api.createDocument(document, "signature");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}