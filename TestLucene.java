package com.dianxinos.opdamisc.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.compressing.CompressingStoredFieldsFormat;
import org.apache.lucene.codecs.lucene41.Lucene41StoredFieldsFormat;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.nodes.RangeQueryNode;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA
 * User: huangqingwei
 * DATE: 2015/6/12
 * Time: 14:17
 * To change this template use: File | Settings | File and Code Templates | Includes | File Header
 */
public class TestLucene {

    public static void buildIndex(Directory directory) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        String text = "This is the test text data to be indexed.";
        //method 1
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(true);
        fieldType.setTokenized(true);
        fieldType.setStored(true);
        fieldType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS); //default
        doc.add(new Field("textField", text, fieldType));
        //method 2
//        doc.add(new TextField("textField", text, Field.Store.YES)); //default: tokenized and indexed, but not stored
//        doc.add(new LongField("longField", System.currentTimeMillis(), Field.Store.NO)); //default: indexed
//        doc.add(new StringField("stringField", "/home/work/local/test", Field.Store.YES)); //default indexed, but not tokenized
        indexWriter.addDocument(doc);
        indexWriter.addDocument(doc);

        indexWriter.forceMerge(1);
        indexWriter.close();
    }

    public static void searchIndexTopDocs(Directory directory) throws IOException {
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        TopDocs td = null;
        // Parse a simple query that searches for "text":

        // BooleanQuery --按“与或”搜索
        BooleanQuery booleanQuery = new BooleanQuery();
        Query query1 = new TermQuery(new Term("textField", "text"));
        Query query2 = new TermQuery(new Term("textField", "qq"));
        booleanQuery.add(query1, BooleanClause.Occur.MUST);
        booleanQuery.add(query2, BooleanClause.Occur.MUST_NOT);
        td = indexSearcher.search(booleanQuery, 100);

        //TermQuery --按词条搜索
        Query termQuery=new TermQuery(new Term("textField","text"));
        td = indexSearcher.search(termQuery, 100);

//        //RangeQuery --在某一范围内搜索
//        NumericRangeQuery rangeQuery = NumericRangeQuery.newLongRange("longField", 0L, 999999999L, false, true); //(0, 999999999]
//        td = indexSearcher.search(rangeQuery, 100);
//
//        //PrefixQuery --前缀匹配搜索
//        PrefixQuery prefixQuery = new PrefixQuery(new Term("textField", "This is"));
//        td = indexSearcher.search(prefixQuery, 100);
//
//        //PhraseQuery --短语搜索
//        PhraseQuery phraseQuery = new PhraseQuery();
//        phraseQuery.add(new Term("textField", "test"));
//        phraseQuery.add(new Term("textField", "data"));
//        phraseQuery.setSlop(1); //match test data or test * data
//        td = indexSearcher.search(phraseQuery, 100);
//
//        //MultiPhraseQuery --多短语搜索
//        MultiPhraseQuery multiPhraseQuery = new MultiPhraseQuery();
//        //构建3个Term，作为短语的后缀
//        Term t1=new Term("textField","test");
//        Term t2=new Term("textField","data");
//        //再向query中加入所有的后缀，与前缀一起，它们将组成3个短语
//        multiPhraseQuery.add(new Term[]{t1,t2});
//        td = indexSearcher.search(multiPhraseQuery, 100);
//
//        QueryWrapperFilter queryWrapperFilter = new QueryWrapperFilter(booleanQuery);
//        TopDocs td = indexSearcher.search(new MatchAllDocsQuery(), queryWrapperFilter, 10);

//        QueryParser parser = new QueryParser("fieldname", new StandardAnalyzer());
//        Query query = parser.parse("textaa");
//        TopDocs td = indexSearcher.search(query, null, 1000);

        ScoreDoc[] hits = td.scoreDocs;
        System.out.println(hits.length);
        // Iterate through the results:
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = indexSearcher.doc(hits[i].doc);
            Iterator<IndexableField> cursor = hitDoc.iterator();
            while (cursor.hasNext()) {
                IndexableField obj = cursor.next();
                System.out.println(obj.name() + "\t" + obj.stringValue());
            }
        }
        directoryReader.close();
    }

    public static void main(String[] args) throws Exception {

        // Store the index in memory:
        Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
        //Directory directory = FSDirectory.open("/tmp/testindex");

        buildIndex(directory);

        // Now search TopDocs from the index:
        searchIndexTopDocs(directory);

        //todo: Now search Hits from the index

        directory.close();
    }
}
