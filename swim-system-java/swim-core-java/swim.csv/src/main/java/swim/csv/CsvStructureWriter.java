// Copyright 2015-2019 SWIM.AI inc.
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

package swim.csv;

import swim.codec.Output;
import swim.codec.Writer;
import swim.structure.Item;
import swim.structure.Value;

public class CsvStructureWriter extends CsvWriter<Value, Item, Item> {
  final int delimiter;

  public CsvStructureWriter(int delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  public Writer<?, ?> writeTable(Value table, Output<?> output) {
    throw new UnsupportedOperationException(); // TODO
  }
}
