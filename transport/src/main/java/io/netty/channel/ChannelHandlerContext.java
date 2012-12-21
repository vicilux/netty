/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

import java.nio.channels.Channels;
import java.util.Set;

/**
 * Enables a {@link ChannelHandler} to interact with its {@link ChannelPipeline}
 * and other handlers.  A handler can notify the next {@link ChannelHandler} in the {@link ChannelPipeline},
 * modify the {@link ChannelPipeline} it belongs to dynamically.
 *
 * <h3>Notify</h3>
 *
 * You can notify the closest handler in the
 * same {@link ChannelPipeline} by calling one of the various methods which are listed in {@link ChannelInboundInvoker}
 * and {@link ChannelOutboundInvoker}.  Please refer to {@link ChannelPipeline} to understand how an event flows.
 *
 * <h3>Modifying a pipeline</h3>
 *
 * You can get the {@link ChannelPipeline} your handler belongs to by calling
 * {@link #pipeline()}.  A non-trivial application could insert, remove, or
 * replace handlers in the pipeline dynamically in runtime.
 *
 * <h3>Retrieving for later use</h3>
 *
 * You can keep the {@link ChannelHandlerContext} for later use, such as
 * triggering an event outside the handler methods, even from a different thread.
 * <pre>
 * public class MyHandler extends {@link ChannelHandlerAdapter} {
 *
 *     <b>private {@link ChannelHandlerContext} ctx;</b>
 *
 *     public void beforeAdd({@link ChannelHandlerContext} ctx) {
 *         <b>this.ctx = ctx;</b>
 *     }
 *
 *     public void login(String username, password) {
 *         ctx.write(new LoginMessage(username, password));
 *     }
 *     ...
 * }
 * </pre>
 *
 * <h3>Storing stateful information</h3>
 *
 * {@link #attr(AttributeKey)} allow you to
 * store and access stateful information that is related with a handler and its
 * context.  Please refer to {@link ChannelHandler} to learn various recommended
 * ways to manage stateful information.
 *
 * <h3>A handler can have more than one context</h3>
 *
 * Please note that a {@link ChannelHandler} instance can be added to more than
 * one {@link ChannelPipeline}.  It means a single {@link ChannelHandler}
 * instance can have more than one {@link ChannelHandlerContext} and therefore
 * the single instance can be invoked with different
 * {@link ChannelHandlerContext}s if it is added to one or more
 * {@link ChannelPipeline}s more than once.
 * <p>
 * For example, the following handler will have as many independent attachments
 * as how many times it is added to pipelines, regardless if it is added to the
 * same pipeline multiple times or added to different pipelines multiple times:
 * <pre>
 * public class FactorialHandler extends {@link ChannelInboundMessageHandlerAdapter}&lt{@link Integer}&gt {
 *
 *   private final {@link AttributeKey}&lt{@link Integer}&gt counter =
 *           new {@link AttributeKey}&lt{@link Integer}&gt("counter");
 *
 *   // This handler will receive a sequence of increasing integers starting
 *   // from 1.
 *   {@code @Override}
 *   public void messageReceived({@link ChannelHandlerContext} ctx, {@link Integer} integer) {
 *     {@link Attribute}&lt{@link Integer}&gt attr = ctx.getAttr(counter);
 *     Integer a = ctx.getAttr(counter).get();
 *
 *     if (a == null) {
 *       a = 1;
 *     }
 *
 *     attr.set(a * integer));
 *   }
 * }
 *
 * // Different context objects are given to "f1", "f2", "f3", and "f4" even if
 * // they refer to the same handler instance.  Because the FactorialHandler
 * // stores its state in a context object (as an attachment), the factorial is
 * // calculated correctly 4 times once the two pipelines (p1 and p2) are active.
 * FactorialHandler fh = new FactorialHandler();
 *
 * {@link ChannelPipeline} p1 = {@link Channels}.pipeline();
 * p1.addLast("f1", fh);
 * p1.addLast("f2", fh);
 *
 * {@link ChannelPipeline} p2 = {@link Channels}.pipeline();
 * p2.addLast("f3", fh);
 * p2.addLast("f4", fh);
 * </pre>
 *
 * <h3>Additional resources worth reading</h3>
 * <p>
 * Please refer to the {@link ChannelHandler}, and
 * {@link ChannelPipeline} to find out more about inbound and outbound operations,
 * what fundamental differences they have, how they flow in a  pipeline,  and how to handle
 * the operation in your application.
 * @apiviz.owns io.netty.channel.ChannelHandler
 */
