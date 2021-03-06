// Copyright 2015-2020 Swim inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package swim.ws;

import swim.codec.Decoder;
import swim.codec.DecoderException;
import swim.codec.InputBuffer;

final class WsFrameDecoder<O> extends Decoder<WsFrame<O>> {

  final WsDecoder ws;
  final Decoder<O> content;
  final int finRsvOp;
  final long position;
  final long offset;
  final long length;
  final byte[] maskingKey;
  final int step;

  WsFrameDecoder(WsDecoder ws, Decoder<O> content, int finRsvOp, long position,
                 long offset, long length, byte[] maskingKey, int step) {
    this.ws = ws;
    this.position = position;
    this.content = content;
    this.finRsvOp = finRsvOp;
    this.offset = offset;
    this.length = length;
    this.maskingKey = maskingKey;
    this.step = step;
  }

  WsFrameDecoder(WsDecoder ws, Decoder<O> content) {
    this(ws, content, 0, 0L, 0L, 0L, null, 1);
  }

  static <O> Decoder<WsFrame<O>> decode(InputBuffer input, WsDecoder ws, Decoder<O> content,
                                        int finRsvOp, long position, long offset, long length,
                                        byte[] maskingKey, int step) {
    if (step == 1 && input.isCont()) { // decode finRsvOp
      finRsvOp = input.head();
      input = input.step();
      step = 2;
    }
    if (step == 2 && input.isCont()) { // decode maskLength
      final int maskLength = input.head();
      input = input.step();
      if ((maskLength & 0x80) != 0) {
        maskingKey = new byte[4];
      }
      final int len = maskLength & 0x7f;
      if (len == 126) { // short length
        step = 3;
      } else if (len == 127) { // long length
        step = 5;
      } else {
        length = (long) len;
        step = maskingKey != null ? 13 : 17;
      }
    }
    if (step >= 3 && step <= 4) { // decode short length
      while (input.isCont()) {
        length = (length << 8) | (long) input.head();
        input = input.step();
        if (step < 4) {
          step += 1;
        } else {
          step = maskingKey != null ? 13 : 17;
          break;
        }
      }
    }
    if (step >= 5 && step <= 12) { // decode long length
      while (input.isCont()) {
        length = (length << 8) | (long) input.head();
        input = input.step();
        if (step < 12) {
          step += 1;
        } else {
          step = maskingKey != null ? 13 : 17;
          break;
        }
      }
    }
    if (step >= 13 && step <= 16) { // decode masking key
      while (input.isCont()) {
        maskingKey[step - 13] = (byte) input.head();
        input = input.step();
        if (step < 16) {
          step += 1;
        } else {
          step = 17;
          break;
        }
      }
    }
    if (step == 17) { // decode payload
      final int base = input.index();
      final int size = (int) Math.min(length - offset, input.remaining());
      if (maskingKey != null) {
        for (int i = 0; i < size; i += 1) {
          input.set(base + i, (input.get(base + i) ^ maskingKey[(int) (position + i) & 0x3]) & 0xff);
        }
      }
      position += size;
      offset += size;

      final boolean eof = offset == length && (finRsvOp & 0x80) != 0;
      final boolean inputPart = input.isPart();
      input = input.isPart(!eof);
      if (input.remaining() < size) {
        content = content.feed(input);
      } else {
        final int inputLimit = input.limit();
        input = input.limit(base + size);
        content = content.feed(input);
        input = input.limit(inputLimit);
      }
      input = input.isPart(inputPart);

      if (input.index() != base + size) {
        return error(new DecoderException("undecoded websocket data"));
      } else if (content.isError()) {
        return content.asError();
      } else if (content.isDone()) {
        if (offset == length) {
          if ((finRsvOp & 0x80) != 0) {
            final int opcode = finRsvOp & 0xf;
            if (opcode < 0x8) { // decoded message
              return done(ws.message(content.bind()));
            } else { // decoded control frame
              return done(ws.control(WsOpcode.from(opcode), content.bind()));
            }
          } else {
            return error(new DecoderException("decoded unfinished websocket message"));
          }
        } else {
          return error(new DecoderException("decoded incomplete websocket frame"));
        }
      } else if (offset == length) {
        if ((finRsvOp & 0x80) == 0) {
          final int opcode = finRsvOp & 0xf;
          if (opcode < 0x8) { // decoded fragment
            return done(ws.fragment(WsOpcode.from(opcode), content));
          } else {
            return error(new DecoderException("decoded fragmented control frame"));
          }
        } else {
          return error(new DecoderException("undecoded websocket message"));
        }
      }
    }
    if (input.isDone()) {
      return error(new DecoderException("incomplete"));
    } else if (input.isError()) {
      return error(input.trap());
    }
    return new WsFrameDecoder<O>(ws, content, finRsvOp, position, offset,
        length, maskingKey, step);
  }

  static <O> Decoder<WsFrame<O>> decode(InputBuffer input, WsDecoder ws, Decoder<O> content) {
    return decode(input, ws, content, 0, 0L, 0L, 0L, null, 1);
  }

  @Override
  public Decoder<WsFrame<O>> feed(InputBuffer input) {
    return decode(input, this.ws, this.content, this.finRsvOp, this.position,
        this.offset, this.length, this.maskingKey, this.step);
  }

}
