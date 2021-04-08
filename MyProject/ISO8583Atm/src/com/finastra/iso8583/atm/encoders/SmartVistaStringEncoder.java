/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.encoders;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;

/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
@ChannelHandler.Sharable
public class SmartVistaStringEncoder extends NettyStringEncoder {

	private static final Log Logger = LogFactory.getLog(SmartVistaStringEncoder.class.getName());

	public SmartVistaStringEncoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength) {
		this(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, Charset.defaultCharset());
	}

	public SmartVistaStringEncoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength,
			Charset charset) {
		super(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, charset);
	}

	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		try {
			if (!(msg instanceof String)) {
				return msg;
			}
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
			readIndex += this.primaryBitMapOffset;
			primaryBitMapStr = msgStr.substring(readIndex, readIndex + this.primaryBitMapLength);
			readIndex += this.primaryBitMapLength;
			if (hasSecondaryBitMap(primaryBitMapStr)) {
				if (Logger.isInfoEnabled()) {
					Logger.info("Secondary bitmap is present");
				}
				secondaryBitMapStr = msgStr.substring(readIndex, readIndex + this.secondaryBitMapLength);
				readIndex += this.secondaryBitMapLength;
				secondaryBitMapByteArray = toByteArray(secondaryBitMapStr);
				secondaryBitMapBuffer = ByteBuffer.allocate(secondaryBitMapByteArray.length);
				secondaryBitMapBuffer.put(secondaryBitMapByteArray);
				secondaryBitMapBuffer.rewind();
			}
			msgRemainderStr = msgStr.substring(readIndex);

			headerBuffer = ChannelBuffers.copiedBuffer(headerStr, this.charset);
			primaryBitMapByteArray = toByteArray(primaryBitMapStr);
			primaryBitMapBuffer = ByteBuffer.allocate(primaryBitMapByteArray.length);
			primaryBitMapBuffer.put(primaryBitMapByteArray);
			primaryBitMapBuffer.rewind();

			msgRemainderBuffer = ChannelBuffers.copiedBuffer(msgRemainderStr, this.charset);
			if (hasSecondaryBitMap(primaryBitMapStr)) {
				ByteBuffer[] byteBuffers = { headerBuffer.toByteBuffer(), primaryBitMapBuffer, secondaryBitMapBuffer,
						msgRemainderBuffer.toByteBuffer() };
				return ChannelBuffers.copiedBuffer(byteBuffers);
			}
			ByteBuffer[] byteBuffers = { headerBuffer.toByteBuffer(), primaryBitMapBuffer,
					msgRemainderBuffer.toByteBuffer() };
			return ChannelBuffers.copiedBuffer(byteBuffers);

		} catch (Exception e) {
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			Logger.error(null != e.getCause() ? ExceptionUtil.getExceptionAsString(e.getCause())
					: ExceptionUtil.getExceptionAsString(e));
			return null;
		}
	}
}
