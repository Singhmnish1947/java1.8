/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.encoders;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public abstract class NettyFrameEncoder extends OneToOneEncoder {
	protected final int lengthFieldLength;
	protected final int lengthFieldOffset;
	protected final boolean lengthIncludesLengthFieldLength;
	protected final Charset charset;
	protected static final Log Logger = LogFactory.getLog(NettyFrameEncoder.class.getName());

	public NettyFrameEncoder(int lengthFieldOffset, int lengthFieldLength) {
		this(lengthFieldOffset, lengthFieldLength, false, Charset.defaultCharset());
	}

	public NettyFrameEncoder(int lengthFieldOffset, int lengthFieldLength, boolean lengthIncludesLengthFieldLength,
			Charset charset) {
		this.lengthFieldLength = lengthFieldLength;
		this.lengthFieldOffset = lengthFieldOffset;
		this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
		this.charset = charset;
	}

	protected ByteBuffer stringToByteBuffer(String msg) {
		try {
			msg = String.format("%" + 4 + "s", msg).replace(' ', '0');
			CharsetEncoder encoder = this.charset.newEncoder();
			return encoder.encode(CharBuffer.wrap(msg));
		} catch (Exception e) {
			Logger.info(e.getCause() != null ? ExceptionUtils.getStackTrace(e.getCause())
					: ExceptionUtils.getStackTrace(e));
			return null;
		}
	}

	protected ByteBuffer stringTwoByteToByteBuffer(String msg) {
		try {
			msg = String.format("%" + 4 + "s", msg).replace(' ', '0');
			Integer y = Integer.parseInt(msg);
			String m = Integer.toHexString(y);
			while (m.length() != 4) {
				m = "0" + m;
			}
			byte[] encodedMsg = hexStringToByteArray(m);
			ByteBuffer buf = ByteBuffer.wrap(encodedMsg);
			return buf;
		} catch (Exception e) {
			Logger.info(e.getCause() != null ? ExceptionUtils.getStackTrace(e.getCause())
					: ExceptionUtils.getStackTrace(e));
			return null;
		}
	}

	private byte[] hexStringToByteArray(String m) {
		int len = m.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[(i / 2)] = ((byte) ((Character.digit(m.charAt(i), 16) << 4) + Character.digit(m.charAt(i + 1), 16)));
		}
		return data;
	}

	protected ByteBuffer binaryToByteBuffer(String msg) {
		try {
			msg = String.format("%" + this.lengthFieldLength + "s", msg).replace(' ', '0');
			CharsetEncoder encoder = this.charset.newEncoder();
			return encoder.encode(CharBuffer.wrap(msg));
		} catch (Exception e) {
			Logger.info(e.getCause() != null ? ExceptionUtils.getStackTrace(e.getCause())
					: ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
}
