package org.example;

import java.util.*;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.util.regex.Pattern;
import java.util.Set;


public class CrawlerClass extends WebCrawler {

    private static final Pattern filterPattern = Pattern.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|rm|smil|wmv|swf|wma|zip|rar|gz|json))$");
    Stat myStat;

    public static CheckType checkValid(String contentType) {
        String tp = contentType.toLowerCase();
        List<String> accepted = new ArrayList<> (Arrays.asList("text/html", "application/msword", "application/pdf", "image/bmp", "image/gif", "image/jpeg", "image/png", "image/tiff", "image/svg+xml", "image/avif", "image/webp"));
        for (String e: accepted) {
            if (tp.contains(e)) {
                return new CheckType(true, e);
            }
        }
        return new CheckType(false, "");
    }
    public CrawlerClass() {
        myStat = new Stat();
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (filterPattern.matcher(href).matches()||!href.startsWith("https://www.usatoday.com")) {
            return false;
        }
        return true;
    }

    @Override
    public void onRedirectedStatusCode(Page page) {
        this.myStat.incRealFetch();
        FetchRow row2 = new FetchRow();
        row2.url = page.getWebURL().getURL();
        row2.status = page.getStatusCode();
        this.myStat.addFetch(row2);
        this.myStat.incFailFetch();
    }
    @Override
    public void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType, String description) {
        if (CrawlerClass.checkValid(contentType).pass) {
            this.myStat.incRealFetch();
            FetchRow row2 = new FetchRow();
            row2.url = urlStr;
            row2.status = statusCode;
            this.myStat.addFetch(row2);
            this.myStat.incFailFetch();
        }
    }
    @Override
    public void onContentFetchError(Page page) {
        if (CrawlerClass.checkValid(page.getContentType()).pass) {
            this.myStat.incRealFetch();
            FetchRow row2 = new FetchRow();
            row2.url = page.getWebURL().getURL();
            row2.status = page.getStatusCode();
            this.myStat.addFetch(row2);
            this.myStat.incFailFetch();
        }
    }

    @Override
    public void visit(Page page) {
        if (!CrawlerClass.checkValid(page.getContentType()).pass) {
            return;
        }
        String ct = CrawlerClass.checkValid(page.getContentType()).cType;

        // media ok, status 2xx
        if (page.getParseData() instanceof HtmlParseData) {
            this.myStat.incRealFetch();
            FetchRow row2 = new FetchRow();
            row2.url = page.getWebURL().getURL();
            row2.status = page.getStatusCode();
            this.myStat.addFetch(row2);

            this.myStat.incSucFetch();
            HtmlParseData parseData = (HtmlParseData) page.getParseData();
            Set<WebURL> links = parseData.getOutgoingUrls();

            for (WebURL i: links) {
                this.myStat.addSeen(i.getURL());
            }

            this.myStat.incOutLink(links.size());
            VisitRow row1 = new VisitRow();
            row1.url = page.getWebURL().getURL();
            row1.sizeByte = parseData.getHtml().length();
            row1.outLink = links.size();
            row1.contentType = ct;
            this.myStat.addVisit(row1);
        } else if (page.getParseData() instanceof BinaryParseData) {
            this.myStat.incRealFetch();
            FetchRow row2 = new FetchRow();
            row2.url = page.getWebURL().getURL();
            row2.status = page.getStatusCode();
            this.myStat.addFetch(row2);

            this.myStat.incSucFetch();
            VisitRow row1 = new VisitRow();
            row1.url = page.getWebURL().getURL();
            row1.sizeByte = page.getContentData().length;
            row1.outLink = 0;
            row1.contentType = ct;
            this.myStat.addVisit(row1);
        } else {
            System.out.println("can't parse");
            System.exit(0);
        }
    }

    @Override
    public Object getMyLocalData() {
        return myStat;
    }
}