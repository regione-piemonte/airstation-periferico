/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds a portion of replay from a data port analyzer
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: Chunk.java,v 1.1 2011/11/14 11:22:55 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn;

/**
 * Holds a portion of replay from a data port analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Chunk {

	private byte[] buffer;

	private int beginIndex;

	private int endIndex;

	public Chunk() {
		this(new byte[0], 0);
	}

	public Chunk(byte[] buffer, int dataSize) {
		if (dataSize > buffer.length)
			throw new IllegalArgumentException("Declared data size ("
					+ dataSize + ") exceeds buffer size (" + buffer.length
					+ ")");
		this.buffer = buffer;
		endIndex = dataSize;
		beginIndex = 0;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getBeginIndex() {
		return beginIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public int getLength() {
		return endIndex - beginIndex;
	}

	public boolean isEmpty() {
		return beginIndex == endIndex;
	}

	public Chunk split(int index) {
		if (index < 0 || index > getLength())
			throw new IllegalArgumentException("Split index (" + index
					+ ") out of bounds (0 to " + getLength() + ")");
		Chunk reminder = new Chunk();
		reminder.buffer = this.buffer;
		reminder.beginIndex = this.beginIndex + index;
		reminder.endIndex = this.endIndex;
		this.endIndex = this.beginIndex + index;
		return reminder;
	}

	public Chunk splitAndRemoveDelimiters(byte[] delimiters) {
		if (delimiters == null || delimiters.length == 0)
			return null;
		for (int i = beginIndex; i < endIndex; i++) {
			if (isDelimiter(buffer[i], delimiters)) {
				Chunk reminder = new Chunk();
				reminder.buffer = this.buffer;
				reminder.beginIndex = i + 1;
				reminder.endIndex = this.endIndex;
				this.endIndex = i;
				reminder.removeDelimitersFromHead(delimiters);
				return reminder;
			}
		}
		return null;
	}

	private boolean isDelimiter(byte b, byte[] delimiters) {
		for (int i = 0; i < delimiters.length; i++)
			if (b == delimiters[i])
				return true;
		return false;
	}

	private void removeDelimitersFromHead(byte[] delimiters) {
		int i = beginIndex;
		while (i < endIndex && isDelimiter(buffer[i++], delimiters))
			beginIndex++;
	}

}
