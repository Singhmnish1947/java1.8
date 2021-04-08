/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.decoders;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public abstract class NettyStringDecoder extends OneToOneDecoder {
	protected Charset charset;
	protected final int primaryBitMapOffset;
	protected final int primaryBitMapLength;
	protected final int secondaryBitMapLength;
	protected static final String HEXES = "0123456789ABCDEF";
	protected static final Log Logger = LogFactory.getLog(NettyStringDecoder.class.getName());

	public NettyStringDecoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength) {
		this(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, Charset.defaultCharset());
	}

	public NettyStringDecoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength,
			Charset charset) {
		this.primaryBitMapOffset = primaryBitMapOffset;
		this.primaryBitMapLength = primaryBitMapLength;
		this.secondaryBitMapLength = secondaryBitMapLength;
		if (charset == null) {
			Logger.info("CharSet is null");
			throw new NullPointerException("charset");
		}
		this.charset = charset;
	}

	protected String getHexString(byte[] raw) {
		if (raw == null) {
			return null;
		}
		StringBuilder hex = new StringBuilder(2 * raw.length);
		for (byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt(b & 0xF));
		}
		if(Logger.isInfoEnabled()) {
		Logger.info(hex.toString());
		}
		return hex.toString();
	}

	protected String getBinaryString(byte[] raw) {
		StringBuilder sb = new StringBuilder(raw.length * 8);
		for (int i = 0; i < 8 * raw.length; i++) {
			sb.append((raw[(i / 8)] << i % 8 & 0x80) == 0 ? '0' : '1');
		}
		if(Logger.isInfoEnabled()) {
		Logger.info(sb.toString());
		}
		return sb.toString();
	}

	protected boolean hasSecondaryBitMap(ChannelBuffer primaryBitMapBuffer) {
		if (primaryBitMapBuffer == null) {
			return false;
		}
		byte firstByte = primaryBitMapBuffer.getByte(0);
		if (firstByte <0) {
			if(Logger.isInfoEnabled()) {
			Logger.info("Secondary bitmap is present");
			}
			return true;

		}
		return false;
	}
}
