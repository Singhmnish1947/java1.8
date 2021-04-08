/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.decoders;

import java.nio.charset.Charset;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;

import com.finastra.iso8583.atm.encoders.SmartVistaFrameEncoder;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;

/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public class SmartVistaFrameDecoder extends NettyFrameDecoder {

	private static final Log Logger = LogFactory.getLog(SmartVistaFrameDecoder.class.getName());

	public SmartVistaFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
		this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 4, 0, Charset.defaultCharset());
	}

	public SmartVistaFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
			int lengthAdjustment, int initialBytesToStrip, Charset charset) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, charset);
	}

	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		try {
			if (this.discardingTooLongFrame) {
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
			if (buffer.readableBytes() < this.lengthFieldEndOffset) {
				Logger.info("lengthFieldEndOffset" + lengthFieldEndOffset
						+ "is greater than readable bytes in Channelbuffer");
				return null;
			}
			int actualLengthFieldOffset = buffer.readerIndex() + this.lengthFieldOffset;
			ChannelBuffer msgLengthBuffer = buffer.slice(actualLengthFieldOffset, this.lengthFieldLength);
			long frameLength = Long.parseLong(msgLengthBuffer.toString(this.charset));
			if (frameLength < 0L) {
				Logger.info("negative pre-adjustment length field:" + frameLength);

				buffer.skipBytes(this.lengthFieldEndOffset);
				throw new CorruptedFrameException("negative pre-adjustment length field: " + frameLength);
			}
			if (frameLength < this.lengthFieldEndOffset) {
				Logger.info("Adjusted frame length is less than lengthFieldEndOffset: " + lengthFieldEndOffset);

				buffer.skipBytes(this.lengthFieldEndOffset);
				throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less "
						+ "than lengthFieldEndOffset: " + this.lengthFieldEndOffset);
			}
			if (frameLength > this.maxFrameLength) {
				Logger.info(frameLength + "is greater than maxFrameLength");
				this.discardingTooLongFrame = true;
				this.tooLongFrameLength = frameLength;
				this.bytesToDiscard = (frameLength - buffer.readableBytes());
				buffer.skipBytes(buffer.readableBytes());
				return null;
			}
			int frameLengthInt = (int) frameLength;
			if (buffer.readableBytes() < frameLengthInt) {
				return null;
			}
			if (this.initialBytesToStrip > frameLengthInt) {
				buffer.skipBytes(frameLengthInt);
				throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less "
						+ "than initialBytesToStrip: " + this.initialBytesToStrip);
			}
			buffer.skipBytes(this.initialBytesToStrip);

			int readerIndex = buffer.readerIndex();
			int actualFrameLength = frameLengthInt - this.initialBytesToStrip + this.lengthAdjustment;
			ChannelBuffer frame = extractFrame(buffer, readerIndex, actualFrameLength);
			buffer.readerIndex(readerIndex + actualFrameLength);
			return frame;
		} catch (Exception e) {
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			Logger.error(null != e.getCause() ? ExceptionUtil.getExceptionAsString(e.getCause())
					: ExceptionUtil.getExceptionAsString(e));
			return null;
		}
	}
}
