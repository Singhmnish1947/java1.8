/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.encoders;

import com.finastra.iso8583.atm.encoders.NettyStringEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelHandler.Sharable;
/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
@Sharable
public class TietoStringEncoder extends NettyStringEncoder {
	public TietoStringEncoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength) {
		this(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, Charset.defaultCharset());
	}

	public TietoStringEncoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength,
			Charset charset) {
		super(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, charset);
	}

	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof String)) {
			return msg;
		} else {
			String headerStr = "";
			String primaryBitMapStr = "";
			String secondaryBitMapStr = "";
			String msgRemainderStr = "";
			ChannelBuffer headerBuffer = null;
			byte[] primaryBitMapByteArray = null;
			ByteBuffer primaryBitMapBuffer = null;
			byte[] secondaryBitMapByteArray = null;
			ByteBuffer secondaryBitMapBuffer = null;
			ChannelBuffer msgRemainderBuffer = null;
			String msgStr = (String) msg;
			int readIndex = 0;
			headerStr = msgStr.substring(readIndex, readIndex + this.primaryBitMapOffset);
			readIndex = readIndex + this.primaryBitMapOffset;
			primaryBitMapStr = msgStr.substring(readIndex, readIndex + this.primaryBitMapLength);
			readIndex += this.primaryBitMapLength;
			if (this.hasSecondaryBitMap(primaryBitMapStr)) {
				secondaryBitMapStr = msgStr.substring(readIndex, readIndex + this.secondaryBitMapLength);
				readIndex += this.secondaryBitMapLength;
				secondaryBitMapByteArray = this.toByteArray(secondaryBitMapStr);
				secondaryBitMapBuffer = ByteBuffer.allocate(secondaryBitMapByteArray.length);
				secondaryBitMapBuffer.put(secondaryBitMapByteArray);
				secondaryBitMapBuffer.rewind();
			}

			msgRemainderStr = msgStr.substring(readIndex);
			headerBuffer = ChannelBuffers.copiedBuffer(headerStr, this.charset);
			 primaryBitMapByteArray = this.toByteArray(primaryBitMapStr);
			primaryBitMapBuffer = ByteBuffer.allocate(primaryBitMapByteArray.length);
			primaryBitMapBuffer.put(primaryBitMapByteArray);
			primaryBitMapBuffer.rewind();
			msgRemainderBuffer = ChannelBuffers.copiedBuffer(msgRemainderStr, this.charset);
			ByteBuffer[] byteBuffers;
			if (this.hasSecondaryBitMap(primaryBitMapStr)) {
				byteBuffers = new ByteBuffer[]{headerBuffer.toByteBuffer(), primaryBitMapBuffer, secondaryBitMapBuffer,
						msgRemainderBuffer.toByteBuffer()};
				if(Logger.isInfoEnabled()) {
				Logger.info("Secondary bitmap is present");
				}
				return ChannelBuffers.copiedBuffer(byteBuffers);
			} else {
				byteBuffers = new ByteBuffer[]{headerBuffer.toByteBuffer(), primaryBitMapBuffer,
						msgRemainderBuffer.toByteBuffer()};
				if(Logger.isInfoEnabled()) {
				Logger.info("Secondary bitmap is absent");
				}
				return ChannelBuffers.copiedBuffer(byteBuffers);
			}
		}
	}
}
