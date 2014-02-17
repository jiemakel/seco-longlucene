/**
 * 
 */
package fi.seco.longlucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiemakel
 * 
 */
public class LongIndexWriter implements ILongIndexWriter {

	private final String path;
	private IndexWriter[] writers;
	private DirectoryReader[] readers;
	private LongIndexReader reader;
	private final Analyzer analyzer;
	private long cwBase = 0;
	private IndexWriter lw;
	private final ExecutorService es;
	private final boolean retainOrderInMerge;

	private static final Logger log = LoggerFactory.getLogger(LongIndexWriter.class);

	public LongIndexWriter(String mpath, Analyzer analyzer, boolean retainOrderInMerge, ExecutorService es) {
		if (!mpath.endsWith("/"))
			path = mpath + "/";
		else path = mpath;
		this.es = es;
		this.analyzer = analyzer;
		this.retainOrderInMerge = retainOrderInMerge;
		File d = new File(path);
		d.mkdirs();
		List<IndexWriter> wtmp = new ArrayList<IndexWriter>();
		for (File f : d.listFiles())
			wtmp.add(getWriter(f));
		if (wtmp.isEmpty()) {
			IndexWriter iw = getWriter(new File(path + "0"));
			try {
				iw.commit();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			wtmp.add(iw);
		}
		writers = wtmp.toArray(new IndexWriter[wtmp.size()]);
		lw = writers[writers.length - 1];
		cwBase = (writers.length - 1) * LongLuceneDirectory.bsize;
		readers = new DirectoryReader[writers.length];
		try {
			for (int i = 0; i < writers.length; i++)
				readers[i] = DirectoryReader.open(writers[i], true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		reader = new LongIndexReader(readers, es);
	}

	private IndexWriter getWriter(File path) {
		try {
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
			iwc.setRAMBufferSizeMB(512);
			if (retainOrderInMerge) iwc.setMergePolicy(new LogByteSizeMergePolicy()); // else tiered
			return new IndexWriter(new MMapDirectory(path, new SingleInstanceLockFactory()), iwc);
			//return new IndexWriter(new NRTCachingDirectory(new MMapDirectory(path, new SingleInstanceLockFactory()), 10.0, 256.0), iwc);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addDocument(Document document) {
		try {
			if (lw.maxDoc() > LongLuceneDirectory.band - 100)
				synchronized (this) {
					if (lw.maxDoc() > LongLuceneDirectory.band - 100) {
						if (log.isDebugEnabled()) log.debug(String.format("Last Lucene index contains %,d documents, creating new index %s", lw.maxDoc(), path + "/" + (writers.length + 1)));
						lw = getWriter(new File(path + writers.length));
						IndexWriter[] twriters = new IndexWriter[writers.length + 1];
						System.arraycopy(writers, 0, twriters, 0, writers.length);
						twriters[twriters.length - 1] = lw;
						writers = twriters;
						cwBase += LongLuceneDirectory.bsize;
						DirectoryReader[] treaders = new DirectoryReader[readers.length + 1];
						System.arraycopy(readers, 0, treaders, 0, readers.length);
						treaders[treaders.length - 1] = DirectoryReader.open(lw, true);
						readers = treaders;
						reader = new LongIndexReader(readers, es);
					}
				}
			lw.addDocument(document);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ILongIndexReader getReader() {
		try {
			DirectoryReader nir = DirectoryReader.openIfChanged(readers[readers.length - 1], lw, true);
			if (nir != null) {
				readers[readers.length - 1] = nir;
				reader = new LongIndexReader(readers, es);
			}
			return reader;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteDocuments(Term term) {
		try {
			for (int i = 0; i < writers.length; i++)
				writers[i].deleteDocuments(term);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteDocuments(Query query) {
		try {
			for (int i = 0; i < writers.length; i++)
				writers[i].deleteDocuments(query);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteDocuments(Term... terms) {
		try {
			for (int i = 0; i < writers.length; i++)
				writers[i].deleteDocuments(terms);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void deleteDocuments(Query... queries) {
		try {
			for (int i = 0; i < writers.length; i++)
				writers[i].deleteDocuments(queries);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clearIndex() {
		try {
			for (int i = 1; i < writers.length; i++) {
				writers[i].close();
				FileUtils.forceDelete(new File(path + i));
			}
			writers[0].deleteAll();
			writers = new IndexWriter[] { writers[0] };
			readers = new DirectoryReader[] { DirectoryReader.open(writers[0], true) };
			reader = new LongIndexReader(readers, es);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			for (int i = 0; i < writers.length; i++)
				writers[i].close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void commit() {
		try {
			for (int i = 0; i < writers.length; i++)
				writers[i].commit();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long maxDoc() {
		return cwBase + lw.maxDoc();
	}

}
