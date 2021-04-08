/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.encoders;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;

public class SmartVistaFrameEncoder extends NettyFrameEncoder {

	private static final Log Logger = LogFactory.getLog(SmartVistaFrameEncoder.class.getName());

	public SmartVistaFrameEncoder(int lengthFieldOffset, int lengthFieldLength) {
		this(lengthFieldOffset, lengthFieldLength, false, Charset.defaultCharset());
	}

	public SmartVistaFrameEncoder(int lengthFieldOffset, int lengthFieldLength, boolean lengthIncludesLengthFieldLength,
			Charset charset) {
		super(lengthFieldOffset, lengthFieldLength, lengthIncludesLengthFieldLength, charset);
	}

	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		try {
			if (!(msg instanceof ChannelBuffer)) {
				return msg;
			}
			ChannelBuffer body = (ChannelBuffer) msg;

			int actualLengthFieldOffset = body.readerIndex() + this.lengthFieldOffset;
			int msglength;
			if (this.lengthIncludesLengthFieldLength) {
				msglength = body.readableBytes();
			} else {
				msglength = body.readableBytes() - this.lengthFieldLength;
			}
			ChannelBuffer msgLengthBuffer = body.slice(actualLengthFieldOffset, this.lengthFieldLength);
			msgLengthBuffer.setBytes(0, stringToByteBuffer(Integer.toString(msglength)));

			return body;

		} catch (Exception e) {
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			Logger.error(null != e.getCause() ? ExceptionUtil.getExceptionAsString(e.getCause())
					: ExceptionUtil.getExceptionAsString(e));
			return null;
		}
	}
}