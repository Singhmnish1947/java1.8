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

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;

/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public class SmartVistaStringDecoder extends NettyStringDecoder {

	private static final Log Logger = LogFactory.getLog(SmartVistaStringDecoder.class.getName());

	public SmartVistaStringDecoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength) {
		this(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, Charset.defaultCharset());
	}

	public SmartVistaStringDecoder(int primaryBitMapOffset, int primaryBitMapLength, int secondaryBitMapLength,
			Charset charset) {
		super(primaryBitMapOffset, primaryBitMapLength, secondaryBitMapLength, charset);
	}

	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		try {
			if (!(msg instanceof ChannelBuffer)) {
				return msg;
			}
			ChannelBuffer headerBuffer = null;
			ChannelBuffer primaryBitMapBuffer = null;
			ChannelBuffer secondaryBitMapBuffer = null;
			ChannelBuffer msgRemainderBuffer = null;

			String headerStr = "";
			String primaryBitMapStr = "";
			String secondaryBitMapStr = "";
			String msgRemainderStr = "";
			ChannelBuffer buffer = (ChannelBuffer) msg;
			if (buffer.readableBytes() < this.primaryBitMapOffset) {
				Logger.info("Readable byte in buffer is less than primary bitmap offset");
				return null;
			}
			headerBuffer = buffer.copy(buffer.readerIndex(), this.primaryBitMapOffset);
			headerStr = headerBuffer.toString(this.charset);
			buffer.skipBytes(this.primaryBitMapOffset);
			primaryBitMapBuffer = buffer.copy(buffer.readerIndex(), this.primaryBitMapLength);
			primaryBitMapStr = getHexString(primaryBitMapBuffer.array());
			buffer.skipBytes(this.primaryBitMapLength);
			if (hasSecondaryBitMap(primaryBitMapBuffer)) {
				secondaryBitMapBuffer = buffer.copy(buffer.readerIndex(), this.secondaryBitMapLength);
				secondaryBitMapStr = getHexString(secondaryBitMapBuffer.array());
				buffer.skipBytes(this.secondaryBitMapLength);
			}
			msgRemainderBuffer = buffer.copy(buffer.readerIndex(), buffer.readableBytes());
			msgRemainderStr = msgRemainderBuffer.toString(this.charset);

			String decodedMsg = headerStr + primaryBitMapStr + secondaryBitMapStr + msgRemainderStr;
			if (Logger.isInfoEnabled()) {
				Logger.info("Decoded message in SmartVistaStringDecoder" + decodedMsg);
			}
			return decodedMsg;
		} catch (Exception e) {
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			Logger.error(null != e.getCause() ? ExceptionUtil.getExceptionAsString(e.getCause())
					: ExceptionUtil.getExceptionAsString(e));
			return null;
		}
	}
}
