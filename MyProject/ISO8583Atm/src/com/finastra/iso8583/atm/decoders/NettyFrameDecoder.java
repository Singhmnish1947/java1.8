/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.decoders;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public abstract class NettyFrameDecoder extends FrameDecoder {
	protected final int maxFrameLength;
	protected final int lengthFieldOffset;
	protected final int lengthFieldLength;
	protected final int lengthFieldEndOffset;
	protected final int lengthAdjustment;
	protected final int initialBytesToStrip;
	protected boolean discardingTooLongFrame;
	protected long tooLongFrameLength;
	protected long bytesToDiscard;
	protected Charset charset;
	protected static final Log Logger = LogFactory.getLog(NettyFrameDecoder.class.getName());

	public NettyFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
		this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0, Charset.defaultCharset());
	}

	public NettyFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
			int initialBytesToStrip, Charset charset) {
		if (maxFrameLength <= 0) {
			Logger.info("maxFrameLength must be a positive integer: " + maxFrameLength);
			throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength);

		}
		if (lengthFieldOffset < 0) {
			Logger.info("lengthFieldOffset must be a non-negative integer: " + lengthFieldOffset);
			throw new IllegalArgumentException(
					"lengthFieldOffset must be a non-negative integer: " + lengthFieldOffset);
		}
		if (initialBytesToStrip < 0) {
			Logger.info("initialBytesToStrip must be a non-negative integer: " + initialBytesToStrip);
			throw new IllegalArgumentException(
					"initialBytesToStrip must be a non-negative integer: " + initialBytesToStrip);
		}
		if (lengthFieldOffset > maxFrameLength - lengthFieldLength) {
			Logger.info("maxFrameLength must be equal to or greater than " + "lengthFieldOffset (" + lengthFieldOffset + ") + "
					+ "lengthFieldLength (" + lengthFieldLength + ").");
			throw new IllegalArgumentException("maxFrameLength (" + maxFrameLength + ") "
					+ "must be equal to or greater than " + "lengthFieldOffset (" + lengthFieldOffset + ") + "
					+ "lengthFieldLength (" + lengthFieldLength + ").");
		}
		this.maxFrameLength = maxFrameLength;
		this.lengthFieldOffset = lengthFieldOffset;
		this.lengthFieldLength = lengthFieldLength;
		this.lengthAdjustment = lengthAdjustment;
		lengthFieldEndOffset = (lengthFieldOffset + lengthFieldLength);
		this.initialBytesToStrip = initialBytesToStrip;
		this.charset = charset;
	}

	protected void fail(ChannelHandlerContext ctx, long frameLength) {
		if (frameLength > 0L) {
			Channels.fireExceptionCaught(ctx.getChannel(), new TooLongFrameException(
					"Adjusted frame length exceeds " + maxFrameLength + ": " + frameLength + " - discarded"));
		} else {
			Channels.fireExceptionCaught(ctx.getChannel(),
					new TooLongFrameException("Adjusted frame length exceeds " + maxFrameLength + " - discarding"));
		}
	}

	protected ChannelBuffer extractFrame(ChannelBuffer buffer, int index, int length) {
		ChannelBuffer frame = buffer.factory().getBuffer(length);
		frame.writeBytes(buffer, index, length);
		return frame;
	}
}
