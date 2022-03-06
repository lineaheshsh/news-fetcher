package com.zzangho.newsfetcher.news.item;

import com.zzangho.newsfetcher.news.model.News;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
public class BulkFileWriter extends SynchronizedItemStreamWriter<News> implements ItemStreamWriter<News> {
    private FileWriter file;
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            file = new FileWriter("bulk.json");
        } catch (IOException e) {
            log.error("File is not create");
            e.printStackTrace();
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
        try {
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(List<? extends News> items) throws Exception {
        for (int i = 0; i < items.size(); i++) {
            JSONObject rootJsonObject = new JSONObject();

            JSONObject indexJsonObject = new JSONObject();
            indexJsonObject.put("_index", "news");
            indexJsonObject.put("_type", "_doc");
            indexJsonObject.put("_id", items.get(i).getContents_id());
            rootJsonObject.put("index", indexJsonObject);

            file.write(rootJsonObject.toString() + "\n");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contents_id", items.get(i).getContents_id());
            jsonObject.put("domain", items.get(i).getDomain());
            jsonObject.put("category_nm", items.get(i).getCategory_nm());
            jsonObject.put("title", items.get(i).getTitle());
//            jsonObject.put("contents", items.get(i).getContents());
            jsonObject.put("writer", items.get(i).getWriter());
            jsonObject.put("date", items.get(i).getDate());
            jsonObject.put("ampm", items.get(i).getAmpm());
            jsonObject.put("time", items.get(i).getTime());
            jsonObject.put("company", items.get(i).getCompany());
            jsonObject.put("url", items.get(i).getUrl());
            jsonObject.put("udt_dt", items.get(i).getUdt_dt());

            file.write(jsonObject.toString() + "\n");
            rootJsonObject = null;
            indexJsonObject = null;
            jsonObject = null;
        }
    }
}
