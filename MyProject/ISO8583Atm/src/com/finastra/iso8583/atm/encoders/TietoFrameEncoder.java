/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.encoders;

import com.finastra.iso8583.atm.encoders.NettyFrameEncoder;
import java.nio.charset.Charset;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public class TietoFrameEncoder extends NettyFrameEncoder {
	public TietoFrameEncoder(int lengthFieldOffset, int lengthFieldLength) {
		this(lengthFieldOffset, lengthFieldLength, false, Charset.defaultCharset());
	}

	public TietoFrameEncoder(int lengthFieldOffset, int lengthFieldLength,
			boolean lengthIncludesLengthFieldLength, Charset charset) {
		super(lengthFieldOffset, lengthFieldLength, lengthIncludesLengthFieldLength, charset);
	}

	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		} else {
			ChannelBuffer body = (ChannelBuffer) msg;
			int actualLengthFieldOffset = body.readerIndex() + this.lengthFieldOffset;
			int msglength = body.readableBytes();
			ChannelBuffer msgLengthBuffer = body.slice(actualLengthFieldOffset, this.lengthFieldLength);
			msgLengthBuffer.setBytes(0, this.stringToByteBuffer(Integer.toString(msglength)));
			return body;
		}
	}
}
