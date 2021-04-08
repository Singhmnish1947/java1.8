/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.encoders;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
@Sharable
public abstract class NettyStringEncoder extends OneToOneEncoder {
	protected final Charset charset;
	protected final int primaryBitMapOffset;
	protected final int primaryBitMapLength;
	protected final int secondaryBitMapLength;
	protected static final Log Logger = LogFactory.getLog(NettyStringEncoder.class.getName());

	public NettyStringEncoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength) {
		this(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, Charset.defaultCharset());
	}

	public NettyStringEncoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength,
			Charset charset) {
		if (charset == null) {
			throw new NullPointerException("charset");
		} else {
			this.charset = charset;
			this.primaryBitMapOffset = primaryBitMapOffset;
			this.primaryBitMapLength = primaryBitMapLength;
			this.secondaryBitMapLength = secondaryBitMapLength;
		}
	}

	protected byte[] toByteArray(String no) {
		byte[] number = new byte[no.length() / 2];

		for (int i = 0; i < no.length(); i += 2) {
			int j = Integer.parseInt(no.substring(i, i + 2), 16);
			number[i / 2] = (byte) (j & 255);
		}

		return number;
	}

	protected byte[] binaryStringToByteArray(String s) {
		ByteBuffer bytes = null;
		if (s.length() % 8 != 0) {
			Logger.debug("String parameter must be a multiple of 8 characters long: " + s);
			return null;
		} else {
			bytes = ByteBuffer.allocate(s.length() / 8);
			ByteBuffer tempBytes = null;

			for (int i = 0; i < s.length(); i += 8) {
				tempBytes = ByteBuffer.allocate(1);
				int tempInt = Integer.parseInt(s.substring(i, i + 8), 2);
				tempBytes.put((byte) tempInt);
				bytes.position(i / 8);
				bytes.put(tempBytes.array());
			}

			return bytes.array();
		}
	}

	protected boolean hasSecondaryBitMap(String primaryBitMapStr) {
		String firstHexChar = primaryBitMapStr.substring(0, 1);
		int i = Integer.valueOf(firstHexChar, 16);
		return i > 7;
	}

	protected String getBinaryFromHex(String hex) {
		String binStr = "";
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < hex.length(); ++i) {
			int val = Integer.parseInt(hex.substring(i, i + 1), 16);

			for (binStr = Integer.toBinaryString(val); binStr.length() != 4; binStr = "0" + binStr) {
				;
			}

			sb.append(binStr);
		}

		return sb.toString();
	}
}
