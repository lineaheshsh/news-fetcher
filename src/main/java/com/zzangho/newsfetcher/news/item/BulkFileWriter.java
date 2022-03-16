package com.zzangho.newsfetcher.news.item;

import com.zzangho.newsfetcher.common.Constants;
import com.zzangho.newsfetcher.news.model.News;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
public class BulkFileWriter extends SynchronizedItemStreamWriter<News> implements ItemStreamWriter<News> {
    private FileWriter file;
    private File bulkDir;

    private String dir;
    private String fileName;

    public BulkFileWriter(String dir, String fileName) {
        this.dir = dir;
        this.fileName = fileName;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            System.out.println(dir + " , " + fileName);
            bulkDir = new File(dir);
            if (!bulkDir.exists()) bulkDir.mkdir();

            file = new FileWriter(dir + "/" + fileName);
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
    public void write(List<? extends News> items) throws Exception{

        for (int i = 0; i < items.size(); i++) {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.CONTENTS_ID, items.get(i).getContents_id());
            jsonObject.put(Constants.DOMAIN, items.get(i).getDomain());
            jsonObject.put(Constants.CATEGORY_NM, items.get(i).getCategory_nm());
            jsonObject.put(Constants.TITLE, items.get(i).getTitle());
            jsonObject.put(Constants.CONTENTS, items.get(i).getContents());
            jsonObject.put(Constants.WRITER, items.get(i).getWriter());
            jsonObject.put(Constants.DATE, items.get(i).getDate());
            jsonObject.put(Constants.AMPM, items.get(i).getAmpm());
            jsonObject.put(Constants.TIME, items.get(i).getTime());
            jsonObject.put(Constants.COMPANY, items.get(i).getCompany());
            jsonObject.put(Constants.URL, items.get(i).getUrl());
            jsonObject.put(Constants.UDT_DT, items.get(i).getUdt_dt());

            file.write(jsonObject + "\n");
        }
    }
}
