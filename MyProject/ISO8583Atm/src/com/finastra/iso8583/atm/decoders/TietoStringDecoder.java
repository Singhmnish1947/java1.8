/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.decoders;

import java.math.BigInteger;
import java.nio.charset.Charset;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public class TietoStringDecoder extends NettyStringDecoder {
	public TietoStringDecoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength) {
		this(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, Charset.defaultCharset());
	}

	public TietoStringDecoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength,
			Charset charset) {
		super(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, charset);
	}

	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}
		ChannelBuffer headerBuffer = null;
		ChannelBuffer primaryBitMapBuffer = null;
		ChannelBuffer secondaryBitMapBuffer = null;
		ChannelBuffer msgRemainderBuffer = null;

		String headerStr = "";
		String primaryBitMapBinaryStr = "";
		String primaryBitMapStr = "";
		String secondaryBitMapBinaryStr = "";
		String secondaryBitMapStr = "";
		String msgRemainderStr = "";

		ChannelBuffer buffer = (ChannelBuffer) msg;
		if (buffer.readableBytes() < primaryBitMapOffset) {
			Logger.info("Readable byte in buffer is less than primary bitmap offset");
			return null;
		}
		headerBuffer = buffer.copy(buffer.readerIndex(), primaryBitMapOffset);
		headerStr = headerBuffer.toString(charset);
		buffer.skipBytes(primaryBitMapOffset);
		primaryBitMapBuffer = buffer.copy(buffer.readerIndex(), primaryBitMapLength);
		primaryBitMapBinaryStr = getBinaryString(primaryBitMapBuffer.array());
		primaryBitMapStr = getHexFromBinaryString(primaryBitMapBinaryStr);
		buffer.skipBytes(primaryBitMapLength);
		if (hasSecondaryBitMap(primaryBitMapBuffer)) {
			secondaryBitMapBuffer = buffer.copy(buffer.readerIndex(), secondaryBitMapLength);
			secondaryBitMapBinaryStr = getBinaryString(secondaryBitMapBuffer.array());
			secondaryBitMapStr = getHexFromBinaryString(secondaryBitMapBinaryStr);

			buffer.skipBytes(secondaryBitMapLength);
		}
		msgRemainderBuffer = buffer.copy(buffer.readerIndex(), buffer.readableBytes());
		msgRemainderStr = msgRemainderBuffer.toString(charset);

		String decodedMsg = headerStr + primaryBitMapStr + secondaryBitMapStr + msgRemainderStr;
		if(Logger.isInfoEnabled()) {
		Logger.info("Decoded message in TietoStringDecoder"+ decodedMsg);
		}
		return decodedMsg;
	}

	public static String getHexFromBinaryString(String binary) {
		BigInteger bi = new BigInteger(binary, 2);
		String hex = bi.toString(16);
		while (hex.length() != binary.length() / 4) {
			hex = "0" + hex;
		}
		return hex.toUpperCase();
	}
}
