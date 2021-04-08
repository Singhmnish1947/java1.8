/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.decoders;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public class TietoFramedecoder extends NettyFrameDecoder {
	public TietoFramedecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
		this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0, Charset.defaultCharset());
	}

	public TietoFramedecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
			int initialBytesToStrip, Charset charset) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, charset);
	}

	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (discardingTooLongFrame) {
			Logger.info("Discarding , too long frame");
			long bytesToDiscard = this.bytesToDiscard;
			int localBytesToDiscard = (int) Math.min(bytesToDiscard, buffer.readableBytes());
			buffer.skipBytes(localBytesToDiscard);
			bytesToDiscard -= localBytesToDiscard;
			this.bytesToDiscard = bytesToDiscard;
			if (bytesToDiscard == 0L) {
				long tooLongFrameLength = this.tooLongFrameLength;
				this.tooLongFrameLength = 0L;
				fail(ctx, tooLongFrameLength);
			}
			return null;
		}
		if (buffer.readableBytes() < lengthFieldEndOffset) {
			Logger.info("lengthFieldEndOffset"+ lengthFieldEndOffset + "is greater than readable bytes in Channelbuffer");
			return null;
		}
		int actualLengthFieldOffset = buffer.readerIndex() + lengthFieldOffset;

		ChannelBuffer msgLengthBuffer = buffer.slice(actualLengthFieldOffset, lengthFieldLength);
		long frameLength = Long.parseLong(msgLengthBuffer.toString(charset));
		if (frameLength < 0L) {
			buffer.skipBytes(lengthFieldEndOffset);
			Logger.info("negative pre-adjustment length field:" + frameLength);
			throw new CorruptedFrameException("negative pre-adjustment length field: " + frameLength);
		}
		if (frameLength < lengthFieldEndOffset) {
			buffer.skipBytes(lengthFieldEndOffset);
			Logger.info("Adjusted frame length is less than lengthFieldEndOffset: " + lengthFieldEndOffset);
			throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less "
					+ "than lengthFieldEndOffset: " + lengthFieldEndOffset);
		}
		if (frameLength > maxFrameLength) {
			Logger.info(frameLength +"is greater than maxFrameLength" );
			discardingTooLongFrame = true;
			this.tooLongFrameLength = frameLength;
			this.bytesToDiscard = (frameLength - buffer.readableBytes());
			buffer.skipBytes(buffer.readableBytes());
			return null;
		}
		int frameLengthInt = (int) frameLength;
		if (buffer.readableBytes() < frameLengthInt) {
			return null;
		}
		if (initialBytesToStrip > frameLengthInt) {
			buffer.skipBytes(frameLengthInt);
			throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less "
					+ "than initialBytesToStrip: " + initialBytesToStrip);
		}
		buffer.skipBytes(initialBytesToStrip);

		int readerIndex = buffer.readerIndex();
		int actualFrameLength = frameLengthInt - initialBytesToStrip + lengthAdjustment;
		ChannelBuffer frame = extractFrame(buffer, readerIndex, actualFrameLength);
		buffer.readerIndex(readerIndex + actualFrameLength);

		return frame;
	}
}