public interface ChannelHandlerContext
         extends AttributeMap, ChannelPropertyAccess,
                 ChannelInboundInvoker, ChannelOutboundInvoker {

    /**
     * Return the {@link Channel} which is bound to the {@link ChannelHandlerContext}.
     */
    Channel channel();

    /**
     * The {@link EventExecutor} that is used to dispatch the events. This can also be used to directly
     * submit tasks that get executed in the event loop. For more informations please refer to the
     * {@link EventExecutor} javadocs.
     */
    EventExecutor executor();

    /**
     * The unique name of the {@link ChannelHandlerContext}.The name was used when then {@link ChannelHandler}
     * was added to the {@link ChannelPipeline}. This name can also be used to access the registered
     * {@link ChannelHandler} from the {@link ChannelPipeline}.
     */
    String name();

    /**
     * The {@link ChannelHandler} that is bound this {@link ChannelHandlerContext}.
     */
    ChannelHandler handler();
    Set<ChannelHandlerType> types();

    /**
     * Return {@code true} if the {@link ChannelHandlerContext} has an {@link ByteBuf} bound for inbound
     * which can be used.
     */
    boolean hasInboundByteBuffer();

    /**
     * Return {@code true} if the {@link ChannelHandlerContext} has a {@link MessageBuf} bound for inbound
     * which can be used.
     */
    boolean hasInboundMessageBuffer();

    /**
     * Return the bound {@link ByteBuf} for inbound data if {@link #hasInboundByteBuffer()} returned
     * {@code true}. If {@link #hasInboundByteBuffer()} returned {@code false} it will throw a
     * {@link UnsupportedOperationException}
     */
    ByteBuf inboundByteBuffer();

    /**
     * Return the bound {@link MessageBuf} for inbound data if {@link #hasInboundMessageBuffer()} returned
     * {@code true}. If {@link #hasInboundMessageBuffer()} returned {@code false} it will throw a
     * {@link UnsupportedOperationException}.
     */
    <T> MessageBuf<T> inboundMessageBuffer();

    /**
     * Return {@code true} if the {@link ChannelHandlerContext} has an {@link ByteBuf} bound for outbound
     * data which can be used.
     *
     */
    boolean hasOutboundByteBuffer();

    /**
     * Return {@code true} if the {@link ChannelHandlerContext} has a {@link MessageBuf} bound for outbound
     * which can be used.
     */
    boolean hasOutboundMessageBuffer();

    /**
     * Return the bound {@link ByteBuf} for outbound data if {@link #hasOutboundByteBuffer()} returned
     * {@code true}. If {@link #hasOutboundByteBuffer()} returned {@code false} it will throw
     * a {@link UnsupportedOperationException}.
     */
    ByteBuf outboundByteBuffer();

    /**
     * Return the bound {@link MessageBuf} for outbound data if {@link #hasOutboundMessageBuffer()} returned
     * {@code true}. If {@link #hasOutboundMessageBuffer()} returned {@code false} it will throw a
     * {@link UnsupportedOperationException}
     */
    <T> MessageBuf<T> outboundMessageBuffer();

    /**
     * Replaces the inbound byte buffer with the given buffer.  This returns the
     * old buffer, so any readable bytes can be handled appropriately by the caller.
     * <p>
     * Be cautious with caching {@link #inboundByteBuffer()} as it may change as a result of this
     * method.  For example, instead of extending {@link io.netty.handler.codec.ByteToMessageDecoder},
     * extend what that class does (currently, {@link ChannelInboundHandlerAdapter} and
     * {@link ChannelInboundByteHandler}.  In other words, implementing your own
     * {@link ChannelInboundHandlerAdapter#inboundBufferUpdated}/{@link ChannelStateHandler#inboundBufferUpdated}
     * will help guarantee a replaced buffer won't be missed.</p>
     *
     * @param newInboundByteBuf the new inbound byte buffer
     * @return the old buffer.
     * @throws NullPointerException if the argument is {@code null}.
     */
    ByteBuf replaceInboundByteBuffer(ByteBuf newInboundByteBuf);

    /**
     * Replaces the inbound message buffer with the given buffer.  This returns the
     * old buffer, so any pending messages can be handled appropriately by the caller.
     * <p>
     * Be cautious with caching {@link #inboundMessageBuffer()} as it may change as a result of this
     * method.  For example, instead of extending {@link io.netty.handler.codec.MessageToMessageDecoder},
     * extend what that class does (currently, {@link ChannelInboundHandlerAdapter} and
     * {@link ChannelInboundMessageHandler}.  In other words, implementing your own
     * {@link ChannelInboundHandlerAdapter#inboundBufferUpdated}/{@link ChannelStateHandler#inboundBufferUpdated}
     * will help guarantee a replaced buffer won't be missed.</p>
     *
     * @param newInboundMsgBuf the new inbound message buffer
     * @return the old buffer.
     * @throws NullPointerException if the argument is {@code null}.
     */
    <T> MessageBuf<T> replaceInboundMessageBuffer(MessageBuf<T> newInboundMsgBuf);

    /**
     * Replaces the outbound byte buffer with the given buffer.  This returns the
     * old buffer, so any readable bytes can be handled appropriately by the caller.
     * <p>
     * Be cautious with caching {@link #outboundByteBuffer()} as it may change as a result of this
     * method.  For example, instead of extending {@link io.netty.handler.codec.ByteToByteEncoder},
     * extend what that class does (currently, {@link ChannelOutboundByteHandlerAdapter}).
     * In other words, implementing your own
     * {@link ChannelOutboundHandlerAdapter#flush}/{@link ChannelOperationHandler#flush}
     * will help guarantee a replaced buffer won't be missed.</p>
     *
     * @param newOutboundByteBuf the new inbound byte buffer
     * @return the old buffer.
     * @throws NullPointerException if the argument is {@code null}.
     */
    ByteBuf replaceOutboundByteBuffer(ByteBuf newOutboundByteBuf);

    /**
     * Replaces the outbound message buffer with the given buffer.  This returns the
     * old buffer, so any pending messages can be handled appropriately by the caller.
     * <p>
     * Be cautious with caching {@link #outboundMessageBuffer()} as it may change as a result of this
     * method.  For example, instead of extending {@link io.netty.handler.codec.MessageToByteEncoder}
     * or {@link io.netty.handler.codec.MessageToMessageEncoder}, extend what these classes do (currently,
     * {@link ChannelOutboundMessageHandlerAdapter}.  In other words, implementing your own
     * {@link ChannelOutboundHandlerAdapter#flush}/{@link ChannelOperationHandler#flush}
     * will help guarantee a replaced buffer won't be missed.</p>
     *
     * @param newOutboundMsgBuf the new inbound message buffer
     * @return the old buffer.
     * @throws NullPointerException if the argument is {@code null}.
     */
    <T> MessageBuf<T> replaceOutboundMessageBuffer(MessageBuf<T> newOutboundMsgBuf);

    /**
     * Return {@code true} if the next {@link ChannelHandlerContext} has a {@link ByteBuf} for handling
     * inbound data.
     */
    boolean hasNextInboundByteBuffer();

    /**
     * Return {@code true} if the next {@link ChannelHandlerContext} has a {@link MessageBuf} for handling
     * inbound data.
     */
    boolean hasNextInboundMessageBuffer();

    /**
     * Return the {@link ByteBuf} of the next {@link ChannelHandlerContext} if {@link #hasNextInboundByteBuffer()}
     * returned {@code true}, otherwise a {@link UnsupportedOperationException} is thrown.
     */
    ByteBuf nextInboundByteBuffer();

    /**
     * Return the {@link MessageBuf} of the next {@link ChannelHandlerContext} if
     * {@link #hasNextInboundMessageBuffer()} returned {@code true}, otherwise a
     * {@link UnsupportedOperationException} is thrown.
     */
    MessageBuf<Object> nextInboundMessageBuffer();

    /**
     * Return {@code true} if the next {@link ChannelHandlerContext} has a {@link ByteBuf} for handling outbound
     * data.
     */
    boolean hasNextOutboundByteBuffer();

    /**
     * Return {@code true} if the next {@link ChannelHandlerContext} has a {@link MessageBuf} for handling
     * outbound data.
     */
    boolean hasNextOutboundMessageBuffer();

    /**
     * Return the {@link ByteBuf} of the next {@link ChannelHandlerContext} if {@link #hasNextOutboundByteBuffer()}
     * returned {@code true}, otherwise a {@link UnsupportedOperationException} is thrown.
     */
    ByteBuf nextOutboundByteBuffer();

    /**
     * Return the {@link MessageBuf} of the next {@link ChannelHandlerContext} if
     * {@link #hasNextOutboundMessageBuffer()} returned {@code true}, otherwise a
     * {@link UnsupportedOperationException} is thrown.
     */
    MessageBuf<Object> nextOutboundMessageBuffer();

    /**
     * Return {@code true} if the {@link ChannelHandlerContext} was marked as readable. This basically means
     * that once its not readable anymore no new data will be read from the transport and passed down the
     * {@link ChannelPipeline}.
     *
     * Only if all {@link ChannelHandlerContext}'s {@link #isReadable()} return {@code true}, the data is
     * passed again down the {@link ChannelPipeline}.
     */
    boolean isReadable();

    /**
     * Mark the {@link ChannelHandlerContext} as readable or suspend it. See {@link #isReadable()}
     */
    void readable(boolean readable);
}
