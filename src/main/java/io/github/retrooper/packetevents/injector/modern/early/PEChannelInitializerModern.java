/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.injector.modern.early;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.injector.modern.PacketDecoderModern;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.lang.reflect.Method;
import java.util.Arrays;

public class PEChannelInitializerModern extends ChannelInitializer<Channel> {
    private final ChannelInitializer<?> oldChannelInitializer;
    private Method initChannelMethod;

    public PEChannelInitializerModern(ChannelInitializer<?> oldChannelInitializer) {
        this.oldChannelInitializer = oldChannelInitializer;
        load();
    }

    public static void postInitChannel(Channel channel) {
        PacketDecoderModern packetDecoderModern = new PacketDecoderModern((ByteToMessageDecoder) channel.pipeline().get("decoder"));
        channel.pipeline().replace("decoder", "decoder", packetDecoderModern);
    }

    public static void postDestroyChannel(Channel channel) {
        ChannelHandler decoder = channel.pipeline().get("decoder");
        if (decoder instanceof PacketDecoderModern) {
            PacketDecoderModern decoderModern = (PacketDecoderModern) decoder;
            channel.pipeline().replace("decoder", "decoder", decoderModern.previousDecoder);
        }
    }

    private void load() {
        initChannelMethod = Reflection.getMethod(oldChannelInitializer.getClass(), "initChannel", 0);
    }

    public ChannelInitializer<?> getOldChannelInitializer() {
        return oldChannelInitializer;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        initChannelMethod.invoke(oldChannelInitializer, channel);
        postInitChannel(channel);
    }
}
