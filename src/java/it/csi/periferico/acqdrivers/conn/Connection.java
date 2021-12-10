/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: low level communication protocol layer for data port
// analyzers
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: Connection.java,v 1.4 2015/11/19 18:15:28 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Low level communication protocol layer for data port analyzers
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class Connection implements Runnable {

	public enum Status {
		OPEN, CLOSED, FAILED
	};

	private static final long DEFAULT_READ_TIMEOUT = 100;

	private static final String READ_THREAD_NAME = "conn_read_";

	private static final int READ_THREAD_PRIORITY = Thread.NORM_PRIORITY + 3;

	private static final int MAX_QUEUE_SIZE = 2000;

	private static long classInstanceCount = 0;

	private long localInstanceCount;

	private long thread_count = 0;

	private Logger logger;

	private Charset charset = Charset.defaultCharset();

	private volatile BlockingQueue<Chunk> chunkQueue = new ArrayBlockingQueue<Chunk>(
			MAX_QUEUE_SIZE);

	private Chunk chunkReminder = null;

	private byte[] partialReadBuffer = null;

	private volatile Thread readThread = null;

	private volatile long chunkQueueOverruns = 0;

	private volatile IOException readIOError = null;

	private volatile InterruptedException interrupted = null;

	private Status status = Status.CLOSED;

	private long totalBytesRead = 0;

	private long totalBytesWritten = 0;

	private long openTime = 0;

	private long endTime = 0;

	public Connection() {
		localInstanceCount = ++classInstanceCount;
		logger = Logger.getLogger("periferico.conn."
				+ getClass().getSimpleName() + "-" + localInstanceCount);
	}

	public long getInstanceCount() {
		return localInstanceCount;
	}

	public void open() throws ConnectionException {
		logger.debug("Opening connection " + localInstanceCount);
		if (getStatus() != Status.CLOSED)
			throw new IllegalStateException(
					"Cannot open a not closed connection");
		try {
			openTime = System.currentTimeMillis();
			readIOError = null;
			interrupted = null;
			chunkQueueOverruns = 0;
			clearReadBuffers();
			openImpl();
			readThread = new Thread(this, READ_THREAD_NAME + localInstanceCount
					+ "." + ++thread_count);
			readThread.setDaemon(true);
			readThread.setPriority(READ_THREAD_PRIORITY);
			readThread.start();
			status = Status.OPEN;
		} catch (Exception ex) {
			endTime = openTime;
			status = Status.FAILED;
			throw new ConnectionException("Connection open failed", ex);
		}
	}

	public void close() {
		logger.debug("Closing connection " + localInstanceCount);
		Thread tmpThread = readThread;
		readThread = null;
		if (tmpThread != null) {
			try {
				int maxWaitTime_s = 2;
				logger.info("Waiting up to " + maxWaitTime_s
						+ "s for read thread " + tmpThread.getName()
						+ " to terminate...");
				tmpThread.join(maxWaitTime_s * 1000);
				logger.info("Finished waiting for read thread "
						+ tmpThread.getName() + " to terminate");
			} catch (InterruptedException ie) {
				logger.error("Wait for read thread " + tmpThread.getName()
						+ " to terminate interrupted");
			} finally {
				endTime = System.currentTimeMillis();
				if (chunkQueueOverruns > 0)
					logger.error(chunkQueueOverruns + " chunk queue ovverruns"
							+ " occurred for this connection");
				else
					logger.debug("No chunk queue ovverruns detected for this"
							+ " connection");
			}
		}
		closeImpl();
		status = Status.CLOSED;
	}

	public final Status getStatus() {
		return status;
	}

	/**
	 * invia il messaggio testuale passato come parametro
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void send(String data) throws IOException {
		send(data, null);
	}

	/**
	 * invia il messaggio testuale passato come parametro, aggiungendo il
	 * separatore passato come parametro
	 * 
	 * @param data
	 * @param messageSeparator
	 * @throws IOException
	 */
	public void send(String data, String messageSeparator) throws IOException {
		logger.trace("SEND:<" + data + ">"
				+ (messageSeparator != null ? " (delimiter used)" : ""));
		if (messageSeparator != null)
			data += messageSeparator;
		sendBytes(data.getBytes(charset));
	}

	/**
	 * invia il vettore di bytes passati come parametro.
	 * 
	 * @param bytes
	 * @throws IOException
	 */
	public void sendBytes(byte[] bytes) throws IOException {
		logTraceBytes("SEND_BYTES:", bytes);
		if (bytes != null)
			totalBytesWritten += bytes.length;
		write(bytes);
	}

	/**
	 * riceve un messaggio testuale, usa come delimitatore una pausa della
	 * trasmissione passata come parametro
	 * 
	 * @param timeout_ms
	 * @param delimiterTime_ms
	 * @return
	 * @throws IOException
	 * @throws ConnectionClosedException
	 * @throws InterruptedException
	 */
	public String receive(long timeout_ms, int delimiterTime_ms)
			throws IOException, ConnectionClosedException, InterruptedException {
		return bytesToString(receiveBytes(timeout_ms, delimiterTime_ms));
	}

	/**
	 * riceve un messaggio testuale, usa come delimitatore uno o più caratteri
	 * passati come parametro; nel caso siano passati più caratteri basta
	 * trovarne uno qualsiasi affinché il messaggio sia considerato completo,
	 * eventuali altri delimitatori trovati di seguito al primo vengono
	 * eliminati automaticamente
	 * 
	 * @param timeout_ms
	 * @param delimiters
	 * @return
	 * @throws IOException
	 * @throws ConnectionClosedException
	 * @throws InterruptedException
	 */
	public String receive(long timeout_ms, String delimiters)
			throws IOException, ConnectionClosedException, InterruptedException {
		if (delimiters == null || delimiters.isEmpty())
			throw new IllegalArgumentException("At least one delimiter should"
					+ " be specified");
		byte[] delimBytes = delimiters.getBytes(charset);
		if (delimBytes.length != delimiters.length())
			throw new IllegalArgumentException("At least one delimiter cannot"
					+ " be converted to a single byte");
		return bytesToString(receiveBytes(timeout_ms, delimBytes));
	}

	/**
	 * riceve un messaggio testuale di lunghezza fissa, espressa in numero di
	 * byte (poiché a basso livello la lettura avviene a byte, con successiva
	 * trasformazione in caratteri; salvo casi molto particolari il numero di
	 * caratteri è uguale al numero di byte)
	 * 
	 * @param timeout_ms
	 * @param numBytes
	 * @return
	 * @throws IOException
	 * @throws ConnectionClosedException
	 * @throws InterruptedException
	 */
	public String receiveFixedLength(long timeout_ms, int numBytes)
			throws IOException, ConnectionClosedException, InterruptedException {
		return bytesToString(receiveBytes(timeout_ms, numBytes));
	}

	/**
	 * riceve una squenza di byte, usa come delimitatore una pausa della
	 * trasmissione passata come parametro
	 * 
	 * @param timeout_ms
	 * @param delimiterTime_ms
	 * @return
	 * @throws IOException
	 * @throws ConnectionClosedException
	 * @throws InterruptedException
	 */
	public byte[] receiveBytes(long timeout_ms, int delimiterTime_ms)
			throws IOException, ConnectionClosedException, InterruptedException {
		partialReadBuffer = null;
		List<Chunk> chunkList = new ArrayList<Chunk>();
		long startTime = System.currentTimeMillis();
		Chunk chunk = dequeueChunk(timeout_ms);
		while (chunk != null) {
			chunkList.add(chunk);
			if (timeout_ms + startTime - System.currentTimeMillis() <= 0)
				break;
			chunk = dequeueChunk(delimiterTime_ms);
			if (chunk == null)
				return mergeChunks(chunkList, false);
		}
		partialReadBuffer = mergeChunks(chunkList, true);
		return null;
	}

	/**
	 * riceve una sequenza di byte, usa come delimitatore uno o più caratteri
	 * passati come parametro; nel caso siano passati più caratteri basta
	 * trovarne uno qualsiasi affinché il messaggio sia considerato completo,
	 * eventuali altri delimitatori trovati di seguito al primo vengono
	 * eliminati automaticamente
	 * 
	 * @param timeout_ms
	 * @param delimiters
	 * @return
	 * @throws IOException
	 * @throws ConnectionClosedException
	 * @throws InterruptedException
	 */
	public byte[] receiveBytes(long timeout_ms, byte[] delimiters)
			throws IOException, ConnectionClosedException, InterruptedException {
		partialReadBuffer = null;
		if (delimiters == null || delimiters.length == 0)
			throw new IllegalArgumentException("At least one delimiter should"
					+ " be specified");
		List<Chunk> chunkList = new ArrayList<Chunk>();
		long startTime = System.currentTimeMillis();
		Chunk chunk = dequeueChunk(timeout_ms);
		while (chunk != null) {
			Chunk reminder = chunk.splitAndRemoveDelimiters(delimiters);
			if (reminder != null) { // delimiter found
				if (!chunk.isEmpty())
					chunkList.add(chunk);
				if (!reminder.isEmpty())
					chunkReminder = reminder;
				return mergeChunks(chunkList, false);
			}
			chunkList.add(chunk);
			long remainingTime_ms = timeout_ms + startTime
					- System.currentTimeMillis();
			if (remainingTime_ms > 0)
				chunk = dequeueChunk(remainingTime_ms);
			else
				chunk = null;
		}
		partialReadBuffer = mergeChunks(chunkList, true);
		return null;
	}

	/**
	 * riceve una sequenza di byte, con lunghezza fissa specificata
	 * 
	 * @param timeout_ms
	 * @param numBytes
	 * @return
	 * @throws IOException
	 * @throws ConnectionClosedException
	 * @throws InterruptedException
	 */
	public byte[] receiveBytesFixedLength(long timeout_ms, int numBytes)
			throws IOException, ConnectionClosedException, InterruptedException {
		partialReadBuffer = null;
		if (numBytes <= 0)
			throw new IllegalArgumentException("At least one byte should"
					+ " be requested");
		List<Chunk> chunkList = new ArrayList<Chunk>();
		long startTime = System.currentTimeMillis();
		Chunk chunk = dequeueChunk(timeout_ms);
		while (chunk != null) {
			if (chunk.getLength() >= numBytes) {
				Chunk reminder = chunk.split(numBytes);
				chunkList.add(chunk);
				if (!reminder.isEmpty())
					chunkReminder = reminder;
				return mergeChunks(chunkList, false);
			}
			chunkList.add(chunk);
			numBytes -= chunk.getLength();
			long remainingTime_ms = timeout_ms + startTime
					- System.currentTimeMillis();
			if (remainingTime_ms > 0)
				chunk = dequeueChunk(remainingTime_ms);
			else
				chunk = null;
		}
		partialReadBuffer = mergeChunks(chunkList, true);
		return null;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public long getChunkQueueOverruns() {
		return chunkQueueOverruns;
	}

	public void clearReadBuffers() {
		chunkReminder = null;
		chunkQueue.clear();
		partialReadBuffer = null;
	}

	public byte[] getPartialReadBytes() {
		return partialReadBuffer;
	}

	public String getPartialReadString() {
		if (partialReadBuffer == null)
			return null;
		return new String(partialReadBuffer, charset);
	}

	public long getTotalBytesRead() {
		return totalBytesRead;
	}

	public long getTotalBytesWritten() {
		return totalBytesWritten;
	}

	public final long getUptime() {
		if (getStatus() == Status.OPEN)
			return System.currentTimeMillis() - openTime;
		return endTime - openTime;
	}

	public final long getLastOpenTime() {
		return openTime;
	}

	@Override
	public void run() {
		try {
			while (Thread.currentThread() == readThread) {
				Chunk chunk = read(DEFAULT_READ_TIMEOUT);
				if (chunk == null) {
					logger.warn("End of file detected");
					break;
				}
				if (!chunk.isEmpty()) {
					logTraceChunk("RX_CHUNK", chunk);
					if (!chunkQueue.offer(chunk)) {
						chunkQueueOverruns++;
						logger.debug("Chunk queue overrun detected, total: "
								+ chunkQueueOverruns);
					}
				}
			}
		} catch (SocketException se) {
			if (readThread != null) {
				logger.error("An error occurred reading data", se);
				readIOError = se;
			}
		} catch (IOException ie) {
			logger.error("An error occurred reading data", ie);
			readIOError = ie;
		} catch (InterruptedException ie) {
			interrupted = ie;
			logger.error("Tokenizer thread " + readThread.getName()
					+ " interrupted", ie);
		}
		readThread = null;
	}

	protected abstract void openImpl() throws Exception;

	protected abstract void closeImpl();

	/**
	 * @param timeout
	 *            how long to wait before giving up, in units of <tt>ms</tt>
	 * @return a chunk of data, or an empty chunk of data if the specified
	 *         waiting time elapses before data is available, or <tt>null</tt>
	 *         if the stream was closed on the remote side
	 * @throws IOException
	 *             if an IO error occurs in the low level read
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	protected abstract Chunk read(long timeout) throws IOException,
			InterruptedException;

	protected abstract void write(byte[] bytes) throws IOException;

	private Chunk dequeueChunk(long timeout_ms) throws IOException,
			ConnectionClosedException, InterruptedException {
		if (chunkReminder != null) {
			Chunk tmp = chunkReminder;
			chunkReminder = null;
			return tmp;
		}
		Chunk chunk = chunkQueue.poll(timeout_ms, TimeUnit.MILLISECONDS);
		if (chunk == null && readThread == null) {
			if (readIOError != null)
				throw readIOError;
			if (interrupted != null)
				throw interrupted;
			throw new ConnectionClosedException(
					"Connection closed by remote device");
		}
		return chunk;
	}

	private byte[] mergeChunks(List<Chunk> chunkList, boolean timeout) {
		int size = 0;
		for (Chunk chunk : chunkList)
			size += chunk.getLength();
		byte[] token = new byte[size];
		int tokenPos = 0;
		for (Chunk chunk : chunkList) {
			System.arraycopy(chunk.getBuffer(), chunk.getBeginIndex(), token,
					tokenPos, chunk.getLength());
			tokenPos += chunk.getLength();
		}
		logTraceBytes("RECEIVE_BYTES:" + (timeout ? "timeout:" : ""), token);
		if (token != null)
			totalBytesRead += token.length;
		return token;
	}

	private String bytesToString(byte[] bytes) {
		if (bytes != null) {
			String data = new String(bytes, charset);
			logger.trace("RECEIVE:<" + data + ">");
			return data;
		}
		logger.debug("RECEIVE:timeout:<" + getPartialReadString() + ">");
		return null;
	}

	private void logTraceBytes(String message, byte[] bytes) {
		if (!logger.isTraceEnabled())
			return;
		StringBuilder result = new StringBuilder();
		if (bytes != null && bytes.length > 0) {
			result.append(toHexStr(bytes[0]));
			for (int i = 1; i < bytes.length; i++)
				result.append(' ').append(toHexStr(bytes[i]));
		}
		logger.trace(message + "<" + result.toString() + ">");
	}

	private void logTraceChunk(String message, Chunk chunk) {
		if (!logger.isTraceEnabled())
			return;
		StringBuilder result = new StringBuilder();
		if (chunk != null && chunk.getLength() > 0) {
			result.append(toHexStr(chunk.getBuffer()[0]));
			for (int i = 1; i < chunk.getLength(); i++)
				result.append(' ').append(toHexStr(chunk.getBuffer()[i]));
			logger.trace(message + ":<" + result.toString() + ">");
		}
	}

	private String toHexStr(byte b) {
		return String.format("%02x", b);
	}

}
